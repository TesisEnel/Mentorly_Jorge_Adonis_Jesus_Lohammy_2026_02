package com.sagrd.mentorly.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.quiz.QuizAnswerDto
import com.sagrd.mentorly.data.remote.dto.quiz.SubmitQuizAttemptDto
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
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var enrollmentId: String? = null
    private var activityId: String? = null

    fun initialize(enrollmentId: String, activityId: String) {
        if (
            this.enrollmentId == enrollmentId &&
            this.activityId == activityId &&
            (_state.value.questions.isNotEmpty() || _state.value.isLoading)
        ) {
            return
        }

        this.enrollmentId = enrollmentId
        this.activityId = activityId
        loadQuiz()
    }

    fun onEvent(event: QuizUiEvent) {
        when (event) {
            QuizUiEvent.LoadQuiz,
            QuizUiEvent.Retry -> loadQuiz()

            is QuizUiEvent.QuestionIndexChanged -> {
                val maxIndex = (_state.value.questions.size - 1).coerceAtLeast(0)
                val validIndex = event.index.coerceIn(0, maxIndex)
                _state.update { it.copy(currentQuestionIndex = validIndex) }
            }

            is QuizUiEvent.AnswerChanged -> updateAnswer(event.questionId, event.answer)
            QuizUiEvent.SubmitQuiz -> submitQuiz()
            QuizUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            QuizUiEvent.ReattemptQuiz -> reattemptQuiz()
        }
    }

    private fun reattemptQuiz() {
        _state.update {
            it.copy(
                isSubmitted = false,
                isSubmitting = false,
                result = null,
                answers = emptyMap(),
                currentQuestionIndex = 0,
                errorMessage = null
            )
        }
        if (_state.value.questions.isEmpty()) {
            loadQuiz()
        }
    }

    private fun updateAnswer(questionId: String, answer: String) {
        if (_state.value.isSubmitted) return

        _state.update { state ->
            state.copy(
                answers = state.answers + (questionId to answer),
                errorMessage = null
            )
        }
    }

    private fun loadQuiz() {
        val currentActivityId = activityId ?: return
        if (_state.value.isLoading && _state.value.questions.isNotEmpty()) return

        viewModelScope.launch {
            quizRepository.getQuizQuestions(currentActivityId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                questions = resource.data.orEmpty().sortedBy { question ->
                                    question.orderIndex
                                },
                                currentQuestionIndex = 0,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No se pudo cargar el cuestionario."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun submitQuiz() {
        val state = _state.value
        val currentEnrollmentId = enrollmentId ?: return
        val currentActivityId = activityId ?: return

        if (state.isSubmitting || state.isSubmitted) return

        val firstUnansweredIndex = state.questions.indexOfFirst {
            state.answers[it.id].isNullOrBlank()
        }
        if (firstUnansweredIndex != -1) {
            _state.update {
                it.copy(
                    currentQuestionIndex = firstUnansweredIndex,
                    errorMessage = "Debes responder todas las preguntas antes de enviar."
                )
            }
            return
        }

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update {
                    it.copy(errorMessage = "No se encontró una sesión activa.")
                }
            } else {
                val attempt = SubmitQuizAttemptDto(
                    studentId = session.studentId,
                    answers = state.questions.map { question ->
                        QuizAnswerDto(
                            questionId = question.id,
                            answer = state.answers.getValue(question.id).trim()
                        )
                    }
                )

                quizRepository.submitQuizAttempt(
                    enrollmentId = currentEnrollmentId,
                    activityId = currentActivityId,
                    attempt = attempt
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _state.update {
                                it.copy(
                                    isSubmitting = true,
                                    errorMessage = null
                                )
                            }
                        }

                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    isSubmitting = false,
                                    result = resource.data,
                                    errorMessage = null,
                                    isSubmitted = true
                                )
                            }
                        }

                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    isSubmitting = false,
                                    errorMessage = "No se pudo enviar el intento. Inténtalo nuevamente."
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
