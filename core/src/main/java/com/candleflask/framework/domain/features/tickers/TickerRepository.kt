package com.candleflask.framework.domain.features.tickers

import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TickerRepository {
  suspend fun optionallyReconnect(force: Boolean): OperationResult

  suspend fun forceSnapshotUpdate(): OperationResult

  fun disconnect()

  fun retrieveHotTickers(): Flow<List<TickerModel>>

  fun storeAndSubscribeNewTicker(ticker: Ticker): OperationResult

  fun removeAndUnsubscribeTicker(ticker: Ticker): OperationResult

  fun retrieveSubscribedTickers(): Set<Ticker>

  fun isStreamConnected(): StateFlow<Boolean>

  sealed class OperationResult {
    object Success : OperationResult()
    sealed class Error : OperationResult() {
      object InvalidToken : Error()
    }
  }

}