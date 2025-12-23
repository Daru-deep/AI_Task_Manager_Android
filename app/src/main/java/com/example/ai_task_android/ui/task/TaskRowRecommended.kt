package com.example.ai_task_android.ui.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.ui.theme.Neon

@Composable
fun TaskRowRecommended(
    task: UiTask,
    onDoneClick: (Long) -> Unit,
    onClick: () -> Unit
) {
    val isDone = task.status == "done"
    val rowBorder = if (isDone) Neon.BorderDim else Neon.Border

        Card(
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, rowBorder),
            colors = CardDefaults.cardColors(containerColor = Neon.RowBg),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (isDone) Neon.TextDim else Neon.Text,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )


                if (!task.reason.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = task.reason!!,
                        color = Neon.TextDim,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }

            OutlinedButton(
                onClick = { onDoneClick(task.id) },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Neon.Border),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isDone) Neon.Purple.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = Neon.Text
                )
            ) {
                Text(text = if (isDone) "戻す" else "完了")
            }
        }
    }
}
