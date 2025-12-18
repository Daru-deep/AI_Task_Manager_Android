package com.example.ai_task_android.storage

import android.content.Context
import com.example.ai_task_android.model.UiTask
import org.json.JSONObject
import java.io.File

object TaskJsonlStorage {

    private const val FILE_NAME = "tasks.jsonl"

    // ✅ 戻り値を List<UiTask> に変更（MutableList → List）
    fun load(context: Context): List<UiTask> {
        val file = File(context.filesDir, FILE_NAME)

        // ファイルが存在しない場合、シードデータを作成
        if (!file.exists()) {
            val seedTasks = createSeedData()
            save(context, seedTasks)
            return seedTasks  // ← List<UiTask> を返す
        }

        // ファイルから読み込み → List<UiTask> を返す
        return file.readLines()
            .filter { it.isNotBlank() }
            .map { line ->
                val json = JSONObject(line)
                UiTask(
                    id = json.getLong("id"),
                    title = json.getString("title"),
                    status = json.getString("status"),
                    score = json.getInt("score"),
                    project = json.optString("project", "default"),
                    dueDate = if (json.has("dueDate")) json.getString("dueDate") else null,
                    tags = when (val raw = json.opt("tags")) {
                        is org.json.JSONArray -> {
                            (0 until raw.length())
                                .map { raw.optString(it) }
                                .filter { it.isNotBlank() }
                        }
                        is String -> {
                            raw.trim()
                                .removePrefix("[")
                                .removeSuffix("]")
                                .split(",", " ")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                        }
                        else -> emptyList()
                    }

                )
            }  // ← .toMutableList() を削除
    }

    // ✅ 引数は List<UiTask> のまま
    fun save(context: Context, tasks: List<UiTask>) {
        val file = File(context.filesDir, FILE_NAME)
        file.printWriter().use { out ->
            tasks.forEach { task ->
                val json = JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("status", task.status)
                    put("score", task.score)
                    put("project", task.project)
                    if (task.dueDate != null) {
                        put("dueDate", task.dueDate)
                    }
                    if (task.tags.isNotEmpty()) {
                        put("tags", task.tags)
                    }
                }
                out.println(json.toString())
            }
        }
    }

    // ✅ シードデータも List<UiTask> を返す
    private fun createSeedData(): List<UiTask> {
        return listOf(
            UiTask(1L, "TEST2", "todo", 0),
            UiTask(2L, "TEST3", "todo", 0),
        )
    }
}