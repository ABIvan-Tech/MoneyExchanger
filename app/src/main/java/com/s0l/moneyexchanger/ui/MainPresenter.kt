package com.s0l.moneyexchanger.ui

import android.util.Log
import com.s0l.moneyexchanger.BuildConfig
import com.s0l.moneyexchanger.api.interfaces.ExchangeRatesApiService
import com.s0l.moneyexchanger.api.providers.ExchangeRatesRepositoryProvider
import com.s0l.moneyexchanger.api.providers.TransactionRepositoryProvider
import com.s0l.moneyexchanger.api.repository.TransactionRepository
import com.s0l.moneyexchanger.db.dao.MoneyDao
import com.s0l.moneyexchanger.model.CardData
import com.s0l.moneyexchanger.model.ExchangeInfoModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.concurrent.TimeUnit


@InjectViewState
class MainPresenter(
    moneyDataSource: MoneyDao
) : MvpPresenter<MainView>() {

    private var transaction: TransactionRepository =
        TransactionRepositoryProvider.provideTransactionRepository(moneyDataSource)

    companion object {
        //мапа которая хранит данные по курсу обмена для всех валют
        val exchangeRateMapFromNetwork: HashMap<String, Double> = hashMapOf()

        //мапа которая хранит данные по курсу обмена для валют которые показываем на экране
        var exchangeMapFilteredForView: HashMap<String, Double> = hashMapOf()

        var firstTimeLoad: Boolean = true

        var compositeDisposable: CompositeDisposable = CompositeDisposable()

        private var disposable: Disposable? = null

        private val apiService = ExchangeRatesApiService.createRetrofit()

        val repository = ExchangeRatesRepositoryProvider.provideExchangeRatesRepository(apiService)

        val interval: Observable<Long> =
            Observable.interval(0L, if (BuildConfig.DEBUG) 1000L else 30L, TimeUnit.SECONDS)
    }

    fun getExchangeCurrency() {
        if (disposable == null) {
            disposable = interval.flatMap { repository.getExchangeRates() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.d("Result", "There are $result")
                    updateExchangeRateMap(result)
                    setFilterForExchangeMoneyMap()
                }, { error ->
                    error.printStackTrace()
                    viewState.showError(error.localizedMessage!!)
                })
            compositeDisposable.add(disposable!!)
        }
    }

    fun stopRefreshing() {
        disposable?.let {
            disposable!!.dispose()
            disposable = null
        }
    }

    private fun updateExchangeRateMap(result: ExchangeInfoModel) {
        exchangeRateMapFromNetwork.clear()
        exchangeRateMapFromNetwork.putAll(result.rates)
            .also { exchangeRateMapFromNetwork["EUR"] = 1.0 }
    }

    private fun setFilterForExchangeMoneyMap() {
        exchangeMapFilteredForView.clear()
        exchangeMapFilteredForView.putAll(
            exchangeRateMapFromNetwork
                //что бы увидеть все валюты нужно просто закомментировать эту строчку
                .filterKeys { it == "EUR" || it == "USD" || it == "GBP" }
        )
        if (firstTimeLoad) {
            firstTimeLoad = false
            loadCurrentMoneyVolumeFromDB()
        } else {
            viewState.updateUI()
        }
    }

    private fun loadCurrentMoneyVolumeFromDB() {
        compositeDisposable.add(
            Observable.fromCallable {
                if (transaction.getInitialMoneyAmountMapFromDB().isEmpty()) {
                    // Первый запуск список валют из БД пустой.
                    // Вносим в БД шаблон в 100 единиц каждой валюты из сетевого запроса
                    transaction.setInitialMoneyVolumeMap(exchangeRateMapFromNetwork)
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.updateUI()
                }, { error ->
                    error.printStackTrace()
                    viewState.showError(error.localizedMessage!!)
                })
        )
    }

    //считаем курс обмена двух валют на относительно друг друга через Евро
    private fun getExchangeRateToEuro(moneyOne: String, moneyTwo: String): Double {
        val exchangeRate: Double
        val currencyOneToEuro: Double = 1.0 / exchangeMapFilteredForView.getValue(moneyOne)
        val currencyTwoToEuro: Double = 1.0 / exchangeMapFilteredForView.getValue(moneyTwo)

        exchangeRate = currencyOneToEuro / currencyTwoToEuro

        return exchangeRate
    }

    //возвращаем мапу которая хранит данные для курса обмена относительно евро для каждой валюты
    //из @exchangeMapForView GBP -> GBP, EUR, USD и т.д.
    private fun getGlobalRateMap(moneyOriginal: String): Map<String, Double> {
        val rateMap: HashMap<String, Double> = hashMapOf()
        exchangeMapFilteredForView.map {
            rateMap.put(it.key, getExchangeRateToEuro(moneyOriginal, it.key))
        }
        return rateMap
    }

    fun prepareInfoForCardOne(): List<CardData> {
        val currentMoneyAmount = transaction.getMoneyVolumeMap()
        return exchangeMapFilteredForView.map {
            CardData(
                it.key,
                currentMoneyAmount[it.key]!!,
                getGlobalRateMap(it.key)
            )
        }.toList()
    }

    fun prepareInfoForCardTwo(): List<CardData> {
        val currentMoneyAmount = transaction.getMoneyVolumeMap()
        return exchangeMapFilteredForView.map {
            CardData(
                it.key,
                currentMoneyAmount[it.key]!!,
                getGlobalRateMap(it.key)
            )
        }.toList()
    }

    fun doExchange(
        amountCardOne: Double,
        moneyNameOne: String,
        amountCardTwo: Double,
        moneyNameTwo: String
    ) {
        compositeDisposable.add(
            Observable.fromCallable {
                if (transaction.checkAmountForTransaction(amountCardOne, moneyNameOne)) {
                    when {
                        !transaction.makeTransaction(
                            amountCardOne, moneyNameOne,
                            amountCardTwo, moneyNameTwo
                        )
                        -> {
                            // по факту в демке недостижимый вариант
                            ErrorCodes.TRANSACTION_PROBLEM
                        }
                        else -> {
                            true
                        }
                    }
                } else {
                    ErrorCodes.INSUFFICIENT_FUNDS
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showProgress() }
                .subscribe({ it ->
                    if (it is ErrorCodes) {
                        viewState.showError(it)
                    } else {
                        viewState.updateUI()
                        viewState.showSuccess(
                            amountCardTwo,
                            moneyNameTwo,
                            transaction.getMoneyVolumeMap().filter { (key, _) -> key in exchangeMapFilteredForView.keys.toList() }
                        )
                    }
                }, { error ->
                    error.printStackTrace()
                    viewState.showError(error.localizedMessage!!)
                })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
