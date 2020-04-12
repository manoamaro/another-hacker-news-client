package com.manoamaro.hackernews.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Entity(tableName = "items", indices = [Index("type")])
data class Item(
    @PrimaryKey val id: Int,
    @ColumnInfo(defaultValue = "false") val deleted: Boolean = false,
    val type: String,
    val by: String,
    val time: Long,
    val text: String?,
    @ColumnInfo(defaultValue = "false") val dead: Boolean = false,
    val parent: Int?,
    val poll: Int?,
    val kids: List<Int>? = emptyList(),
    val url: String?,
    @ColumnInfo(defaultValue = "0") val score: Int,
    val title: String?,
    val parts: List<Int>? = emptyList(),
    @ColumnInfo(defaultValue = "0") val descendants: Int = 0
) {
    val dateTime: LocalDateTime
    get() = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
}