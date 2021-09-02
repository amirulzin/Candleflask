package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TiingoRESTTickerLatest(
  val bidPrice: String?,
  val open: String?,
  val prevClose: String?,
  val mid: String?,
  val volume: Long?,
  val timestamp: Date,
  val askSize: Long?,
  val lastSize: Long?,
  val high: String?,
  val askPrice: String?,
  val low: String?,
  val ticker: String,
  val tngoLast: String?,
  val lastSaleTimestamp: Date,
  val last: String?,
  val bidSize: Long?,
  val quoteTimestamp: Date?
)



