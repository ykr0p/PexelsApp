package com.example.pexelsapp.data.api

import com.example.pexelsapp.data.api.model.PexelsCollectionResponse
import com.example.pexelsapp.data.api.model.PexelsPhotoResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PexelsApiService {

    @GET("collections/featured")
    suspend fun getFeaturedCollections(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 7
    ): PexelsCollectionResponse

    @GET("curated")
    suspend fun getCuratedPhotos(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): PexelsPhotoResponse

    @GET("search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("orientation") orientation: String? = null,
        @Query("size") size: String? = null,
        @Query("color") color: String? = null
    ): PexelsPhotoResponse

    @GET("photos/{id}")
    suspend fun getPhoto(
        @Header("Authorization") apiKey: String,
        @Path("id") photoId: Int
    ): PexelsPhotoResponse

    companion object {
        const val BASE_URL = "https://api.pexels.com/v1/"
        const val API_KEY = "0L6YZUD9uGjzFyxhhdpeJBn05QYKHP58FvLrzwN320QufQBpmtwc9730"
    }
}