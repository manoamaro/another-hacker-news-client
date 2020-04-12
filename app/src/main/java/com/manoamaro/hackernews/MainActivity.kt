package com.manoamaro.hackernews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.manoamaro.hackernews.api.HackerNewsApi
import com.manoamaro.hackernews.repository.ItemRepository
import com.manoamaro.hackernews.worker.UpdateStoriesWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val workManager: WorkManager by inject()
    private val api: HackerNewsApi by inject()
    private val repository: ItemRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_newest_stories, R.id.navigation_dashboard, R.id.navigation_notifications))
        app_collapsingToolbar.setupWithNavController(app_toolbar, navController, appBarConfiguration)

        bottomNavigationView.setupWithNavController(navController)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val updateStoriesRequest = PeriodicWorkRequestBuilder<UpdateStoriesWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(UpdateStoriesWorker.TASK_ID, ExistingPeriodicWorkPolicy.KEEP, updateStoriesRequest)

        api.getEventsFlow().asLiveData(Dispatchers.IO).observe(this, Observer { event ->
            this.lifecycleScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    if (event.name == "put") {
                        event.data
                            .getAsJsonObject("data")
                            .getAsJsonArray("items")
                            .mapNotNull { it.asInt }
                            .forEach { item -> repository.updateItem(item) }
                    }
                }
            }
        })
    }
}
