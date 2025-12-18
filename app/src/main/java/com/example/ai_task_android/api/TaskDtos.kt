package com.example.ai_task_android.api

data class TasksResponse(
    val success: Boolean,
    val tasks: List<TaskDto>
)

data class TaskDto(
    val id: Int,
    val text: String,
    val status: String,
    val project: String? = null,
    val created_at: String? = null,
    val completed_at: String? = null,
    val tags: List<String>? = null
)
