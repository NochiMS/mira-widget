package com.mira.widget

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class MiraUpdateWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        MiraWidget.updateAllWidgets(applicationContext)
        return Result.success()
    }

    companion object {
        private const val TAG = "mira_periodic"

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<MiraUpdateWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        }
    }
}
