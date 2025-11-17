package com.example.pexelsapp.presentation.home

import com.example.pexelsapp.domain.model.FeaturedCollection
import com.example.pexelsapp.domain.usecase.GetCuratedImagesUseCase
import com.example.pexelsapp.domain.usecase.GetFeaturedCollectionsUseCase
import com.example.pexelsapp.domain.usecase.SearchImagesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AC15CollectionReorderTest {
    
    @Test
    fun `reorderCollectionsForActive - moves active collection to first position`() = runTest {
        val mockGetCollections = mock<GetFeaturedCollectionsUseCase>()
        val mockGetImages = mock<GetCuratedImagesUseCase>()
        val mockSearch = mock<SearchImagesUseCase>()
        
        val viewModel = HomeViewModel(mockGetCollections, mockGetImages, mockSearch)
        
        val collections = listOf(
            FeaturedCollection("1", "Nature", "Beautiful landscapes"),
            FeaturedCollection("2", "Architecture", "Modern designs"),
            FeaturedCollection("3", "People", "Portrait photography"),
            FeaturedCollection("4", "Technology", "Digital innovation")
        )
        
        val reordered = viewModel.reorderCollectionsForActive(collections, "3")
        
        // Active collection should be first
        assertEquals("3", reordered[0].id)
        assertEquals("People", reordered[0].title)
        
        // Other collections should maintain original order
        assertEquals("1", reordered[1].id)
        assertEquals("Nature", reordered[1].title)
        assertEquals("2", reordered[2].id)
        assertEquals("Architecture", reordered[2].title)
        assertEquals("4", reordered[3].id)
        assertEquals("Technology", reordered[3].title)
    }
    
    @Test
    fun `reorderCollectionsForActive - returns original order when no active collection`() = runTest {
        val mockGetCollections = mock<GetFeaturedCollectionsUseCase>()
        val mockGetImages = mock<GetCuratedImagesUseCase>()
        val mockSearch = mock<SearchImagesUseCase>()
        
        val viewModel = HomeViewModel(mockGetCollections, mockGetImages, mockSearch)
        
        val collections = listOf(
            FeaturedCollection("1", "Nature", "Beautiful landscapes"),
            FeaturedCollection("2", "Architecture", "Modern designs"),
            FeaturedCollection("3", "People", "Portrait photography")
        )
        
        val reordered = viewModel.reorderCollectionsForActive(collections, null)
        
        // Should maintain original order
        assertEquals("1", reordered[0].id)
        assertEquals("2", reordered[1].id)
        assertEquals("3", reordered[2].id)
    }
    
    @Test
    fun `reorderCollectionsForActive - handles non-existent active collection`() = runTest {
        val mockGetCollections = mock<GetFeaturedCollectionsUseCase>()
        val mockGetImages = mock<GetCuratedImagesUseCase>()
        val mockSearch = mock<SearchImagesUseCase>()
        
        val viewModel = HomeViewModel(mockGetCollections, mockGetImages, mockSearch)
        
        val collections = listOf(
            FeaturedCollection("1", "Nature", "Beautiful landscapes"),
            FeaturedCollection("2", "Architecture", "Modern designs")
        )
        
        val reordered = viewModel.reorderCollectionsForActive(collections, "999")

        assertEquals("1", reordered[0].id)
        assertEquals("2", reordered[1].id)
    }
    
    @Test
    fun `reorderCollectionsForActive - handles empty collection list`() = runTest {
        val mockGetCollections = mock<GetFeaturedCollectionsUseCase>()
        val mockGetImages = mock<GetCuratedImagesUseCase>()
        val mockSearch = mock<SearchImagesUseCase>()
        
        val viewModel = HomeViewModel(mockGetCollections, mockGetImages, mockSearch)
        
        val collections = emptyList<FeaturedCollection>()
        
        val reordered = viewModel.reorderCollectionsForActive(collections, "1")

        assertTrue(reordered.isEmpty())
    }
}