package com.candleflask.android.ui.searchtickerdialog

import com.candleflask.framework.domain.entities.ticker.PriceCents
import com.candleflask.framework.domain.entities.ticker.Ticker

data class UISearchItem(
  val ticker: Ticker,
  val priceCents: PriceCents? = null,
  val info: String? = null,
  val isAdded: Boolean = false
)