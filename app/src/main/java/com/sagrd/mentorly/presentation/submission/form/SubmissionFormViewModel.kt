package com.sagrd.mentorly.presentation.submission.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.submission.CreateSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.UpdateSubmissionDto
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SubmissionFormViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionFormUiState())
    val uiState: StateFlow<SubmissionFormUiState> = _uiState.asStateFlow()

    private var enrollmentId: String = ""
    private var activityId: String = ""
    private var submissionId: String? = null

    fun onEvent(event: SubmissionFormUiEvent) {
        when (event) {
            is SubmissionFormUiEvent.Load -> load(event.enrollmentId, event.activityId, event.submissionId)
            is SubmissionFormUiEvent.UrlContentChanged -> _uiState.update {
                it.copy(urlContent = event.value, evidenceContentError = null)
            }
            is SubmissionFormUiEvent.CommentsContentChanged -> _uiState.update {
                it.copy(commentsContent = event.value)
            }
            is SubmissionFormUiEvent.TextContentChanged -> _uiState.update {
                it.copy(textContent = event.value, evidenceContentError = null)
            }
            is SubmissionFormUiEvent.Save -> save()
            is SubmissionFormUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(enrollmentId: String, activityId: String, submissionId: String?) {
        this.enrollmentId = enrollmentId
        this.activityId = activityId
        this.submissionId = submissionId

        loadActivityMetadata(enrollmentId, activityId)

        if (submissionId == null) {
            _uiState.update {
                it.copy(
                    isEditing = false,
                    urlContent = "",
                    commentsContent = "",
                    textContent = "",
                    savedSubmissionId = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isEditing = true,
                    isLoading = true,
                    savedSubmissionId = null
                )
            }

            viewModelScope.launch {
                submissionRepository.getSubmissionById(submissionId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is Resource.Success -> {
                            val submission = resource.data
                            val type = submission?.evidenceType ?: EvidenceType.URL
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    evidenceType = type,
                                    urlContent = if (type == EvidenceType.URL) submission?.evidenceContent.orEmpty() else "",
                                    textContent = if (type == EvidenceType.TEXT) submission?.evidenceContent.orEmpty() else ""
                                )
                            }
                        }
                        is Resource.Error -> _uiState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    private fun loadActivityMetadata(enrollmentId: String, targetActivityId: String) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollmentById(enrollmentId).collect { enrollmentRes ->
                if (enrollmentRes is Resource.Success && enrollmentRes.data != null) {
                    val courseId = enrollmentRes.data.courseId
                    courseRepository.getCourseContent(courseId).collect { courseRes ->
                        if (courseRes is Resource.Success && courseRes.data != null) {
                            val course = courseRes.data
                            val foundActivity = course.units.flatMap { it.themes }.flatMap { it.activities }
                                .firstOrNull { it.id == targetActivityId }

                            if (foundActivity != null) {
                                _uiState.update {
                                    it.copy(
                                        activityTitle = foundActivity.title,
                                        activityDescription = foundActivity.description,
                                        isMandatory = foundActivity.isMandatory,
                                        approvalStrategy = foundActivity.approvalStrategy,
                                        requiredPeerReviews = course.requiredPeerReviews
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val validationError = validateEvidence(state)

        if (validationError != null) {
            _uiState.update { it.copy(evidenceContentError = validationError) }
        } else {
            val contentPayload = when (state.evidenceType) {
                EvidenceType.URL -> state.urlContent.trim()
                EvidenceType.TEXT -> state.textContent.trim()
            }

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isSaving = true,
                        errorMessage = null,
                        savedSubmissionId = null
                    )
                }

                val currentSubmissionId = submissionId

                if (currentSubmissionId == null) {
                    submissionRepository.createSubmission(
                        enrollmentId,
                        activityId,
                        CreateSubmissionDto(
                            evidenceType = state.evidenceType.apiValue,
                            evidenceContent = contentPayload
                        )
                    ).collect(::handleCreateResult)
                } else {
                    submissionRepository.updateSubmission(
                        currentSubmissionId,
                        UpdateSubmissionDto(
                            evidenceType = state.evidenceType.apiValue,
                            evidenceContent = contentPayload
                        )
                    ).collect { resource ->
                        handleUpdateResult(resource, currentSubmissionId)
                    }
                }
            }
        }
    }

    private fun handleCreateResult(resource: Resource<Submission>) {
        when (resource) {
            is Resource.Success -> {
                val createdSubmissionId = resource.data?.id
                _uiState.update {
                    if (createdSubmissionId == null) {
                        it.copy(
                            isSaving = false,
                            errorMessage = "No se pudo obtener la entrega guardada."
                        )
                    } else {
                        it.copy(
                            isSaving = false,
                            savedSubmissionId = createdSubmissionId
                        )
                    }
                }
            }
            is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = resource.message) }
            is Resource.Loading -> Unit
        }
    }

    private fun handleUpdateResult(
        resource: Resource<Unit>,
        updatedSubmissionId: String
    ) {
        when (resource) {
            is Resource.Success -> _uiState.update {
                it.copy(
                    isSaving = false,
                    savedSubmissionId = updatedSubmissionId
                )
            }
            is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = resource.message) }
            is Resource.Loading -> Unit
        }
    }

    private fun validateEvidence(state: SubmissionFormUiState): String? {
        when (state.evidenceType) {
            EvidenceType.URL -> {
                val trimmed = state.urlContent.trim()
                if (trimmed.isEmpty()) {
                    return "El enlace de la entrega es obligatorio."
                }
                val isAbsoluteHttp = trimmed.startsWith("http://") || trimmed.startsWith("https://")
                if (!isAbsoluteHttp) {
                    return "El enlace debe ser una URL absoluta http o https."
                }
            }
            EvidenceType.TEXT -> {
                val trimmed = state.textContent.trim()
                if (trimmed.isEmpty()) {
                    return "La respuesta escrita no puede estar vacía."
                }
            }
        }
        return null
    }
}
