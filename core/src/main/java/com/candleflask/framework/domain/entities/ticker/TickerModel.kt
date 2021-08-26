package com.candleflask.framework.domain.entities.ticker

data class TickerModel(
  val symbol: String,
  val todayOpenPriceCents: PriceCents? = null,
  val yesterdayClosePriceCents: PriceCents? = null,
  val currentPriceCents: PriceCents? = null,
  val lastUpdated: Long? = null,
) {

  val priceMovement = when {
    currentPriceCents?.value isMoreThanOrFalse todayOpenPriceCents?.value -> PriceMovement.POSITIVE
    currentPriceCents?.value isLessThanOrFalse todayOpenPriceCents?.value -> PriceMovement.NEGATIVE
    else -> PriceMovement.UNKNOWN
  }

  enum class PriceMovement {
    POSITIVE,
    NEGATIVE,
    UNKNOWN
  }

  private infix fun Double?.isMoreThanOrFalse(value: Double?): Boolean {
    if (this == null || value == null) {
      return false
    }
    return this > value
  }

  private infix fun Double?.isLessThanOrFalse(value: Double?): Boolean {
    if (this == null || value == null) {
      return false
    }
    return this < value
  }
}
