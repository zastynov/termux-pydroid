package ru.zastynov.termuxpydroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF101113)
private val Glass = Color(0xCC202226)
private val Line = Color(0xFF414347)
private val White = Color(0xFFF0F0F0)
private val Muted = Color(0xFF96999E)
private val Accent = Color(0xFFB9BBC0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(this) }
    }
}

private enum class Page {
    Home, Python, Files, Terminal, Settings
}

@Composable
private fun App(context: Context) {
    var page by remember { mutableStateOf(Page.Home) }
    var code by remember {
        mutableStateOf(
            "print(\"Hello from TermuxPydroid!\")\n\nname = input(\"Your name: \")\nprint(f\"Hello, {name}!\")"
        )
    }
    var fileName by remember { mutableStateOf("main.py") }
    var command by remember { mutableStateOf("python --version") }
    var log by remember { mutableStateOf("Ready.") }
    var currentUri by remember { mutableStateOf<Uri?>(null) }

    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                code = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() } ?: ""
                currentUri = uri
                fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "main.py"
                page = Page.Python
            } catch (e: Exception) {
                toast(context, "Open error: ${e.message}")
            }
        }
    }

    val saveAs = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/x-python")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri, "wt")
                    ?.bufferedWriter()?.use { it.write(code) }
                currentUri = uri
                toast(context, "File saved")
            } catch (e: Exception) {
                toast(context, "Save error: ${e.message}")
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            background = Bg,
            surface = Glass,
            onSurface = White
        )
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    listOf(Color(0xFF1C1E21), Bg, Color(0xFF25272A))
                )
            )
        ) {
            Column(
                Modifier.fillMaxSize().padding(bottom = 94.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("TERMUX", color = Muted, fontSize = 10.sp, letterSpacing = 3.sp)
                        Text(
                            "PYDROID",
                            color = White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(Icons.Default.Code, null, tint = Accent, modifier = Modifier.size(30.dp))
                }

                when (page) {
                    Page.Home -> {
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Panel {
                                Text(
                                    "Your mobile workspace",
                                    color = White,
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Python, files and terminal tools in one place.",
                                    color = Muted
                                )
                                Spacer(Modifier.height(18.dp))
                                Button(
                                    onClick = { page = Page.Python },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Accent,
                                        contentColor = Bg
                                    )
                                ) {
                                    Icon(Icons.Default.Code, null)
                                    Text("  Open editor")
                                }
                            }
                            ActionCard("Python editor", "Write and run Python", Icons.Default.Code) {
                                page = Page.Python
                            }
                            ActionCard("File manager", "Open a file from your device", Icons.Default.FolderOpen) {
                                openFile.launch(arrayOf("*/*"))
                            }
                            ActionCard("Terminal", "Run commands using Termux", Icons.Default.Terminal) {
                                page = Page.Terminal
                            }
                            Panel {
                                Text("Environment", color = White, fontWeight = FontWeight.SemiBold)
                                Info("Interface", "Frosted OS")
                                Info("Accent", "Graphite gray")
                                Info("Runtime", "Termux integration")
                            }
                        }
                    }

                    Page.Python -> {
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(fileName, color = White, fontSize = 20.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { openFile.launch(arrayOf("*/*")) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Open") }
                                OutlinedButton(
                                    onClick = {
                                        val uri = currentUri
                                        if (uri == null) {
                                            saveAs.launch(fileName)
                                        } else {
                                            try {
                                                context.contentResolver.openOutputStream(uri, "wt")
                                                    ?.bufferedWriter()?.use { it.write(code) }
                                                toast(context, "Saved")
                                            } catch (e: Exception) {
                                                toast(context, "Save error: ${e.message}")
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Icon(Icons.Default.Save, null); Text(" Save") }
                                Button(
                                    onClick = {
                                        runPython(context, code)
                                        log = "Code sent to Termux. Check its session for output."
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Accent,
                                        contentColor = Bg
                                    )
                                ) { Icon(Icons.Default.PlayArrow, null); Text(" Run") }
                            }
                            Panel(Modifier.weight(1f).fillMaxWidth()) {
                                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
                                    Text(
                                        (1..maxOf(1, code.count { it == '\n' } + 1))
                                            .joinToString("\n"),
                                        color = Muted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    BasicTextField(
                                        value = code,
                                        onValueChange = { code = it },
                                        modifier = Modifier.fillMaxSize(),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp
                                        ),
                                        cursorBrush = Brush.verticalGradient(listOf(Accent, Accent))
                                    )
                                }
                            }
                            Text(log, color = Muted, fontSize = 12.sp)
                        }
                    }

                    Page.Files -> {
                        Column(
                            Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Files", color = White, fontSize = 22.sp)
                            Panel {
                                Text("Open a file from Android storage.", color = Muted)
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { openFile.launch(arrayOf("*/*")) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Accent,
                                        contentColor = Bg
                                    )
                                ) { Icon(Icons.Default.FolderOpen, null); Text("  Browse files") }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = {
                                    fileName = "new_file.py"
                                    code = ""
                                    currentUri = null
                                    page = Page.Python
                                }) { Text("Create new Python file") }
                            }
                        }
                    }

                    Page.Terminal -> {
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Terminal", color = White, fontSize = 22.sp)
                            Panel {
                                Text("COMMAND", color = Muted, fontSize = 11.sp)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = command,
                                    onValueChange = { command = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        runShell(context, command)
                                        log = "Sent to Termux: $command"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Accent,
                                        contentColor = Bg
                                    )
                                ) { Text("Run command") }
                                OutlinedButton(onClick = { openTermux(context) }) {
                                    Text("Open Termux")
                                }
                            }
                            Panel(Modifier.fillMaxWidth()) {
                                Text("LOG", color = Muted, fontSize = 11.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(log, color = White, fontFamily = FontFamily.Monospace)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Output is currently shown in Termux, not streamed into this panel.",
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Page.Settings -> {
                        Column(
                            Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Settings", color = White, fontSize = 22.sp)
                            Panel {
                                Info("Theme", "Frosted OS / Industrial Dark")
                                Info("Accent", "Neutral gray")
                                Info("Python runtime", "Termux")
                                Info("Version", "0.2.0")
                            }
                            Button(onClick = { openTermux(context) }) {
                                Text("Open Termux")
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp)
                    .border(1.dp, Line, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xF01B1D20),
                shadowElevation = 12.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    NavButton("Home", Icons.Default.Home, page == Page.Home) { page = Page.Home }
                    NavButton("Python", Icons.Default.Code, page == Page.Python) { page = Page.Python }
                    NavButton("Files", Icons.Default.FolderOpen, page == Page.Files) { page = Page.Files }
                    NavButton("Terminal", Icons.Default.Terminal, page == Page.Terminal) { page = Page.Terminal }
                    NavButton("Settings", Icons.Default.Settings, page == Page.Settings) { page = Page.Settings }
                }
            }
        }
    }
}

@Composable
private fun Panel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.border(1.dp, Line, RoundedCornerShape(20.dp)),
        color = Glass,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(15.dp), content = content)
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Panel(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Info(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, color = Muted, modifier = Modifier.weight(1f))
        Text(value, color = White, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NavButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier.clickable { onClick() }.background(
            if (selected) Color(0xFF36383C) else Color.Transparent,
            RoundedCornerShape(14.dp)
        ).padding(horizontal = 7.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = if (selected) White else Muted, modifier = Modifier.size(20.dp))
        Text(label, color = if (selected) White else Muted, fontSize = 9.sp)
    }
}

