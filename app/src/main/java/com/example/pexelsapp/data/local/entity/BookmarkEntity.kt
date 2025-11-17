package com.example.pexelsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val photographer: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val tags: String,
    val bookmarkedAt: Long = System.currentTimeMillis()
)
