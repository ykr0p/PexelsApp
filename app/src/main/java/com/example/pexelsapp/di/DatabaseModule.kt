package com.example.pexelsapp.di

import androidx.room.Room
import com.example.pexelsapp.data.local.database.PexelsDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule: Module = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            PexelsDatabase::class.java,
            "pexels_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    single {
        get<PexelsDatabase>().curatedImageDao()
    }

    single {
        get<PexelsDatabase>().featuredCollectionDao()
    }

    single {
        get<PexelsDatabase>().bookmarkDao()
    }
}