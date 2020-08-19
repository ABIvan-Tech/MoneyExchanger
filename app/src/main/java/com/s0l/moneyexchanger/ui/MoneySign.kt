package com.s0l.moneyexchanger.ui

import java.util.*


object MoneySign {
    fun getCurrencySymbol(moneyCode: String): String? {
        return Currency.getInstance(moneyCode).symbol
    }
}