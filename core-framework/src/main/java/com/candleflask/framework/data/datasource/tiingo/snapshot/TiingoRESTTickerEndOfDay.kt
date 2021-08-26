package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.candleflask.framework.domain.entities.ticker.PriceCents
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TiingoRESTTickerEndOfDay(
  val date: Date,
  val close: PriceCents
)