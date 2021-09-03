package com.candleflask.framework.features.tickers

import com.candleflask.framework.data.datasource.SnapshotTickerDataSource
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.features.securitytoken.EncryptedTokenRepository
import java.util.*
import javax.inject.Inject

class SearchTickersUseCase @Inject constructor(
  private val snapshotTickerDataSource: SnapshotTickerDataSource,
  private val tokenRepository: EncryptedTokenRepository
) {
  suspend fun execute(searchInput: String): Output {
    try {
      val token = tokenRepository.retrieveToken() ?: return Output.TokenError
      val resultList = snapshotTickerDataSource.retrieve(setOf(searchInput.uppercase(Locale.US)), token)
      return Output.Success(resultList)
    } catch (e: Exception) {
      return Output.NetworkError
    }
  }

  sealed class Output {
    data class Success(val tickers: List<TickerModel>) : Output()
    object NetworkError : Output()
    object TokenError : Output()
  }
}