package com.example.pexelsapp.data.local

import java.util.concurrent.TimeUnit

object CacheUtils {

    private const val CACHE_EXPIRATION_HOURS = 1L
    private const val CACHE_EXPIRATION_MILLIS = 60 * 60 * 1000L // 1 hour in milliseconds

    fun isCacheValid(createdAt: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - createdAt
        return timeDiff < CACHE_EXPIRATION_MILLIS
    }

    fun getCacheExpirationTimestamp(): Long {
        return System.currentTimeMillis() - CACHE_EXPIRATION_MILLIS
    }

    fun shouldRefreshCache(createdAt: Long): Boolean {
        return !isCacheValid(createdAt)
    }

    fun getRemainingCacheTime(createdAt: Long): Long {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - createdAt
        val remainingTime = CACHE_EXPIRATION_MILLIS - timeDiff
        return maxOf(0L, remainingTime)
    }

    fun getCacheStatus(createdAt: Long): String {
        return when {
            isCacheValid(createdAt) -> {
                val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(getRemainingCacheTime(createdAt))
                "Cache valid - $remainingMinutes minutes remaining"
            }
            else -> "Cache expired - needs refresh"
        }
    }
}