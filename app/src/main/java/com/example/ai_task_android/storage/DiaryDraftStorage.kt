package com.example.ai_task_android.storage

import android.content.Context
import org.json.JSONObject

object DiaryDraftStorage {
    private const val FILE_NAME = "diary_drafts.jsonl"

    fun appendDraft(context: Context, body: String, health: Int) {
        val json = JSONObject().apply {
            put("body", body)
            put("health", health)
        }.toString()

        context.openFileOutput(FILE_NAME, Context.MODE_APPEND).use { fos ->
            fos.write((json + "\n").toByteArray(Charsets.UTF_8))
        }
    }
}
