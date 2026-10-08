
package ru.zastynov.termuxpydroid

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF101113)
private val SurfaceGlass = Color(0xC8202225)
private val SurfaceDeep = Color(0xE0191B1E)
private val BorderGlass = Color(0xFF414347)
private val TextMain = Color(0xFFF0F0F0)
private val TextMuted = Color(0xFF96999E)
private val Accent = Color(0xFFB9BBC0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Background,
                    surface = SurfaceDeep,
                    primary = Accent,
                    onPrimary = Background,
                    onBackground = TextMain,
                    onSurface = TextMain,
                    outline = BorderGlass
                )
            ) {
                AppScreen()
            }
        }
    }
}

@Composable
private fun AppScreen() {
    var page by remember { mutableStateOf("Обзор") }
    var code by remember {
        mutableStateOf(
            "name = input(\"Как тебя зовут? \")\n" +
            "print(f\"Привет, {name}!\")\n" +
            "print(\"Python работает!\")"
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF202124),
                        Background,
                        Color(0xFF0B0C0D)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 100.dp)
        ) {
            Header()

            Spacer(Modifier.height(26.dp))

            when (page) {
                "Python" -> PythonPage(
                    code = code,
                    onCodeChange = { code = it },
                    onRun = { runPython(context, code) },
                    onReset = {
                        code = "print(\"Hello, world!\")"
                    }
                )

                "Терминал" -> TerminalPage(
                    onOpen = { openTermux(context) }
                )

                "Проекты" -> ProjectsPage()

                "Настройки" -> SettingsPage()

                else -> HomePage(
                    onNavigate = { page = it }
                )
            }
        }

        FloatingNavigation(
            currentPage = page,
            onNavigate = { page = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "TERMUXPYDROID",
                color = TextMain,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "MOBILE DEVELOPMENT ENVIRONMENT",
                color = TextMuted,
                fontSize = 9.sp,
                letterSpacing = 1.1.sp
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(SurfaceGlass, CircleShape)
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = "Код",
                tint = Accent,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun HomePage(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "РАБОЧЕЕ ПРОСТРАНСТВО",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(14.dp))

        GlassPanel {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(Color(0xFFAAAAAA), CircleShape)
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "DEV ENVIRONMENT",
                        color = TextMuted,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    "Пиши. Запускай. Создавай.",
                    color = TextMain,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Python и инструменты Termux в одном месте.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onNavigate("Python") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = Color(0xFF171819)
                    )
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Открыть редактор")
                }
            }
        }

        Spacer(Modifier.height(25.dp))

        Text(
            "ИНСТРУМЕНТЫ",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(12.dp))

        ToolCard(
            title = "Python Editor",
            subtitle = "Редактирование и запуск скриптов",
            icon = Icons.Default.Code,
            tag = "PY"
        ) {
            onNavigate("Python")
        }

        Spacer(Modifier.height(10.dp))

        ToolCard(
            title = "Terminal",
            subtitle = "Команды и пакеты через Termux",
            icon = Icons.Default.Terminal,
            tag = "CLI"
        ) {
            onNavigate("Терминал")
        }

        Spacer(Modifier.height(10.dp))

        ToolCard(
            title = "Projects",
            subtitle = "Рабочее пространство проектов",
            icon = Icons.Default.FolderOpen,
            tag = "FILES"
        ) {
            onNavigate("Проекты")
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "VERSION 0.2.0  •  GRAPHITE INTERFACE",
            color = Color(0xFF696C71),
            fontSize = 9.sp,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun PythonPage(
    code: String,
    onCodeChange: (String) -> Unit,
    onRun: () -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Python Editor",
                    color = TextMain,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Редактор исходного кода",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Text(
                "PY",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(18.dp))

        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x552D2F32))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .background(Accent, CircleShape)
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "main.py",
                        color = TextMain,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "PYTHON",
                        color = TextMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }

                HorizontalDivider(color = BorderGlass.copy(alpha = 0.55f))

                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    textStyle = TextStyle(
                        color = Color(0xFFE0E1E3),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(Accent),
                    decorationBox = { innerTextField ->
                        Box {
                            if (code.isEmpty()) {
                                Text(
                                    "Введи Python-код...",
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = onRun,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Color(0xFF171819)
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "Запустить Python",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGlass),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextMuted
            )
        ) {
            Text("Сбросить пример")
        }
    }
}

