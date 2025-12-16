package com.example.ai_task_android.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun observeAll(): Flow<List<TaskEntity>> = dao.observeAll()

    suspend fun add(task: TaskEntity): Long = dao.insert(task)

    suspend fun toggleDone(task: TaskEntity) {
        val newStatus = if (task.status == "todo") "done" else "todo"
        dao.update(task.copy(status = newStatus))
    }

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun ensureSeedData() {
        if (dao.countAll() > 0) return

        dao.insert(TaskEntity(title = "Pythonのタスク管理ツールのタグ機能を試す", project = "default", score = 15))
        dao.insert(TaskEntity(title = "テストタスク", project = "default", score = 15))
        dao.insert(TaskEntity(title = "今日やったことを纏める", project = "default", score = 0))
        dao.insert(TaskEntity(title = "doneの拡張", project = "default", score = 0))
        dao.insert(TaskEntity(title = "ベースの飾り方レイアウト案を3パターン書き出す", project = "room", dueText = "2025-12-03", score = 5))
    }
}
