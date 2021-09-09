package com.candleflask.android.ui.searchticker

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.android.di.DelegatedDispatchers
import com.candleflask.android.di.DelegatedNetwork
import com.candleflask.android.di.UIDelegatedStateFlow
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.domain.features.tickers.SearchTickersUseCase
import com.candleflask.framework.domain.features.tickers.TickerRepository.OperationResult
import com.candleflask.framework.domain.features.tickers.UpdateSubscribedTickersUseCase
import common.android.ui.UIResource.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchTickerViewModel @Inject constructor(
  private val delegatedDispatchers: DelegatedDispatchers,
  private val searchTickersUseCase: SearchTickersUseCase,
  private val updateSubscribedTickersUseCase: UpdateSubscribedTickersUseCase,
  private val _searchResultStateFlow: UIDelegatedStateFlow<List<UISearchItem>>,
  private val delegatedNetwork: DelegatedNetwork
) : ViewModel() {

  val searchResultStateFlow = _searchResultStateFlow.immutable

  fun searchTicker(input: String) {
    viewModelScope.launch(delegatedDispatchers.IO) {
      with(_searchResultStateFlow.mutable) {
        if (value !is Loading) {
          when {
            !delegatedNetwork.isNetworkConnected() -> value = Error("Network Disconnected")
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
    viewModelScope.launch(delegatedDispatchers.IO) {
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