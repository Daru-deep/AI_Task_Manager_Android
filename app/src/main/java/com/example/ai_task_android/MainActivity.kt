package com.example.ai_task_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ai_task_android.api.FakeScoreClient
import com.example.ai_task_android.api.TaskSyncClient
import com.example.ai_task_android.api.TasksResponse
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.storage.TaskJsonlStorage
import com.google.gson.Gson
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.clickable
import com.example.ai_task_android.screen.DiaryScreen
import com.example.ai_task_android.ui.theme.Neon
import com.example.ai_task_android.ui.task.TaskListCard
import com.example.ai_task_android.ui.task.TaskScreen



enum class Screen { Tasks, Diary }

private const val BASE_URL = "http://10.0.2.2:5000"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {


            TaskCoreNeonTheme {
                // ① State: Single Source of Truth
                var tasks by remember { 
                    mutableStateOf(TaskJsonlStorage.load(this@MainActivity))
                }

                var selectedTask by remember { mutableStateOf<UiTask?>(null) }
                var syncStatus by remember { mutableStateOf("未同期") }

                val fetchFromServer: suspend () -> Unit = {
                    try {
                        syncStatus = "同期中..."
                        val json = TaskSyncClient(BASE_URL).fetchTasksJson()
                        val resp = Gson().fromJson(json, TasksResponse::class.java)

                        val newTasks = resp.tasks.map { dto ->
                            UiTask(
                                id = dto.id.toLong(),
                                title = dto.text,
                                status = dto.status,
                                score = dto.score ?: 0,
                                reason = dto.reason,
                                project = dto.project ?: "default",
                                tags = dto.tags ?: emptyList()
                            )
                        }

                        tasks = newTasks
                        TaskJsonlStorage.save(this@MainActivity, tasks) // キャッシュとして保存
                        syncStatus = "同期OK (${tasks.size})"
                    } catch (e: Exception) {
                        syncStatus = "同期失敗（ローカル）"
                        Log.e("TaskSync", "サーバー取得失敗: ${e.message}", e)
                        tasks = TaskJsonlStorage.load(this@MainActivity)
                    }
                }



                // ② サーバーからタスクを取得
                LaunchedEffect(Unit) {
                    try {
                        val json = TaskSyncClient(BASE_URL).fetchTasksJson()
                        Log.d("TaskSync", "fromServer json head=" + json.take(200))
                        val resp = Gson().fromJson(json, TasksResponse::class.java)
                        Log.d("TaskSync", "fromServer tasks count=" + resp.tasks.size)

                        tasks = resp.tasks.map { dto ->
                            UiTask(
                                id = dto.id.toLong(),
                                title = dto.text,
                                status = dto.status,
                                score = dto.score ?: 0,
                                reason = dto.reason,
                                project = dto.project ?: "default",
                                tags = dto.tags ?: emptyList()

                            )
                        }
                    } catch (e: Exception) {
                        Log.e("TaskSync", "サーバー取得失敗: ${e.message}", e)
                        tasks = TaskJsonlStorage.load(this@MainActivity)
                    }

                }
                
                // ③ スコアラー（Fake）
                val scorer = remember { FakeScoreClient() }
                
                // ④ コルーチンスコープ
                val scope = rememberCoroutineScope()

                // ★ 同期ボタン用（サーバーから再取得）
                val onSync: () -> Unit = {
                    scope.launch { fetchFromServer() }
                }

                // ⑤ スコア更新処理
                val refreshScores: () -> Unit = {
                    scope.launch {
                        val scoreMap = scorer.scoreTasks(tasks)
                        tasks = tasks.map { task ->
                            val newScore = scoreMap[task.id]
                            if (newScore != null) task.copy(score = newScore) else task
                        }
                        TaskJsonlStorage.save(this@MainActivity, tasks)
                    }
                }
                
                // ⑥ 完了/戻す処理
                val onToggleDone: (Long) -> Unit = { taskId ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(status = if (task.status == "todo") "done" else "todo")
                        } else {
                            task
                        }
                    }
                    TaskJsonlStorage.save(this@MainActivity, tasks)
                }

                var current by remember { mutableStateOf(Screen.Tasks) }

                Surface(modifier = Modifier.fillMaxSize(), color = Neon.Bg) {
                    Column(Modifier.fillMaxSize()) {

                        // 上：画面本体
                        Box(Modifier.weight(1f)) {
                            when (current) {
                                Screen.Tasks -> {
                                    TaskScreen(
                                        tasks = tasks,
                                        onToggleDone = onToggleDone,
                                        onSync = onSync,
                                        syncStatus = syncStatus,
                                        onTaskClick = { selectedTask = it }
                                    )


                                }
                                Screen.Diary -> {
                                    DiaryScreen(
                                        baseUrl = BASE_URL,
                                        onPosted = {
                                            // 投稿できたらタスク画面に戻す、くらいでOK
                                            current = Screen.Tasks
                                        }
                                    )
                                }
                            }
                        }

                        // 下：タブ
                        BottomTabs(
                            current = current,
                            onChange = { current = it }
                        )
                    }
                }


                selectedTask?.let { task ->
                    AlertDialog(
                        onDismissRequest = { selectedTask = null },
                        confirmButton = {
                            TextButton(onClick = { selectedTask = null }) { Text("閉じる") }
                        },
                        title = { Text("タスク詳細") },
                        text = {
                            Column {
                                Text(task.title)
                                Spacer(Modifier.height(8.dp))
                                Text("score: ${task.score}")
                                Text("tags: ${task.tags.joinToString(", ")}")
                            }
                        }

                    )
                }





            }


        }
    }
}









@Composable
fun Pill(text: String) {
    Surface(
        color = Neon.PillBg,
        contentColor = Neon.TextDim,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, Neon.BorderDim)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun FooterMessage(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Neon.Border),
        colors = CardDefaults.cardColors(containerColor = Neon.Panel)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Neon.Pink, Neon.Purple)),
                        shape = RoundedCornerShape(999.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("千紗", color = Color.White, fontWeight = FontWeight.Black)
            }

            Text(text = text, color = Neon.Text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/* -----------------------------
 *  Theme
 * ----------------------------- */


@Composable
fun TaskCoreNeonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Neon.Bg,
            surface = Neon.Panel,
            primary = Neon.Pink,
            secondary = Neon.Purple,
            tertiary = Neon.Cyan
        ),
        content = content
    )
}

@Composable
fun BottomTabs(
    current: Screen,
    onChange: (Screen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val tasksSelected = current == Screen.Tasks
        val diarySelected = current == Screen.Diary

        OutlinedButton(
            onClick = { onChange(Screen.Tasks) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (tasksSelected) Neon.Border else Neon.BorderDim),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (tasksSelected) Neon.Purple.copy(alpha = 0.18f) else Color.Transparent,
                contentColor = Neon.Text
            )
        ) { Text("タスク") }

        OutlinedButton(
            onClick = { onChange(Screen.Diary) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (diarySelected) Neon.Border else Neon.BorderDim),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (diarySelected) Neon.Purple.copy(alpha = 0.18f) else Color.Transparent,
                contentColor = Neon.Text
            )
        ) { Text("日誌") }
    }
}
