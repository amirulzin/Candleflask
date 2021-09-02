package com.candleflask.framework.domain.entities.ticker

import kotlin.math.pow

fun Long.countDigit(): Int {
  var digit = 0
  var value = this
  while (value != 0L) {
    value /= 10L
    digit++
  }
  return digit
}

infix fun Long.power(root: Int): Long {
  return this.toDouble().pow(root).toLong()
}