package com.example.pexelsapp.data.mapper

import com.example.pexelsapp.data.local.entity.BookmarkEntity
import com.example.pexelsapp.domain.model.CuratedImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object BookmarkMapper {
    private val gson = Gson()

    fun fromEntityToDomain(entity: BookmarkEntity): CuratedImage {
        val tagsType = object : TypeToken<List<String>>() {}.type
        val tags: List<String> = try {
            gson.fromJson(entity.tags, tagsType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return CuratedImage(
            id = entity.id,
            photographer = entity.photographer,
            imageUrl = entity.imageUrl,
            thumbnailUrl = entity.thumbnailUrl,
            width = entity.width,
            height = entity.height,
            tags = tags
        )
    }

    fun fromDomainToEntity(image: CuratedImage): BookmarkEntity {
        return BookmarkEntity(
            id = image.id,
            photographer = image.photographer,
            imageUrl = image.imageUrl,
            thumbnailUrl = image.thumbnailUrl,
            width = image.width,
            height = image.height,
            tags = gson.toJson(image.tags),
            bookmarkedAt = System.currentTimeMillis()
        )
    }

    fun fromEntityListToDomain(entities: List<BookmarkEntity>): List<CuratedImage> {
        return entities.map { fromEntityToDomain(it) }
    }

    fun fromDomainListToEntity(images: List<CuratedImage>): List<BookmarkEntity> {
        return images.map { fromDomainToEntity(it) }
    }
}
