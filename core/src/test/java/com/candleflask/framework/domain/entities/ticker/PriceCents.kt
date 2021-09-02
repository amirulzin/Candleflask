package com.candleflask.framework.domain.entities.ticker

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Initial implementation of Double based data model prior to the usage of Java Money API (BigMoney)
 */
data class PriceCents(val value: Double) {
  constructor(valueInCents: Long) : this(valueInCents.toDouble())

  private val bigDecimal = value.toBigDecimal().stripTrailingZeros()

  val standardDisplay: String = bigDecimal
    .setScale(2, getRoundingMode(bigDecimal, 100))
    .toPlainString()

  companion object {
    private val BIG_DECIMAL_100 = 100.toBigDecimal()

    /**
     * This is only correct up to 14 significant digits (e.g 10,000,000,000,000)
     *
     * For historical purpose only. Switched to Java Money API
     *
     * **History:**
     *
     * Storing API Double values as a scaled Long is technically the correct
     * way to go about it. However, floating points are nasty when it comes to rounding
     * in monetary terms.
     *
     * Due to this, it is practically better to opt for String representation of Double
     * (from API endpoints) for 100% accurate `storage` and only convert them when you really
     * need it e.g. right before computing them in the UI layer.
     * */
    @JvmStatic
    fun getRoundingMode(bigDecimal: BigDecimal, centScale: Int = 100): RoundingMode {
      val rightShift: BigDecimal = when (centScale) {
        100 -> BIG_DECIMAL_100
        else -> centScale.toBigDecimal()
      }
      val scale = bigDecimal.setScale(4, RoundingMode.HALF_UP) //10.0095
      val remainder = scale.times(rightShift).remainder(BigDecimal.ONE)  //10009.5 -> .5
      val significantRemainder = remainder.times(BigDecimal.TEN)
      val extraScale = significantRemainder.toInt()
      val requireCeiling = extraScale >= 5
      return if (requireCeiling) RoundingMode.CEILING else RoundingMode.HALF_UP
    }
  }
}
