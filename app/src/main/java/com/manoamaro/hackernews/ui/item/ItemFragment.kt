package com.manoamaro.hackernews.ui.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.manoamaro.hackernews.R
import org.koin.android.viewmodel.ext.android.viewModel

class ItemFragment : Fragment() {

    private val viewModel: ItemViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.item_fragment, container, false)
        return root
    }
}