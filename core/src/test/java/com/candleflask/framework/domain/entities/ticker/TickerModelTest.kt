package com.candleflask.framework.domain.entities.ticker

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class TickerModelTest {

  @Test
  fun `get symbolNormalized, returns uppercase symbol`() {
    assertEquals(TickerModel(symbol = "googl").symbolNormalized, "GOOGL")
  }

  @Test
  fun `when currentPrice is higher than open price, returns PriceMovement POSITIVE`() {
    TickerModel(
      symbol = "googl",
      todayOpenPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
      currentPrice = BigMoney.ofScale(CurrencyUnit.USD, 5000, 2),
    ).let {
      assertEquals(it.priceMovement, TickerModel.PriceMovement.POSITIVE)
    }
  }

  @Test
  fun `when currentPrice is lower than open price, returns PriceMovement NEGATIVE`() {
    TickerModel(
      symbol = "googl",
      todayOpenPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
      currentPrice = BigMoney.ofScale(CurrencyUnit.USD, 3000, 2),
    ).let {
      assertEquals(it.priceMovement, TickerModel.PriceMovement.NEGATIVE)
    }
  }

  @Test
  fun `when currentPrice is equals to open price, returns PriceMovement UNKNOWN`() {
    TickerModel(
      symbol = "googl",
      todayOpenPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
      currentPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
    ).let {
      assertEquals(it.priceMovement, TickerModel.PriceMovement.UNKNOWN)
    }
  }

  @Test
  fun `when currentPrice is null, returns PriceMovement UNKNOWN`() {
    TickerModel(
      symbol = "googl",
      todayOpenPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
      currentPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
    ).let {
      assertEquals(it.priceMovement, TickerModel.PriceMovement.UNKNOWN)
    }
  }

  @Test
  fun `when todayOpenPrice is null, returns PriceMovement UNKNOWN`() {
    TickerModel(
      symbol = "googl",
      todayOpenPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
      currentPrice = BigMoney.ofScale(CurrencyUnit.USD, 4200, 2),
    ).let {
      assertEquals(it.priceMovement, TickerModel.PriceMovement.UNKNOWN)
    }
  }
}