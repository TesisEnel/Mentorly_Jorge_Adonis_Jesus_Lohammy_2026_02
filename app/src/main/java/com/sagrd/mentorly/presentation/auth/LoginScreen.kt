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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.R
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private val MentorlyBlue = Color(0xFF168FCA)
private val GoogleButtonBorder = Color(0xFFDADCE0)
private val DividerColor = Color(0xFFEAEAEA)

@Composable
fun LoginScreen(
    onLoginCompleted: (Student) -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
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
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 44.dp, y = (-58).dp)
                .size(118.dp)
                .clip(CircleShape)
                .background(MentorlyBlue)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-45).dp, y = 45.dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(MentorlyBlue)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(66.dp))

            Text(
                text = "Mentorly",
                color = MentorlyBlue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Beyond imagination.",
                color = Color(0xFF333333),
                style = MaterialTheme.typography.labelMedium,
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(36.dp))

            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
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
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = DividerColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onSignInClick
            ) {
                Text(
                    text = "¿Eres Administrador?\nInicia sesión con Google.",
                    color = Color(0xFF79BFE5),
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
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
            .height(48.dp),
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
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

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                text = "Iniciar sesión con Google",
                style = MaterialTheme.typography.labelMedium,
                fontStyle = FontStyle.Italic
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenSuccessPreview() {
    MentorlyTheme {
        LoginContent(
            state = LoginUiState(
                student = Student(
                    id = "student-id",
                    email = "estudiante@mentorly.com",
                    displayName = "Adonis Mercado",
                    role = StudentRole.STUDENT,
                    isLeaderboardPublic = true,
                    totalPoints = 0
                )
            ),
            onSignInClick = {},
            onSignOutClick = {}
        )
    }
}
