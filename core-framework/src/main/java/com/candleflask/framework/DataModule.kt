package com.candleflask.framework

import com.candleflask.framework.data.APITokenRepository
import com.candleflask.framework.data.TiingoTickerRepository
import com.candleflask.framework.data.datasource.*
import com.candleflask.framework.data.datasource.db.DatabaseController
import com.candleflask.framework.data.datasource.db.TickerDAO
import com.candleflask.framework.data.datasource.tiingo.snapshot.TiingoREST
import com.candleflask.framework.data.datasource.tiingo.snapshot.TiingoSnapshotTickerDataSource
import com.candleflask.framework.data.datasource.tiingo.streaming.TiingoStreamingTickerDataFactory
import com.candleflask.framework.domain.features.securitytoken.EncryptedTokenRepository
import com.candleflask.framework.domain.features.tickers.TickerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface DataModule {
  @Singleton
  @Binds
  fun tickerRepository(impl: TiingoTickerRepository): TickerRepository

  @Singleton
  @Binds
  fun streamingTickerDataSource(impl: TiingoStreamingTickerDataFactory): StreamingTickerDataFactory

  @Singleton
  @Binds
  fun encryptedDataSource(impl: EncryptedSharedPrefsDataSource): EncryptedDataSource

  @Singleton
  @Binds
  fun favoriteTickerDataSource(impl: PrefsFavoriteTickerDataSource): FavoriteTickerDataSource

  @Singleton
  @Binds
  fun encryptedTokenRepository(impl: APITokenRepository): EncryptedTokenRepository

  @Singleton
  @Binds
  fun snapshotTickerDataSource(impl: TiingoSnapshotTickerDataSource): SnapshotTickerDataSource

  @Singleton
  @Binds
  fun webSocketController(impl: OkHttpWebSocketController): WebSocketController

  @InstallIn(SingletonComponent::class)
  @Module
  class FrameworkProvidersModule {
    @Singleton
    @Provides
    fun tiingoREST(httpClient: OkHttpClient): TiingoREST {
      return TiingoREST.retrofitBuilder(httpClient)
        .build()
        .create(TiingoREST::class.java)
    }

    @Singleton
    @Provides
    fun tickerDAO(databaseController: DatabaseController): TickerDAO {
      return databaseController.database.tickerDAO()
    }
  }
}