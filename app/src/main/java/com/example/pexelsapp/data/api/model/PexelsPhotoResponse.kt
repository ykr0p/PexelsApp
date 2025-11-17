package com.example.pexelsapp.data.api.model

import com.google.gson.annotations.SerializedName

data class PexelsPhotoResponse(
    @SerializedName("total_results")
    val totalResults: Int,
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("next_page")
    val nextPage: String?,
    @SerializedName("prev_page")
    val prevPage: String?,
    val photos: List<PexelsPhoto>
)

data class PexelsPhoto(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    @SerializedName("photographer_url")
    val photographerUrl: String,
    @SerializedName("photographer_id")
    val photographerId: Long,
    @SerializedName("avg_color")
    val avgColor: String,
    val src: PexelsPhotoSrc,
    val liked: Boolean,
    val alt: String
)

data class PexelsPhotoSrc(
    val original: String,
    @SerializedName("large2x")
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
)