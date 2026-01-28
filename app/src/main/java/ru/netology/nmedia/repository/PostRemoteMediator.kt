package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import okio.IOException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApiService,
    private val postDao: PostDao,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> apiService.getLatest(state.config.pageSize)
                LoadType.PREPEND -> {
                    val id = state.firstItemOrNull()?.id ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getAfter(
                        id = id,
                        count = state.config.pageSize
                    )
                }

                LoadType.APPEND -> {
                    val id = state.lastItemOrNull()?.id ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getBefore(
                        id = id,
                        count = state.config.pageSize
                    )
                }
            }

            postDao.insert(result.map(PostEntity::fromDto))

            return MediatorResult.Success(result.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}