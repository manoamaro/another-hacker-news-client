package com.manoamaro.hackernews.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class Converters {

    @TypeConverter
    fun timestampToDate(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault()) }
    }

    @TypeConverter
    fun dateToTimestamp(value: LocalDateTime?): Long? {
        return value?.toEpochSecond(ZoneOffset.of(ZoneId.systemDefault().id))
    }

    @TypeConverter
    fun intListToString(value: List<Int>?): String? {
        return value?.joinToString(";")
    }

    @TypeConverter
    fun stringToIntList(value: String?): List<Int>? {
        return value?.split(";")?.mapNotNull { it.toIntOrNull() }
    }
}