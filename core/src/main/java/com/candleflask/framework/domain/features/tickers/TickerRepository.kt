package com.candleflask.framework.domain.features.tickers

import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TickerRepository {
  suspend fun optionallyReconnect(force: Boolean): CompletableResult

  suspend fun forceSnapshotUpdate(): CompletableResult

  suspend fun search(input: String): OperationResult<List<TickerModel>>

  fun disconnect()

  fun retrieveHotTickers(): Flow<List<TickerModel>>

  fun storeAndSubscribeNewTicker(ticker: Ticker): CompletableResult

  fun removeAndUnsubscribeTicker(ticker: Ticker): CompletableResult

  fun retrieveSubscribedTickers(): Set<Ticker>

  fun isStreamConnected(): StateFlow<Boolean>

  sealed class OperationResult<T> {
    data class Success<T>(val output: T) : OperationResult<T>()
    class InvalidTokenError<T> : OperationResult<T>()
  }

  sealed class CompletableResult {
    object Success : CompletableResult()
    object InvalidTokenError : CompletableResult()
  }
}