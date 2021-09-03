package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.candleflask.framework.domain.entities.ticker.Ticker
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.*
import javax.inject.Provider

class TiingoSnapshotTickerDataSourceTest {

  private val json = """
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

  private val jsonAdapter by lazy {
    TiingoREST.createMoshiBuilder().build()
      .adapter<List<TiingoRESTTickerLatest>>(
        Types.newParameterizedType(
          List::class.java,
          TiingoRESTTickerLatest::class.java
        )
      )
  }

  private val resultResponse by lazy {
    requireNotNull(jsonAdapter.fromJson(json))
  }

  @Test
  fun `request 2 tickers to snapshot API, return 2 matching TickerModels in 1 request`() {
    val googleTicker = Ticker("GOOGL")
    val appleTicker = Ticker("AAPL")
    val fakeToken = "fakeToken"
    val tickers = setOf(googleTicker.key, appleTicker.key)

    val tiingoRest: TiingoREST = mock {
      onBlocking { iexLatest(any(), any()) } doReturn resultResponse
    }

    val dataSource = TiingoSnapshotTickerDataSource(Provider { tiingoRest })
    runBlocking {
      val tickerModels = dataSource.retrieve(tickers, fakeToken)
      assertEquals(appleTicker.key, tickerModels[0].symbolNormalized)
      assertEquals(googleTicker.key, tickerModels[1].symbolNormalized)
    }

    verifyBlocking(tiingoRest, times(1)) {
      iexLatest(any(), any())
    }
  }

  @Test
  fun `convert array of JSON should return 2 matching TickerLatest`() {
    val response = requireNotNull(jsonAdapter.fromJson(json))

    val apple = response[0]
    assertEquals("AAPL", apple.ticker)
    assertEquals("148.6", apple.last)

    val google = response[1]
    assertEquals("GOOGL", google.ticker)
    assertEquals("2880.08", google.last)
  }
}