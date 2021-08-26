package com.candleflask.framework.data.datasource.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TickerEntity(
  @PrimaryKey
  val tickerSymbol: String,
  val yesterdayClosePriceCents: Long? = null,
  val todayOpenPriceCents: Long? = null,
  val currentAskPriceCents: Long? = null,
  val lastUpdatedEpochMillis: Long? = null
)