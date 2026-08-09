package com.sagrd.mentorly.presentation.course.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseDetailUiState())
    val state: StateFlow<CourseDetailUiState> = _state.asStateFlow()

    private var courseId: String? = null
    private var loadJob: Job? = null

    fun onEvent(event: CourseDetailUiEvent) {
        when (event) {
            is CourseDetailUiEvent.LoadCourseContent -> {
                courseId = event.courseId
                loadCourseContent(event.courseId)
            }

            CourseDetailUiEvent.Retry -> {
                courseId?.let(::loadCourseContent)
            }
        }
    }

    private fun loadCourseContent(courseId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            courseRepository.getCourseContent(courseId).collect { resource ->
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
                                course = resource.data,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                                    ?: "No se pudo cargar el contenido del curso."
                            )
                        }
                    }
                }
            }
        }
    }
}
