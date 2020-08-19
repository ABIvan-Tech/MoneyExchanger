package com.s0l.moneyexchanger.model

class CardData(
    var moneyName: String, // имя валюты
    var currentMoneyVolume: Double, // объем текущих денег данной валюты
    var currentExchangeRate: Map<String, Double> // текущий курс обмена
)
