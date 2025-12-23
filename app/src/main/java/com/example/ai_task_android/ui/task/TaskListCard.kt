package com.example.ai_task_android.ui.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.ui.theme.Neon

@Composable
fun TaskListCard(
    modifier: Modifier = Modifier,
    tasks: List<UiTask>,
    onDoneClick: (Long) -> Unit,
    onTaskClick: (UiTask) -> Unit
) {
    val recommended = tasks
        .filter { it.status == "todo" }
        .sortedByDescending { it.score }
        .take(3)

    Card(
        modifier = modifier.heightIn(),
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

            if (recommended.isEmpty()) {
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
                // DEBUG は後で消してOK
                // Text("DEBUG tasks size = ${recommended.size}")

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(recommended, key = { it.id }) { task ->
                        TaskRowRecommended(
                            task = task,
                            onDoneClick = onDoneClick,
                            onClick = { onTaskClick(task) } // ← ここが正解
                        )
                    }
                }
            }
        }
    }
}
