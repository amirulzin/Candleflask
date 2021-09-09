package com.candleflask.android.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class DelegatedDispatchers constructor(
  val IO: CoroutineDispatcher,
  val MAIN: CoroutineDispatcher,
  val DEFAULT: CoroutineDispatcher,
  val UNCONFINED: CoroutineDispatcher
) {

  @Inject
  constructor() : this(
    IO = Dispatchers.IO,
    MAIN = Dispatchers.Main,
    DEFAULT = Dispatchers.Default,
    UNCONFINED = Dispatchers.Unconfined
  )

  /** For tests **/
  constructor(singleDispatcher: CoroutineDispatcher) : this(
    singleDispatcher,
    singleDispatcher,
    singleDispatcher,
    singleDispatcher
  )

}