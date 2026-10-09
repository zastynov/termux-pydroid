
package ru.zastynov.termuxpydroid

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

private val Background = Color(0xFF101114)
private val Panel = Color(0xFF191B20)
private val Accent = Color(0xFFD0D2DA)
private val TextColor = Color(0xFFE9EAF0)
private val Muted = Color(0xFF9295A0)
private val Green = Color(0xFF9DCEA8)
private val Red = Color(0xFFFF9696)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Accent,
                    background = Background,
                    surface = Panel,
                    onPrimary = Background,
                    onBackground = TextColor,
                    onSurface = TextColor
                )
            ) {
                InternalTerminal()
            }
        }
    }
}

private class TerminalSession(private val directory: File) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val ids = AtomicInteger(0)

    @Volatile
    private var process: Process? = null

    @Volatile
    private var writer: BufferedWriter? = null

    @Volatile
    var busy: Boolean = false
        private set

    @Volatile
    private var closed = false

    fun start(
        onOutput: (String) -> Unit,
        onFinished: (Int) -> Unit
    ) {
        try {
            directory.mkdirs()

            val shell = ProcessBuilder("/system/bin/sh")
                .directory(directory)
                .redirectErrorStream(true)
                .start()

            process = shell
            writer = BufferedWriter(
                OutputStreamWriter(shell.outputStream)
            )

            mainHandler.post {
                onOutput("TermuxPydroid internal shell")
                onOutput("Android system shell: /system/bin/sh")
                onOutput("Рабочая папка: ${directory.absolutePath}")
                onOutput("Введи help, чтобы увидеть примеры команд.")
                onOutput("")
            }

            thread(name = "internal-shell-reader", isDaemon = true) {
                try {
                    BufferedReader(
                        InputStreamReader(shell.inputStream)
                    ).use { reader ->
                        while (!closed) {
                            val line = reader.readLine() ?: break
                            val marker = Regex(
                                "__TPD_DONE_(\\d+)__=(-?\\d+)"
                            ).find(line)

                            if (marker != null) {
                                val prefix = line.substring(
                                    0,
                                    marker.range.first
                                )

                                if (prefix.isNotEmpty()) {
                                    mainHandler.post {
                                        onOutput(prefix)
                                    }
                                }

                                val exitCode =
                                    marker.groupValues[2].toIntOrNull() ?: -1

                                busy = false

                                mainHandler.post {
                                    onFinished(exitCode)
                                }
                            } else {
                                mainHandler.post {
                                    onOutput(line)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!closed) {
                        mainHandler.post {
                            onOutput("Ошибка чтения: ${e.message}")
                        }
                    }
                } finally {
                    busy = false
                    if (!closed) {
                        mainHandler.post {
                            onOutput("Shell завершился.")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            mainHandler.post {
                onOutput("Не удалось запустить shell: ${e.message}")
            }
        }
    }

    @Synchronized
    fun execute(command: String): Boolean {
        if (closed || process?.isAlive != true || busy) {
            return false
        }

        val id = ids.incrementAndGet()
        busy = true

        return try {
            val outputWriter = writer
                ?: throw IllegalStateException("Shell не готов")

            outputWriter.write(command)
            outputWriter.newLine()
            outputWriter.write("echo __TPD_DONE_${id}__=$?")
            outputWriter.newLine()
            outputWriter.flush()
            true
        } catch (e: Exception) {
            busy = false
            false
        }
    }

    @Synchronized
    fun close() {
        closed = true
        try {
            writer?.close()
        } catch (_: Exception) {
        }
        process?.destroy()
        process = null
        writer = null
    }
}

@androidx.compose.runtime.Composable
private fun InternalTerminal() {
    val context = LocalContext.current
    val terminalDirectory = remember {
        File(context.filesDir, "terminal")
    }

    val session = remember {
        TerminalSession(terminalDirectory)
    }

    val output = remember {
        mutableStateListOf<String>()
    }

    var command by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    DisposableEffect(session) {
        session.start(
            onOutput = { line ->
                output.add(line)
            },
            onFinished = { exitCode ->
                busy = false
                output.add("[exit code: $exitCode]")
                output.add("")
            }
        )

        onDispose {
            session.close()
        }
    }

    LaunchedEffect(output.size) {
        if (output.isNotEmpty()) {
            listState.animateScrollToItem(output.lastIndex)
        }
    }

    fun runCommand() {
        val entered = command.trimEnd()
        if (entered.isBlank() || busy) return

        output.add("$ $entered")
        command = ""

        busy = true

        if (!session.execute(entered)) {
            busy = false
            output.add("Не удалось выполнить команду.")
            output.add("")
        }
    }

    Scaffold(
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Terminal",
                        color = TextColor,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Внутренняя оболочка Android",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }

                Text(
                    if (busy) "Выполняется" else "Готов",
                    color = if (busy) Accent else Green,
                    fontSize = 12.sp
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF08090B)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(output) { _, line ->
                        Text(
                            text = line.ifEmpty { " " },
                            color = when {
                                line.startsWith("$ ") -> Green
                                line.startsWith("[exit code:") ->
                                    if (line.endsWith("0]")) Green else Red
                                line.startsWith("Ошибка") -> Red
                                else -> TextColor
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    if (busy) {
                        item {
                            Text(
                                "Выполняется команда…",
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
                singleLine = true,
                label = { Text("Команда") },
                placeholder = { Text("pwd, ls, mkdir test…") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { runCommand() }
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { runCommand() },
                    enabled = !busy && command.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = Background
                    )
                ) {
                    Text("Выполнить")
                }

                Button(
                    onClick = {
                        output.clear()
                        output.add("Экран очищен.")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Panel,
                        contentColor = TextColor
                    )
                ) {
                    Text("Очистить")
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "Команды выполняются в папке приложения. " +
                    "Python и pip не встроены в системный shell.",
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}