@Composable
private fun TerminalPage(onOpen: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Terminal",
            color = TextMain,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Системная оболочка Termux",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(20.dp))

        GlassPanel {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "SHELL / ANDROID",
                    color = TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    "$ pkg update",
                    color = Color(0xFFE0E1E3),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "$ python --version",
                    color = Color(0xFFE0E1E3),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Команды выполняются в установленном Termux.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Color(0xFF171819)
            )
        ) {
            Icon(Icons.Default.Terminal, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Открыть Termux")
        }
    }
}

@Composable
private fun ProjectsPage() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Projects",
            color = TextMain,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Твои рабочие пространства",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(20.dp))

        GlassPanel {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    "Пока пусто",
                    color = TextMain,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Управление файлами и сохранение проектов добавим следующим этапом.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsPage() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Настройки",
            color = TextMain,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(18.dp))

        GlassPanel {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "ВНЕШНИЙ ВИД",
                    color = TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                )

                Spacer(Modifier.height(14.dp))

                SettingRow("Тема", "Frosted OS × Industrial Dark")
                HorizontalDivider(
                    color = BorderGlass.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                SettingRow("Акцент", "Нейтральный серый")
                HorizontalDivider(
                    color = BorderGlass.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                SettingRow("Прозрачность", "Средняя")
            }
        }

        Spacer(Modifier.height(14.dp))

        GlassPanel {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "О ПРИЛОЖЕНИИ",
                    color = TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(12.dp))
                SettingRow("TermuxPydroid", "Версия 0.2.0")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Android development environment",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, value: String) {
    Column {
        Text(title, color = TextMain, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Color(0xFF303236).copy(alpha = 0.75f),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Text(
                tag,
                color = TextMuted,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(19.dp),
        color = SurfaceGlass,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFF55575B),
                    Color(0x663B3D40),
                    Color(0xFF292B2E)
                )
            )
        ),
        content = {
            Column(content = content)
        }
    )
}

@Composable
private fun FloatingNavigation(
    currentPage: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("Обзор", Icons.Default.Home, "Home"),
        Triple("Python", Icons.Default.Code, "Code"),
        Triple("Терминал", Icons.Default.Terminal, "Terminal"),
        Triple("Проекты", Icons.Default.FolderOpen, "Files"),
        Triple("Настройки", Icons.Default.Settings, "Settings")
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xE6242629),
        border = BorderStroke(1.dp, Color(0xFF484A4E)),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (label, icon, _) ->
                val selected = currentPage == label

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(label) }
                        .background(
                            if (selected) Color(0xFF414347).copy(alpha = 0.7f)
                            else Color.Transparent,
                            RoundedCornerShape(15.dp)
                        )
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) TextMain else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = label,
                        color = if (selected) TextMain else TextMuted,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun runPython(context: Context, code: String) {
    if (code.isBlank()) {
        Toast.makeText(
            context,
            "Сначала введи Python-код",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val intent = Intent("com.termux.RUN_COMMAND").apply {
        component = ComponentName(
            "com.termux",
            "com.termux.app.RunCommandService"
        )
        putExtra(
            "com.termux.RUN_COMMAND_PATH",
            "/data/data/com.termux/files/usr/bin/python"
        )
        putExtra(
            "com.termux.RUN_COMMAND_ARGUMENTS",
            arrayOf("-c", code)
        )
        putExtra(
            "com.termux.RUN_COMMAND_WORKDIR",
            "/data/data/com.termux/files/home"
        )
        putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
        putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
    }

    try {
        context.startService(intent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Не удалось запустить Termux. Проверь установку и разрешения.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun openTermux(context: Context) {
    val intent = context.packageManager
        .getLaunchIntentForPackage("com.termux")

    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(
            context,
            "Сначала установи Termux",
            Toast.LENGTH_LONG
        ).show()
    }
}
