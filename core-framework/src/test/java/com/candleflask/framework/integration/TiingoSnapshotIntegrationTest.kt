package com.candleflask.framework.integration

import com.candleflask.framework.data.datasource.tiingo.snapshot.TiingoREST
import com.candleflask.framework.data.datasource.tiingo.snapshot.TiingoSnapshotTickerDataSource
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.framework.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Provider

class TiingoSnapshotIntegrationTest {

  @Before
  fun before() {
    assumeTrue(BuildConfig.INTEGRATION_TEST_API_KEY.isNotBlank())
  }

  @Test
  fun whenSearchTicker_mustReturnOneResult() {
    val tiingoREST = TiingoREST.retrofitBuilder(OkHttpClient())
      .build()
      .create(TiingoREST::class.java)

    runBlocking {
      val targetTicker = Ticker("NVDA")
      val result = TiingoSnapshotTickerDataSource(Provider { tiingoREST })
        .retrieve(setOf(targetTicker.key), BuildConfig.INTEGRATION_TEST_API_KEY)

      assertEquals(result.first().symbol, targetTicker.key)
    }
  }
}