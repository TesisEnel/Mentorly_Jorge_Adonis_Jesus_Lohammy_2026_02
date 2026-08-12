package com.sagrd.mentorly.presentation.admin.course.form

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun CourseFormScreen(courseId: String? = null, onBackClick: () -> Unit, onSaved: (String) -> Unit, viewModel: CourseFormViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(courseId) { viewModel.onEvent(CourseFormUiEvent.Load(courseId)) }
    LaunchedEffect(state.savedCourseId) { state.savedCourseId?.let(onSaved) }
    CourseFormContent(state, viewModel::onEvent, onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun CourseFormContent(state: CourseFormUiState, onEvent: (CourseFormUiEvent) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(if (state.isEditMode) "Editar curso" else "Crear curso") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) }) { padding ->
        if (!state.hasAdminAccess) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("No tienes permisos para administrar cursos.") }; return@Scaffold }
        if (state.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }; return@Scaffold }
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(state.title, { onEvent(CourseFormUiEvent.TitleChanged(it)) }, label = { Text("Título") }, isError = state.fieldErrors.containsKey("title"), modifier = Modifier.fillMaxWidth()); FieldError(state.fieldErrors["title"])
            OutlinedTextField(state.description, { onEvent(CourseFormUiEvent.DescriptionChanged(it)) }, label = { Text("Descripción") }, isError = state.fieldErrors.containsKey("description"), modifier = Modifier.fillMaxWidth(), minLines = 3); FieldError(state.fieldErrors["description"])
            OutlinedTextField(state.imageUrl, { onEvent(CourseFormUiEvent.ImageUrlChanged(it)) }, label = { Text("URL de imagen") }, isError = state.fieldErrors.containsKey("imageUrl"), modifier = Modifier.fillMaxWidth()); FieldError(state.fieldErrors["imageUrl"])
            OutlinedTextField(state.requiredPeerReviews, { onEvent(CourseFormUiEvent.RequiredPeerReviewsChanged(it)) }, label = { Text("Revisiones por pares requeridas") }, isError = state.fieldErrors.containsKey("requiredPeerReviews"), modifier = Modifier.fillMaxWidth()); FieldError(state.fieldErrors["requiredPeerReviews"])
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { onEvent(CourseFormUiEvent.Save) }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) { if (state.isSaving) CircularProgressIndicator() else Text("Guardar") }
        }
    }
}
@Composable private fun FieldError(error: String?) { error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } }
@Preview(showBackground = true) @Composable private fun CreatePreview() { MentorlyTheme { CourseFormContent(CourseFormUiState(), {}, {}) } }
@Preview(showBackground = true) @Composable private fun EditPreview() { MentorlyTheme { CourseFormContent(CourseFormUiState(isEditMode = true, title = "Android", description = "Compose", requiredPeerReviews = "2"), {}, {}) } }
