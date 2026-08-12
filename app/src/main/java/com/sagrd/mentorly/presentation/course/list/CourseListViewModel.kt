package com.sagrd.mentorly.presentation.course.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseListUiState())
    val state: StateFlow<CourseListUiState> = _state.asStateFlow()

    init {
        loadCourses()
    }

    fun onEvent(event: CourseListUiEvent) {
        when (event) {
            CourseListUiEvent.Refresh -> loadCourses()
        }
    }

    private fun loadCourses() {
        viewModelScope.launch {
            courseRepository.getCourses().collect { resource ->
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
                                courses = resource.data.orEmpty(),
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                                    ?: "No se pudieron cargar los cursos."
                            )
                        }
                    }
                }
            }
        }
    }
}
