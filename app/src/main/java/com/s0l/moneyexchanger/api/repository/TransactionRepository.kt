package com.s0l.moneyexchanger.api.repository

import com.s0l.moneyexchanger.db.dao.MoneyDao
import com.s0l.moneyexchanger.db.model.MoneyAmountModel
import kotlin.math.abs

//фейковый клас реализующий транзакцию
class TransactionRepository(private val moneyDataSource: MoneyDao) {
    //текущее количество денег для каждой валюты из @exchangeMapForView
    private var currentAmountForEveryMoney: HashMap<String, Double> = hashMapOf()

    //true - транзация прошла, false - ошибка в проведении транзакции
    //В реальности можно сделать отдельный класс TransactionResult и там передавать статус отработки транзакции, ошибки и т.д.
    //по факту всегда возвращаем true
    fun makeTransaction(
        amountCardOne: Double,
        moneyNameOne: String,
        amountCardTwo: Double,
        moneyNameTwo: String
    ): Boolean {
        // тут должен быть запрос на транзакцию в банк и по его результату мы проводим обновление внутри программы и БД.
        currentAmountForEveryMoney[moneyNameOne] =
            currentAmountForEveryMoney[moneyNameOne]!! + amountCardOne
        currentAmountForEveryMoney[moneyNameTwo] =
            currentAmountForEveryMoney[moneyNameTwo]!! + amountCardTwo

        moneyDataSource.updateMoney(
            MoneyAmountModel(
                moneyNameOne,
                currentAmountForEveryMoney[moneyNameOne]!!
            )
        )
        moneyDataSource.updateMoney(
            MoneyAmountModel(
                moneyNameTwo,
                currentAmountForEveryMoney[moneyNameTwo]!!
            )
        )

        return true
    }

    fun checkAmountForTransaction(
        amountCardOne: Double,
        moneyNameOne: String
    ): Boolean {
        return currentAmountForEveryMoney[moneyNameOne]!! >= abs(amountCardOne)
    }

    //если первый запуск
    fun setInitialMoneyVolumeMap(exchangeMap: Map<String, Double>) {
        //заполняем все валюты дефолтным значением
        currentAmountForEveryMoney.putAll(exchangeMap.map{it.key to 100.0})
        //записываем дефолтный список в БД
        moneyDataSource.updateMoney(currentAmountForEveryMoney.map { MoneyAmountModel(it.key, it.value) }.toList())
    }

    fun getInitialMoneyAmountMapFromDB(): Map<String, Double> {
        currentAmountForEveryMoney.putAll(moneyDataSource.getAllMoneyList()?.associate { it.money_name to it.money_volume }!!.toMap())
         //currentAmountForEveryMoney.putAll(moneyDataSource.getAllMoneyList()?.asSequence()?.mapNotNull { it.money_name to it.money_volume }!!.toMap())
        return currentAmountForEveryMoney
    }

    //возвращаем наш текущий баланс
    fun getMoneyVolumeMap(): HashMap<String, Double> {
        return currentAmountForEveryMoney
    }
}