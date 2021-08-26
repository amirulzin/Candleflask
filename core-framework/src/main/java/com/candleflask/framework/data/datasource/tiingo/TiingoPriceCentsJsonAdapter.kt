package com.candleflask.framework.data.datasource.tiingo

import com.candleflask.framework.domain.entities.ticker.PriceCents
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class TiingoPriceCentsJsonAdapter {
  @ToJson
  fun toJson(@Suppress("UNUSED_PARAMETER") model: PriceCents): String =
    throw IllegalStateException("${PriceCents::class.simpleName} can't be used for outbound message")

  @FromJson
  fun fromJson(dollarPrice: Double): PriceCents {
    return PriceCents(dollarPrice)
  }
}