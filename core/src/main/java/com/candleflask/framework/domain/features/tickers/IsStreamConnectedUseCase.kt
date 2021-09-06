package com.candleflask.framework.domain.features.tickers

import javax.inject.Inject

class IsStreamConnectedUseCase @Inject constructor(private val tickerRepository: TickerRepository) {
  val stateFlow = tickerRepository.isStreamConnected()
}