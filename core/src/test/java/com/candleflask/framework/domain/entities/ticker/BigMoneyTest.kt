package com.candleflask.framework.domain.entities.ticker

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode


class BigMoneyTest {

  private val representationMap = SamplePrices.representationMap

  @Test
  fun `given USD Long MAX_VALUE with 3 decimals, returns BigMoney rounded scale of 2 rounded HALF_UP`() {
    val bigMoneyString = BigMoney.parse("USD ${Long.MAX_VALUE}.009")
      .withScale(2, RoundingMode.HALF_UP)
      .amount
      .toPlainString()

    Assert.assertEquals("${Long.MAX_VALUE}.01", bigMoneyString)
  }

  @Test
  fun `when BigMoney USD is rounded HALF_UP, return correct representation`() {
    representationMap.forEach { (display, value) ->
      val result = BigMoney.ofScale(CurrencyUnit.USD, BigDecimal.valueOf(value), 2, RoundingMode.HALF_UP)
        .amount
        .toPlainString()

      Assert.assertEquals(display, result)
    }
  }

  @Test
  fun `given USD Long cents with scale, return BigMoney`() {
    val priceCents = 42069L
    val scale = 2
    val bigMoney = BigMoney.ofScale(CurrencyUnit.USD, priceCents, scale)

    Assert.assertEquals("420.69", bigMoney.amount.toPlainString())
  }

}