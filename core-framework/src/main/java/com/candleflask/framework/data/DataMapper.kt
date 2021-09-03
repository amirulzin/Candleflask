package com.candleflask.framework.data

import com.candleflask.framework.data.datasource.db.TickerEntity
import com.candleflask.framework.data.datasource.tiingo.snapshot.TiingoRESTTickerLatest
import com.candleflask.framework.data.datasource.tiingo.streaming.TiingoWSResponse
import com.candleflask.framework.domain.entities.ticker.TickerModel
import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

object DataMapper {
  @JvmStatic
  fun toPlainString(bigMoney: BigMoney): String {
    return bigMoney.withScale(2, RoundingMode.HALF_UP)
      .amount
      .toPlainString()
  }

  @JvmStatic
  fun toBigMoney(doubleInString: String): BigMoney {
    return BigMoney.of(CurrencyUnit.USD, BigDecimal(doubleInString))
  }

  @JvmStatic
  fun toTickerModel(entity: TickerEntity) = with(entity) {
    TickerModel(
      symbol = tickerSymbol,
      todayOpenPrice = todayOpenPriceCents?.let(::toBigMoney),
      yesterdayClosePrice = yesterdayClosePriceCents?.let(::toBigMoney),
      currentPrice = currentAskPriceCents?.let(::toBigMoney),
      lastUpdated = lastUpdatedEpochMillis,
    )
  }

  @JvmStatic
  fun toTickerEntity(model: TickerModel) = with(model) {
    TickerEntity(
      tickerSymbol = symbolNormalized,
      yesterdayClosePriceCents = yesterdayClosePrice?.let(::toPlainString),
      todayOpenPriceCents = todayOpenPrice?.let(::toPlainString),
      currentAskPriceCents = currentPrice?.let(::toPlainString),
      lastUpdatedEpochMillis = lastUpdated,
    )
  }

  @JvmStatic
  fun toTickerEntity(wsResponseTick: TiingoWSResponse.Tick) = with(wsResponseTick.data) {
    //null values = Tiingo WS API doesn't provide this
    TickerEntity(
      tickerSymbol = ticker,
      yesterdayClosePriceCents = null,
      todayOpenPriceCents = null,
      currentAskPriceCents = lastPrice?.let(::toBigMoney)?.let(::toPlainString),
      lastUpdatedEpochMillis = TimeUnit.NANOSECONDS.toMillis(epochNanos),
    )
  }

  @JvmStatic
  fun toTickerModel(wsResponseTick: TiingoWSResponse.Tick) = with(wsResponseTick.data) {
    TickerModel(
      symbol = ticker,
      todayOpenPrice = null,
      yesterdayClosePrice = null,
      currentPrice = lastPrice?.let(::toBigMoney),
      lastUpdated = TimeUnit.NANOSECONDS.toMillis(epochNanos),
    )
  }

  @JvmStatic
  fun toTickerEntity(model: TiingoRESTTickerLatest) = with(model) {
    TickerEntity(
      tickerSymbol = ticker,
      yesterdayClosePriceCents = open,
      todayOpenPriceCents = prevClose,
      currentAskPriceCents = last,
      lastUpdatedEpochMillis = quoteTimestamp?.time,
    )
  }

  @JvmStatic
  fun toTickerModel(model: TiingoRESTTickerLatest) = with(model) {
    TickerModel(
      symbol = ticker,
      todayOpenPrice = open?.let(::toBigMoney),
      yesterdayClosePrice = prevClose?.let(::toBigMoney),
      currentPrice = last?.let(::toBigMoney),
      lastUpdated = quoteTimestamp?.time,
    )
  }
}