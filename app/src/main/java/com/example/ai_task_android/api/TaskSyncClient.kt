package com.example.ai_task_android.api


import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class TaskSyncClient(private val baseUrl: String) {

    private val client = OkHttpClient()

    fun fetchTasks() {
        val url = "$baseUrl/api/tasks"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TaskSync", "failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    Log.d("TaskSync", "response=$body")
                }
            }
        })
    }
}
