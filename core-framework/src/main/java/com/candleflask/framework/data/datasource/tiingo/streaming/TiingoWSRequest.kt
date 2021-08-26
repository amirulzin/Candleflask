package com.candleflask.framework.data.datasource.tiingo.streaming

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TiingoWSRequest(
  val eventName: String, // "subscribe" or "unsubscribe"
  val authorization: String,
  val eventData: EventData = EventData()
) {
  @JsonClass(generateAdapter = true)
  data class EventData(
    val subscriptionId: String? = null,
    val thresholdLevel: Int = 5,
    val tickers: Set<String> = emptySet(),
  )
}