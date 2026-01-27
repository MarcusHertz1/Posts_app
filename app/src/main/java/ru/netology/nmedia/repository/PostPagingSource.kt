package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import okio.IOException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Post

class PostPagingSource(
    private val apiService: PostApiService
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val result = when (params) {
                is LoadParams.Refresh -> apiService.getLatest(params.loadSize)
                is LoadParams.Append -> apiService.getBefore(
                    id = params.key,
                    count = params.loadSize
                )

                is LoadParams.Prepend -> return LoadResult.Page(
                    data = emptyList(), nextKey = null, prevKey = params.key
                )
            }
            return LoadResult.Page(data = result, prevKey = params.key, result.lastOrNull()?.id)
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }
    }
}