package com.example.ai_task_android.api

import com.example.ai_task_android.model.UiTask

interface GptScoreClient {
    suspend fun scoreTasks(tasks: List<UiTask>): Map<Long, Int>
}
