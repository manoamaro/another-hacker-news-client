package com.manoamaro.hackernews.module

import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.WorkManager
import com.manoamaro.hackernews.api.HackerNewsApi
import com.manoamaro.hackernews.db.AppDatabase
import com.manoamaro.hackernews.repository.ItemRepository
import com.manoamaro.hackernews.ui.item.ItemViewModel
import com.manoamaro.hackernews.ui.neweststories.NewestStoriesViewModel
import com.manoamaro.hackernews.ui.notifications.NotificationsViewModel
import com.manoamaro.hackernews.ui.topstories.TopStoriesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "hackernews-db").build()
    }

    single {
        HackerNewsApi(androidApplication())
    }

    single {
        ItemRepository(get(), get())
    }

    single {
        WorkManager.getInstance(androidApplication())
    }

    single { NotificationManagerCompat.from(androidApplication()) }
}


val viewModelModules = module {
    viewModel { TopStoriesViewModel(get()) }
    viewModel { NewestStoriesViewModel(get()) }
    viewModel { NotificationsViewModel() }
    viewModel { ItemViewModel(get()) }
}