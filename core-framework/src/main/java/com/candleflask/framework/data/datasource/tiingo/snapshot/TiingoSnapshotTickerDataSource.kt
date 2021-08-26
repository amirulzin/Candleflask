package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.candleflask.framework.data.datasource.SnapshotTickerDataSource
import com.candleflask.framework.domain.entities.ticker.TickerModel
import javax.inject.Inject
import javax.inject.Provider

class TiingoSnapshotTickerDataSource @Inject constructor(private val tiingoRESTProvider: Provider<TiingoREST>) :
  SnapshotTickerDataSource {
  private val tiingoREST by lazy(tiingoRESTProvider::get)

  override suspend fun retrieve(symbols: Set<String>, token: String): List<TickerModel> {
    val commaSeparatedTickers = symbols.joinToString()
    val resultList = tiingoREST.iexLatest(commaSeparatedTickers, token)
    return resultList.map { resultItem ->
      TickerModel(
        symbol = resultItem.ticker.key,
        todayOpenPriceCents = resultItem.open,
        yesterdayClosePriceCents = resultItem.prevClose,
        currentPriceCents = resultItem.last,
        lastUpdated = resultItem.quoteTimestamp?.time,
      )
    }
  }
}

