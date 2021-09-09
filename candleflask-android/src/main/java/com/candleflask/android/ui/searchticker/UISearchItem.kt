package com.candleflask.android.ui.searchticker

import com.candleflask.framework.domain.entities.ticker.Ticker

data class UISearchItem(
  val ticker: Ticker,
  val priceCents: String? = null,
  val info: String? = null,
  val isAdded: Boolean = false
)