package com.example.pexelsapp.presentation.home

import androidx.lifecycle.viewModelScope
import com.example.pexelsapp.data.api.NetworkException
import com.example.pexelsapp.data.api.ApiException
import com.example.pexelsapp.domain.model.CuratedImage
import com.example.pexelsapp.domain.model.FeaturedCollection
import com.example.pexelsapp.domain.usecase.GetCuratedImagesUseCase
import com.example.pexelsapp.domain.usecase.GetFeaturedCollectionsUseCase
import com.example.pexelsapp.domain.usecase.SearchImagesUseCase
import com.example.pexelsapp.presentation.base.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isPerformingSearch: Boolean = false,
    val hasNetworkError: Boolean = false,
    val hasRealCachedData: Boolean = false,
    val usingFallbackData: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCollection: String? = null,
    val featuredCollections: List<FeaturedCollection> = emptyList(),
    val curatedImages: List<CuratedImage> = emptyList(),
    val hasMoreItems: Boolean = true,
    val currentPage: Int = 1,
    val originalCollectionOrder: List<String> = emptyList(),
    val lastFailedQuery: String? = null,
    val isSearchRequest: Boolean = false
)

class HomeViewModel(
    private val getFeaturedCollectionsUseCase: GetFeaturedCollectionsUseCase,
    private val getCuratedImagesUseCase: GetCuratedImagesUseCase,
    private val searchImagesUseCase: SearchImagesUseCase
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val searchDebounceTime = 500L
    
    init {
        loadInitialData()
    }
    

    private fun loadInitialData() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            hasNetworkError = false,
            lastFailedQuery = null,
            isSearchRequest = false
        )

        viewModelScope.launch {
            val collectionsResult = getFeaturedCollectionsUseCase()
            val imagesResult = getCuratedImagesUseCase(1)

            if (collectionsResult.isSuccess && imagesResult.isSuccess) {
                val collections = collectionsResult.getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    featuredCollections = collections,
                    curatedImages = imagesResult.getOrDefault(emptyList()),
                    hasRealCachedData = true,
                    usingFallbackData = false,
                    hasNetworkError = false,
                    errorMessage = null,
                    originalCollectionOrder = collections.map { it.id }
                )
            } else {

                val collections = collectionsResult.getOrNull() ?: emptyList()
                val images = imagesResult.getOrNull() ?: emptyList()
                val isNetworkError = collectionsResult.exceptionOrNull() is NetworkException ||
                        imagesResult.exceptionOrNull() is NetworkException

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    featuredCollections = collections.ifEmpty { getFallbackFeaturedCollections() },
                    curatedImages = images,
                    hasRealCachedData = collections.isNotEmpty() || images.isNotEmpty(),
                    hasNetworkError = isNetworkError && collections.isEmpty() && images.isEmpty(),
                    usingFallbackData = collections.isEmpty() && images.isEmpty(),
                    errorMessage = if (!isNetworkError) "Failed to load data" else null,
                    originalCollectionOrder = collections.map { it.id }
                )
            }
        }
    }
    

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )

        val matchingCollection = _uiState.value.featuredCollections.find { collection ->
            collection.title.equals(query, ignoreCase = true)
        }?.id
        val currentCollections = _uiState.value.featuredCollections
        val reorderedCollections = reorderCollectionsForActive(currentCollections, matchingCollection)
        
        _uiState.value = _uiState.value.copy(
            selectedCollection = matchingCollection,
            featuredCollections = reorderedCollections
        )

        viewModelScope.launch {
            delay(searchDebounceTime)
            if (query.isNotEmpty()) {
                performSearch(query)
            } else {

                val originalOrderCollections = reorderCollectionsForActive(currentCollections, null)
                _uiState.value = _uiState.value.copy(
                    featuredCollections = originalOrderCollections,
                    selectedCollection = null
                )
                loadCuratedImages()
            }
        }
    }

    fun onSearchSubmitted(query: String) {
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                performSearch(query)
            }
        }
    }

    fun reorderCollectionsForActive(
        collections: List<FeaturedCollection>,
        activeCollectionId: String?
    ): List<FeaturedCollection> {

        val originalOrderIds = _uiState.value.originalCollectionOrder.ifEmpty {
            collections.map { it.id }
        }
        
        if (activeCollectionId.isNullOrEmpty()) {

            val collectionMap = collections.associateBy { it.id }

            return originalOrderIds.mapNotNull { id -> collectionMap[id] }
        }
        
        val activeCollection = collections.find { it.id == activeCollectionId }
        if (activeCollection == null) {

            val collectionMap = collections.associateBy { it.id }
            return originalOrderIds.mapNotNull { id -> collectionMap[id] }
        }
        

        val otherCollections = originalOrderIds
            .filter { it != activeCollectionId }
            .mapNotNull { id -> collections.find { it.id == id } }
        

        return listOf(activeCollection) + otherCollections
    }
    

    fun onClearSearch() {
        val currentCollections = _uiState.value.featuredCollections
        val originalOrderCollections = reorderCollectionsForActive(currentCollections, null)
        
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCollection = null,
            isPerformingSearch = false,
            featuredCollections = originalOrderCollections
        )
        viewModelScope.launch {
            loadCuratedImages()
        }
    }
    

    fun onFeaturedCollectionSelected(collectionTitle: String) {
        val currentCollections = _uiState.value.featuredCollections
        val selectedCollection = currentCollections.find { it.title == collectionTitle }
        val selectedCollectionId = selectedCollection?.id
        val reorderedCollections = reorderCollectionsForActive(currentCollections, selectedCollection?.id)
        
        _uiState.value = _uiState.value.copy(
            searchQuery = collectionTitle,
            selectedCollection = selectedCollectionId,
            featuredCollections = reorderedCollections
        )
        viewModelScope.launch {
            performSearch(collectionTitle)
        }
    }
    

    fun onExploreClick() {
        val currentCollections = _uiState.value.featuredCollections
        val originalOrderCollections = reorderCollectionsForActive(currentCollections, null)
        
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCollection = null,
            isRefreshing = true,
            featuredCollections = originalOrderCollections
        )
        
        viewModelScope.launch {
            try {
                val result = getCuratedImagesUseCase(1)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        curatedImages = result.getOrDefault(emptyList()),
                        errorMessage = null,
                        hasRealCachedData = true
                    )
                }
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }
    

    fun onRefresh() {
        if (_uiState.value.searchQuery.isNotEmpty()) {
            viewModelScope.launch {
                performSearch(_uiState.value.searchQuery)
            }
        } else {
            loadInitialData()
        }
    }
    

    fun loadMoreImages() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMoreItems || currentState.isPerformingSearch) {
            return
        }

        val nextPage = currentState.currentPage + 1
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(
                    isLoadingMore = true
                )
                
                val result = getCuratedImagesUseCase(nextPage)
                if (result.isSuccess) {
                    val newImages = result.getOrDefault(emptyList())
                    val allImages = currentState.curatedImages + newImages
                    
                    _uiState.value = currentState.copy(
                        curatedImages = allImages,
                        hasMoreItems = newImages.size == 30,
                        currentPage = nextPage,
                        errorMessage = null,
                        hasRealCachedData = true
                    )
                }
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false
                )
            }
        }
    }

    fun onRetryClick() {
        val currentState = _uiState.value
        if (currentState.isSearchRequest && currentState.lastFailedQuery != null) {
            // AC7
            viewModelScope.launch {
                performSearch(currentState.lastFailedQuery)
            }
        } else if (currentState.searchQuery.isNotEmpty()) {
            // retrycurrent search
            viewModelScope.launch {
                performSearch(currentState.searchQuery)
            }
        } else {
            // AC7
            loadInitialData()
        }
    }
    

    private suspend fun performSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            isPerformingSearch = true,
            hasNetworkError = false,
            currentPage = 1,
            hasMoreItems = true,
            isSearchRequest = true,
            lastFailedQuery = query
        )
        
        val result = searchImagesUseCase(query)
        
        if (result.isSuccess) {
            val images = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                isPerformingSearch = false,
                curatedImages = images,
                errorMessage = if (images.isEmpty()) "No results found for \"$query\"" else null,
                hasRealCachedData = false,
                usingFallbackData = false,
                hasNetworkError = false,
                lastFailedQuery = null
            )
        } else {
            // AC7 || AC8
            val exception = result.exceptionOrNull()
            val isNetworkError = exception is NetworkException
            
            if (isNetworkError) {
                // AC7
                _uiState.value = _uiState.value.copy(
                    isPerformingSearch = false,
                    hasNetworkError = true,
                    hasRealCachedData = false,
                    curatedImages = emptyList(),
                    errorMessage = null
                )
            } else {
                // AC8
                val errorMsg = when (exception) {
                    is ApiException -> exception.message ?: "Search failed"
                    else -> "Search failed: ${exception?.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isPerformingSearch = false,
                    hasNetworkError = false,
                    errorMessage = errorMsg,
                    curatedImages = emptyList()
                )
            }
        }
    }

    private fun loadCuratedImages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchRequest = false,
                lastFailedQuery = null
            )
            
            val result = getCuratedImagesUseCase(1)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    curatedImages = result.getOrDefault(emptyList()),
                    errorMessage = null,
                    hasRealCachedData = true,
                    usingFallbackData = false,
                    hasNetworkError = false
                )
            } else {
                // AC7
                val exception = result.exceptionOrNull()
                val isNetworkError = exception is NetworkException
                val images = result.getOrNull() ?: emptyList()
                
                if (images.isEmpty() && isNetworkError) {
                    // AC7: No cached data and network error
                    _uiState.value = _uiState.value.copy(
                        hasNetworkError = true,
                        hasRealCachedData = false,
                        curatedImages = emptyList()
                    )
                } else if (images.isNotEmpty()) {
                    // AC7
                    _uiState.value = _uiState.value.copy(
                        curatedImages = images,
                        hasRealCachedData = true,
                        hasNetworkError = isNetworkError
                    )
                } else {

                    val errorMsg = when (exception) {
                        is ApiException -> exception.message ?: "Failed to load images"
                        else -> "Failed to load images"
                    }
                    _uiState.value = _uiState.value.copy(
                        errorMessage = errorMsg,
                        hasNetworkError = false
                    )
                }
            }
        }
    }
    

    private fun getMatchingCollectionTitle(query: String): String? {
        return _uiState.value.featuredCollections.find { collection ->
            collection.title.equals(query, ignoreCase = true)
        }?.id
    }
    

    private fun getFallbackFeaturedCollections(): List<FeaturedCollection> = listOf(
        FeaturedCollection("1", "Nature", "Beautiful landscapes"),
        FeaturedCollection("2", "Architecture", "Modern designs"),
        FeaturedCollection("3", "People", "Portrait photography"),
        FeaturedCollection("4", "Technology", "Digital innovation"),
        FeaturedCollection("5", "Food", "Culinary arts"),
        FeaturedCollection("6", "Travel", "Adventure awaits"),
        FeaturedCollection("7", "Art", "Creative expression")
    )
    
    private fun getFallbackCuratedImages(): List<CuratedImage> =
        (1..30).map { index ->
            CuratedImage(
                id = index.toString(),
                photographer = "Photographer $index",
                imageUrl = "https://picsum.photos/600/800?random=$index",
                thumbnailUrl = "https://picsum.photos/300/400?random=$index",
                width = 600,
                height = 800,
                tags = listOf("popular", "trending", "featured")
            )
        }
    
    override fun handleError(exception: Throwable) {
        val fallbackCollections = getFallbackFeaturedCollections()
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isRefreshing = false,
            isPerformingSearch = false,
            hasNetworkError = true,
            errorMessage = exception.message ?: "Unknown error occurred",
            usingFallbackData = true,
            featuredCollections = fallbackCollections,
            curatedImages = getFallbackCuratedImages(),
            originalCollectionOrder = fallbackCollections.map { it.id }
        )
    }
    

    private fun handleException(e: Exception, isSearch: Boolean = false) {
        val isNetworkError = e is NetworkException
        val errorMsg = when (e) {
            is NetworkException -> null
            is ApiException -> e.message
            else -> e.message ?: "Unknown error occurred"
        }
        
        val fallbackCollections = getFallbackFeaturedCollections()
        _uiState.value = _uiState.value.copy(
            hasNetworkError = isNetworkError,
            errorMessage = errorMsg,
            usingFallbackData = !isNetworkError,
            isLoading = false,
            isRefreshing = false,
            isPerformingSearch = if (isSearch) false else _uiState.value.isPerformingSearch,
            featuredCollections = if (isNetworkError) emptyList() else fallbackCollections,
            originalCollectionOrder = if (isNetworkError) emptyList() else fallbackCollections.map { it.id }
        )
    }

}