package com.manoamaro.hackernews.ui.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.manoamaro.hackernews.db.entity.Item
import com.manoamaro.hackernews.repository.ItemRepository

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {
    fun getItem(itemId: Int): LiveData<Item?> = repository.getItem(itemId)
}