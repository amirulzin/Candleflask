package com.candleflask.framework.domain.features.tickers

import javax.inject.Inject

class StreamSubscribedTickersUseCase @Inject constructor(
  private val tickerRepository: TickerRepository
) {
  val tickerUpdates by lazy {
    tickerRepository.retrieveHotTickers()
  }

  suspend fun execute(force: Boolean) {
    tickerRepository.optionallyReconnect(force)
  }

  fun cleanUp() {
    tickerRepository.disconnect()
  }
}