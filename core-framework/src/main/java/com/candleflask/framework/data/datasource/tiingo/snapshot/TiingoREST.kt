package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface TiingoREST {
  companion object {
    private const val ENDPOINT = "https://api.tiingo.com"

    @JvmStatic
    fun retrofitBuilder(httpClient: OkHttpClient): Retrofit.Builder {
      //Use separate Moshi to avoid polluting JSON adapters defaults
      val moshi = createMoshiBuilder().build()
      val converterFactory = MoshiConverterFactory.create(moshi)

      return Retrofit.Builder()
        .baseUrl(ENDPOINT)
        .client(httpClient)
        .addConverterFactory(converterFactory)
    }

    @JvmStatic
    fun createMoshiBuilder(): Moshi.Builder {
      return Moshi.Builder()
        .add(TiingoRESTSimpleDate.JsonAdapter())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
    }
  }

  @GET("tiingo/daily/{ticker}")
  suspend fun metaInfo(@Path("ticker") ticker: String, @Query("token") token: String): TiingoRESTMetaInfo

  @GET("tiingo/daily/{ticker}/prices")
  suspend fun endOfDay(@Path("ticker") ticker: String, @Query("token") token: String): List<TiingoRESTTickerEndOfDay>

  @GET("iex/{commaSeparatedTickers}")
  suspend fun iexLatest(
    @Path("commaSeparatedTickers") commaSeparatedTickers: String,
    @Query("token") token: String
  ): List<TiingoRESTTickerLatest>
}