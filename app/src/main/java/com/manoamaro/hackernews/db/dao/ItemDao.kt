package com.manoamaro.hackernews.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.manoamaro.hackernews.db.entity.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE NOT deleted AND NOT dead AND type IN ('story', 'ask', 'job') ORDER BY time DESC ")
    fun listNewestStoriesDataSource(): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM items WHERE NOT deleted AND NOT dead AND type IN ('story', 'ask', 'job') ORDER BY (score - 1) / ((((strftime('%s','now') - time) / 86400.0) + 2) * (((strftime('%s','now') - time) / 86400.0) + 2)) DESC ")
    fun listTopStoriesDataSource(): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItem(id: Int): Item?

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemAsync(id: Int): LiveData<Item?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item): Long

    @Update
    suspend fun updateItems(vararg items: Item): Int
}