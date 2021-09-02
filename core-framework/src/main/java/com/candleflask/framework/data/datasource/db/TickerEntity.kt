package com.candleflask.framework.data.datasource.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TickerEntity(
  @PrimaryKey
  val tickerSymbol: String,
  val yesterdayClosePriceCents: String? = null,
  val todayOpenPriceCents: String? = null,
  val currentAskPriceCents: String? = null,
  val lastUpdatedEpochMillis: Long? = null
)