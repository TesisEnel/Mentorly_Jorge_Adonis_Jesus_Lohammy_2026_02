package com.sagrd.mentorly.domain.repository.quiz

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.quiz.CreateQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.SubmitQuizAttemptDto
import com.sagrd.mentorly.data.remote.dto.quiz.UpdateQuizQuestionDto
import com.sagrd.mentorly.domain.model.quiz.AdminQuizQuestion
import com.sagrd.mentorly.domain.model.quiz.QuizAttempt
import com.sagrd.mentorly.domain.model.quiz.QuizQuestion
import kotlinx.coroutines.flow.Flow

interface QuizRepository {

    fun getQuizQuestions(activityId: String): Flow<Resource<List<QuizQuestion>>>

    fun createQuizQuestion(
        adminId: String,
        activityId: String,
        question: CreateQuizQuestionDto
    ): Flow<Resource<QuizQuestion>>

    fun getAdminQuizQuestions(
        adminId: String,
        activityId: String
    ): Flow<Resource<List<AdminQuizQuestion>>>

    fun updateQuizQuestion(
        adminId: String,
        questionId: String,
        question: UpdateQuizQuestionDto
    ): Flow<Resource<AdminQuizQuestion>>

    fun deleteQuizQuestion(
        adminId: String,
        questionId: String
    ): Flow<Resource<Unit>>

    fun submitQuizAttempt(
        enrollmentId: String,
        activityId: String,
        attempt: SubmitQuizAttemptDto
    ): Flow<Resource<QuizAttempt>>
}
