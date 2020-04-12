package com.manoamaro.hackernews.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.manoamaro.hackernews.db.dao.ItemDao
import com.manoamaro.hackernews.db.entity.Item

@Database(entities = [Item::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun itemDao(): ItemDao
}