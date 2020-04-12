package com.manoamaro.hackernews.ui.topstories

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.manoamaro.hackernews.R
import com.manoamaro.hackernews.ui.ItemAdapter
import com.manoamaro.hackernews.worker.SyncStoriesWorker
import kotlinx.android.synthetic.main.fragment_stories_list.view.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class TopStoriesFragment : Fragment() {

    private val topStoriesViewModel: TopStoriesViewModel by viewModel()
    private val workManager: WorkManager by inject()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_stories_list, container, false)
        val adapter = ItemAdapter { item ->
            item?.let {
                CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(item.url))
            }
        }

        topStoriesViewModel.topStories().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it) {
                root.stories_recyclerView.scrollToPosition(0)
            }
        })

        root.stories_recyclerView.adapter = adapter
        root.stories_recyclerView.layoutManager = LinearLayoutManager(this.context)
        root.stories_swipeRefresh.setOnRefreshListener {
            SyncStoriesWorker.startUniqueWork(workManager, SyncStoriesWorker.Companion.NewsChannel.TOP_STORIES)
        }

        workManager.getWorkInfosForUniqueWorkLiveData(SyncStoriesWorker.TASK_ID).observe(viewLifecycleOwner, Observer { workInfos ->
            root.stories_swipeRefresh.isRefreshing = workInfos.map { it.state }.contains(WorkInfo.State.RUNNING)
        })

        SyncStoriesWorker.startUniqueWork(workManager, SyncStoriesWorker.Companion.NewsChannel.TOP_STORIES)

        return root
    }
}
