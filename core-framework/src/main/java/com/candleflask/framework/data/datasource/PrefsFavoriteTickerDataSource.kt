package com.candleflask.framework.data.datasource

import androidx.core.content.edit
import com.candleflask.framework.domain.entities.ticker.Ticker
import javax.inject.Inject

class PrefsFavoriteTickerDataSource @Inject constructor(private val prefsController: SharedPrefsController) :
  FavoriteTickerDataSource {
  companion object {
    const val KEY_FAV_TICKERS = "FAV_TICKERS"
  }


  override fun persistFavoriteTickers(tickers: Set<String>) {
    prefsController.prefs.edit {
      putStringSet(KEY_FAV_TICKERS, tickers)
    }
  }

  override fun storeFavoriteTicker(ticker: Ticker) {
    val update = loadFavoriteTickers().apply {
      add(ticker)
    }.mapTo(mutableSetOf(), Ticker::key)

    persistFavoriteTickers(update)
  }

  override fun removeFavoriteTicker(ticker: Ticker) {
    val update = loadFavoriteTickers()
      .asSequence()
      .filterNot { it == ticker }
      .map(Ticker::key)
      .toSet()

    persistFavoriteTickers(update)
  }

  override fun loadFavoriteTickers(): MutableSet<Ticker> {
    return prefsController.prefs
      .getStringSet(KEY_FAV_TICKERS, null)
      .orEmpty()
      .mapTo(mutableSetOf(), ::Ticker)
  }
}