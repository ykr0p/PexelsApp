package com.example.pexelsapp.data.repository

import com.example.pexelsapp.data.api.NetworkException
import com.example.pexelsapp.data.api.ApiException
import com.example.pexelsapp.data.api.PexelsApiService
import com.example.pexelsapp.data.local.CacheUtils
import com.example.pexelsapp.data.local.database.PexelsDatabase
import com.example.pexelsapp.data.mapper.CuratedImageMapper
import com.example.pexelsapp.data.mapper.FeaturedCollectionMapper
import com.example.pexelsapp.domain.model.CuratedImage
import com.example.pexelsapp.domain.model.FeaturedCollection
import com.example.pexelsapp.domain.repository.PexelsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

class PexelsRepositoryImpl(
    private val apiService: PexelsApiService,
    private val database: PexelsDatabase
) : PexelsRepository {
    
    override suspend fun getFeaturedCollections(): Result<List<FeaturedCollection>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFeaturedCollections(
                apiKey = PexelsApiService.API_KEY,
                perPage = 7
            )
            
            val collections = FeaturedCollectionMapper.fromApiListToDomain(response.collections)
            
            // Cache in database
            val entities = FeaturedCollectionMapper.fromDomainListToEntity(collections)
            database.featuredCollectionDao().insertFeaturedCollections(entities)
            
            Result.success(collections)
        } catch (e: Exception) {
            val networkException = classifyException(e)

            val cachedCollections = try {
                getValidCachedFeaturedCollections()
            } catch (cacheException: Exception) {
                emptyList()
            }

            if (networkException is NetworkException && cachedCollections.isEmpty()) {
                Result.failure(networkException)
            } else if (cachedCollections.isNotEmpty()) {

                Result.failure(NetworkException.NoConnectionException("Using cached data"))
            } else {

                Result.failure(networkException)
            }
        }
    }
    
    override suspend fun getCuratedImages(page: Int): Result<List<CuratedImage>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCuratedPhotos(
                apiKey = PexelsApiService.API_KEY,
                page = page,
                perPage = 30
            )
            
            val images = CuratedImageMapper.fromApiListToDomain(response.photos)

            if (page == 1) {
                val entities = CuratedImageMapper.fromDomainListToEntity(images)
                database.curatedImageDao().insertCuratedImages(entities)
            }
            
            Result.success(images)
        } catch (e: Exception) {

            val networkException = classifyException(e)

            val cachedImages = if (page == 1) {
                try {
                    getValidCachedCuratedImages()
                } catch (cacheException: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            if (networkException is NetworkException && cachedImages.isEmpty()) {
                Result.failure(networkException)
            } else if (cachedImages.isNotEmpty()) {

                Result.failure(NetworkException.NoConnectionException("Using cached data"))
            } else {

                Result.failure(networkException)
            }
        }
    }
    
    override suspend fun searchImages(query: String, page: Int): Result<List<CuratedImage>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchPhotos(
                apiKey = PexelsApiService.API_KEY,
                query = query,
                page = page,
                perPage = 30
            )
            
            val images = CuratedImageMapper.fromApiListToDomain(response.photos)
            Result.success(images)
        } catch (e: Exception) {

            val networkException = classifyException(e)
            Result.failure(networkException)
        }
    }
    
    override suspend fun clearCache() = withContext(Dispatchers.IO) {

        cleanExpiredCache()

        database.curatedImageDao().deleteAllCuratedImages()
        database.featuredCollectionDao().deleteAllFeaturedCollections()
    }

    private suspend fun cleanExpiredCache() = withContext(Dispatchers.IO) {
        try {
            val expirationTimestamp = CacheUtils.getCacheExpirationTimestamp()

            val deletedCuratedCount = database.curatedImageDao().deleteOldCuratedImages(expirationTimestamp)
            if (deletedCuratedCount > 0) {
                println("CacheUtils: Cleaned $deletedCuratedCount expired curated images from cache")
            }

            val deletedCollectionsCount = database.featuredCollectionDao().deleteOldFeaturedCollections(expirationTimestamp)
            if (deletedCollectionsCount > 0) {
                println("CacheUtils: Cleaned $deletedCollectionsCount expired featured collections from cache")
            }
        } catch (e: Exception) {
            println("CacheUtils: Error cleaning expired cache: ${e.message}")
        }
    }
    
    override suspend fun getCachedCuratedImages(): List<CuratedImage> = withContext(Dispatchers.IO) {
        try {
            val entities = database.curatedImageDao().getCuratedImages(30)
            CuratedImageMapper.fromEntityListToDomain(entities)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getCachedFeaturedCollections(): List<FeaturedCollection> = withContext(Dispatchers.IO) {
        try {
            val entities = database.featuredCollectionDao().getFeaturedCollections(7)
            FeaturedCollectionMapper.fromEntityListToDomain(entities)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getValidCachedCuratedImages(): List<CuratedImage> = withContext(Dispatchers.IO) {
        try {
            val entities = database.curatedImageDao().getCuratedImages(30)

            if (entities.isNotEmpty() && CacheUtils.isCacheValid(entities[0].createdAt)) {
                val domainImages = CuratedImageMapper.fromEntityListToDomain(entities)
                println("CacheUtils: Using valid cached curated images - ${CacheUtils.getCacheStatus(entities[0].createdAt)}")
                domainImages
            } else {
                if (entities.isNotEmpty()) {
                    println("CacheUtils: Cache expired for curated images - ${CacheUtils.getCacheStatus(entities[0].createdAt)}")
                }
                emptyList()
            }
        } catch (e: Exception) {
            println("CacheUtils: Error retrieving cached curated images: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getValidCachedFeaturedCollections(): List<FeaturedCollection> = withContext(Dispatchers.IO) {
        try {
            val entities = database.featuredCollectionDao().getFeaturedCollections(7)

            if (entities.isNotEmpty() && CacheUtils.isCacheValid(entities[0].createdAt)) {
                val domainCollections = FeaturedCollectionMapper.fromEntityListToDomain(entities)
                println("CacheUtils: Using valid cached featured collections - ${CacheUtils.getCacheStatus(entities[0].createdAt)}")
                domainCollections
            } else {

                if (entities.isNotEmpty()) {
                    println("CacheUtils: Cache expired for featured collections - ${CacheUtils.getCacheStatus(entities[0].createdAt)}")
                }
                emptyList()
            }
        } catch (e: Exception) {
            println("CacheUtils: Error retrieving cached featured collections: ${e.message}")
            emptyList()
        }
    }

    private fun getFallbackFeaturedCollections(): List<FeaturedCollection> {
        return listOf(
            FeaturedCollection("1", "Nature", "Beautiful landscapes"),
            FeaturedCollection("2", "Architecture", "Modern designs"),
            FeaturedCollection("3", "People", "Portrait photography"),
            FeaturedCollection("4", "Technology", "Digital innovation"),
            FeaturedCollection("5", "Food", "Culinary arts"),
            FeaturedCollection("6", "Travel", "Adventure awaits"),
            FeaturedCollection("7", "Art", "Creative expression")
        )
    }
    
    private fun getFallbackCuratedImages(): List<CuratedImage> {
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

    private fun classifyException(e: Exception): Exception {
        return when (e) {
            is UnknownHostException -> NetworkException.NoConnectionException("No internet connection")
            is SocketTimeoutException -> NetworkException.TimeoutException("Request timed out")
            is IOException -> NetworkException.NetworkErrorException("Network error: ${e.message}")
            is HttpException -> {
                val code = e.code()
                val message = when (code) {
                    401 -> "Unauthorized: Invalid API key"
                    403 -> "Forbidden: Access denied"
                    404 -> "Not found"
                    429 -> "Too many requests: Rate limit exceeded"
                    in 500..599 -> "Server error: Please try again later"
                    else -> "HTTP error $code"
                }
                ApiException.ServerException(code, message)
            }
            else -> ApiException.ParseException(e.message ?: "Unknown error occurred")
        }
    }
}