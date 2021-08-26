package com.candleflask.android.ui.updatetokendialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.fragment.findNavController
import com.candleflask.android.databinding.UpdateTokenDialogFragmentBinding
import common.android.ui.BottomSheetDialogViewBindingFragment
import common.android.ui.ClipboardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class UpdateTokenDialogFragment : BottomSheetDialogViewBindingFragment<UpdateTokenDialogFragmentBinding>() {
  override fun createBinding(
    inflater: LayoutInflater,
    container: ViewGroup?,
    attachToParent: Boolean
  ): UpdateTokenDialogFragmentBinding {
    return UpdateTokenDialogFragmentBinding.inflate(inflater, container, false)
  }

  private val updateTokenViewModel: UpdateTokenViewModel by viewModels()

  init {
    lifecycleScope.launch {
      whenResumed {
        val currentToken = updateTokenViewModel.retrieveCurrentToken()
        withContext(Dispatchers.Main) {
          binding.tokenTextInput.setText(currentToken.orEmpty())
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initLogic()
  }

  private fun initLogic() {
    with(binding.pasteButton) {
      setOnClickListener {
        binding.tokenTextInput.setText(ClipboardUtils.retrieveString(context).orEmpty())
        updateToken()
      }
    }

    with(binding.tokenTextInput) {
      setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          updateToken()
          post(::navBack)
        }
        false
      }
    }

    with(binding.doneButton) {
      setOnClickListener {
        updateToken()
        post(::navBack)
      }
    }
  }

  private fun updateToken() {
    binding.tokenTextInput.text
      ?.toString()
      .orEmpty()
      .let(updateTokenViewModel::updateToken)
  }

  private fun navBack() {
    findNavController().popBackStack()
  }
}