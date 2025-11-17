package com.example.pexelsapp.data.repository

import com.example.pexelsapp.data.api.ApiClient
import com.example.pexelsapp.data.api.PexelsApiService
import com.example.pexelsapp.domain.model.FeaturedCollection
import com.example.pexelsapp.domain.model.CuratedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PexelsRepository {
    
    suspend fun getFeaturedCollections(): List<FeaturedCollection> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getFeaturedCollections(
                apiKey = PexelsApiService.API_KEY,
                perPage = 7
            )
            
            response.collections.map { apiCollection ->
                FeaturedCollection(
                    id = apiCollection.id,
                    title = apiCollection.title,
                    subtitle = apiCollection.description ?: "Featured collection",
                    imageUrl = "https://picsum.photos/400/300?random=${apiCollection.id.hashCode()}"
                )
            }
        } catch (e: Exception) {

            getSampleFeaturedCollections()
        }
    }
    
    suspend fun getCuratedImages(page: Int = 1): List<CuratedImage> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getCuratedPhotos(
                apiKey = PexelsApiService.API_KEY,
                page = page,
                perPage = 30
            )
            
            response.photos.map { apiPhoto ->
                CuratedImage(
                    id = apiPhoto.id.toString(),
                    photographer = apiPhoto.photographer,
                    imageUrl = apiPhoto.src.original,
                    thumbnailUrl = apiPhoto.src.medium,
                    width = apiPhoto.width,
                    height = apiPhoto.height,
                    tags = listOf("featured", "curated", "popular")
                )
            }
        } catch (e: Exception) {
            getSampleCuratedImages()
        }
    }
    
    suspend fun searchImages(query: String, page: Int = 1): List<CuratedImage> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.searchPhotos(
                apiKey = PexelsApiService.API_KEY,
                query = query,
                page = page,
                perPage = 30
            )
            
            response.photos.map { apiPhoto ->
                CuratedImage(
                    id = apiPhoto.id.toString(),
                    photographer = apiPhoto.photographer,
                    imageUrl = apiPhoto.src.original,
                    thumbnailUrl = apiPhoto.src.medium,
                    width = apiPhoto.width,
                    height = apiPhoto.height,
                    tags = listOf("search", query)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun getSampleFeaturedCollections(): List<FeaturedCollection> {
        return listOf(
            FeaturedCollection("1", "Nature", "Beautiful landscapes", "https://picsum.photos/400/300?random=1"),
            FeaturedCollection("2", "Architecture", "Modern designs", "https://picsum.photos/400/300?random=2"),
            FeaturedCollection("3", "People", "Portrait photography", "https://picsum.photos/400/300?random=3"),
            FeaturedCollection("4", "Technology", "Digital innovation", "https://picsum.photos/400/300?random=4"),
            FeaturedCollection("5", "Food", "Culinary arts", "https://picsum.photos/400/300?random=5"),
            FeaturedCollection("6", "Travel", "Adventure awaits", "https://picsum.photos/400/300?random=6"),
            FeaturedCollection("7", "Art", "Creative expression", "https://picsum.photos/400/300?random=7")
        )
    }
    
    private fun getSampleCuratedImages(): List<CuratedImage> {
        return (1..30).map { index ->
            CuratedImage(
                id = index.toString(),
                photographer = "Photographer $index",
                imageUrl = "https://picsum.photos/600/800?random=$index",
                thumbnailUrl = "https://picsum.photos/300/400?random=$index",
                width = 600,
                height = 800,
                tags = listOf("popular", "trending", "featured")
            )
        }
    }
}