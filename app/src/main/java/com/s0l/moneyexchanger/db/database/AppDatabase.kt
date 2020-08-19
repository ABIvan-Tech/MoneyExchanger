package com.s0l.moneyexchanger.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.s0l.moneyexchanger.db.dao.MoneyDao
import com.s0l.moneyexchanger.db.model.MoneyAmountModel

@Database(entities = [MoneyAmountModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moneyDao(): MoneyDao

    //singleton instance нашей локальной БД
    companion object {
        @Volatile
        var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "money.db"
            ).build()
    }
}