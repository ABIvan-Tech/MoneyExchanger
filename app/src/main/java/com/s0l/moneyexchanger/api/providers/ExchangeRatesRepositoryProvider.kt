package com.s0l.moneyexchanger.api.providers

import com.s0l.moneyexchanger.api.interfaces.ExchangeRatesApiService
import com.s0l.moneyexchanger.api.repository.ExchangeRatesRepository

object ExchangeRatesRepositoryProvider {
    fun provideExchangeRatesRepository(apiService: ExchangeRatesApiService): ExchangeRatesRepository {
        return ExchangeRatesRepository(
            apiService
        )
    }
}