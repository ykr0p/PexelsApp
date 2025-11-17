package com.example.pexelsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "curated_images")
data class CuratedImageEntity(
    @PrimaryKey
    val id: String,
    val photographer: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val tags: String,
    val createdAt: Long = System.currentTimeMillis()
)