package com.candleflask.framework.features.tickers

import javax.inject.Inject

class DisplaySubscribedTickersUseCase @Inject constructor(
  private val tickerRepository: TickerRepository
) {
  val tickerUpdates by lazy {
    tickerRepository.retrieveHotTickers()
  }

  suspend fun execute(force: Boolean) {
    tickerRepository.optionallyReconnect(force)
    if (force) {
      tickerRepository.forceSnapshotUpdate()
    }
  }

  fun cleanUp() {
    tickerRepository.disconnect()
  }
}