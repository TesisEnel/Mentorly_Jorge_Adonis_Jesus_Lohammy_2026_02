package com.sagrd.mentorly.presentation.admin.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.quiz.CreateQuizQuestionDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.quiz.QuizRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AdminQuizQuestionViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminQuizQuestionUiState())
    val uiState: StateFlow<AdminQuizQuestionUiState> = _uiState.asStateFlow()

    init {
        validateAdminAccess()
    }

    fun onEvent(event: AdminQuizQuestionUiEvent, activityId: String? = null) {
        when (event) {
            is AdminQuizQuestionUiEvent.QuestionChanged -> {
                _uiState.update { it.copy(question = event.value, questionError = null) }
            }

            is AdminQuizQuestionUiEvent.CorrectAnswerChanged -> {
                _uiState.update { it.copy(correctAnswer = event.value, correctAnswerError = null) }
            }

            AdminQuizQuestionUiEvent.SaveQuestion -> activityId?.let(::saveQuestion)
            AdminQuizQuestionUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            AdminQuizQuestionUiEvent.ClearCreatedState -> {
                _uiState.update { it.copy(isQuestionCreated = false) }
            }
        }
    }

    private fun saveQuestion(activityId: String) {
        viewModelScope.launch {
            if (_uiState.value.isSaving) return@launch

            val session = sessionRepository.session.first()
            when {
                session == null -> {
                    _uiState.update {
                        it.copy(
                            hasSession = false,
                            hasAdminAccess = false,
                            errorMessage = "No se encontró una sesión activa.",
                        )
                    }
                    return@launch
                }

                session.role != StudentRole.ADMIN -> {
                    _uiState.update {
                        it.copy(
                            hasSession = true,
                            hasAdminAccess = false,
                            errorMessage = "No tienes permisos para crear preguntas de quiz.",
                        )
                    }
                    return@launch
                }
            }

            val state = _uiState.value
            val question = state.question.trim()
            val correctAnswer = state.correctAnswer.trim()

            if (question.isBlank() || correctAnswer.isBlank()) {
                _uiState.update {
                    it.copy(
                        hasSession = true,
                        hasAdminAccess = true,
                        questionError = if (question.isBlank()) "La pregunta es obligatoria." else null,
                        correctAnswerError =
                            if (correctAnswer.isBlank()) "La respuesta correcta es obligatoria." else null,
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    hasSession = true,
                    hasAdminAccess = true,
                )
            }

            quizRepository.createQuizQuestion(
                adminId = session.studentId,
                activityId = activityId,
                question = CreateQuizQuestionDto(
                    prompt = question,
                    correctAnswer = correctAnswer,
                    orderIndex = 0,
                ),
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                question = "",
                                correctAnswer = "",
                                questionError = null,
                                correctAnswerError = null,
                                isQuestionCreated = true,
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage =
                                    resource.message ?: "No se pudo crear la pregunta de quiz.",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun validateAdminAccess() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            _uiState.update {
                when {
                    session == null -> {
                        it.copy(
                            hasSession = false,
                            hasAdminAccess = false,
                            errorMessage = "No se encontró una sesión activa.",
                        )
                    }

                    session.role != StudentRole.ADMIN -> {
                        it.copy(
                            hasSession = true,
                            hasAdminAccess = false,
                            errorMessage = "No tienes permisos para crear preguntas de quiz.",
                        )
                    }

                    else -> it.copy(hasSession = true, hasAdminAccess = true)
                }
            }
        }
    }
}
