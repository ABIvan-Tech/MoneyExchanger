package com.s0l.moneyexchanger.db.dao

import androidx.room.*
import com.s0l.moneyexchanger.db.model.MoneyAmountModel

@Dao
interface MoneyDao {
    @Query("SELECT * FROM money")
    fun getAllMoneyList(): List<MoneyAmountModel>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateMoney(vararg moneyObj: MoneyAmountModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateMoney(moneyObjs: List<MoneyAmountModel>)

    @Delete
    fun delete(moneyObj: MoneyAmountModel)
}