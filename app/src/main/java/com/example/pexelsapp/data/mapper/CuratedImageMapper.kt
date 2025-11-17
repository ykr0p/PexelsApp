package com.example.pexelsapp.data.mapper

import com.example.pexelsapp.data.api.model.PexelsPhoto
import com.example.pexelsapp.data.local.entity.CuratedImageEntity
import com.example.pexelsapp.domain.model.CuratedImage

object CuratedImageMapper {

    fun fromApiToDomain(apiPhoto: PexelsPhoto): CuratedImage {
        return CuratedImage(
            id = apiPhoto.id.toString(),
            photographer = apiPhoto.photographer,
            imageUrl = apiPhoto.src.original,
            thumbnailUrl = apiPhoto.src.medium,
            width = apiPhoto.width,
            height = apiPhoto.height,
            tags = listOf("featured", "curated", "popular")
        )
    }

    fun fromApiListToDomain(apiPhotos: List<PexelsPhoto>): List<CuratedImage> {
        return apiPhotos.map { fromApiToDomain(it) }
    }

    fun fromEntityToDomain(entity: CuratedImageEntity): CuratedImage {
        return CuratedImage(
            id = entity.id,
            photographer = entity.photographer,
            imageUrl = entity.imageUrl,
            thumbnailUrl = entity.thumbnailUrl,
            width = entity.width,
            height = entity.height,
            tags = entity.tags.split(",").filter { it.isNotBlank() }
        )
    }

    fun fromEntityListToDomain(entities: List<CuratedImageEntity>): List<CuratedImage> {
        return entities.map { fromEntityToDomain(it) }
    }

    fun fromDomainToEntity(domain: CuratedImage): CuratedImageEntity {
        return CuratedImageEntity(
            id = domain.id,
            photographer = domain.photographer,
            imageUrl = domain.imageUrl,
            thumbnailUrl = domain.thumbnailUrl,
            width = domain.width,
            height = domain.height,
            tags = domain.tags.joinToString(",")
        )
    }

    fun fromDomainListToEntity(domains: List<CuratedImage>): List<CuratedImageEntity> {
        return domains.map { fromDomainToEntity(it) }
    }
}