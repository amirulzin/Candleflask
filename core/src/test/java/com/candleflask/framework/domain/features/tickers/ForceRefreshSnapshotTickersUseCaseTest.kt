package com.candleflask.framework.domain.features.tickers

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class ForceRefreshSnapshotTickersUseCaseTest {
  @Test
  fun `verify TickerRepository forceSnapshotUpdate invoked`() {
    val repositoryMock: TickerRepository = mock()
    runBlocking {
      ForceRefreshSnapshotTickersUseCase(repositoryMock).execute()
    }
    verifyBlocking(repositoryMock) {
      forceSnapshotUpdate()
    }
  }
}