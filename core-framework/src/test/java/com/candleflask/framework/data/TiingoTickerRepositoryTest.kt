package com.candleflask.framework.data

import com.candleflask.framework.data.datasource.SnapshotTickerDataSource
import com.candleflask.framework.data.datasource.StreamingTickerDataFactory
import com.candleflask.framework.data.datasource.StreamingTickerDataFactory.OperationOutput.Heartbeat
import com.candleflask.framework.data.datasource.StreamingTickerDataFactory.OperationOutput.PriceUpdate
import com.candleflask.framework.data.datasource.WebSocketController
import com.candleflask.framework.data.datasource.db.TickerDAO
import com.candleflask.framework.data.datasource.db.TickerEntity
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.domain.features.securitytoken.EncryptedTokenRepository
import com.candleflask.framework.domain.features.tickers.TickerRepository
import com.candleflask.framework.domain.features.tickers.TickerRepository.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.*
import javax.inject.Provider

class TiingoTickerRepositoryTest {
  private val tickerSymbol = "NVDA"

  private val stubRequest = Request.Builder()
    .url("https://requesturl.com")
    .get()
    .build()

  private val stubResponse = Response.Builder()
    .request(stubRequest)
    .protocol(Protocol.HTTP_1_1)
    .message("")
    .body("{}".toResponseBody("application/json".toMediaType()))
    .code(200)
    .build()

  private val encryptedTokenRepository = mock<EncryptedTokenRepository> {
    on { retrieveToken() } doReturn "testToken"
  }

  private val webSocket = mock<WebSocket>()

  private val webSocketController = mock<WebSocketController> {
    on { optionallyReconnect(any(), any(), any()) } doAnswer { invocation ->
      if (invocation.arguments[1] as Boolean) {
        mock.disconnect()
      }

      (invocation.arguments[2] as WebSocketListener).let { listener ->
        runBlocking {
          listener.onOpen(webSocket, stubResponse)
          delay(10)
          listener.onMessage(webSocket, "WSHeartbeatString")
          delay(10)
          listener.onMessage(webSocket, "WSMessageString")
        }
      }
    }
  }

  private val tickerDAO = mock<TickerDAO> {
    on { getAll() } doReturn listOf(TickerEntity(tickerSymbol))
    on { observeAll() } doReturn flow { emit(listOf(TickerEntity(tickerSymbol = tickerSymbol))) }
  }

  private val streamingTickerDataFactory = mock<StreamingTickerDataFactory> {
    on { wsSubscribeTickersMessage(any(), any()) } doReturn "subscribeMessage"
    on { wsUnsubscribeTickersMessage(any(), any()) } doReturn "unsubscribeMessage"
    on { wsInitializationRequest(any()) } doReturn stubRequest
    on { wsHandleMessage(any()) } doAnswer {
      Heartbeat
    } doAnswer {
      PriceUpdate(TickerModel(tickerSymbol))
    }
  }

  private val snapshotTickerDataSource = mock<SnapshotTickerDataSource> {
    onBlocking { retrieve(any(), any()) } doReturn listOf(TickerModel(tickerSymbol))
  }

  private val tickerRepositoryImpl = TiingoTickerRepository(
    encryptedTokenRepository = encryptedTokenRepository,
    webSocketController = webSocketController,
    tickerDAOProvider = Provider { tickerDAO },
    streamingTickerDataFactory = streamingTickerDataFactory,
    snapshotTickerDataSource = snapshotTickerDataSource
  )

  @Test
  fun `during first init, when optionallyReconnect is forced, disconnect before reconnecting`() {
    runBlocking {
      val emissionList = mutableListOf<Boolean>()
      val collectionJob = launch(Dispatchers.Unconfined) {
        tickerRepositoryImpl.isStreamConnected().collectLatest {
          emissionList.add(it)
        }
      }

      launch {
        tickerRepositoryImpl.optionallyReconnect(force = true)
        collectionJob.cancel()
        assertEquals(listOf(false, true), emissionList)
      }
    }

    verify(webSocketController).disconnect()
  }

  @Test
  fun `during first init, when optionallyReconnect is not forced, do not disconnect`() {
    runBlocking {
      val emissionList = mutableListOf<Boolean>()
      val collectionJob = launch(Dispatchers.Unconfined) {
        tickerRepositoryImpl.isStreamConnected().collectLatest {
          emissionList.add(it)
        }
      }

      launch {
        tickerRepositoryImpl.optionallyReconnect(force = false)
        collectionJob.cancel()
        assertEquals(listOf(false, true), emissionList)
      }
    }

    verify(webSocketController, never()).disconnect()
  }

