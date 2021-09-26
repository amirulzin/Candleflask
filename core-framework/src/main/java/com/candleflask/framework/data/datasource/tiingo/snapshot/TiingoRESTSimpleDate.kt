package com.candleflask.framework.data.datasource.tiingo.snapshot

import com.candleflask.framework.BuildConfig
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class TiingoRESTSimpleDate(private val date: Date) {
  //Java 8 LocalDate only exist in API 26 and above
  init {
    GregorianCalendar.getInstance().let { cal ->
      cal.time = date
      dayDisplay = cal[Calendar.DAY_OF_MONTH]
      monthDisplay = cal[Calendar.MONTH] + 1
      yearDisplay = cal[Calendar.YEAR]
    }
  }

  var dayDisplay: Int private set
  var monthDisplay: Int private set
  var yearDisplay: Int private set

  fun toBasicDisplayString(): String {
    return "%02d-%02d-%d".format(yearDisplay, monthDisplay, dayDisplay)
  }

  class JsonAdapter {
    private val dateFormatter by lazy {
      SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    @ToJson
    fun toJson(@Suppress("UNUSED_PARAMETER") model: TiingoRESTSimpleDate): String =
      throw IllegalStateException("${TiingoRESTSimpleDate::class.simpleName} can't be used for outbound message")

    @FromJson
    @Synchronized
    fun fromJson(json: String): TiingoRESTSimpleDate? {
      try {
        return dateFormatter.parse(json)?.let(::TiingoRESTSimpleDate)
      } catch (e: ParseException) {
        if (BuildConfig.DEBUG) e.printStackTrace()
      }
      return null
    }
  }
}