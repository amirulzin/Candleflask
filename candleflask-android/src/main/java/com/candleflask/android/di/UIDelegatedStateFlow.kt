package com.candleflask.android.di

import common.android.ui.UIResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UIDelegatedStateFlow<T> @Inject constructor() {
  val mutable = MutableStateFlow<UIResource<T>>(UIResource.Empty())
  val immutable = mutable.asStateFlow()
}