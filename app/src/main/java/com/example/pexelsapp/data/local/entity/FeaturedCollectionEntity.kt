package com.example.pexelsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "featured_collections")
data class FeaturedCollectionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val createdAt: Long = System.currentTimeMillis()
)