package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.candleflask.framework.domain.entities.ticker.PriceCents
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TiingoRESTResponseConversionTest {
  @Test
  fun `convert meta info`() {
    val json = """
    {
      "name": "Square Inc - Class A",
      "description": "Square, Inc. builds tools to empower businesses and individuals to participate in the economy. Sellers use Square to reach buyers online and in person, manage their business, and access financing. Individuals use Cash App to spend, send, store, and invest money. And TIDAL is a global music and entertainment platform that expands Square's purpose of economic empowerment to artists. Square, Inc. has offices in the United States, Canada, Japan, Australia, Ireland, Spain, Norway, and the UK.",
      "exchangeCode": "NYSE",
      "endDate": "2021-08-27",
      "ticker": "SQ",
      "startDate": "2015-11-19"
    }
  """.trimIndent()

    val result = moshi().adapter(TiingoRESTMetaInfo::class.java).fromJson(json)
    assertEquals(result?.startDate?.toBasicDisplayString(), "2015-11-19")
    assertEquals(result?.endDate?.toBasicDisplayString(), "2021-08-27")
    assertEquals(result?.exchangeCode, "NYSE")
    assertEquals(result?.ticker, "SQ")
    assertEquals(result?.name, "Square Inc - Class A")
  }

  @Test
  fun convertFromJson() {
    val json =
      """
      [
        {
          "adjClose": 268.01,
          "adjHigh": 270.4838,
          "adjLow": 261.6153,
          "adjOpen": 262,
          "adjVolume": 4646212,
          "close": 268.01,
          "date": "2021-08-27T00:00:00+00:00",
          "divCash": 0,
          "high": 270.4838,
          "low": 261.6153,
          "open": 262,
          "splitFactor": 1,
          "volume": 4646212
        }
      ]
    """.trimIndent()

    val type = Types.newParameterizedType(List::class.java, TiingoRESTTickerEndOfDay::class.java)
    val result = moshi().adapter<List<TiingoRESTTickerEndOfDay>>(type)
      .fromJson(json)

    assertFalse(result.isNullOrEmpty())
    assertEquals(result?.first()?.close, PriceCents(26801))
  }

  private fun moshi(): Moshi = TiingoREST
    .createMoshiBuilder()
    .build()
}