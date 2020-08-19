package com.s0l.moneyexchanger.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeInfoModel(
    var rates: Map<String, Double>,
    var base: String ="",
    var date: String = ""
)

