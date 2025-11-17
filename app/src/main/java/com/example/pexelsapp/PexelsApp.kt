package com.example.pexelsapp

import android.app.Application
import com.example.pexelsapp.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PexelsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PexelsApp)
            modules(
                networkModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }
    }
}


