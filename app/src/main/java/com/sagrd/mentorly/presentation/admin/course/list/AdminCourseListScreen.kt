package com.sagrd.mentorly.presentation.admin.course.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminCourseListScreen(onCreateCourseClick: () -> Unit, onEditCourseClick: (String) -> Unit, onManageContentClick: (String) -> Unit, onBackClick: () -> Unit, viewModel: AdminCourseListViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    AdminCourseListContent(state, viewModel::onEvent, onCreateCourseClick, onEditCourseClick, onManageContentClick, onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AdminCourseListContent(state: AdminCourseListUiState, onEvent: (AdminCourseListUiEvent) -> Unit, onCreate: () -> Unit, onEdit: (String) -> Unit, onContent: (String) -> Unit, onBack: () -> Unit) {
    var pendingDelete by remember { mutableStateOf<Course?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text("Administrar cursos") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }, actions = { TextButton(onClick = { onEvent(AdminCourseListUiEvent.Refresh) }) { Text("Recargar") } }) }, floatingActionButton = { FloatingActionButton(onClick = onCreate) { Text("Crear") } }) { padding ->
        when {
            !state.hasAdminAccess -> CenterMessage("No tienes permisos para administrar cursos.", Modifier.padding(padding))
            state.isLoading && state.courses.isEmpty() -> CenterLoading(Modifier.padding(padding))
            state.errorMessage != null && state.courses.isEmpty() -> CenterMessage(state.errorMessage, Modifier.padding(padding), { onEvent(AdminCourseListUiEvent.Refresh) })
            state.courses.isEmpty() -> CenterMessage("No hay cursos disponibles.", Modifier.padding(padding))
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(state.courses, key = { it.id }) { course -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { if (!course.imageUrl.isNullOrBlank()) { AsyncImage(model = course.imageUrl, contentDescription = "Imagen de ${course.title}", modifier = Modifier.fillMaxWidth().height(140.dp)); Spacer(Modifier.height(12.dp)) }; Text(course.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(course.description, maxLines = 2); Text(if (course.isPublished) "Publicado" else "Borrador"); Text("Revisiones requeridas: ${course.requiredPeerReviews}"); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = { onEdit(course.id) }) { Text("Editar") }; TextButton(onClick = { onContent(course.id) }) { Text("Contenido") }; TextButton(onClick = { onEvent(AdminCourseListUiEvent.TogglePublication(course.id)) }, enabled = state.publishingCourseId == null) { Text(if (course.isPublished) "Despublicar" else "Publicar") }; TextButton(onClick = { pendingDelete = course }, enabled = state.deletingCourseId == null) { Text("Eliminar") } } } } } }
        }
    }
    pendingDelete?.let { course -> AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Eliminar curso") }, text = { Text("¿Deseas eliminar ${course.title}?") }, confirmButton = { TextButton(onClick = { onEvent(AdminCourseListUiEvent.DeleteCourse(course.id)); pendingDelete = null }) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } }) }
}
@Composable private fun CenterLoading(modifier: Modifier) = Box(modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
@Composable private fun CenterMessage(message: String?, modifier: Modifier, retry: (() -> Unit)? = null) = Box(modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Column { Text(message.orEmpty()); retry?.let { Button(onClick = it) { Text("Reintentar") } } } }
@Preview(showBackground = true) @Composable private fun AdminCoursesPreview() { MentorlyTheme { AdminCourseListContent(AdminCourseListUiState(courses = listOf(Course("1", "Android", "Curso de Compose", null, true, 2))), {}, {}, {}, {}, {}) } }
@Preview(showBackground = true) @Composable private fun AdminCoursesEmptyPreview() { MentorlyTheme { AdminCourseListContent(AdminCourseListUiState(), {}, {}, {}, {}, {}) } }
