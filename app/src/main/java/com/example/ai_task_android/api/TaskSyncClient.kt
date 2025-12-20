package com.example.ai_task_android.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class TaskSyncClient(private val baseUrl: String) {
    private val client = OkHttpClient()

    suspend fun fetchTasksJson(): String = withContext(Dispatchers.IO) {
        val url = "$baseUrl/api/tasks"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { resp ->
            val json = resp.body?.string().orEmpty()
            Log.d("TaskSync", "json head=" + json.take(200))
            json
        }


    }
}
