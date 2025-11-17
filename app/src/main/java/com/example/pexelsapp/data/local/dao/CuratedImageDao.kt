package com.example.pexelsapp.data.local.dao

import androidx.room.*
import com.example.pexelsapp.data.local.entity.CuratedImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuratedImageDao {
    
    @Query("SELECT * FROM curated_images ORDER BY createdAt DESC")
    fun getAllCuratedImages(): Flow<List<CuratedImageEntity>>
    
    @Query("SELECT * FROM curated_images WHERE id = :imageId")
    suspend fun getCuratedImageById(imageId: String): CuratedImageEntity?
    
    @Query("SELECT * FROM curated_images ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getCuratedImages(limit: Int = 30): List<CuratedImageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuratedImage(image: CuratedImageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuratedImages(images: List<CuratedImageEntity>)
    
    @Delete
    suspend fun deleteCuratedImage(image: CuratedImageEntity)
    
    @Query("DELETE FROM curated_images")
    suspend fun deleteAllCuratedImages()
    
    @Query("DELETE FROM curated_images WHERE createdAt < :timestamp")
    suspend fun deleteOldCuratedImages(timestamp: Long): Int
}