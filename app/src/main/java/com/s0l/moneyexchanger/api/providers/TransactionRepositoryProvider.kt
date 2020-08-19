package com.s0l.moneyexchanger.api.providers

import com.s0l.moneyexchanger.api.repository.TransactionRepository
import com.s0l.moneyexchanger.db.dao.MoneyDao

object TransactionRepositoryProvider {
    fun provideTransactionRepository(provideMoneyDataSource: MoneyDao): TransactionRepository {
        return TransactionRepository(provideMoneyDataSource)
    }
}