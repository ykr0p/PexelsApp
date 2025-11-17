package com.example.pexelsapp.presentation.details

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pexelsapp.data.local.database.PexelsDatabase
import com.example.pexelsapp.data.mapper.BookmarkMapper
import com.example.pexelsapp.domain.model.CuratedImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val database: PexelsDatabase
) : ViewModel() {
    
    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()


//    private val _isLoading = MutableStateFlow(true)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isImageLoading = MutableStateFlow(true)
    val isImageLoading: StateFlow<Boolean> = _isImageLoading.asStateFlow()

    fun checkBookmarkStatus(imageId: String) {
        viewModelScope.launch {
            try {
                _isBookmarked.value = database.bookmarkDao().isBookmarked(imageId)
            } catch (e: Exception) {
                _isBookmarked.value = false
            }
        }
    }

    fun toggleBookmark(image: CuratedImage) {
        viewModelScope.launch {
            try {
                val currentlyBookmarked = database.bookmarkDao().isBookmarked(image.id)
                
                if (currentlyBookmarked) {

                    database.bookmarkDao().deleteBookmark(image.id)
                    _isBookmarked.value = false
                } else {

                    val bookmarkEntity = BookmarkMapper.fromDomainToEntity(image)
                    database.bookmarkDao().insertBookmark(bookmarkEntity)
                    _isBookmarked.value = true
                }
            } catch (e: Exception) {

            }
        }
    }


    fun setImageLoading(loading: Boolean) {
        _isImageLoading.value = loading
    }

    fun downloadImage(context: Context, url: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading image...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "$fileName.jpg")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

}
