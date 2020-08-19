package com.s0l.moneyexchanger.api.repository

import com.s0l.moneyexchanger.api.interfaces.ExchangeRatesApiService
import com.s0l.moneyexchanger.model.ExchangeInfoModel
import io.reactivex.Observable

class ExchangeRatesRepository(private val apiService: ExchangeRatesApiService) {
    fun getExchangeRates(): Observable<ExchangeInfoModel> {
        return apiService.getCurrentExchangeInfo()
    }
}