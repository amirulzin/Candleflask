package com.candleflask.framework.domain.entities.ticker

import org.joda.money.BigMoney

data class TickerModel(
  val symbol: String,
  val todayOpenPrice: BigMoney? = null,
  val yesterdayClosePrice: BigMoney? = null,
  val currentPrice: BigMoney? = null,
  val lastUpdated: Long? = null,
) {

  val priceMovement = when {
    currentPrice isMoreThanOrFalse todayOpenPrice -> PriceMovement.POSITIVE
    currentPrice isLessThanOrFalse todayOpenPrice -> PriceMovement.NEGATIVE
    else -> PriceMovement.UNKNOWN
  }

  enum class PriceMovement {
    POSITIVE,
    NEGATIVE,
    UNKNOWN
  }

  private infix fun BigMoney?.isMoreThanOrFalse(value: BigMoney?): Boolean {
    if (this == null || value == null) {
      return false
    }
    return this > value
  }

  private infix fun BigMoney?.isLessThanOrFalse(value: BigMoney?): Boolean {
    if (this == null || value == null) {
      return false
    }
    return this < value
  }
}
