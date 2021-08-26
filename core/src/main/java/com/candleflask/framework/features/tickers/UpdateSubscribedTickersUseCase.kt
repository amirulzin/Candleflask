package com.candleflask.framework.features.tickers

import com.candleflask.framework.domain.entities.ticker.Ticker
import javax.inject.Inject

class UpdateSubscribedTickersUseCase @Inject constructor(private val tickerRepository: TickerRepository) {

  fun addAndSubscribe(ticker: Ticker) {
    tickerRepository.storeAndSubscribeNewTicker(ticker)
  }

  fun removeAndUnsubscribe(ticker: Ticker) {
    tickerRepository.removeAndUnsubscribeTicker(ticker)
  }

  fun retrieveSubscribedTickers(): Set<Ticker> {
    return tickerRepository.retrieveSubscribedTickers()
  }
}