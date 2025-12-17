package com.example.ai_task_android.storage

import android.content.Context
import com.example.ai_task_android.model.UiTask
import org.json.JSONObject
import java.io.File

object TaskJsonlStorage {

    private const val FILE_NAME = "tasks.jsonl"

    fun load(context: Context): MutableList<UiTask> {
        val file = File(context.filesDir, FILE_NAME)

        // If file doesn't exist, create it with seed data
        if (!file.exists()) {
            val seedTasks = createSeedData()
            save(context, seedTasks)
            return seedTasks.toMutableList()
        }

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

    private fun createSeedData(): List<UiTask> {
        return listOf(
            UiTask(1L, "Pythonのタスク管理ツールを試す", "todo", 0),
            UiTask(2L, "今日やったことを纏める", "todo", 0),
            UiTask(3L, "doneの拡張", "todo", 0),
            UiTask(4L, "Pythonのタスク管理ツールのタグ機能を試す", "todo", 15),
            UiTask(5L, "ゲーム業界への質問を考える", "todo", 0),
            UiTask(9L, "ベースの飾り方のレイアウト案を3パターン書き出す", "todo", 5),
            UiTask(11L, "テストタスク", "todo", 15)
        )
    }
}