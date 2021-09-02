package com.candleflask.framework.data.datasource.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TickerDAO {
  @Query("SELECT * FROM tickerEntity")
  fun observeAll(): Flow<List<TickerEntity>>

  @Query("SELECT * FROM tickerEntity")
  fun getAll(): List<TickerEntity>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insert(tickerEntity: TickerEntity): Long

  @Delete
  fun delete(tickerEntity: TickerEntity)

  @Update
  fun update(tickerEntity: TickerEntity)

  @Transaction
  fun upsert(tickerEntity: TickerEntity) {
    if (insert(tickerEntity) == -1L) {
      update(tickerEntity)
    }
  }

  @Update(entity = TickerEntity::class)
  fun updateCurrentPrice(remoteUpdateArgument: RemoteUpdateArgument)

  @Transaction
  fun deleteBySymbol(vararg tickerSymbols: String) {
    for (tickerSymbol in tickerSymbols) {
      delete(TickerEntity(tickerSymbol))
    }
  }

  data class RemoteUpdateArgument(
    val tickerSymbol: String,
    val currentAskPriceCents: String?,
    val lastUpdatedEpochMillis: Long?
  )
}
