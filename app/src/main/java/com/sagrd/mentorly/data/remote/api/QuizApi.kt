package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.quiz.AdminQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.CreateQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.QuizAttemptDto
import com.sagrd.mentorly.data.remote.dto.quiz.QuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.SubmitQuizAttemptDto
import com.sagrd.mentorly.data.remote.dto.quiz.UpdateQuizQuestionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface QuizApi {

    @GET("api/activities/{activityId}/quiz")
    suspend fun getQuizQuestions(
        @Path("activityId") activityId: String
    ): Response<List<QuizQuestionDto>>

    @POST("api/admins/{adminId}/activities/{activityId}/quiz/questions")
    suspend fun createQuizQuestion(
        @Path("adminId") adminId: String,
        @Path("activityId") activityId: String,
        @Body question: CreateQuizQuestionDto
    ): Response<QuizQuestionDto>

    @GET("api/admins/{adminId}/activities/{activityId}/quiz/questions")
    suspend fun getAdminQuizQuestions(
        @Path("adminId") adminId: String,
        @Path("activityId") activityId: String
    ): Response<List<AdminQuizQuestionDto>>

    @PUT("api/admins/{adminId}/quiz/questions/{questionId}")
    suspend fun updateQuizQuestion(
        @Path("adminId") adminId: String,
        @Path("questionId") questionId: String,
        @Body question: UpdateQuizQuestionDto
    ): Response<AdminQuizQuestionDto>

    @DELETE("api/admins/{adminId}/quiz/questions/{questionId}")
    suspend fun deleteQuizQuestion(
        @Path("adminId") adminId: String,
        @Path("questionId") questionId: String
    ): Response<Unit>

    @POST("api/enrollments/{enrollmentId}/activities/{activityId}/quiz-attempts")
    suspend fun submitQuizAttempt(
        @Path("enrollmentId") enrollmentId: String,
        @Path("activityId") activityId: String,
        @Body attempt: SubmitQuizAttemptDto
    ): Response<QuizAttemptDto>
}
