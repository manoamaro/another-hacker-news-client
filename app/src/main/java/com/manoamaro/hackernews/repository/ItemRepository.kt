package com.manoamaro.hackernews.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.manoamaro.hackernews.api.HackerNewsApi
import com.manoamaro.hackernews.db.AppDatabase
import com.manoamaro.hackernews.db.entity.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(db: AppDatabase, private val api: HackerNewsApi) {
    private val itemDao = db.itemDao()

    private val pageSize = 20

    fun listTopStories(): LiveData<PagedList<Item>> {
        return itemDao.listTopStoriesDataSource().toLiveData(pageSize)
    }

    fun listNewestStories(): LiveData<PagedList<Item>> {
        return itemDao.listNewestStoriesDataSource().toLiveData(pageSize)
    }

    fun getItem(id: Int): LiveData<Item?> {
        return itemDao.getItemAsync(id)
    }

    suspend fun upsert(item: Item) = withContext(Dispatchers.IO) {
        if (itemDao.insert(item) <= 0) {
            itemDao.updateItems(item)
        }
    }

    suspend fun updateItem(id: Int) = withContext(Dispatchers.IO) {
        try {
            api.getItem(id)?.let { item ->
                upsert(item)
            }
        } catch (e: Exception) {

        }
    }
}