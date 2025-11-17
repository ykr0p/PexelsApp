package com.example.pexelsapp.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


abstract class BaseViewModel : ViewModel() {
    
    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        handleError(exception)
    }
    
    protected abstract fun handleError(exception: Throwable)
    
    protected fun launchWithErrorHandling(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(errorHandler) {
            block()
        }
    }
}