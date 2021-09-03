package com.candleflask.framework.features.tickers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IsStreamConnectedUseCase @Inject constructor(private val tickerRepository: TickerRepository) {
  fun observe(): Flow<Boolean> {
    return tickerRepository.isStreamConnected().map { connectionState ->
      connectionState == TickerRepository.StreamingConnectionState.CONNECTED
    }
  }
}