package com.sagrd.mentorly.domain.repository.course

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.course.CreateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCoursePublicationDto
import com.sagrd.mentorly.domain.model.course.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getCourses(): Flow<Resource<List<Course>>>
    fun getCourseById(courseId: String): Flow<Resource<Course>>
    fun getCourseContent(courseId: String): Flow<Resource<Course>>

    fun createCourse(
        adminId: String,
        course: CreateCourseDto
    ): Flow<Resource<Course>>

    fun updateCourse(
        adminId: String,
        courseId: String,
        course: UpdateCourseDto
    ): Flow<Resource<Unit>>

    fun updateCoursePublication(
        adminId: String,
        courseId: String,
        publication: UpdateCoursePublicationDto
    ): Flow<Resource<Unit>>

    fun deleteCourse(
        adminId: String,
        courseId: String
    ): Flow<Resource<Unit>>
}