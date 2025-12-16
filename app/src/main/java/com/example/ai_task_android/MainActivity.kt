package com.example.ai_task_android


import androidx.compose.runtime.*
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.storage.TaskJsonlStorage
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskCoreNeonTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Neon.Bg) {
                    TaskScreen()
                }
            }
        }
    }
}

/* -----------------------------
 *  仮データ（Room前のMVP）
 * ----------------------------- */
data class UiTask(
    val dbId: Long,
    val title: String,
    val project: String,
    val dueText: String?,
    val score: Int,
    val status: String
)


@Composable
fun TaskScreen() {
    // 仮のタスク一覧（あとでRoom+ViewModelに差し替える）
    val tasks = remember {
        mutableStateListOf(
            UiTask(4, "Pythonのタスク管理ツールのタグ機能を試す", "default", null, 15, "todo"),
            UiTask(11, "テストタスク", "default", null, 15, "todo"),
            UiTask(1, "Pythonのタスク管理ツールを試す", "default", null, 0, "todo"),
            UiTask(2, "今日やったことを纏める", "default", null, 0, "todo"),
            UiTask(3, "doneの拡張", "default", null, 0, "todo"),
            UiTask(5, "ゲーム業界への質問を考える", "default", null, 0, "todo"),
            UiTask(9, "ベースの飾り方のレイアウト案を3パターン書き出す", "room", "2025-12-03（締切から9日経過）", 0, "todo"),
        )
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
            // ① 一番上：おすすめタスク
            TaskListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                tasks = tasks,
                onDoneClick = { id ->
                    val idx = tasks.indexOfFirst { it.dbId == id }
                    if (idx >= 0) {
                        val t = tasks[idx]
                        tasks[idx] = t.copy(status = if (t.status == "todo") "done" else "todo")
                    }
                }
            )

            // ② 下：千紗の一言（コメント）
            FooterMessage(
                text = "今日も一緒に頑張ろう！",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ChisaCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    message: String
) {
    Card(
        modifier = modifier.heightIn(min = 220.dp),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Neon.Border),
        colors = CardDefaults.cardColors(containerColor = Neon.Panel)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Neon.Pink, Neon.Purple, Neon.Cyan)),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Text(
                text = subtitle,
                color = Neon.TextDim,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = message,
                color = Neon.Text,
                style = MaterialTheme.typography.bodyLarge
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
            val topTasks = remember(tasks) { tasks.take(3) }
            Text(
                text = "今日のおすすめ",
                color = Neon.Text,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(topTasks, key = { it.dbId}) { task ->
                    TaskRow(task = task, onDoneClick = { onDoneClick(task.dbId) })
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
                text = task.dbId.toString(),
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
                    Pill(text = task.project)
                    Pill(text = "score ${task.score}")
                    task.dueText?.let { Pill(text = it) }
                }
            }

            OutlinedButton(
                onClick = { onDoneClick(task.dbId) },
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
 *  テーマ（最小）
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

    val PillBg = Color(0x2216A5FF) // ほんのり青紫
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
