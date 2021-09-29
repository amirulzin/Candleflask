package com.candleflask.android.di

import common.android.ui.UIResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UIDelegatedStateFlow<T> @Inject constructor() {
  private val mutable = MutableStateFlow<UIResource<T>>(UIResource.Empty())
  val immutable = mutable.asStateFlow()
  var value
    get() = mutable.value
    set(value) {
      mutable.value = value
    }

  /**
   * Only executed if the last value is [UIResource.Success] with valid payload
   */
  inline fun update(crossinline func: (T) -> T) {
    (value as? UIResource.Success<T>)?.let { currentSuccess ->
      value = UIResource.Success(func(currentSuccess.payload))
    }
  }
}