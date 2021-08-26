package com.candleflask.android.di

import com.candleflask.framework.data.APITokenRepository
import com.candleflask.framework.data.TiingoTickerRepository
import com.candleflask.framework.data.datasource.*
import com.candleflask.framework.data.datasource.tiingo.snapshot.TiingoREST
import com.candleflask.framework.data.datasource.tiingo.snapshot.TiingoSnapshotTickerDataSource
import com.candleflask.framework.data.datasource.tiingo.streaming.TiingoStreamingTickerDataSource
import com.candleflask.framework.features.securitytoken.EncryptedTokenRepository
import com.candleflask.framework.features.tickers.TickerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient

@InstallIn(ViewModelComponent::class)
@Module
interface FrameworkModule {
  @Binds
  fun tickerRepository(impl: TiingoTickerRepository): TickerRepository

  @Binds
  fun streamingTickerDataSource(impl: TiingoStreamingTickerDataSource): StreamingTickerDataSource

  @Binds
  fun encryptedDataSource(impl: EncryptedSharedPrefsDataSource): EncryptedDataSource

  @Binds
  fun favoriteTickerDataSource(impl: PrefsFavoriteTickerDataSource): FavoriteTickerDataSource

  @Binds
  fun encryptedTokenRepository(impl: APITokenRepository): EncryptedTokenRepository

  @Binds
  fun snapshotTickerDataSource(impl: TiingoSnapshotTickerDataSource): SnapshotTickerDataSource

  @InstallIn(ViewModelComponent::class)
  @Module
  class FrameworkProvidersModule {
    @Provides
    fun tiingoREST(httpClient: OkHttpClient): TiingoREST {
      return TiingoREST.retrofitBuilder(httpClient)
        .build()
        .create(TiingoREST::class.java)
    }
  }
}