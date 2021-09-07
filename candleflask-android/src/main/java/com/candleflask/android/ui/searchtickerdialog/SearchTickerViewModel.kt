package com.candleflask.android.ui.searchtickerdialog

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.domain.features.tickers.SearchTickersUseCase
import com.candleflask.framework.domain.features.tickers.TickerRepository.OperationResult
import com.candleflask.framework.domain.features.tickers.UpdateSubscribedTickersUseCase
import common.android.network.isNetworkConnected
import common.android.ui.UIResource
import common.android.ui.UIResource.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchTickerViewModel @Inject constructor(
  private val searchTickersUseCase: SearchTickersUseCase,
  private val updateSubscribedTickersUseCase: UpdateSubscribedTickersUseCase,
  private val application: Application
) : ViewModel() {

  private val _searchResultStateFlow by lazy {
    MutableStateFlow<UIResource<List<UISearchItem>>>(Empty())
  }

  val searchResultStateFlow get() = _searchResultStateFlow.asStateFlow()

  fun searchTicker(input: String) {
    viewModelScope.launch(Dispatchers.IO) {
      with(_searchResultStateFlow) {
        if (value !is Loading) {
          when {
            !application.isNetworkConnected() -> value = Error("Network Disconnected")
            input.isBlank() -> value = Error("Valid ticker required")
            else -> {
              value = Loading()
              value = when (val result = searchTickersUseCase.execute(input.trim())) {
                is OperationResult.InvalidTokenError -> Error("Please use a valid token in Settings")
                is OperationResult.Success -> Success(render(result.output))
              }
            }
          }
        }
      }
    }
  }

  fun addTicker(searchItem: UISearchItem) {
    viewModelScope.launch(Dispatchers.IO) {
      updateSubscribedTickersUseCase.addAndSubscribe(searchItem.ticker)
    }
  }

  @WorkerThread
  private fun render(result: List<TickerModel>): List<UISearchItem> {
    return result.map { model ->
      val ticker = Ticker(model.symbolNormalized)
      UISearchItem(
        ticker = ticker,
        priceCents = model.currentPrice?.amount?.toPlainString(),
        info = null,
        isAdded = updateSubscribedTickersUseCase.retrieveSubscribedTickers().contains(ticker)
      )
    }
  }
}