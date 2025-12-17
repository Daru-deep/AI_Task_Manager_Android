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
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.storage.TaskJsonlStorage
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ai_task_android.api.FakeScoreClient
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()

            TaskCoreNeonTheme {

                // ① State：jsonlから読み込み
                var tasks by remember { mutableStateOf(TaskJsonlStorage.load(this@MainActivity)) }

                // ② スコアラー（今はFake）
                val scorer = remember { FakeScoreClient() }

                // ③ コルーチンスコープ（ボタンから呼ぶ用）
                val scope = rememberCoroutineScope()

                // ④ スコア更新処理（suspendでもOKだけど、呼び出し側でlaunchする）
                suspend fun refreshScores() {
                    val scoreMap = scorer.scoreTasks(tasks)

                    val updated = tasks.map { t ->
                        val newScore = scoreMap[t.id]
                        if (newScore != null && newScore != t.score) t.copy(score = newScore) else t
                    }

                    tasks = updated
                    TaskJsonlStorage.save(this@MainActivity, tasks)
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Neon.Bg) {
                    TaskScreen(
                        tasks = tasks,                          // ← ここ重要：UIはStateの写像
                        onToggleDone = { id ->
                            tasks = tasks.map { t ->
                                if (t.id == id) t.copy(status = if (t.status == "done") "todo" else "done") else t
                            }
                            TaskJsonlStorage.save(this@MainActivity, tasks)
                        },
                        onRefreshScores = {
                            scope.launch { refreshScores() }     // ← ボタンから呼ぶ
                        }
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
    onRefreshScores: () -> Unit
) {
    OutlinedButton(
        onClick = { scope.launch { refreshScores() } }

    }) { Text("優先度更新") }


    // Load tasks from jsonl on initial composition
    var tasks by remember {
        mutableStateOf(TaskJsonlStorage.load(context))
    }


    // Calculate recommended tasks (top 3 incomplete tasks sorted by score)
    val recommendedTasks = remember(tasks) {
        tasks
            .filter { it.status != "done" }
            .sortedByDescending { it.score }
            .take(3)
    }

    // Handler for task completion
    val onDoneClick: (Long) -> Unit = { taskId ->
        // Update task status
        tasks = tasks.map { task ->
            if (task.id == taskId) {
                task.copy(status = if (task.status == "todo") "done" else "todo")
            } else {
                task
            }
        }.toMutableList()

        // Save to jsonl
        TaskJsonlStorage.save(context, tasks)
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
            // Top: Recommended tasks
            TaskListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                tasks = recommendedTasks,
                onDoneClick = onDoneClick
            )

            // Bottom: Footer message
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
    onDoneClick: (Long) -> Unit
) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(task = task, onDoneClick = onDoneClick)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    task: UiTask,
    onDoneClick: (Long) -> Unit
) {
    val isDone = task.status == "done"
    val rowBorder = if (isDone) Neon.BorderDim else Neon.Border

    Card(
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