package com.s0l.moneyexchanger.ui

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {

    fun updateUI()

    fun showError(message: String)

    fun showError(messageCodes: ErrorCodes)
    fun showProgress()
    fun hideProgress()
    fun showSuccess(
        amountCardTwo: Double,
        moneyNameTwo: String,
        totalBalance: Map<String, Double>
    )
}