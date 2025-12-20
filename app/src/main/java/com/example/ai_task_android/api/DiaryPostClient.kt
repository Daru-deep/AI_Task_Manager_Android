package com.example.ai_task_android.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DiaryPostClient(private val baseUrl: String) {

    suspend fun postDiary(body: String, health: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/diary")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            val payload = JSONObject().apply {
                put("body", body)
                put("health", health)
            }

            BufferedWriter(OutputStreamWriter(conn.outputStream, Charsets.UTF_8)).use { w ->
                w.write(payload.toString())
            }

            val code = conn.responseCode
            Log.d("DiaryPost", "code=$code")
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val respText = stream?.bufferedReader()?.use { it.readText() }

            Log.d("DiaryPost", "POST $baseUrl/api/diary code=$code resp=$respText")

            code in 200..299

        } catch (e: Exception) {
            Log.e("DiaryPost", "failed", e)
            false
        }
    }
}
