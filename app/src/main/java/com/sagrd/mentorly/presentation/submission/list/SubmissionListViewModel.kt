package com.sagrd.mentorly.presentation.submission.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SubmissionListViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val sessionRepository: SessionRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SubmissionListUiState())
    val state: StateFlow<SubmissionListUiState> = _state.asStateFlow()

    init {
        onEvent(SubmissionListUiEvent.Load)
    }

    fun onEvent(event: SubmissionListUiEvent) {
        when (event) {
            is SubmissionListUiEvent.Load -> load()
            is SubmissionListUiEvent.Refresh -> load()
            is SubmissionListUiEvent.OnSearchQueryChanged -> updateSearchQuery(event.query)
            is SubmissionListUiEvent.ClearSearch -> updateSearchQuery("")
            is SubmissionListUiEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.update { state ->
            state.copy(
                searchQuery = query,
                filteredSubmissions = filterSubmissions(state.submissions, query)
            )
        }
    }

    private fun filterSubmissions(
        submissions: List<SubmissionItemUiState>,
        query: String
    ): List<SubmissionItemUiState> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            submissions
        } else {
            submissions.filter { item ->
                item.submission.activityTitle.contains(trimmed, ignoreCase = true) ||
                    item.courseTitle.contains(trimmed, ignoreCase = true)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val session = sessionRepository.session.first()
            val studentId = session?.studentId

            if (studentId == null) {
                _state.update { it.copy(isLoading = false, errorMessage = "Sesión no encontrada") }
            } else {
                submissionRepository.getSubmissionsByStudentId(studentId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                        is Resource.Success -> {
                            val rawSubmissions = resource.data ?: emptyList()
                            val initialItems = rawSubmissions.map { raw ->
                                val existing = _state.value.submissions.find { it.submission.id == raw.id }
                                existing?.copy(submission = raw) ?: SubmissionItemUiState(submission = raw)
                            }
                            _state.update { state ->
                                state.copy(
                                    isLoading = false,
                                    submissions = initialItems,
                                    filteredSubmissions = filterSubmissions(initialItems, state.searchQuery)
                                )
                            }
                            if (rawSubmissions.isNotEmpty()) {
                                loadMetadata(studentId, rawSubmissions)
                            }
                        }
                        is Resource.Error -> _state.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    private fun loadMetadata(studentId: String, submissions: List<Submission>) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollments(studentId).collect { result ->
                val enrollmentMap = if (result is Resource.Success && result.data != null) {
                    result.data.associateBy { it.id }
                } else {
                    emptyMap()
                }

                _state.update { state ->
                    val updated = state.submissions.map { item ->
                        val enrollment = enrollmentMap[item.submission.enrollmentId]
                        val courseTitle = enrollment?.courseTitle ?: ""
                        item.copy(courseTitle = courseTitle)
                    }
                    state.copy(
                        submissions = updated,
                        filteredSubmissions = filterSubmissions(updated, state.searchQuery)
                    )
                }

                val courseIds = enrollmentMap.values.map { it.courseId }.distinct()
                courseIds.forEach { courseId ->
                    launch {
                        courseRepository.getCourseContent(courseId).collect { courseRes ->
                            if (courseRes is Resource.Success && courseRes.data != null) {
                                val course = courseRes.data
                                val activityMap = course.units
                                    .flatMap { it.themes }
                                    .flatMap { it.activities }
                                    .associateBy { it.id }

                                _state.update { state ->
                                    val updated = state.submissions.map { item ->
                                        val enrollment = enrollmentMap[item.submission.enrollmentId]
                                        if (enrollment?.courseId == courseId) {
                                            val activity = activityMap[item.submission.activityId]
                                            val strategy = activity?.approvalStrategy ?: ApprovalStrategy.AUTO
                                            val required = if (strategy == ApprovalStrategy.PEER_REVIEW) course.requiredPeerReviews else 0
                                            item.copy(
                                                approvalStrategy = strategy,
                                                requiredReviewsCount = required
                                            )
                                        } else {
                                            item
                                        }
                                    }
                                    state.copy(
                                        submissions = updated,
                                        filteredSubmissions = filterSubmissions(updated, state.searchQuery)
                                    )
                                }
                            }
                        }
                    }
                }

                submissions.forEach { submission ->
                    launch {
                        submissionRepository.getSubmissionReviews(studentId, submission.id).collect { reviewsRes ->
                            if (reviewsRes is Resource.Success && reviewsRes.data != null) {
                                val reviews = reviewsRes.data
                                val positiveCount = reviews.count { it.isApproved }

                                _state.update { state ->
                                    val updated = state.submissions.map { item ->
                                        if (item.submission.id == submission.id) {
                                            item.copy(
                                                positiveReviewsCount = positiveCount,
                                                hasReviewsInfo = reviews.isNotEmpty()
                                            )
                                        } else {
                                            item
                                        }
                                    }
                                    state.copy(
                                        submissions = updated,
                                        filteredSubmissions = filterSubmissions(updated, state.searchQuery)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}