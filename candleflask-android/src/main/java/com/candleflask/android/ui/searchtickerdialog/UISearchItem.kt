package com.candleflask.android.ui.searchtickerdialog

import com.candleflask.framework.domain.entities.ticker.Ticker

data class UISearchItem(
  val ticker: Ticker,
  val priceCents: String? = null,
  val info: String? = null,
  val isAdded: Boolean = false
)