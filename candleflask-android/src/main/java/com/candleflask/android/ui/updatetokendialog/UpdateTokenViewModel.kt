package com.candleflask.android.ui.updatetokendialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.framework.domain.features.securitytoken.RetrieveSecurityTokenUseCase
import com.candleflask.framework.domain.features.securitytoken.UpdateSecurityTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UpdateTokenViewModel @Inject constructor(
  private val updateSecurityTokenUseCase: UpdateSecurityTokenUseCase,
  private val retrieveSecurityTokenUseCase: RetrieveSecurityTokenUseCase
) :
  ViewModel() {

  fun updateToken(token: String?) {
    viewModelScope.launch(Dispatchers.IO) {
      updateSecurityTokenUseCase.execute(token)
    }
  }

  suspend fun retrieveCurrentToken(): String? {
    return withContext(Dispatchers.IO) {
      retrieveSecurityTokenUseCase.execute()
    }
  }
}