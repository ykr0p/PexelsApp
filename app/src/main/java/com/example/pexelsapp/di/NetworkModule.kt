package com.example.pexelsapp.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.example.pexelsapp.data.api.PexelsApiService
import com.example.pexelsapp.data.api.ApiClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val networkModule: Module = module {

    single {
        GsonBuilder()
            .setLenient()
            .create()
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        val logging = get<HttpLoggingInterceptor>()
        
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        val gson = get<Gson>()
        val httpClient = get<OkHttpClient>()
        
        retrofit2.Retrofit.Builder()
            .baseUrl(PexelsApiService.BASE_URL)
            .client(httpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
            .build()
            .create(PexelsApiService::class.java)
    }

    single {
        ApiClient
    }
}