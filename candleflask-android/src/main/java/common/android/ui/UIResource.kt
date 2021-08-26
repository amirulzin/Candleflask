package common.android.ui

sealed class UIResource<T> {
  class Empty<T> : UIResource<T>()

  data class Success<T>(val payload: T) : UIResource<T>()

  class Loading<T> : UIResource<T>()

  data class Error<T>(
    val message: String? = null,
    val throwable: Throwable? = null
  ) : UIResource<T>()
}