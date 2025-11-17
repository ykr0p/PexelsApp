package com.example.pexelsapp.data.local.dao

import androidx.room.*
import com.example.pexelsapp.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :imageId")
    suspend fun getBookmarkById(imageId: String): BookmarkEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :imageId)")
    suspend fun isBookmarked(imageId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :imageId")
    suspend fun deleteBookmark(imageId: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}
