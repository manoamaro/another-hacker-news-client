package com.manoamaro.hackernews

import android.app.Application
import com.manoamaro.hackernews.module.appModule
import com.manoamaro.hackernews.module.viewModelModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HackerNewsApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HackerNewsApplication)
            modules(appModule, viewModelModules)
        }
    }
}