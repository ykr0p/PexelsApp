package com.example.pexelsapp.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pexelsapp.data.local.database.PexelsDatabase
import com.example.pexelsapp.data.mapper.BookmarkMapper
import com.example.pexelsapp.domain.model.CuratedImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class BookmarkViewModel(
    private val database: PexelsDatabase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()
    
    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.value = BookmarkUiState.Loading
            
            try {
                database.bookmarkDao().getAllBookmarks().collect { bookmarkEntities ->
                    val bookmarkedImages = BookmarkMapper.fromEntityListToDomain(bookmarkEntities)
                    
                    if (bookmarkedImages.isEmpty()) {
                        _uiState.value = BookmarkUiState.Empty
                    } else {
                        _uiState.value = BookmarkUiState.Success(bookmarkedImages)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = BookmarkUiState.Error(e.message ?: "Failed to load bookmarks")
            }
        }
    }

    fun removeBookmark(imageId: String) {
        viewModelScope.launch {
            try {
                database.bookmarkDao().deleteBookmark(imageId)
            } catch (e: Exception) {

            }
        }
    }
}

sealed class BookmarkUiState {
    object Loading : BookmarkUiState()
    object Empty : BookmarkUiState()
    data class Success(val bookmarks: List<CuratedImage>) : BookmarkUiState()
    data class Error(val message: String) : BookmarkUiState()
}
