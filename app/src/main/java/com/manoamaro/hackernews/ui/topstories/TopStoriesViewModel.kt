package com.manoamaro.hackernews.ui.topstories

import androidx.lifecycle.ViewModel
import com.manoamaro.hackernews.repository.ItemRepository

class TopStoriesViewModel(private val itemRepository: ItemRepository) : ViewModel() {

    fun topStories() = itemRepository.listTopStories()
}