private fun toast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

private fun openTermux(context: Context) {
    val launch = context.packageManager.getLaunchIntentForPackage("com.termux")
    if (launch != null) context.startActivity(launch)
    else context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/termux/termux-app/releases"))
    )
}

private fun runPython(context: Context, code: String) {
    val encoded = android.util.Base64.encodeToString(
        code.toByteArray(Charsets.UTF_8),
        android.util.Base64.NO_WRAP
    )
    runTermuxCommand(
        context,
        "/data/data/com.termux/files/usr/bin/sh",
        arrayOf("-lc", "echo '$encoded' | base64 -d | python")
    )
}

private fun runShell(context: Context, command: String) {
    runTermuxCommand(
        context,
        "/data/data/com.termux/files/usr/bin/sh",
        arrayOf("-lc", command)
    )
}

private fun runTermuxCommand(context: Context, path: String, args: Array<String>) {
    try {
        val intent = Intent("com.termux.RUN_COMMAND").apply {
            setClassName("com.termux", "com.termux.app.RunCommandService")
            putExtra("com.termux.RUN_COMMAND_PATH", path)
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args)
            putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
        }
        context.startService(intent)
        toast(context, "Command sent to Termux")
    } catch (e: Exception) {
        toast(context, "Termux error: ${e.message}")
    }
}              color = Accent,
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
