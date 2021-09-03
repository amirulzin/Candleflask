package com.candleflask.framework.domain.features.tickers

import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import kotlinx.coroutines.flow.Flow

interface TickerRepository {
  suspend fun optionallyReconnect(force: Boolean): OperationResult

  fun disconnect()

  fun retrieveHotTickers(): Flow<List<TickerModel>>

  fun storeAndSubscribeNewTicker(ticker: Ticker): OperationResult

  fun removeAndUnsubscribeTicker(ticker: Ticker): OperationResult

  fun retrieveSubscribedTickers(): Set<Ticker>

  fun isStreamConnected(): Flow<StreamingConnectionState>

  suspend fun forceSnapshotUpdate()

  sealed class OperationResult {
    object Success : OperationResult()
    sealed class Error : OperationResult() {
      object InvalidToken : Error()
    }
  }

  enum class StreamingConnectionState {
    CONNECTED,
    DISCONNECTED
  }
}