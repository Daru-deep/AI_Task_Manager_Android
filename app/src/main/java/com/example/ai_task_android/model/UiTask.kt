package com.example.ai_task_android.model

data class UiTask(
    val id: Long,
    val title: String,
    val status: String,      // "todo" / "done"
    val score: Int,
    val project: String = "default",
    val dueDate: String? = null,          // "2025-12-31" みたいな文字列でOK
    val tags: List<String> = emptyList()
)
