package com.example.ai_task_android.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ai_task_android.api.DiaryPostClient
import com.example.ai_task_android.storage.DiaryDraftStorage
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun DiaryScreen(
    baseUrl: String,
    onPosted: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var body by remember { mutableStateOf("") }
    var health by remember { mutableIntStateOf(3) }
    var msg by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val client = remember(baseUrl) { DiaryPostClient(baseUrl) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("日誌投稿", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("本文") },
            modifier = Modifier.fillMaxWidth().height(240.dp)
        )

        Text("体調スコア: $health（0〜5）")
        Slider(
            value = health.toFloat(),
            onValueChange = { health = it.toInt() },
            valueRange = 0f..5f,
            steps = 4
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                enabled = !busy && body.isNotBlank(),
                onClick = {
                    busy = true
                    msg = null
                    scope.launch {
                        val ok = client.postDiary(body.trim(), health)
                        if (ok) {
                            msg = "送信完了"
                            body = ""
                            busy = false
                            onPosted()
                        } else {
                            DiaryDraftStorage.appendDraft(ctx, body.trim(), health)
                            msg = "送信失敗：下書きに保存しました"
                            busy = false
                        }
                    }
                }
            ) { Text(if (busy) "送信中…" else "送信") }

            OutlinedButton(
                enabled = !busy,
                onClick = {
                    // 送信せず閉じたい時用
                    onPosted()
                }
            ) { Text("戻る") }
        }

        msg?.let { Text(it) }
    }
}
