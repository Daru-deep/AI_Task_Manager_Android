package com.example.ai_task_android.ui.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ai_task_android.Pill
import com.example.ai_task_android.model.UiTask
import com.example.ai_task_android.ui.theme.Neon

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
                task.reason?.takeIf { it.isNotBlank() }?.let { r ->
                    Text(
                        text = r,
                        style = MaterialTheme.typography.bodySmall,
                        color = Neon.TextDim,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }


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