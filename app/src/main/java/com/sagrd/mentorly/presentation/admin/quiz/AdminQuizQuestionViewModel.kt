package com.sagrd.mentorly.presentation.admin.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.quiz.CreateQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.UpdateQuizQuestionDto
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

    private val _state = MutableStateFlow(AdminQuizQuestionUiState())
    val state: StateFlow<AdminQuizQuestionUiState> = _state.asStateFlow()

    private var activityId = ""

    fun onEvent(event: AdminQuizQuestionUiEvent) {
        when (event) {
            is AdminQuizQuestionUiEvent.Load -> load(event.activityId)
            is AdminQuizQuestionUiEvent.QuestionChanged -> _state.update {
                it.copy(question = event.value, questionError = null)
            }
            is AdminQuizQuestionUiEvent.CorrectAnswerChanged -> _state.update {
                it.copy(correctAnswer = event.value, correctAnswerError = null)
            }
            is AdminQuizQuestionUiEvent.OrderChanged -> _state.update {
                it.copy(orderIndex = event.value, orderError = null)
            }
            is AdminQuizQuestionUiEvent.EditQuestion -> editQuestion(event.questionId)
            AdminQuizQuestionUiEvent.SaveQuestion -> saveQuestion()
            AdminQuizQuestionUiEvent.CancelEdit -> clearForm()
            AdminQuizQuestionUiEvent.DeleteQuestion -> deleteQuestion()
            AdminQuizQuestionUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            AdminQuizQuestionUiEvent.QuestionSavedHandled -> _state.update {
                it.copy(isQuestionSaved = false)
            }
        }
    }

    private fun load(newActivityId: String) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else {
                activityId = newActivityId
                _state.update {
                    AdminQuizQuestionUiState(
                        isLoading = true,
                        hasSession = true,
                        hasAdminAccess = true,
                    )
                }
                loadQuestions(session.studentId)
            }
        }
    }

    private fun editQuestion(questionId: String) {
        _state.update { state ->
            state.questions.firstOrNull { it.id == questionId }?.let { question ->
                state.copy(
                    editingQuestionId = question.id,
                    question = question.prompt,
                    correctAnswer = question.correctAnswer,
                    orderIndex = question.orderIndex.toString(),
                    questionError = null,
                    correctAnswerError = null,
                    orderError = null,
                    errorMessage = null,
                )
            } ?: state
        }
    }

    private fun saveQuestion() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            val form = _state.value
            val errors = validate(form)

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (form.isSaving || form.isDeleting) {
                Unit
            } else if (errors.isNotEmpty()) {
                _state.update {
                    it.copy(
                        questionError = errors["question"],
                        correctAnswerError = errors["correctAnswer"],
                        orderError = errors["order"],
                    )
                }
            } else {
                _state.update { it.copy(isSaving = true, errorMessage = null) }

                if (form.editingQuestionId == null) {
                    quizRepository.createQuizQuestion(
                        adminId = session.studentId,
                        activityId = activityId,
                        question = form.toCreateDto(),
                    ).collect { resource ->
                        handleSaveResult(resource, session.studentId, "No se pudo crear la pregunta de quiz.")
                    }
                } else {
                    quizRepository.updateQuizQuestion(
                        adminId = session.studentId,
                        questionId = form.editingQuestionId,
                        question = form.toUpdateDto(),
                    ).collect { resource ->
                        handleSaveResult(resource, session.studentId, "No se pudo actualizar la pregunta de quiz.")
                    }
                }
            }
        }
    }

    private fun deleteQuestion() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            val questionId = _state.value.editingQuestionId

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (questionId != null && !_state.value.isDeleting) {
                _state.update { it.copy(isDeleting = true, errorMessage = null) }
                quizRepository.deleteQuizQuestion(session.studentId, questionId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            clearForm()
                            _state.update { it.copy(isDeleting = false, isQuestionSaved = true) }
                            loadQuestions(session.studentId)
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                isDeleting = false,
                                errorMessage = resource.message ?: "No se pudo eliminar la pregunta.",
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleSaveResult(
        resource: Resource<*>,
        adminId: String,
        defaultErrorMessage: String,
    ) {
        when (resource) {
            is Resource.Loading -> Unit
            is Resource.Success -> {
                clearForm()
                _state.update { it.copy(isSaving = false, isQuestionSaved = true) }
                loadQuestions(adminId)
            }
            is Resource.Error -> _state.update {
                it.copy(isSaving = false, errorMessage = resource.message ?: defaultErrorMessage)
            }
        }
    }

    private suspend fun loadQuestions(adminId: String) {
        quizRepository.getAdminQuizQuestions(adminId, activityId).collect { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        questions = resource.data.orEmpty().sortedBy { question -> question.orderIndex },
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = resource.message ?: "No se pudieron cargar las preguntas.",
                    )
                }
            }
        }
    }

    private fun clearForm() {
        _state.update { state ->
            state.copy(
                editingQuestionId = null,
                question = "",
                correctAnswer = "",
                orderIndex = (state.questions.maxOfOrNull { question -> question.orderIndex }?.plus(1) ?: 0).toString(),
                questionError = null,
                correctAnswerError = null,
                orderError = null,
            )
        }
    }

    private fun validate(form: AdminQuizQuestionUiState): Map<String, String> = buildMap {
        if (form.question.isBlank()) put("question", "La pregunta es obligatoria.")
        if (form.correctAnswer.isBlank()) put("correctAnswer", "La respuesta correcta es obligatoria.")
        if (form.orderIndex.toIntOrNull() == null) put("order", "La posición debe ser un entero.")
    }

    private fun AdminQuizQuestionUiState.toCreateDto() = CreateQuizQuestionDto(
        prompt = question.trim(),
        correctAnswer = correctAnswer.trim(),
        orderIndex = orderIndex.toInt(),
    )

    private fun AdminQuizQuestionUiState.toUpdateDto() = UpdateQuizQuestionDto(
        prompt = question.trim(),
        correctAnswer = correctAnswer.trim(),
        orderIndex = orderIndex.toInt(),
    )

    private fun updateMissingSession() {
        _state.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                isDeleting = false,
                hasSession = false,
                hasAdminAccess = false,
                errorMessage = "No se encontró una sesión activa.",
            )
        }
    }

    private fun updateMissingAdminAccess() {
        _state.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                isDeleting = false,
                hasSession = true,
                hasAdminAccess = false,
                errorMessage = "No tienes permisos para administrar preguntas de quiz.",
            )
        }
    }
}
