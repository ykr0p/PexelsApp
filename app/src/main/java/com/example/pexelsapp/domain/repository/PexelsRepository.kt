package com.example.pexelsapp.domain.repository

import com.example.pexelsapp.domain.model.CuratedImage
import com.example.pexelsapp.domain.model.FeaturedCollection

interface PexelsRepository {
    
    suspend fun getFeaturedCollections(): Result<List<FeaturedCollection>>
    
    suspend fun getCuratedImages(page: Int = 1): Result<List<CuratedImage>>
    
    suspend fun searchImages(query: String, page: Int = 1): Result<List<CuratedImage>>
    
    suspend fun clearCache()
    
    suspend fun getCachedCuratedImages(): List<CuratedImage>
    
    suspend fun getCachedFeaturedCollections(): List<FeaturedCollection>
}