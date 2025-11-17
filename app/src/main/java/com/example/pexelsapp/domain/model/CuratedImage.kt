package com.example.pexelsapp.domain.model

data class CuratedImage(
    val id: String,
    val photographer: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val tags: List<String> = emptyList()
)