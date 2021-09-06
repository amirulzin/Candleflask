package com.candleflask.framework.domain.features.securitytoken

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UpdateSecurityTokenUseCaseTest {

  @Test
  fun `verify EncryptedTokenRepository updateToken is invoked once`() {
    val repositoryMock: EncryptedTokenRepository = mock()
    val token = "stubToken"

    runBlocking {
      UpdateSecurityTokenUseCase(repositoryMock).execute(token)
    }

    argumentCaptor<String> {
      verify(repositoryMock).updateToken(capture())
      assertEquals(token, firstValue)
    }
  }
}