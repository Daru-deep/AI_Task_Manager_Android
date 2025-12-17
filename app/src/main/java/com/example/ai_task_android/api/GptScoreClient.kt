package com.example.ai_task_android.api

import com.example.ai_task_android.model.UiTask

/**
 * タスクのスコアリングを行うクライアントのインターフェース
 *
 * 実装：
 * - FakeScoreClient: テスト用（全タスクのスコアを+1するだけ）
 * - OpenAiScoreClient: 本番用（GPT APIでスコアを計算）
 */
interface GptScoreClient {
    /**
     * タスクのリストを受け取り、各タスクの優先度スコアを計算
     *
     * @param tasks スコアを計算したいタスクのリスト
     * @return タスクID → 新しいスコア のマップ
     */
    suspend fun scoreTasks(tasks: List<UiTask>): Map<Long, Int>
}