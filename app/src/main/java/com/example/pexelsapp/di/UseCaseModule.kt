package com.example.pexelsapp.di

import com.example.pexelsapp.domain.usecase.*
import org.koin.core.module.Module
import org.koin.dsl.module

val useCaseModule: Module = module {

    factory {
        GetCuratedImagesUseCase(
            repository = get()
        )
    }

    factory {
        GetFeaturedCollectionsUseCase(
            repository = get()
        )
    }

    factory {
        SearchImagesUseCase(
            repository = get()
        )
    }

    factory {
        RefreshDataUseCase(
            repository = get()
        )
    }

    factory {
        ClearCacheUseCase(
            repository = get()
        )
    }
}