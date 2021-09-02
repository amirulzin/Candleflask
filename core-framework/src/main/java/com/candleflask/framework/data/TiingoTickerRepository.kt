package com.candleflask.framework.data

import android.util.Log
import com.candleflask.framework.data.datasource.OkHttpWebSocketController
import com.candleflask.framework.data.datasource.SnapshotTickerDataSource
import com.candleflask.framework.data.datasource.StreamingTickerDataSource
import com.candleflask.framework.data.datasource.StreamingTickerDataSource.OperationOutput
import com.candleflask.framework.data.datasource.db.DatabaseController
import com.candleflask.framework.data.datasource.db.TickerDAO
import com.candleflask.framework.data.datasource.db.TickerEntity
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.features.securitytoken.EncryptedTokenRepository
import com.candleflask.framework.features.tickers.TickerRepository
import com.candleflask.framework.features.tickers.TickerRepository.OperationResult
import com.candleflask.framework.features.tickers.TickerRepository.StreamingConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class TiingoTickerRepository @Inject constructor(
  private val encryptedTokenRepository: EncryptedTokenRepository,
  private val webSocketController: OkHttpWebSocketController,
  private val databaseController: DatabaseController,
  private val streamingTickerDataSource: StreamingTickerDataSource,
  private val snapshotTickerDataSource: SnapshotTickerDataSource
) : TickerRepository {

  private val streamingConnectionState by lazy {
    MutableStateFlow(StreamingConnectionState.DISCONNECTED)
  }

  private val tickerDAO by lazy {
    databaseController.database.tickerDAO()
  }

  private val wsListener = object : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
      super.onOpen(webSocket, response)
      notifyWebSocketConnected()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
      super.onClosed(webSocket, code, reason)
      Log.d("@DBG-WS", "onClosed")
      streamingConnectionState.value = StreamingConnectionState.DISCONNECTED
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
      super.onClosing(webSocket, code, reason)
      Log.d("@DBG-WS", "onClosing")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
      super.onFailure(webSocket, t, response)
      Log.e("@DBG-WS", "onFailure", t)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
      super.onMessage(webSocket, text)
      Log.d("@DBG-WS-MSG", text)
      when (val output = streamingTickerDataSource.wsHandleMessage(text)) {
        is OperationOutput.PriceUpdate -> updateExistingTicker(output.tickerModel)
        OperationOutput.Heartbeat -> notifyWebSocketConnected()
        OperationOutput.Unknown -> { /* no-op */
        }
      }
    }
  }

  override fun optionallyReconnect(force: Boolean): OperationResult {
    if (!webSocketController.isSocketConnected()) {
      notifySocketDisconnected()
    }

    val token = encryptedTokenRepository.retrieveToken()
    if (token == null)
      return OperationResult.Error.InvalidToken
    else {
      val initializationRequest = streamingTickerDataSource.wsInitializationRequest(token)
      webSocketController.optionallyReconnect(initializationRequest, force, wsListener)
      val subscribedTickers = retrieveSubscribedTickers()
      if (subscribedTickers.isNotEmpty()) {
        streamingTickerDataSource.wsSubscribeTickersMessage(token, *subscribedTickers.toTypedArray())
          .let(webSocketController::sendMessage)
      }
    }
    return OperationResult.Success
  }

  override suspend fun forceSnapshotUpdate() {
    val token = encryptedTokenRepository.retrieveToken()
    if (token != null) {
      val symbols = retrieveSubscribedTickers().mapTo(mutableSetOf(), Ticker::key)
      val result = snapshotTickerDataSource.retrieve(symbols, token)
      if (result.isNotEmpty()) {
        for (tickerModel in result) {
          updateExistingTicker(tickerModel)
        }
      }
    }
  }

  override fun disconnect() {
    notifySocketDisconnected()
    webSocketController.disconnect()
  }

  override fun retrieveHotTickers(): Flow<List<TickerModel>> {
    return tickerDAO.observeAll().map { list ->
      list.map(DataMapper::toTickerModel)
    }
  }

  override fun storeAndSubscribeNewTicker(ticker: Ticker): OperationResult {
    val token = encryptedTokenRepository.retrieveToken()
      ?: return OperationResult.Error.InvalidToken

    tickerDAO.upsert(TickerEntity(tickerSymbol = ticker.key))
    streamingTickerDataSource.wsSubscribeTickersMessage(token, ticker)
      .let(webSocketController::sendMessage)
    return OperationResult.Success
  }

  override fun removeAndUnsubscribeTicker(ticker: Ticker): OperationResult {
    val token = encryptedTokenRepository.retrieveToken()
      ?: return OperationResult.Error.InvalidToken

    tickerDAO.deleteBySymbol(ticker.key)
    streamingTickerDataSource.wsUnsubscribeTickersMessage(token, ticker)
      .let(webSocketController::sendMessage)

    return OperationResult.Success
  }

  override fun retrieveSubscribedTickers(): Set<Ticker> {
    return tickerDAO.getAll()
      .map { model -> Ticker(model.tickerSymbol) }
      .toSet()
  }

  private fun updateExistingTicker(model: TickerModel) {
    val currentPrice = model.currentPrice
    if (currentPrice != null) {
      tickerDAO.updateCurrentPrice(
        TickerDAO.RemoteUpdateArgument(
          tickerSymbol = model.symbol,
          currentAskPriceCents = DataMapper.toPlainString(currentPrice),
          lastUpdatedEpochMillis = model.lastUpdated ?: currentTimeMillis()
        )
      )
    }
  }

  private fun notifyWebSocketConnected() {
    streamingConnectionState.value = StreamingConnectionState.CONNECTED
  }

  private fun notifySocketDisconnected() {
    streamingConnectionState.value = StreamingConnectionState.DISCONNECTED
  }

  private fun currentTimeMillis() = System.currentTimeMillis()
}
