package com.candleflask.android.ui.searchtickerdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.whenResumed
import com.candleflask.android.databinding.SearchTickersFragmentBinding
import common.android.ui.UIResource
import common.android.ui.ViewBindingFragment
import common.android.ui.launchInViewLifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SearchTickersFragment : ViewBindingFragment<SearchTickersFragmentBinding>() {
  private val searchTickerViewModel: SearchTickerViewModel by viewModels()

  override fun createBinding(inflater: LayoutInflater, container: ViewGroup?, attachToParent: Boolean) =
    SearchTickersFragmentBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initViews()
    launchRoutines()
  }

  private fun launchRoutines() {
    launchInViewLifecycleScope {
      whenResumed {
        searchTickerViewModel.searchResultStateFlow.collectLatest { event ->
          withContext(Dispatchers.Main) {
            when (event) {
              is UIResource.Error -> showError(event.message)
              is UIResource.Loading -> toggleLoadingIndicatorVisibility(isVisible = true)
              is UIResource.Success -> {
                val searchResults = event.payload
                binding.searchResultEmpty.visibility = if (searchResults.isEmpty()) View.VISIBLE else View.GONE
                (binding.searchResultRecyclerView.adapter as? SearchTickersResultAdapter)
                  ?.submitList(searchResults)
              }
              is UIResource.Empty -> {
              }
            }
            if (event !is UIResource.Loading) {
              toggleLoadingIndicatorVisibility(isVisible = false)
            }

            if (event !is UIResource.Success) {
              binding.searchResultEmpty.visibility = View.GONE
            }
          }
        }
      }
    }
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

  private fun showError(message: String?) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
  }

  private fun toggleLoadingIndicatorVisibility(isVisible: Boolean) {
    binding.loadingIndicator.visibility = if (isVisible) View.VISIBLE else View.GONE
  }

}