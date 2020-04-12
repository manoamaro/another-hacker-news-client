package com.manoamaro.hackernews.ui.neweststories

import androidx.lifecycle.ViewModel
import com.manoamaro.hackernews.repository.ItemRepository

class NewestStoriesViewModel(private val repository: ItemRepository) : ViewModel() {
    fun newestStories() = repository.listNewestStories()
}