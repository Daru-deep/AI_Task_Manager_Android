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
                                score = 0,
                                project = dto.project ?: "default",
                                tags = dto.tags ?: emptyList()
                            )
                        }
                    } catch (e: Exception) {
                        // エラー時はローカルのデータを使う
                        println("サーバー取得失敗: ${e.message}")
                    }
                }
                
                // ③ スコアラー（Fake）
                val scorer = remember { FakeScoreClient() }
                
                // ④ コルーチンスコープ
                val scope = rememberCoroutineScope()
                
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
                                        onRefreshScores = refreshScores,
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
                        text = { Text(task.title) }
                    )
                }

            }
        }
    }
}

@Composable
fun TaskScreen(
    tasks: List<UiTask>,
    onToggleDone: (Long) -> Unit,
    onRefreshScores: () -> Unit,
    onTaskClick: (UiTask) -> Unit
) {

    // おすすめタスクを算出（未完了・スコア順・最大3件）
    val recommendedTasks = remember(tasks) {
        tasks
            .filter { it.status != "done" }
            .sortedByDescending { it.score }
            .take(3)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neon.Bg)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 優先度更新ボタン
            OutlinedButton(
                onClick = onRefreshScores,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Neon.Border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Neon.Text
                )
            ) {
                Text("優先度更新", style = MaterialTheme.typography.bodyLarge)
            }
            
            // おすすめタスク
            TaskListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                tasks = recommendedTasks,
                onDoneClick = onToggleDone,
                onTaskClick = onTaskClick
            )


            // フッターメッセージ
            FooterMessage(
                text = "今日も一緒に頑張ろう！",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TaskListCard(
    modifier: Modifier = Modifier,
    tasks: List<UiTask>,
    onDoneClick: (Long) -> Unit,
    onTaskClick: (UiTask) -> Unit
)
 {
    Card(
        modifier = modifier.heightIn(min = 220.dp),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Neon.Border),
        colors = CardDefaults.cardColors(containerColor = Neon.Panel)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Text(
                text = "今日のおすすめ",
                color = Neon.Text,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(10.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "すべてのタスクが完了しました！",
                        color = Neon.TextDim,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text("DEBUG tasks size = ${tasks.size}")

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onDoneClick = onDoneClick,
                            onClick = { onTaskClick(task) }
                        )



                    }
                }

            }
        }
    }
}

@Composable
fun TaskRow(
    task: UiTask,
    onDoneClick: (Long) -> Unit,
    onClick: () -> Unit
) {

    val isDone = task.status == "done"
    val rowBorder = if (isDone) Neon.BorderDim else Neon.Border

    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, rowBorder),
        colors = CardDefaults.cardColors(containerColor = Neon.RowBg)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.id.toString(),
                color = Neon.TextDim,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (isDone) Neon.TextDim else Neon.Text,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pill(text = "score ${task.score}")
                    
                    // タグがあれば表示
                    task.tags.forEach { tag ->
                        Pill(text = tag)
                    }
                }
            }

            OutlinedButton(
                onClick = { onDoneClick(task.id) },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Neon.Border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isDone) Neon.Purple.copy(alpha = 0.25f) else Color.Transparent,
                    contentColor = Neon.Text
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(text = if (isDone) "戻す" else "完了")
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
private object Neon {
    val Bg = Color(0xFF12041F)
    val Panel = Color(0xFF1A0830)
    val RowBg = Color(0xFF160727)

    val Pink = Color(0xFFFF3EA5)
    val Purple = Color(0xFF8B5CF6)
    val Cyan = Color(0xFF22D3EE)

    val Border = Color(0xFFFF3EA5)
    val BorderDim = Color(0x66FF3EA5)

    val Text = Color(0xFFF5E9FF)
    val TextDim = Color(0xFFC7A9E6)

    val PillBg = Color(0x2216A5FF)
}

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
