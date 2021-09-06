package com.candleflask.framework.domain.features.securitytoken

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RetrieveSecurityTokenUseCaseTest {

  @Test
  fun `verify EncryptedTokenRepository retrieveToken is invoked once`() {
    val token = "fakeToken"
    val repositoryMock: EncryptedTokenRepository = mock {
      onBlocking { retrieveToken() } doReturn token
    }

    val result = runBlocking {
      RetrieveSecurityTokenUseCase(repositoryMock).execute()
    }

    assertEquals(token, result)
    verify(repositoryMock).retrieveToken()
  }

}