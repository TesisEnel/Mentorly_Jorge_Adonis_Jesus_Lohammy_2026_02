package com.sagrd.mentorly.presentation.theme.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.progress.EnrollmentThemeProgress
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private val PrimaryBlue = Color(0xFF1565C0)
private val DarkerBlue = Color(0xFF0D47A1)
private val CompletedGreen = Color(0xFF2E7D32)
private val CompletedGreenBg = Color(0xFFE8F5E9)
private val CodeBlockBg = Color(0xFFF1F5F9)
private val CodeHeaderBg = Color(0xFFE2E8F0)

@Composable
fun ThemeDetailScreen(
    enrollmentId: String,
    themeId: String,
    onBackClick: () -> Unit,
    viewModel: ThemeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(enrollmentId, themeId) {
        viewModel.onEvent(ThemeDetailUiEvent.LoadTheme(enrollmentId, themeId))
    }

    ThemeDetailBody(
        uiState = uiState,
        onBackClick = onBackClick,
        onCompleteTheme = { viewModel.onEvent(ThemeDetailUiEvent.CompleteTheme) },
        onRetry = { viewModel.onEvent(ThemeDetailUiEvent.LoadTheme(enrollmentId, themeId)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailBody(
    uiState: ThemeDetailUiState,
    onBackClick: () -> Unit,
    onCompleteTheme: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeTitle = uiState.theme?.let { "Tema ${it.orderIndex}: ${it.title}" } ?: "Detalle del Tema"

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = themeTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uiState.theme != null) {
                ThemeBottomBar(
                    isCompleted = uiState.isCompleted,
                    isCompleting = uiState.isCompleting,
                    onComplete = onCompleteTheme
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.theme == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            uiState.errorMessage != null && uiState.theme == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            uiState.theme != null -> {
                ThemeDetailContent(
                    theme = uiState.theme,
                    unitTitle = uiState.unitTitle,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ThemeDetailContent(
    theme: EnrollmentThemeProgress,
    unitTitle: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!unitTitle.isNullOrBlank()) {
                        Text(
                            text = unitTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = theme.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ThemeTextAndCodeRenderer(contentText = theme.contentText)
                }
            }
        }
    }
}

@Composable
private fun ThemeTextAndCodeRenderer(contentText: String) {
    val context = LocalContext.current

    if (contentText.isBlank()) {
        Text(
            text = "No hay contenido disponible para este tema.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\\r?\\n?([\\s\\S]*?)```")
    val parts = mutableListOf<ContentPart>()
    var lastIndex = 0

    codeBlockRegex.findAll(contentText).forEach { matchResult ->
        val beforeText = contentText.substring(lastIndex, matchResult.range.first).trim()
        if (beforeText.isNotEmpty()) {
            parts.add(ContentPart.TextPart(beforeText))
        }

        val rawLang = matchResult.groupValues[1].trim()
        val language = if (rawLang.isNotBlank()) rawLang.uppercase() else "KOTLIN"
        val code = matchResult.groupValues[2].trim()
        parts.add(ContentPart.CodePart(language = language, code = code))
        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < contentText.length) {
        val remainingText = contentText.substring(lastIndex).trim()
        if (remainingText.isNotEmpty()) {
            parts.add(ContentPart.TextPart(remainingText))
        }
    }

    if (parts.isEmpty()) {
        parts.add(ContentPart.TextPart(contentText))
    }

    parts.forEach { part ->
        when (part) {
            is ContentPart.TextPart -> {
                Text(
                    text = formatMarkdownText(part.text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 24.sp
                )
            }

            is ContentPart.CodePart -> {
                CodeSnippetCard(
                    language = part.language,
                    code = part.code,
                    onCopyCode = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Código", part.code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

private sealed interface ContentPart {
    data class TextPart(val text: String) : ContentPart
    data class CodePart(val language: String, val code: String) : ContentPart
}

@Composable
private fun CodeSnippetCard(
    language: String,
    code: String,
    onCopyCode: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CodeBlockBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeHeaderBg.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                )
                IconButton(
                    onClick = onCopyCode,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar código",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Text(
                text = highlightKotlinCode(code),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.5.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private fun highlightKotlinCode(code: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val tokenRegex = Regex("""(//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\b(?:val|var|fun|class|data|object|interface|sealed|if|else|when|return|import|package|true|false|null|override)\b|\b(?:Button|Text|Modifier|Column|Row|Box|Spacer|Surface|Card|Scaffold|LazyColumn|LazyRow|Image|Icon|IconButton|CircularProgressIndicator|LinearProgressIndicator)\b|\b\w+\b|[^\w\s])""")

    var lastIndex = 0
    tokenRegex.findAll(code).forEach { match ->
        val matchStart = match.range.first
        val matchEnd = match.range.last + 1

        if (matchStart > lastIndex) {
            builder.append(code.substring(lastIndex, matchStart))
        }

        val token = match.value
        when {
            token.startsWith("//") || token.startsWith("/*") -> {
                builder.pushStyle(SpanStyle(color = Color(0xFF78909C)))
                builder.append(token)
                builder.pop()
            }
            token.startsWith("\"") -> {
                builder.pushStyle(SpanStyle(color = Color(0xFF00897B), fontWeight = FontWeight.Medium))
                builder.append(token)
                builder.pop()
            }
            token in setOf("val", "var", "fun", "class", "data", "object", "interface", "sealed", "if", "else", "when", "return", "import", "package", "true", "false", "null", "override") -> {
                builder.pushStyle(SpanStyle(color = Color(0xFF0288D1), fontWeight = FontWeight.Bold))
                builder.append(token)
                builder.pop()
            }
            token in setOf("Button", "Text", "Modifier", "Column", "Row", "Box", "Spacer", "Surface", "Card", "Scaffold", "LazyColumn", "LazyRow", "Image", "Icon", "IconButton", "CircularProgressIndicator", "LinearProgressIndicator") -> {
                builder.pushStyle(SpanStyle(color = Color(0xFF1565C0), fontWeight = FontWeight.Bold))
                builder.append(token)
                builder.pop()
            }
            token.firstOrNull()?.isUpperCase() == true && token.all { it.isLetterOrDigit() } -> {
                builder.pushStyle(SpanStyle(color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold))
                builder.append(token)
                builder.pop()
            }
            else -> {
                builder.pushStyle(SpanStyle(color = Color(0xFF1E293B)))
                builder.append(token)
                builder.pop()
            }
        }
        lastIndex = matchEnd
    }

    if (lastIndex < code.length) {
        builder.append(code.substring(lastIndex))
    }

    return builder.toAnnotatedString()
}

private fun formatMarkdownText(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val boldRegex = Regex("""\*\*(.*?)\*\*|`(.*?)`""")
    var lastIndex = 0

    boldRegex.findAll(text).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1

        if (start > lastIndex) {
            builder.append(text.substring(lastIndex, start))
        }

        val content = match.groupValues[1].ifEmpty { match.groupValues[2] }
        builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
        builder.append(content)
        builder.pop()

        lastIndex = end
    }

    if (lastIndex < text.length) {
        builder.append(text.substring(lastIndex))
    }

    return builder.toAnnotatedString()
}

@Composable
private fun ThemeBottomBar(
    isCompleted: Boolean,
    isCompleting: Boolean,
    onComplete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                isCompleted -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = CompletedGreenBg,
                            disabledContentColor = CompletedGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = CompletedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEMA COMPLETADO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CompletedGreen
                        )
                    }
                }

                else -> {
                    Button(
                        onClick = onComplete,
                        enabled = !isCompleting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkerBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isCompleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MARCAR COMO COMPLETADO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Detalle del Tema", showBackground = true, showSystemUi = true)
@Composable
private fun ThemeDetailPreview() {
    MentorlyTheme {
        ThemeDetailBody(
            uiState = ThemeDetailUiState(
                isLoading = false,
                unitTitle = "Tema 1: Texto y Botón",
                unitOrderIndex = 1,
                isCompleted = false,
                theme = EnrollmentThemeProgress(
                    themeId = "theme-1",
                    title = "Introducción a los Modificadores",
                    contentText = """
                        En Jetpack Compose, **Text** y **Button** son bloques de construcción fundamentales para cualquier interfaz de usuario. Comprender cómo configurarlos y ordenarlos es esencial para construir apps intuitivas en Android.
                        
                        El composable Text muestra texto en pantalla, mientras que el composable Button provee una superficie clickeable que dispara una acción. Veamos un ejemplo básico combinando ambos.
                        
                        ```kotlin
                        Button(onClick = { /* Do something */ }) {
                            Text("Click Me")
                        }
                        ```
                        
                        Observa cómo el Text se coloca dentro de la lambda final de Button. Esto se debe a que Button en Compose es un contenedor flexible que puede albergar cualquier contenido, no solo texto.
                    """.trimIndent(),
                    orderIndex = 1,
                    isCompleted = false,
                    activities = emptyList()
                )
            ),
            onBackClick = {},
            onCompleteTheme = {},
            onRetry = {}
        )
    }
}
