package com.s0l.moneyexchanger

import android.content.Context
import com.s0l.moneyexchanger.db.dao.MoneyDao
import com.s0l.moneyexchanger.db.database.AppDatabase


object Injection {
    fun provideMoneyDataSource(context: Context): MoneyDao {
        val database = AppDatabase.getInstance(context)
        return database!!.moneyDao()
    }

}
