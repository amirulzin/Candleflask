package com.candleflask.framework.domain.entities.ticker

import org.junit.Assert.assertEquals
import org.junit.Test

class TickerTest {
  @Test
  fun `when any symbol is passed to key property, get property in uppercase`() {
    assertEquals("GOOGL", Ticker("GooGl").key)
  }
}