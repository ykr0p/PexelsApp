package com.example.pexelsapp.di

import com.example.pexelsapp.presentation.bookmarks.BookmarkViewModel
import com.example.pexelsapp.presentation.details.DetailViewModel
import com.example.pexelsapp.presentation.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module


val viewModelModule: Module = module {

    viewModel {
        HomeViewModel(
            getFeaturedCollectionsUseCase = get(),
            getCuratedImagesUseCase = get(),
            searchImagesUseCase = get()
        )
    }

    viewModel {
        BookmarkViewModel(
            database = get()
        )
    }

    viewModel {
        DetailViewModel(
            database = get()
        )
    }
}