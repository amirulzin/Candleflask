package com.candleflask.framework.domain.entities.ticker

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

class PriceCentsTest {

  private val representationMap = mapOf(
    "100.56" to 100.555,
    "100.55" to 100.554,
    "102.00" to 101.995,
    "100.02" to 100.015,
    "100.99" to 100.991,
    "100.94" to 100.935,
    "100.10" to 100.1,
    "100.24" to 100.24400,
    "99.99" to 99.9909,
    "100.50" to 100.5,
    "99.01" to 99.009,
    "1000000000.00" to 1000000000.0000000009990,
    "10000000000000.01" to 10000000000000.009,
  )

  @Test
  fun `when given Long, return 2 padded standardDisplay`() {
    assertEquals("1234.00", PriceCents(1234L).standardDisplay)
  }

  @Test
  fun `when Double is rounded, return ceiling on point 5+, half up for below`() {
    assertEquals(RoundingMode.CEILING, PriceCents.getRoundingMode(1233.995.toBigDecimal(), centScale = 100))
    assertEquals(RoundingMode.HALF_UP, PriceCents.getRoundingMode(1233.934.toBigDecimal(), centScale = 100))
  }

  @Test
  fun `when Double is rounded, return correct standardDisplay`() {
    representationMap.forEach { (display, value) ->
      assertEquals(display, PriceCents(value).standardDisplay)
    }
    assertNotEquals("100000000000000.01", PriceCents(100000000000000.009).standardDisplay)
  }

  @Test
  fun `assume rounding is correct up to edge value`() {
    for (i in 0..Long.MAX_VALUE.countDigit()) {
      val tenthPower = 10L power i
      val double = tenthPower + 0.009
      val testCase = "$tenthPower.01" == PriceCents(double).standardDisplay
      if (!testCase) {
        println("Edge value at $tenthPower")
      }
      assumeTrue(testCase)
    }
  }

  @Test
  fun `given USD Long MAX_VALUE with 3 decimals, returns BigMoney rounded scale of 2 rounded HALF_UP`() {
    val bigMoneyString = BigMoney.parse("USD ${Long.MAX_VALUE}.009")
      .withScale(2, RoundingMode.HALF_UP)
      .amount
      .toPlainString()

    assertEquals("${Long.MAX_VALUE}.01", bigMoneyString)
  }

  @Test
  fun `when BigMoney USD is rounded HALF_UP, return correct representation`() {
    representationMap.forEach { (display, value) ->
      val result = BigMoney.ofScale(CurrencyUnit.USD, BigDecimal.valueOf(value), 2, RoundingMode.HALF_UP)
        .amount
        .toPlainString()

      assertEquals(display, result)
    }
  }

  @Test
  fun `when Long MAX_VALUE countDigit, return 19`() {
    assertEquals(19, Long.MAX_VALUE.countDigit())
  }

  private fun Long.countDigit(): Int {
    var digit = 0
    var value = this
    while (value != 0L) {
      value /= 10L
      digit++
    }
    return digit
  }

  private infix fun Long.power(root: Int): Long {
    return this.toDouble().pow(root).toLong()
  }
}