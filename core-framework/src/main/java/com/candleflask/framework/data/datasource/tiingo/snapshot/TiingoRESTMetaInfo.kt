package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TiingoRESTMetaInfo(
  val ticker: String,
  val name: String,
  val exchangeCode: String,
  val startDate: TiingoRESTSimpleDate?,
  val endDate: TiingoRESTSimpleDate?,
  val description: String?
)