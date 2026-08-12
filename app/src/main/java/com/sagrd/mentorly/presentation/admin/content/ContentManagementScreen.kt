package com.sagrd.mentorly.presentation.admin.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.content.ActivityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentManagementScreen(
    courseId: String,
    onBackClick: () -> Unit,
    onCreateUnitClick: (String) -> Unit,
    onEditUnitClick: (String, String) -> Unit,
    onCreateThemeClick: (String) -> Unit,
    onEditThemeClick: (String, String) -> Unit,
    onCreateActivityClick: (String) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
    viewModel: ContentManagementViewModel = hiltViewModel(),
) {
    val s by viewModel.uiState.collectAsState()
    LaunchedEffect(courseId) { viewModel.setCourseId(courseId) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar contenido") },
                navigationIcon = { TextButton(onClick = onBackClick) { Text("Volver") } },
            )
        }
    ) { p ->
        when {
            !s.hasAdminAccess ->
                Box(Modifier.fillMaxSize().padding(p)) {
                    Text("No tienes permisos para administrar el contenido del curso.")
                }
            s.isLoading -> Box(Modifier.fillMaxSize().padding(p)) { CircularProgressIndicator() }
            else ->
                LazyColumn(Modifier.fillMaxSize().padding(p).padding(16.dp)) {
                    item {
                        Text(
                            s.courseContent?.title ?: "Contenido",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Button(onClick = { onCreateUnitClick(courseId) }) { Text("Crear unidad") }
                    }
                    s.courseContent
                        ?.units
                        ?.sortedBy { it.orderIndex }
                        ?.forEach { u ->
                            item {
                                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("Unidad: ${u.title}")
                                        Row {
                                            TextButton(
                                                onClick = { onEditUnitClick(courseId, u.id) }
                                            ) {
                                                Text("Editar")
                                            }
                                            TextButton(onClick = { onCreateThemeClick(u.id) }) {
                                                Text("Crear tema")
                                            }
                                            TextButton(
                                                onClick = {
                                                    viewModel::onEvent.bind(
                                                        ContentManagementUiEvent.DeleteUnit(u.id)
                                                    )
                                                }
                                            ) {
                                                Text("Eliminar")
                                            }
                                        }
                                        u.themes
                                            .sortedBy { it.orderIndex }
                                            .forEach { t ->
                                                Text("  Tema: ${t.title}")
                                                Row {
                                                    TextButton(
                                                        onClick = { onEditThemeClick(u.id, t.id) }
                                                    ) {
                                                        Text("Editar")
                                                    }
                                                    TextButton(
                                                        onClick = { onCreateActivityClick(t.id) }
                                                    ) {
                                                        Text("Crear actividad")
                                                    }
                                                    TextButton(
                                                        onClick = {
                                                            viewModel::onEvent.bind(
                                                                ContentManagementUiEvent
                                                                    .DeleteTheme(t.id)
                                                            )
                                                        }
                                                    ) {
                                                        Text("Eliminar")
                                                    }
                                                }
                                                t.activities
                                                    .sortedBy { it.orderIndex }
                                                    .forEach { a ->
                                                        Text("    Actividad: ${a.title}")
                                                        TextButton(
                                                            onClick = {
                                                                onEditActivityClick(t.id, a.id)
                                                            }
                                                        ) {
                                                            Text("Editar")
                                                        }
                                                        if (a.type == ActivityType.QUIZ) {
                                                            TextButton(
                                                                onClick = {
                                                                    onManageQuizQuestionsClick(a.id)
                                                                }
                                                            ) {
                                                                Text("Administrar preguntas")
                                                            }
                                                        }
                                                    }
                                            }
                                    }
                                }
                            }
                        }
                }
        }
    }
}

private fun <T> ((T) -> Unit).bind(value: T): () -> Unit = { { this(value) } }
