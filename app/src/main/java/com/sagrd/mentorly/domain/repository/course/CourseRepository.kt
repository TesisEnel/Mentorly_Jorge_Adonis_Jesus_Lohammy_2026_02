package com.sagrd.mentorly.domain.repository.course

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.course.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getCourses(): Flow<Resource<List<Course>>>
    fun getCourseById(courseId: String): Flow<Resource<Course>>
    fun getCourseContent(courseId: String): Flow<Resource<Course>>
}