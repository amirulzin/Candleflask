package com.candleflask.android.ui.home

import com.candleflask.framework.domain.entities.ticker.TickerModel

data class UITickerItem(
  val index: Int = -1,
  val model: TickerModel,
  val isChecked: Boolean = false,
  val isExpanded: Boolean = false
)