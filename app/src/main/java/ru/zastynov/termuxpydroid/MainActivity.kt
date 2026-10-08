
package ru.zastynov.termuxpydroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0B0D12)
private val Panel = Color(0xFF151923)
private val Accent = Color(0xFF8B7CFF)
private val Muted = Color(0xFF9299AA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Bg,
                    surface = Panel,
                    primary = Accent
                )
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var page by remember { mutableStateOf("Обзор") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(20.dp)
    ) {
        Text(
            "TermuxPydroid",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Мобильная среда разработки",
            color = Muted,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))

        Text(
            "РАБОЧЕЕ ПРОСТРАНСТВО",
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MenuCard(
                    "Python",
                    "Редактор кода и скрипты",
                    Icons.Default.Code
                ) { page = "Python" }
            }

            item {
                MenuCard(
                    "Терминал",
                    "Командная строка",
                    Icons.Default.Terminal
                ) { page = "Терминал" }
            }

            item {
                MenuCard(
                    "Проекты",
                    "Файлы и рабочие папки",
                    Icons.Default.FolderOpen
                ) { page = "Проекты" }
            }

            item {
                Spacer(Modifier.height(12.dp))

                Text(
                    page,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    when (page) {
                        "Python" ->
                            "Здесь будет редактор Python и запуск программ."
                        "Терминал" ->
                            "Здесь будет терминал с командами Linux."
                        "Проекты" ->
                            "Здесь будет список твоих проектов и файлов."
                        else ->
                            "Выбери раздел, чтобы начать работу."
                    },
                    color = Muted,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { page = "Обзор" }
                ) {
                    Text("На главную")
                }
            }
        }

        Text(
            "VERSION 0.1.0",
            color = Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Panel
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    subtitle,
                    color = Muted,
                    fontSize = 13.sp
                )
            }
        }
    }
}
 
