package com.example.billbuddy.di

import com.example.billbuddy.data.repo.AuthRepository
import com.example.billbuddy.data.repo.AuthRepositoryImpl
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.data.repo.ExpenseRepositoryImpl
import com.example.billbuddy.data.repo.DebtRepository
import com.example.billbuddy.data.repo.DebtRepositoryImpl
import com.example.billbuddy.data.repo.UserRepository
import com.example.billbuddy.data.repo.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
}
    abstract fun bindDebtRepository(
        debtRepositoryImpl: DebtRepositoryImpl
    ): DebtRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
