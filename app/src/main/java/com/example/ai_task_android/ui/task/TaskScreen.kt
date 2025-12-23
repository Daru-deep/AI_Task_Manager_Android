package com.example.ai_task_android.ui.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ai_task_android.FooterMessage
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.ui.theme.Neon

@Composable
fun TaskScreen(
    tasks: List<UiTask>,
    onToggleDone: (Long) -> Unit,
    onSync: () -> Unit,
    syncStatus: String,
    onTaskClick: (UiTask) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSync,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Neon.Border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Neon.Text
                    )
                ) {
                    Text("同期", style = MaterialTheme.typography.bodyLarge)
                }

                // ★ バッジ（同期状態）
                Surface(
                    color = Neon.Purple.copy(alpha = 0.18f),
                    contentColor = Neon.Text,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, Neon.BorderDim)
                ) {
                    Text(
                        text = syncStatus,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }


            // おすすめタスク
            TaskListCard(
                tasks = tasks,
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