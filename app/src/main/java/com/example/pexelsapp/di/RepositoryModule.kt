package com.example.pexelsapp.di

import com.example.pexelsapp.data.repository.PexelsRepositoryImpl
import com.example.pexelsapp.domain.repository.PexelsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoryModule: Module = module {

    single<PexelsRepository> {
        PexelsRepositoryImpl(
            apiService = get(),
            database = get()
        )
    }
}