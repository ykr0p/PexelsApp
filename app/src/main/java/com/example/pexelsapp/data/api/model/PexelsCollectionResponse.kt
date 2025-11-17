package com.example.pexelsapp.data.api.model

import com.google.gson.annotations.SerializedName

data class PexelsCollectionResponse(
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    val total_results: Int,
    @SerializedName("next_page")
    val nextPage: String?,
    @SerializedName("prev_page")
    val prevPage: String?,
    val collections: List<PexelsCollection>
)

data class PexelsCollection(
    val id: String,
    val title: String,
    val description: String?,
    val private: Boolean,
    @SerializedName("media_count")
    val mediaCount: Int,
    @SerializedName("photos_count")
    val photosCount: Int,
    @SerializedName("videos_count")
    val videosCount: Int
)