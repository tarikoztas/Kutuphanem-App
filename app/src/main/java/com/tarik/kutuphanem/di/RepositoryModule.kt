package com.tarik.kutuphanem.di

import com.tarik.kutuphanem.data.repository.AuthRepositoryImpl
import com.tarik.kutuphanem.data.repository.KitapAramaRepositoryImpl
import com.tarik.kutuphanem.data.repository.KitapRepositoryImpl
import com.tarik.kutuphanem.data.repository.KutuphaneRepositoryImpl
import com.tarik.kutuphanem.domain.repository.AuthRepository
import com.tarik.kutuphanem.domain.repository.KitapAramaRepository
import com.tarik.kutuphanem.domain.repository.KitapRepository
import com.tarik.kutuphanem.domain.repository.KutuphaneRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindKutuphaneRepository(impl: KutuphaneRepositoryImpl): KutuphaneRepository

    @Binds
    @Singleton
    abstract fun bindKitapRepository(impl: KitapRepositoryImpl): KitapRepository

    @Binds
    @Singleton
    abstract fun bindKitapAramaRepository(impl: KitapAramaRepositoryImpl): KitapAramaRepository
}
