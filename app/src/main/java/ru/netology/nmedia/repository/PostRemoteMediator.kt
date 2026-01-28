package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import okio.IOException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> apiService.getLatest(state.config.pageSize)
                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getAfter(
                        id = id,
                        count = state.config.pageSize
                    )
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getBefore(
                        id = id,
                        count = state.config.pageSize
                    )
                }
            }

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postDao.clear()
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    result.first().id
                                ),
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    result.last().id
                                ),
                            )
                        )
                    }

                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    result.first().id
                                ),
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    result.last().id
                                ),
                            )
                        )
                    }
                }
            }

            postDao.insert(result.map(PostEntity::fromDto))

            return MediatorResult.Success(result.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}