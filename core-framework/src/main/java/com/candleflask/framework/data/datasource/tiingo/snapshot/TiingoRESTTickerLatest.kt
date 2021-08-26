package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.candleflask.framework.domain.entities.ticker.PriceCents
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TiingoRESTTickerLatest(
  val bidPrice: PriceCents?,
  val open: PriceCents?,
  val prevClose: PriceCents?,
  val mid: PriceCents?,
  val volume: Long?,
  val timestamp: Date,
  val askSize: Long?,
  val lastSize: Long?,
  val high: PriceCents?,
  val askPrice: PriceCents?,
  val low: PriceCents?,
  val ticker: Ticker,
  val tngoLast: PriceCents?,
  val lastSaleTimestamp: Date,
  val last: PriceCents?,
  val bidSize: Long?,
  val quoteTimestamp: Date?
)



