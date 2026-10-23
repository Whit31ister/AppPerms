package com.example.appperms.data.di

import com.example.appperms.data.AppAuditRepository
import com.example.appperms.data.DefaultAppAuditRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppAuditModule {
    @Binds
    @Singleton
    abstract fun bindAppAuditRepository(
        repository: DefaultAppAuditRepository
    ): AppAuditRepository
}
