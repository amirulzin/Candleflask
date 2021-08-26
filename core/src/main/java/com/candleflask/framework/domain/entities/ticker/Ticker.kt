package com.candleflask.framework.domain.entities.ticker

data class Ticker(private val _key: String) {
  val key: String = _key.uppercase()
}