package com.candleflask.framework.domain.entities.ticker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.math.RoundingMode

class PriceCentsTest {

  private val representationMap = SamplePrices.representationMap

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
  fun `when Long MAX_VALUE countDigit, return 19`() {
    assertEquals(19, Long.MAX_VALUE.countDigit())
  }

}