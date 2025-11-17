package com.example.pexelsapp.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.pexelsapp.data.local.converter.Converters
import com.example.pexelsapp.data.local.dao.CuratedImageDao
import com.example.pexelsapp.data.local.dao.FeaturedCollectionDao
import com.example.pexelsapp.data.local.dao.BookmarkDao
import com.example.pexelsapp.data.local.entity.CuratedImageEntity
import com.example.pexelsapp.data.local.entity.FeaturedCollectionEntity
import com.example.pexelsapp.data.local.entity.BookmarkEntity

@Database(
    entities = [
        CuratedImageEntity::class,
        FeaturedCollectionEntity::class,
        BookmarkEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PexelsDatabase : RoomDatabase() {
    
    abstract fun curatedImageDao(): CuratedImageDao
    
    abstract fun featuredCollectionDao(): FeaturedCollectionDao
    
    abstract fun bookmarkDao(): BookmarkDao
    
    companion object {
        @Volatile
        private var INSTANCE: PexelsDatabase? = null
        
        fun getDatabase(context: Context): PexelsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PexelsDatabase::class.java,
                    "pexels_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}