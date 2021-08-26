package com.candleflask.framework.data.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TickerEntity::class], version = 1, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
  abstract fun tickerDAO(): TickerDAO
}