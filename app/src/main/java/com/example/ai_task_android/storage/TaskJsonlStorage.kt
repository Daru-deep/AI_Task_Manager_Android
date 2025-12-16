package com.example.ai_task_android.storage

import android.content.Context
import com.example.ai_task_android.model.UiTask
import org.json.JSONObject
import java.io.File

object TaskJsonlStorage {

    private const val FILE_NAME = "tasks.jsonl"

    fun load(context: Context): MutableList<UiTask> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()

        return file.readLines()
            .filter { it.isNotBlank() }
            .map { line ->
                val json = JSONObject(line)
                UiTask(
                    id = json.getLong("id"),
                    title = json.getString("title"),
                    status = json.getString("status"),
                    score = json.getInt("score")
                )
            }
            .toMutableList()
    }

    fun save(context: Context, tasks: List<UiTask>) {
        val file = File(context.filesDir, FILE_NAME)
        file.printWriter().use { out ->
            tasks.forEach { task ->
                val json = JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("status", task.status)
                    put("score", task.score)
                }
                out.println(json.toString())
            }
        }
    }
}
