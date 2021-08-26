package com.candleflask.framework.data.datasource.tiingo

import com.candleflask.framework.domain.entities.ticker.Ticker
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class TiingoTickerJsonAdapter {
  @ToJson
  fun toJson(ticker: Ticker): String = ticker.key

  @FromJson
  fun fromJson(ticker: String): Ticker {
    return Ticker(ticker)
  }
}