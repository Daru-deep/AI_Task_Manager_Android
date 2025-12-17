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
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.storage.TaskJsonlStorage
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import com.example.ai_task_android.api.TaskSyncClient


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TaskCoreNeonTheme {
                LaunchedEffect(Unit) {
                    TaskSyncClient("http://10.0.2.2:5000").fetchTasks()
                }

                // ① State: jsonlから読み込み（Single Source of Truth）
                var tasks: List<UiTask> by remember {
                    mutableStateOf(TaskJsonlStorage.load(this@MainActivity))
                }


                // ② スコアラー（今はFake、将来OpenAI）
                val scorer = remember { FakeScoreClient() }

                // ③ コルーチンスコープ（ボタンから呼ぶ用）
                val scope = rememberCoroutineScope()

                // ④ スコア更新処理
                val refreshScores: () -> Unit = {
                    scope.launch {
                        // スコア計算（非同期）
                        val scoreMap = scorer.scoreTasks(tasks)

                        // tasksを更新
                        tasks = tasks.map { task ->
                            val newScore = scoreMap[task.id]
                            if (newScore != null && newScore != task.score) {
                                task.copy(score = newScore)
                            } else {
                                task
                            }
                        }

                        // jsonlに保存
                        TaskJsonlStorage.save(this@MainActivity, tasks)
                    }
                }

                // ⑤ 完了/戻す処理
                val onToggleDone: (Long) -> Unit = { taskId ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(
                                status = if (task.status == "todo") "done" else "todo"
                            )
                        } else {
                            task
                        }
                    }

                    // jsonlに保存
                    TaskJsonlStorage.save(this@MainActivity, tasks)
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Neon.Bg) {
                    TaskScreen(
                        tasks = tasks,
                        onToggleDone = onToggleDone,
                        onRefreshScores = refreshScores
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
                onDoneClick = onToggleDone
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


