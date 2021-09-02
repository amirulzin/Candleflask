package com.candleflask.android.ui.searchtickerdialog

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.features.tickers.SearchTickersUseCase
import com.candleflask.framework.features.tickers.UpdateSubscribedTickersUseCase
import common.android.ui.UIResource
import common.android.ui.UIResource.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchTickerViewModel @Inject constructor(
  private val searchTickersUseCase: SearchTickersUseCase,
  private val updateSubscribedTickersUseCase: UpdateSubscribedTickersUseCase
) :
  ViewModel() {

  val searchResultStateFlow by lazy {
    MutableStateFlow<UIResource<List<UISearchItem>>>(Success(emptyList()))
  }

  fun searchTicker(input: String) {
    viewModelScope.launch {
      if (searchResultStateFlow.value !is Loading) {
        searchResultStateFlow.value = Loading()
        withContext(Dispatchers.IO) {
          searchResultStateFlow.value = when (val resultList = searchTickersUseCase.search(input.trim())) {
            is SearchTickersUseCase.Output.Success -> Success(resultList.tickers.render())
            SearchTickersUseCase.Output.NetworkError -> Error("Network Error")
            SearchTickersUseCase.Output.TokenError -> Error("Invalid token")
          }
        }
      }
    }
  }

  fun addTicker(searchItem: UISearchItem) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        updateSubscribedTickersUseCase.addAndSubscribe(searchItem.ticker)
      }
    }
  }

  @WorkerThread
  private fun List<TickerModel>.render(): List<UISearchItem> {
    return map { model ->
      val ticker = Ticker(model.symbol)
      UISearchItem(
        ticker = ticker,
        priceCents = model.currentPrice?.amount?.toPlainString(),
        info = null,
        isAdded = updateSubscribedTickersUseCase.retrieveSubscribedTickers().contains(ticker)
      )
    }
  }
}