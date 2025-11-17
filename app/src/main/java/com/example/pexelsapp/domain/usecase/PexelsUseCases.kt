package com.example.pexelsapp.domain.usecase

import com.example.pexelsapp.domain.model.CuratedImage
import com.example.pexelsapp.domain.repository.PexelsRepository

class GetCuratedImagesUseCase(
    private val repository: PexelsRepository
) {
    
    suspend operator fun invoke(page: Int = 1): Result<List<CuratedImage>> {
        return repository.getCuratedImages(page)
    }
}

class GetFeaturedCollectionsUseCase(
    private val repository: PexelsRepository
) {
    
    suspend operator fun invoke(): Result<List<com.example.pexelsapp.domain.model.FeaturedCollection>> {
        return repository.getFeaturedCollections()
    }
}

class SearchImagesUseCase(
    private val repository: PexelsRepository
) {
    
    suspend operator fun invoke(query: String, page: Int = 1): Result<List<CuratedImage>> {
        // Validate query
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        
        return repository.searchImages(query.trim(), page)
    }
}

class RefreshDataUseCase(
    private val repository: PexelsRepository
) {
    
    suspend operator fun invoke(page: Int = 1): Result<Pair<List<com.example.pexelsapp.domain.model.FeaturedCollection>, List<CuratedImage>>> {
        return try {
            val collectionsResult = repository.getFeaturedCollections()
            val imagesResult = repository.getCuratedImages(page)
            
            when {
                collectionsResult.isSuccess && imagesResult.isSuccess -> {
                    Result.success(
                        collectionsResult.getOrThrow() to imagesResult.getOrThrow()
                    )
                }
                else -> {
                    Result.failure(Exception("Failed to refresh data"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ClearCacheUseCase(
    private val repository: PexelsRepository
) {
    
    suspend operator fun invoke() {
        repository.clearCache()
    }
}