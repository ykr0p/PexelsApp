package com.example.pexelsapp.domain.model

data class FeaturedCollection(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null
)