package com.candleflask.android.ui.updatetokendialog

import com.candleflask.android.di.DelegatedDispatchers
import com.candleflask.framework.domain.features.securitytoken.EncryptedTokenRepository
import com.candleflask.framework.domain.features.securitytoken.RetrieveSecurityTokenUseCase
import com.candleflask.framework.domain.features.securitytoken.UpdateSecurityTokenUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*


class UpdateTokenViewModelTest {
  private lateinit var vm: UpdateTokenViewModel
  private lateinit var encryptedTokenRepositoryMock: EncryptedTokenRepository
  private val token = "testToken"

  @ExperimentalCoroutinesApi
  @Before
  fun setup() {
    val testDispatcher = TestCoroutineDispatcher()
    encryptedTokenRepositoryMock = mock {
      on { updateToken(token) }
        .then { on { retrieveToken() } doReturn token }
    }
    vm = UpdateTokenViewModel(
      DelegatedDispatchers(testDispatcher),
      UpdateSecurityTokenUseCase(encryptedTokenRepositoryMock),
      RetrieveSecurityTokenUseCase(encryptedTokenRepositoryMock)
    )
  }

  @Test
  fun `when ViewModel retrieveCurrentToken without updating, returns null`() {
    runBlocking {
      assertEquals(null, vm.retrieveCurrentToken())
    }
    verify(encryptedTokenRepositoryMock).retrieveToken()
    verify(encryptedTokenRepositoryMock, never()).updateToken(any())
  }

  @Test
  fun `when update with valid token, return same token`() {
    runBlocking {
      vm.updateToken(token)
      assertEquals(token, vm.retrieveCurrentToken())
    }

    argumentCaptor<String> {
      verify(encryptedTokenRepositoryMock).updateToken(capture())
      assertEquals(token, firstValue)
    }
  }
}