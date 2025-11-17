package com.example.pexelsapp.data.local

import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

class CacheUtilsTest {
    
    @Test
    fun `isCacheValid - returns true for fresh cache`() {
        val currentTime = System.currentTimeMillis()
        assertTrue("Fresh cache should be valid", CacheUtils.isCacheValid(currentTime))
    }
    
    @Test
    fun `isCacheValid - returns false for expired cache`() {
        val expiredTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        assertFalse("Expired cache should be invalid", CacheUtils.isCacheValid(expiredTime))
    }
    
    @Test
    fun `isCacheValid - returns false for very old cache`() {
        val veryOldTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)
        assertFalse("Very old cache should be invalid", CacheUtils.isCacheValid(veryOldTime))
    }
    
    @Test
    fun `shouldRefreshCache - returns false for fresh cache`() {
        val currentTime = System.currentTimeMillis()
        assertFalse("Fresh cache should not need refresh", CacheUtils.shouldRefreshCache(currentTime))
    }
    
    @Test
    fun `shouldRefreshCache - returns true for expired cache`() {
        val expiredTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        assertTrue("Expired cache should need refresh", CacheUtils.shouldRefreshCache(expiredTime))
    }
    
    @Test
    fun `getRemainingCacheTime - returns positive time for fresh cache`() {
        val currentTime = System.currentTimeMillis()
        val remainingTime = CacheUtils.getRemainingCacheTime(currentTime)
        assertTrue("Fresh cache should have remaining time", remainingTime > 0)
        assertTrue("Fresh cache should have close to 1 hour remaining", 
            remainingTime <= TimeUnit.HOURS.toMillis(1))
    }
    
    @Test
    fun `getRemainingCacheTime - returns zero for expired cache`() {
        val expiredTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        val remainingTime = CacheUtils.getRemainingCacheTime(expiredTime)
        assertEquals("Expired cache should have zero remaining time", 0L, remainingTime)
    }
    
    @Test
    fun `getCacheExpirationTimestamp - returns correct cutoff time`() {
        val expirationTimestamp = CacheUtils.getCacheExpirationTimestamp()
        val currentTime = System.currentTimeMillis()

        val expectedExpirationTime = currentTime - TimeUnit.HOURS.toMillis(1)

        assertTrue("Expiration timestamp should be approximately 1 hour ago", 
            kotlin.math.abs(expirationTimestamp - expectedExpirationTime) < 1000)
    }
    
    @Test
    fun `getCacheStatus - returns correct status for fresh cache`() {
        val currentTime = System.currentTimeMillis()
        val status = CacheUtils.getCacheStatus(currentTime)
        assertTrue("Status should indicate cache is valid", status.contains("valid"))
        assertTrue("Status should mention minutes remaining", status.contains("minutes"))
    }
    
    @Test
    fun `getCacheStatus - returns correct status for expired cache`() {
        val expiredTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        val status = CacheUtils.getCacheStatus(expiredTime)
        assertTrue("Status should indicate cache is expired", status.contains("expired"))
        assertTrue("Status should mention needs refresh", status.contains("refresh"))
    }
    
    @Test
    fun `cache validation edge cases`() {
        // Test exactly 1 hour old cache
        val exactlyOneHourAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        assertFalse("Cache exactly 1 hour old should be expired", 
            CacheUtils.isCacheValid(exactlyOneHourAgo))

        val fiftyNineMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(59)
        assertTrue("Cache 59 minutes old should be valid", 
            CacheUtils.isCacheValid(fiftyNineMinutesAgo))

        val sixtyOneMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(61)
        assertFalse("Cache 61 minutes old should be expired", 
            CacheUtils.isCacheValid(sixtyOneMinutesAgo))
    }
}