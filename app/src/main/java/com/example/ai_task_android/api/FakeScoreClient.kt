package com.example.ai_task_android.api

import com.example.ai_task_android.model.UiTask
import kotlinx.coroutines.delay

/**
 * テスト用のスコアリングクライアント
 *
 * 動作：
 * - 全タスクのスコアを +1 する
 * - 300ms の遅延を入れて「それっぽい待ち」を再現
 *
 * 使い方：
 * ```kotlin
 * val scorer = FakeScoreClient()
 * val scoreMap = scorer.scoreTasks(tasks)
 * ```
 */
class FakeScoreClient : GptScoreClient {
    override suspend fun scoreTasks(tasks: List<UiTask>): Map<Long, Int> {
        // APIを叩いてる感を出すための遅延
        delay(300)

        // 全タスクのスコアを +1 して返す（仮実装）
        return tasks.associate { task ->
            task.id to (task.score + 1)
        }
    }
}