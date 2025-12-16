package com.example.ai_task_android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val project: String = "default",
    val dueText: String? = null,
    val score: Int = 0,
    val status: String = "todo",
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
