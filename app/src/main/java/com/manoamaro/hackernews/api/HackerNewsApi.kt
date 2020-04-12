package com.manoamaro.hackernews.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.manoamaro.hackernews.api.response.UpdatesResponse
import com.manoamaro.hackernews.db.entity.Item
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class HackerNewsApi(context: Context) {
    private val basePath = "https://hacker-news.firebaseio.com/v0"
    private val queue: RequestQueue = Volley.newRequestQueue(context)
    private val gson = GsonBuilder().create()


    suspend fun getItem(id: Int): Item? = suspendCoroutine {
        val r = StringRequest(
            Request.Method.GET,
            "$basePath/item/$id.json",
            Response.Listener { response ->
                try {
                    it.resume(gson.fromJson(response, Item::class.java))
                } catch (e: Exception) {
                    it.resumeWithException(e)
                }
            },
            Response.ErrorListener { error ->
                it.resumeWithException(error)
            }
        )
        queue.add(r)
    }

    suspend fun getUpdates(): UpdatesResponse = suspendCoroutine {
        val r = StringRequest(
            Request.Method.GET,
            "$basePath/updates.json",
            Response.Listener { response ->
                try {
                    it.resume(gson.fromJson(response, UpdatesResponse::class.java))
                } catch (e: Exception) {
                    it.resumeWithException(e)
                }
            },
            Response.ErrorListener { error ->
                it.resumeWithException(error)
            }
        )
        queue.add(r)
    }

    suspend fun getTopStoriesIds(): List<Int> = getStoriesIds("topstories.json")

    suspend fun getNewestStoriesIds(): List<Int> = getStoriesIds("newstories.json")

    suspend fun getBestStoriesIds(): List<Int> = getStoriesIds("beststories.json")

    private suspend fun getStoriesIds(path: String): List<Int> = suspendCoroutine {
        val r = StringRequest(
            "$basePath/$path",
            Response.Listener { response ->
                try {
                    it.resume(gson.fromJson(response, List::class.java).mapNotNull {
                        (it as? Double)?.toInt()
                    })
                } catch (e: Exception) {
                    it.resumeWithException(e)
                }
            },
            Response.ErrorListener { error ->
                it.resumeWithException(error)
            }
        )
        queue.add(r)
    }

    private suspend fun getStreamConnection(url: String): HttpURLConnection =
        withContext(Dispatchers.IO) {
            return@withContext (URL(url).openConnection() as HttpURLConnection).also {
                it.setRequestProperty("Accept", "text/event-stream")
                it.doInput = true
            }
        }

    data class Event(val name: String = "", val data: JsonObject = JsonObject())

    fun getEventsFlow(): Flow<Event> = flow {
        coroutineScope {
            val conn = getStreamConnection("https://hacker-news.firebaseio.com/v0/updates.json")
            val input = conn.inputStream.bufferedReader()
            try {
                conn.connect()
                var event = Event()
                while (isActive) {
                    val line = input.readLine()
                    when {
                        line.startsWith("event:") -> {
                            event = event.copy(name = line.substring(6).trim())
                        }
                        line.startsWith("data:") -> {
                            val data = line.substring(5).trim()
                            try {
                                event =
                                    event.copy(data = gson.fromJson(data, JsonObject::class.java))
                            } catch (e: JsonSyntaxException) {
                            }
                        }
                        line.isEmpty() -> {
                            emit(event)
                            event = Event()
                        }
                    }
                }
            } catch (e: IOException) {
                this.cancel(CancellationException("Network Problem", e))
            } finally {
                conn.disconnect()
                input.close()
            }
        }
    }
}