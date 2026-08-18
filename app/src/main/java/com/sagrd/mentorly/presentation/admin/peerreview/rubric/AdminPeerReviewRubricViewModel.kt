package com.sagrd.mentorly.presentation.admin.peerreview.rubric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRubricCriterionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.UpdatePeerReviewRubricCriterionDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
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
class AdminPeerReviewRubricViewModel @Inject constructor(
    private val peerReviewRepository: PeerReviewRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminPeerReviewRubricUiState())
    val state: StateFlow<AdminPeerReviewRubricUiState> = _state.asStateFlow()

    private var activityId = ""

    fun onEvent(event: AdminPeerReviewRubricUiEvent) {
        when (event) {
            is AdminPeerReviewRubricUiEvent.Load -> load(event.activityId)
            is AdminPeerReviewRubricUiEvent.TitleChanged -> _state.update {
                it.copy(title = event.value, titleError = null)
            }
            is AdminPeerReviewRubricUiEvent.DescriptionChanged -> _state.update {
                it.copy(description = event.value, descriptionError = null)
            }
            is AdminPeerReviewRubricUiEvent.MaxScoreChanged -> _state.update {
                it.copy(maxScore = event.value, maxScoreError = null)
            }
            is AdminPeerReviewRubricUiEvent.OrderChanged -> _state.update {
                it.copy(orderIndex = event.value, orderError = null)
            }
            is AdminPeerReviewRubricUiEvent.EditCriterion -> editCriterion(event.criterionId)
            AdminPeerReviewRubricUiEvent.SaveCriterion -> saveCriterion()
            AdminPeerReviewRubricUiEvent.CancelEdit -> clearForm()
            AdminPeerReviewRubricUiEvent.DeleteCriterion -> deleteCriterion()
            AdminPeerReviewRubricUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            AdminPeerReviewRubricUiEvent.CriterionSavedHandled -> _state.update {
                it.copy(isCriterionSaved = false)
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
            } else if (activityId != newActivityId) {
                activityId = newActivityId
                _state.update {
                    AdminPeerReviewRubricUiState(
                        isLoading = true,
                        hasSession = true,
                        hasAdminAccess = true,
                    )
                }
                loadCriteria()
            }
        }
    }

    private fun editCriterion(criterionId: String) {
        _state.update { state ->
            state.criteria.firstOrNull { it.id == criterionId }?.let { criterion ->
                state.copy(
                    editingCriterionId = criterion.id,
                    title = criterion.title,
                    description = criterion.description,
                    maxScore = criterion.maxScore.toString(),
                    orderIndex = criterion.orderIndex.toString(),
                    titleError = null,
                    descriptionError = null,
                    maxScoreError = null,
                    orderError = null,
                    errorMessage = null,
                )
            } ?: state
        }
    }

    private fun saveCriterion() {
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
                        titleError = errors["title"],
                        descriptionError = errors["description"],
                        maxScoreError = errors["maxScore"],
                        orderError = errors["order"],
                    )
                }
            } else {
                _state.update { it.copy(isSaving = true, errorMessage = null) }

                if (form.editingCriterionId == null) {
                    peerReviewRepository.createRubricCriterion(
                        adminId = session.studentId,
                        activityId = activityId,
                        dto = form.toCreateDto(),
                    ).collect { resource ->
                        handleSaveResult(resource, "No se pudo crear el criterio de rúbrica.")
                    }
                } else {
                    peerReviewRepository.updateRubricCriterion(
                        adminId = session.studentId,
                        criterionId = form.editingCriterionId,
                        dto = form.toUpdateDto(),
                    ).collect { resource ->
                        handleSaveResult(resource, "No se pudo actualizar el criterio de rúbrica.")
                    }
                }
            }
        }
    }

    private fun deleteCriterion() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            val criterionId = _state.value.editingCriterionId

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (criterionId != null && !_state.value.isDeleting) {
                _state.update { it.copy(isDeleting = true, errorMessage = null) }
                peerReviewRepository.deleteRubricCriterion(session.studentId, criterionId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            clearForm()
                            _state.update { it.copy(isDeleting = false, isCriterionSaved = true) }
                            loadCriteria()
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                isDeleting = false,
                                errorMessage = resource.message ?: "No se pudo eliminar el criterio.",
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleSaveResult(
        resource: Resource<*>,
        defaultErrorMessage: String,
    ) {
        when (resource) {
            is Resource.Loading -> Unit
            is Resource.Success -> {
                clearForm()
                _state.update { it.copy(isSaving = false, isCriterionSaved = true) }
                loadCriteria()
            }
            is Resource.Error -> _state.update {
                it.copy(isSaving = false, errorMessage = resource.message ?: defaultErrorMessage)
            }
        }
    }

    private suspend fun loadCriteria() {
        peerReviewRepository.getRubric(activityId).collect { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        criteria = resource.data.orEmpty().sortedBy { criterion -> criterion.orderIndex },
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = resource.message ?: "No se pudieron cargar los criterios de rúbrica.",
                    )
                }
            }
        }
    }

    private fun clearForm() {
        _state.update { state ->
            state.copy(
                editingCriterionId = null,
                title = "",
                description = "",
                maxScore = "5",
                orderIndex = (state.criteria.maxOfOrNull { criterion -> criterion.orderIndex }?.plus(1) ?: 0).toString(),
                titleError = null,
                descriptionError = null,
                maxScoreError = null,
                orderError = null,
            )
        }
    }

    private fun validate(form: AdminPeerReviewRubricUiState): Map<String, String> = buildMap {
        if (form.title.isBlank()) put("title", "El título es obligatorio.")
        if (form.description.isBlank()) put("description", "La descripción es obligatoria.")
        val score = form.maxScore.toIntOrNull()
        if (score == null || score <= 0) put("maxScore", "El puntaje máximo debe ser un entero positivo.")
        if (form.orderIndex.toIntOrNull() == null) put("order", "La posición debe ser un entero.")
    }

    private fun AdminPeerReviewRubricUiState.toCreateDto() = CreatePeerReviewRubricCriterionDto(
        title = title.trim(),
        description = description.trim(),
        maxScore = maxScore.toIntOrNull() ?: 5,
        orderIndex = orderIndex.toIntOrNull() ?: 0,
    )

    private fun AdminPeerReviewRubricUiState.toUpdateDto() = UpdatePeerReviewRubricCriterionDto(
        title = title.trim(),
        description = description.trim(),
        maxScore = maxScore.toIntOrNull() ?: 5,
        orderIndex = orderIndex.toIntOrNull() ?: 0,
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
                errorMessage = "No tienes permisos para administrar rúbricas.",
            )
        }
    }
}
