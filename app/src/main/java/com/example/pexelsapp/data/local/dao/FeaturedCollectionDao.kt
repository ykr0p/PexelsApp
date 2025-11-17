package com.example.pexelsapp.data.local.dao

import androidx.room.*
import com.example.pexelsapp.data.local.entity.FeaturedCollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeaturedCollectionDao {
    
    @Query("SELECT * FROM featured_collections ORDER BY createdAt DESC")
    fun getAllFeaturedCollections(): Flow<List<FeaturedCollectionEntity>>
    
    @Query("SELECT * FROM featured_collections WHERE id = :collectionId")
    suspend fun getFeaturedCollectionById(collectionId: String): FeaturedCollectionEntity?
    
    @Query("SELECT * FROM featured_collections ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getFeaturedCollections(limit: Int = 7): List<FeaturedCollectionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeaturedCollection(collection: FeaturedCollectionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeaturedCollections(collections: List<FeaturedCollectionEntity>)
    
    @Delete
    suspend fun deleteFeaturedCollection(collection: FeaturedCollectionEntity)
    
    @Query("DELETE FROM featured_collections")
    suspend fun deleteAllFeaturedCollections()
    
    @Query("DELETE FROM featured_collections WHERE createdAt < :timestamp")
    suspend fun deleteOldFeaturedCollections(timestamp: Long): Int
}