package com.manoamaro.hackernews.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.manoamaro.hackernews.R
import com.manoamaro.hackernews.db.entity.Item
import kotlinx.android.synthetic.main.card_item.view.*
import java.time.ZoneId

class ItemAdapter(private val onClickListener: (Item?) -> Unit): PagedListAdapter<Item, ItemViewHolder>(ITEM_DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindTo(getItem(position), onClickListener)
    }
}

class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    fun bindTo(item: Item?, onClickListener: (Item?) -> Unit) {
        itemView.cardItem_title.text = item?.title
        itemView.cardItem_comments.text = item?.descendants?.toString()
        itemView.cardItem_score.text = item?.score?.toString()
        item?.dateTime?.let { dateTime ->
            ZoneId.systemDefault().id
            val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
            itemView.cardItem_datetime.text = DateUtils.getRelativeTimeSpanString(instant.toEpochMilli())
        }
        itemView.cardItem_title.setOnClickListener { onClickListener(item) }
    }
}

val ITEM_DIFF_CALLBACK = object: DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
}