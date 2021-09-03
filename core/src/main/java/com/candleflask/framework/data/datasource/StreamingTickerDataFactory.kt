package com.candleflask.framework.data.datasource

import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import okhttp3.Request

interface StreamingTickerDataFactory {
  fun wsInitializationRequest(token: String): Request

  fun wsSubscribeTickersMessage(token: String, vararg tickers: Ticker): String

  fun wsUnsubscribeTickersMessage(token: String, vararg tickers: Ticker): String

  fun wsHandleMessage(message: String): OperationOutput

  sealed class OperationOutput {
    object Heartbeat : OperationOutput()
    data class PriceUpdate(val tickerModel: TickerModel) : OperationOutput()
    object Unknown : OperationOutput()
  }

}