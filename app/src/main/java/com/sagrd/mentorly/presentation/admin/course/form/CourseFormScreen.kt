package com.sagrd.mentorly.presentation.admin.course.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun CourseFormScreen(
    courseId: String? = null,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: CourseFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) {
        viewModel.onEvent(CourseFormUiEvent.Load(courseId))
    }
    LaunchedEffect(state.savedCourseId) {
        state.savedCourseId?.let { savedCourseId ->
            viewModel.onEvent(CourseFormUiEvent.SavedHandled)
            onSaved(savedCourseId)
        }
    }

    CourseFormContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseFormContent(
    state: CourseFormUiState,
    onEvent: (CourseFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditMode) "Editar curso" else "Crear curso")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { paddingValues ->
        when {
            !state.hasSession -> CenterMessage(
                "No se encontró una sesión activa.",
                Modifier.padding(paddingValues),
            )
            !state.hasAdminAccess -> CenterMessage(
                "No tienes permisos para administrar cursos.",
                Modifier.padding(paddingValues),
            )
            state.isLoading -> LoadingContent(Modifier.padding(paddingValues))
            else -> CourseForm(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CourseForm(
    state: CourseFormUiState,
    onEvent: (CourseFormUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CourseImage(state.imageUrl, state.title)

        if (state.isEditMode) {
            SuggestionChip(
                onClick = {},
                label = { Text(if (state.isPublished) "Publicado" else "Borrador") },
            )
        }

        Text(
            if (state.isEditMode) "Editar datos del curso" else "Información del nuevo curso",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(CourseFormUiEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Título del curso") },
            isError = state.fieldErrors.containsKey("title"),
            supportingText = {
                state.fieldErrors["title"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving,
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onEvent(CourseFormUiEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            isError = state.fieldErrors.containsKey("description"),
            supportingText = {
                state.fieldErrors["description"]?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            enabled = !state.isSaving,
            minLines = 4,
        )

        OutlinedTextField(
            value = state.imageUrl,
            onValueChange = { onEvent(CourseFormUiEvent.ImageUrlChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL de imagen") },
            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
            isError = state.fieldErrors.containsKey("imageUrl"),
            supportingText = {
                state.fieldErrors["imageUrl"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving,
            singleLine = true,
        )

        OutlinedTextField(
            value = state.requiredPeerReviews,
            onValueChange = { onEvent(CourseFormUiEvent.RequiredPeerReviewsChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Revisiones por pares requeridas") },
            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
            isError = state.fieldErrors.containsKey("requiredPeerReviews"),
            supportingText = {
                state.fieldErrors["requiredPeerReviews"]?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            enabled = !state.isSaving,
            singleLine = true,
        )

        state.errorMessage?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = { onEvent(CourseFormUiEvent.ClearError) }) {
                        Text("Cerrar")
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(CourseFormUiEvent.Save) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.isEditMode) "Guardar cambios" else "Crear curso")
            }
        }
    }
}

@Composable
private fun CourseImage(imageUrl: String, title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        if (imageUrl.isBlank()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(170.dp).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen de $title",
                modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenterMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message)
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseFormPreview() {
    MentorlyTheme {
        CourseFormContent(
            state = CourseFormUiState(
                isEditMode = true,
                title = "Programación en Python",
                description = "Aprende las bases del lenguaje y desarrolla soluciones prácticas.",
                requiredPeerReviews = "3",
                isPublished = true,
            ),
            onEvent = {},
            onBackClick = {},
        )
    }
}
