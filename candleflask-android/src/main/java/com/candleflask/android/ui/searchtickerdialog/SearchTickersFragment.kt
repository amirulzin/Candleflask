package com.candleflask.android.ui.searchtickerdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.candleflask.android.databinding.SearchTickersDialogFragmentBinding
import common.android.ui.UIResource
import common.android.ui.ViewBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SearchTickersFragment : ViewBindingFragment<SearchTickersDialogFragmentBinding>() {
  private val searchTickerViewModel: SearchTickerViewModel by viewModels()

  init {
    lifecycleScope.launch {
      whenResumed {
        searchTickerViewModel.searchResultStateFlow.collectLatest { event ->
          withContext(Dispatchers.Main) {
            when (event) {
              is UIResource.Error -> showError(event.message)
              is UIResource.Loading -> toggleLoadingIndicatorVisibility(isVisible = true)
              is UIResource.Success -> (binding.searchResultRecyclerView.adapter as? SearchTickersResultAdapter)
                ?.submitList(event.payload)
              is UIResource.Empty -> {
              }
            }
            if (event !is UIResource.Loading) {
              toggleLoadingIndicatorVisibility(isVisible = false)
            }
          }
        }
      }
    }
  }

  private fun showError(message: String?) {

  }

  private fun toggleLoadingIndicatorVisibility(isVisible: Boolean) {

  }

  override fun createBinding(inflater: LayoutInflater, container: ViewGroup?, attachToParent: Boolean) =
    SearchTickersDialogFragmentBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initViews()
  }

  private fun initViews() {
    with(binding.searchResultRecyclerView) {
      adapter = SearchTickersResultAdapter(::onClickItem)
    }

    with(binding.searchButton) {
      setOnClickListener {
        invokeSearch()
        hideKeyboard()
      }
    }

    with(binding.searchTextInput) {
      setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          invokeSearch()
          hideKeyboard()
        }
        false
      }
    }
  }

  private fun invokeSearch() {
    val input = binding.searchTextInput.text?.toString().orEmpty()
    searchTickerViewModel.searchTicker(input)
  }

  private fun hideKeyboard() {
    WindowInsetsControllerCompat(requireActivity().window, binding.root)
      .hide(WindowInsetsCompat.Type.ime())
  }

  private fun onClickItem(searchItem: UISearchItem) {
    searchTickerViewModel.addTicker(searchItem)
  }

}