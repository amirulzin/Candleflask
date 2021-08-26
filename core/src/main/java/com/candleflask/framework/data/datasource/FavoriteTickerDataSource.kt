package com.candleflask.framework.data.datasource

import com.candleflask.framework.domain.entities.ticker.Ticker

interface FavoriteTickerDataSource {
  fun persistFavoriteTickers(tickers: Set<String>)

  fun storeFavoriteTicker(ticker: Ticker)

  fun removeFavoriteTicker(ticker: Ticker)

  fun loadFavoriteTickers(): MutableSet<Ticker>
}