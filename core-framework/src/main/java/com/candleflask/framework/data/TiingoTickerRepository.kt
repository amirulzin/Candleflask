package com.candleflask.framework.data

import com.candleflask.framework.data.datasource.SnapshotTickerDataSource
import com.candleflask.framework.data.datasource.StreamingTickerDataFactory
import com.candleflask.framework.data.datasource.StreamingTickerDataFactory.OperationOutput
import com.candleflask.framework.data.datasource.WebSocketController
import com.candleflask.framework.data.datasource.db.TickerDAO
import com.candleflask.framework.data.datasource.db.TickerEntity
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.domain.features.securitytoken.EncryptedTokenRepository
import com.candleflask.framework.domain.features.tickers.TickerRepository
import com.candleflask.framework.domain.features.tickers.TickerRepository.CompletableResult
import com.candleflask.framework.domain.features.tickers.TickerRepository.OperationResult
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Provider

class TiingoTickerRepository @Inject constructor(
  private val encryptedTokenRepository: EncryptedTokenRepository,
  private val webSocketController: WebSocketController,
  private val streamingTickerDataFactory: StreamingTickerDataFactory,
  private val snapshotTickerDataSource: SnapshotTickerDataSource,
  private val tickerDAOProvider: Provider<TickerDAO>
) : TickerRepository {

  private val isStreamConnected = MutableStateFlow(false)

  private val tickerDAO by lazy(tickerDAOProvider::get)

  private val wsListener = object : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
      super.onOpen(webSocket, response)
      notifyWebSocketConnected()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
      super.onClosed(webSocket, code, reason)
      notifyWebSocketDisconnected()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
      super.onMessage(webSocket, text)
      when (val output = streamingTickerDataFactory.wsHandleMessage(text)) {
        is OperationOutput.PriceUpdate -> updateExistingTickerPrice(output.tickerModel)
        OperationOutput.Heartbeat -> notifyWebSocketConnected()
        OperationOutput.Unknown -> { /* no-op */
        }
      }
    }
  }

  override suspend fun optionallyReconnect(force: Boolean): CompletableResult {
    val token = encryptedTokenRepository.retrieveToken()
    if (token == null)
      return CompletableResult.InvalidTokenError
    else {
      val initializationRequest = streamingTickerDataFactory.wsInitializationRequest(token)
      webSocketController.optionallyReconnect(initializationRequest, force, wsListener)
      val subscribedTickers = retrieveSubscribedTickers()
      if (subscribedTickers.isNotEmpty()) {
        coroutineScope {
          launch {
            streamingTickerDataFactory.wsSubscribeTickersMessage(token, *subscribedTickers.toTypedArray())
              .let(webSocketController::sendMessage)
          }
        }
      }
    }
    return CompletableResult.Success
  }

  override suspend fun forceSnapshotUpdate(): CompletableResult {
    val token = encryptedTokenRepository.retrieveToken()
    if (token == null) {
      return CompletableResult.InvalidTokenError
    } else {
      val symbols = retrieveSubscribedTickers().mapTo(mutableSetOf(), Ticker::key)
      val result = snapshotTickerDataSource.retrieve(symbols, token)
      if (result.isNotEmpty()) {
        for (tickerModel in result) {
          updateTickerSnapshot(tickerModel)
        }
      }
      return CompletableResult.Success
    }
  }

  @Suppress("LiftReturnOrAssignment")
  override suspend fun search(input: String): OperationResult<List<TickerModel>> {
    val token = encryptedTokenRepository.retrieveToken()
    if (token == null) {
      return OperationResult.InvalidTokenError()
    } else {
      val searchResult = snapshotTickerDataSource.retrieve(setOf(input), token)
      return OperationResult.Success(searchResult)
    }
  }

  override fun disconnect() {
    notifyWebSocketDisconnected()
    webSocketController.disconnect()
  }

  override fun retrieveHotTickers(): Flow<List<TickerModel>> {
    return tickerDAO.observeAll().map { list ->
      list.map(DataMapper::toTickerModel)
    }
  }

  override fun storeAndSubscribeNewTicker(ticker: Ticker): CompletableResult {
    val token = encryptedTokenRepository.retrieveToken()
      ?: return CompletableResult.InvalidTokenError

    tickerDAO.upsert(TickerEntity(tickerSymbol = ticker.key))
    streamingTickerDataFactory.wsSubscribeTickersMessage(token, ticker)
      .let(webSocketController::sendMessage)
    return CompletableResult.Success
  }

  override fun removeAndUnsubscribeTicker(ticker: Ticker): CompletableResult {
    val token = encryptedTokenRepository.retrieveToken()
      ?: return CompletableResult.InvalidTokenError

    tickerDAO.deleteBySymbol(ticker.key)
    streamingTickerDataFactory.wsUnsubscribeTickersMessage(token, ticker)
      .let(webSocketController::sendMessage)

    return CompletableResult.Success
  }

  override fun retrieveSubscribedTickers(): Set<Ticker> {
    return tickerDAO.getAll()
      .map { model -> Ticker(model.tickerSymbol) }
      .toSet()
  }

  override fun isStreamConnected(): StateFlow<Boolean> {
    return isStreamConnected
  }

  private fun updateExistingTickerPrice(model: TickerModel) {
    val currentPrice = model.currentPrice
    if (currentPrice != null) {
      tickerDAO.updateCurrentPrice(
        TickerDAO.RemoteUpdateArgument(
          tickerSymbol = model.symbolNormalized,
          currentAskPriceCents = DataMapper.toPlainString(currentPrice),
          lastUpdatedEpochMillis = model.lastUpdated ?: currentTimeMillis()
        )
      )
    }
  }

  private fun updateTickerSnapshot(model: TickerModel) {
    tickerDAO.upsert(DataMapper.toTickerEntity(model))
  }

  private fun notifyWebSocketConnected() {
    isStreamConnected.value = true
  }

  private fun notifyWebSocketDisconnected() {
    isStreamConnected.value = false
  }

  private fun currentTimeMillis() = System.currentTimeMillis()
}
