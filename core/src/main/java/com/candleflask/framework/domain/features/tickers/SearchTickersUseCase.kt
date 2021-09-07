package com.candleflask.framework.domain.features.tickers

import com.candleflask.framework.domain.entities.ticker.TickerModel
import javax.inject.Inject

class SearchTickersUseCase @Inject constructor(
  private val tickerRepository: TickerRepository
) {
  suspend fun execute(searchInput: String): TickerRepository.OperationResult<List<TickerModel>> {
    return tickerRepository.search(searchInput)
  }
}