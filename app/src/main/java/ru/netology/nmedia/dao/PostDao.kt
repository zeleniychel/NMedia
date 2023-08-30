package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.util.DateUtil

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER by id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE PostEntity SET content = :text WHERE id = :id")
    fun changeContentById(id: Long, text: String)

    @Query("""UPDATE PostEntity SET
            likesCount = likesCount + CASE WHEN likeByMe THEN -1 ELSE 1 END,
        likeByMe = CASE WHEN likeByMe THEN 0 ELSE 1 END
                WHERE id = :id;
    """)
    fun likeById(id: Long)

    @Query("""UPDATE PostEntity SET sharesCount = sharesCount +1
            WHERE id = :id;""")
    fun share(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    fun removeById(id: Long)
    fun save(post: PostEntity) =
        if (post.id == 0L) insert(post.copy(author = "Me", published = DateUtil.getCurrentDate())) else changeContentById(post.id, post.content)


}