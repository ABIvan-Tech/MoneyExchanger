package com.s0l.moneyexchanger.db.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "money")
data class MoneyAmountModel(
    @PrimaryKey//в текущей вариации можно и не выставлять...
    @NonNull
    val money_name: String,
    @NonNull
    val money_volume: Double
)