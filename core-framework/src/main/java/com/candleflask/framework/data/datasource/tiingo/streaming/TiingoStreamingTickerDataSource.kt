package com.candleflask.framework.data.datasource.tiingo.streaming

import com.candleflask.framework.data.datasource.StreamingTickerDataSource
import com.candleflask.framework.data.datasource.StreamingTickerDataSource.OperationOutput
import com.candleflask.framework.domain.entities.ticker.PriceCents
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TiingoStreamingTickerDataSource @Inject constructor(private val messageConverter: TiingoWSMessageConverter) :
  StreamingTickerDataSource {
  companion object {
    const val WS_ENDPOINT = "wss://api.tiingo.com/iex"
    const val WS_EVENT_SUBSCRIBE = "subscribe"
    const val WS_EVENT_UNSUBSCRIBE = "unsubscribe"
  }

  override fun wsInitializationRequest(token: String): Request {
    return Request.Builder()
      .url(WS_ENDPOINT)
      .build()
  }

  override fun wsSubscribeTickersMessage(token: String, vararg tickers: Ticker): String {
    val request = TiingoWSRequest(
      eventName = WS_EVENT_SUBSCRIBE,
      authorization = token,
      eventData = TiingoWSRequest.EventData(
        tickers = tickers.mapTo(mutableSetOf(), Ticker::key)
      )
    )
    return messageConverter.asJson(request)
  }

  override fun wsUnsubscribeTickersMessage(token: String, vararg tickers: Ticker): String {
    val request = TiingoWSRequest(
      eventName = WS_EVENT_UNSUBSCRIBE,
      authorization = token,
      eventData = TiingoWSRequest.EventData(
        tickers = tickers.mapTo(mutableSetOf(), Ticker::key)
      )
    )
    return messageConverter.asJson(request)
  }

  override fun wsHandleMessage(message: String): OperationOutput {
    return when (val response = messageConverter.parseWebSocketMessage(message)) {
      is TiingoWSResponse.HeartBeat -> OperationOutput.Heartbeat
      is TiingoWSResponse.Subscribe -> OperationOutput.Unknown
      is TiingoWSResponse.Tick -> OperationOutput.PriceUpdate(response.asTickerModel())
      else -> OperationOutput.Unknown
    }
  }

  private fun TiingoWSResponse.Tick.asTickerModel(): TickerModel {
    return with(data) {
      TickerModel(
        symbol = Ticker(ticker).key,
        todayOpenPriceCents = null,
        yesterdayClosePriceCents = null,
        currentPriceCents = lastPrice?.let(::PriceCents),
        lastUpdated = TimeUnit.NANOSECONDS.toMillis(epochNanos),
      )
    }
  }
}
