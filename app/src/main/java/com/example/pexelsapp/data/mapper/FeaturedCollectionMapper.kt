package com.example.pexelsapp.data.mapper

import com.example.pexelsapp.data.api.model.PexelsCollection
import com.example.pexelsapp.data.local.entity.FeaturedCollectionEntity
import com.example.pexelsapp.domain.model.FeaturedCollection

object FeaturedCollectionMapper {

    fun fromApiToDomain(apiCollection: PexelsCollection): FeaturedCollection {
        return FeaturedCollection(
            id = apiCollection.id,
            title = apiCollection.title,
            subtitle = apiCollection.description ?: "Featured collection",
            imageUrl = "https://picsum.photos/400/300?random=${apiCollection.id.hashCode()}"
        )
    }

    fun fromApiListToDomain(apiCollections: List<PexelsCollection>): List<FeaturedCollection> {
        return apiCollections.map { fromApiToDomain(it) }
    }

    fun fromEntityToDomain(entity: FeaturedCollectionEntity): FeaturedCollection {
        return FeaturedCollection(
            id = entity.id,
            title = entity.title,
            subtitle = entity.subtitle,
            imageUrl = entity.imageUrl
        )
    }

    fun fromEntityListToDomain(entities: List<FeaturedCollectionEntity>): List<FeaturedCollection> {
        return entities.map { fromEntityToDomain(it) }
    }

    fun fromDomainToEntity(domain: FeaturedCollection): FeaturedCollectionEntity {
        return FeaturedCollectionEntity(
            id = domain.id,
            title = domain.title,
            subtitle = domain.subtitle,
            imageUrl = domain.imageUrl
        )
    }

    fun fromDomainListToEntity(domains: List<FeaturedCollection>): List<FeaturedCollectionEntity> {
        return domains.map { fromDomainToEntity(it) }
    }
}