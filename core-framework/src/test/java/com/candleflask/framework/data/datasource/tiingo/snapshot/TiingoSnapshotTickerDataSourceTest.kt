package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.candleflask.framework.domain.entities.ticker.Ticker
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import javax.inject.Provider

class TiingoSnapshotTickerDataSourceTest {

  @Test
  fun retrieve() {
    val json = """
        [
          {
            "askSize": null,
            "ticker": "AAPL",
            "low": 146.83,
            "quoteTimestamp": "2021-08-27T20:00:00+00:00",
            "volume": 55802388,
            "lastSaleTimestamp": "2021-08-27T20:00:00+00:00",
            "tngoLast": 148.6,
            "mid": null,
            "open": 147.48,
            "bidPrice": null,
            "timestamp": "2021-08-27T20:00:00+00:00",
            "high": 148.75,
            "bidSize": null,
            "lastSize": null,
            "prevClose": 147.54,
            "askPrice": null,
            "last": 148.6
          },
          {
            "askSize": null,
            "ticker": "GOOGL",
            "low": 2829.94,
            "quoteTimestamp": "2021-08-27T20:00:00+00:00",
            "volume": 1439010,
            "lastSaleTimestamp": "2021-08-27T20:00:00+00:00",
            "tngoLast": 2880.08,
            "mid": null,
            "prevClose": 2828.81,
            "open": 2833.05,
            "timestamp": "2021-08-27T20:00:00+00:00",
            "high": 2890.25,
            "bidSize": null,
            "lastSize": null,
            "bidPrice": null,
            "askPrice": null,
            "last": 2880.08
          }
        ]
      """.trimIndent()

    val jsonAdapter = TiingoREST.createMoshiBuilder().build()
      .adapter<List<TiingoRESTTickerLatest>>(
        Types.newParameterizedType(
          List::class.java,
          TiingoRESTTickerLatest::class.java
        )
      )

    val resultResponse = requireNotNull(jsonAdapter.fromJson(json))

    val googleTicker = Ticker("GOOGL")
    val appleTicker = Ticker("AAPL")
    val fakeToken = "fakeToken"
    val tickers = setOf(googleTicker.key, appleTicker.key)
    val tickersString = tickers.joinToString()

    val tiingoRest = mock<TiingoREST> {
      onBlocking { iexLatest(tickersString, fakeToken) } doReturn resultResponse
    }

    val tiingoRestProvider = Provider<TiingoREST> { tiingoRest }

    runBlocking {
      val dataSource = TiingoSnapshotTickerDataSource(tiingoRestProvider)
      val result = dataSource.retrieve(tickers, fakeToken)
      assertEquals(googleTicker.key, result.first { it.symbol == googleTicker.key }.symbol)
      assertEquals(appleTicker.key, result.first { it.symbol == appleTicker.key }.symbol)
    }
  }
}