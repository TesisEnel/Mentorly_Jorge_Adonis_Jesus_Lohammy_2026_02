package com.sagrd.mentorly.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.R
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private val MentorlyBlue = Color(0xFF168FCA)
private val GoogleButtonBorder = Color(0xFFDADCE0)

@Composable
fun LoginScreen(
    onLoginCompleted: (Student) -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.student?.id) {
        state.student?.let { student ->
            viewModel.onEvent(LoginUiEvent.LoginCompletedHandled)
            onLoginCompleted(student)
        }
    }

    LoginContent(
        state = state,
        onSignInClick = {
            viewModel.onEvent(LoginUiEvent.SignInWithGoogle(context))
        },
        onSignOutClick = {
            viewModel.onEvent(LoginUiEvent.SignOut)
        }
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo de Mentorly",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = 2.8f, scaleY = 2.8f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Bienvenido a Mentorly",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tu experiencia de aprendizaje\npersonalizada comienza aquí.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4C5567),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.login_learning_hero),
                contentDescription = "Estudiante aprendiendo con Mentorly",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(208.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(32.dp),
                        color = MentorlyBlue,
                        strokeWidth = 3.dp
                    )
                }

                state.student != null -> {
                    LoggedInContent(
                        student = state.student,
                        onSignOutClick = onSignOutClick
                    )
                }

                else -> {
                    GoogleSignInButton(
                        onClick = onSignInClick
                    )

                    state.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            width = 1.dp,
            color = GoogleButtonBorder
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF202124)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_google_logo),
                contentDescription = "Google",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "Continuar con Google",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LoggedInContent(
    student: Student,
    onSignOutClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "¡Bienvenido, ${student.displayName}!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        TextButton(onClick = onSignOutClick) {
            Text("Cerrar sesión")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    MentorlyTheme {
        LoginContent(
            state = LoginUiState(),
            onSignInClick = {},
            onSignOutClick = {}
        )
    }
}