  @Test
  fun `when forceSnapshotUpdate, invoke tickerDAO upsert once`() {
    runBlocking {
      tickerRepositoryImpl.forceSnapshotUpdate()
    }

    verify(encryptedTokenRepository).retrieveToken()

    argumentCaptor<String> {
      verifyBlocking(snapshotTickerDataSource) {
        retrieve(setOf(capture()), any())
        assertEquals(setOf(tickerSymbol), firstValue)
      }
    }

    argumentCaptor<TickerEntity> {
      verify(tickerDAO).upsert(capture())
      assertEquals(tickerSymbol, firstValue.tickerSymbol)
    }
  }

  @Test
  fun `when search success, match exactly one result`() {
    val result = runBlocking {
      tickerRepositoryImpl.search(tickerSymbol)
    }

    when (result) {
      is OperationResult.InvalidTokenError -> throw IllegalStateException("Token is not supposed to be null")
      is OperationResult.Success -> assertEquals(tickerSymbol, result.output.firstOrNull()?.symbolNormalized)
    }

    argumentCaptor<Set<String>> {
      verifyBlocking(snapshotTickerDataSource) {
        retrieve(capture(), any())
      }
      assertEquals(setOf(tickerSymbol), firstValue)
    }
  }

  @Test
  fun `when disconnect invoked after running, disconnect webSocketController`() {
    runBlocking {
      val emissionList = mutableListOf<Boolean>()
      val collectionJob = launch(Dispatchers.Unconfined) {
        tickerRepositoryImpl.isStreamConnected().collectLatest {
          emissionList.add(it)
        }
      }

      launch {
        tickerRepositoryImpl.optionallyReconnect(false)
        delay(10)
        tickerRepositoryImpl.disconnect()
        collectionJob.cancel()
        assertEquals(listOf(false, true, false), emissionList)
      }
    }

    verify(webSocketController).disconnect()
  }

  @Test
  fun `when retrieveHotTickers, invoke tickerDAO observeAll`() {
    val emissionList = mutableListOf<TickerModel>()
    runBlocking {
      val collectionJob = launch(Dispatchers.Unconfined) {
        tickerRepositoryImpl.retrieveHotTickers().collectLatest {
          emissionList.addAll(it)
        }
      }
      delay(10)
      collectionJob.cancel()
    }

    assertEquals(listOf(TickerModel(tickerSymbol)), emissionList)

    verify(tickerDAO).observeAll()
  }

  @Test
  fun `when storeAndSubscribeNewTicker, invoke tickerDAO upsert and streamingTickerDataFactory wsSubscribeTickersMessage`() {
    val result = tickerRepositoryImpl.storeAndSubscribeNewTicker(Ticker(tickerSymbol))

    assertTrue(result is TickerRepository.CompletableResult.Success)

    argumentCaptor<TickerEntity> {
      verify(tickerDAO).upsert(capture())
      assertEquals(tickerSymbol, firstValue.tickerSymbol)
    }

    argumentCaptor<Ticker> {
      verify(streamingTickerDataFactory).wsSubscribeTickersMessage(any(), capture())
      assertEquals(tickerSymbol, firstValue.key)
    }
  }

  @Test
  fun `when removeAndUnsubscribeTicker, invoke tickerDAO deleteBySymbol and streamingTickerDataFactory wsUnsubscribeTickersMessage`() {
    val result = tickerRepositoryImpl.removeAndUnsubscribeTicker(Ticker(tickerSymbol))

    assertTrue(result is TickerRepository.CompletableResult.Success)

    argumentCaptor<String> {
      verify(tickerDAO).deleteBySymbol(capture())
      assertEquals(tickerSymbol, firstValue)
    }

    argumentCaptor<Ticker> {
      verify(streamingTickerDataFactory).wsUnsubscribeTickersMessage(any(), capture())
      assertEquals(tickerSymbol, firstValue.key)
    }
  }

  @Test
  fun `when retrieveSubscribedTickers, invoke tickerDAO getAll once`() {
    val result = tickerRepositoryImpl.retrieveSubscribedTickers()
    assertEquals(setOf(Ticker(tickerSymbol)), result)

    verify(tickerDAO).getAll()
  }
}
