package com.candleflask.framework.features.tickers

import javax.inject.Inject

class ForceRefreshSnapshotTickersUseCase @Inject constructor(private val tickerRepository: TickerRepository) {
  suspend fun execute() {
    tickerRepository.forceSnapshotUpdate()
  }
}