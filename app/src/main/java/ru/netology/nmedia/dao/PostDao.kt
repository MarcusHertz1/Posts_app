package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
     fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) = 0 FROM PostEntity")
    fun isEmpty(): Boolean

    /*suspend fun save(post: PostEntity) {
        if (post.id == 0L) {
            insert(post)
        } else {
            updateById (post.id, post.content)
        }
    }*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateById(id: Long, content: String)

    @Query("""
           UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
        """)
    suspend fun likeById(id: Long)

    @Query("""
           UPDATE PostEntity SET
               shares = shares + 1
           WHERE id = :id;
        """)
    suspend fun shareById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id=:id")
    suspend fun removeById(id: Long)

    @Query("SELECT * FROM PostEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PostEntity?

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}