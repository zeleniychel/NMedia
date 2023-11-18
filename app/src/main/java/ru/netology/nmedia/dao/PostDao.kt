package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE isHidden = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("UPDATE PostEntity SET isHidden = 0")
    suspend fun changeIsHiddenFlag()

    @Query("SELECT COUNT(*) FROM PostEntity WHERE isHidden = 1")
    suspend fun getIsHiddenCount(): Int

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query(
        """UPDATE PostEntity SET
            likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
    """
    )
    suspend fun likeById(id: Long)

    @Query("SELECT isSaved FROM PostEntity WHERE id = :id")
    suspend fun getIsSavedById(id: Long): Boolean?


}