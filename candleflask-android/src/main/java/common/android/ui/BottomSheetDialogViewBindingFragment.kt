package common.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BottomSheetDialogViewBindingFragment<T : ViewBinding> : BottomSheetDialogFragment() {
  private var _binding: T? = null
  protected val binding get() = _binding!!

  protected abstract fun createBinding(
    inflater: LayoutInflater,
    container: ViewGroup?,
    attachToParent: Boolean = false
  ): T

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = createBinding(inflater, container)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}