package com.example.ai_task_android.api

import com.example.ai_task_android.model.UiTask
import kotlinx.coroutines.delay

class FakeScoreClient : GptScoreClient {
    override suspend fun scoreTasks(tasks: List<UiTask>): Map<Long, Int> {
        delay(300) // “それっぽい待ち”
        return tasks.associate { it.id to (it.score + 1) } // 仮：全部+1
    }
}
