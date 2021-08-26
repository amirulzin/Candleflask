package com.candleflask.framework.data.datasource

import com.candleflask.framework.domain.entities.ticker.TickerModel

interface SnapshotTickerDataSource {
  suspend fun retrieve(symbols: Set<String>, token: String): List<TickerModel>
}