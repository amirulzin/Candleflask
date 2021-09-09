package com.candleflask.android.di

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
interface AppModule {
  @Singleton
  @Binds
  fun delegatedNetwork(androidDelegatedNetwork: AndroidDelegatedNetwork): DelegatedNetwork

  @InstallIn(SingletonComponent::class)
  @Module
  class Providers {
    @Singleton
    @Provides
    fun okHttpClient(application: Application): OkHttpClient {
      return OkHttpClient.Builder()
        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .cache(Cache(application.cacheDir, 25L * 1024 * 1024))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    }
  }
}