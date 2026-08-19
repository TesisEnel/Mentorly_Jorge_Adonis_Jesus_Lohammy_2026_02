package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.QuizApi
import com.sagrd.mentorly.data.remote.dto.quiz.AdminQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.CreateQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.QuizAttemptDto
import com.sagrd.mentorly.data.remote.dto.quiz.QuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.SubmitQuizAttemptDto
import com.sagrd.mentorly.data.remote.dto.quiz.UpdateQuizQuestionDto
import retrofit2.HttpException
import javax.inject.Inject

class QuizRemoteDataSource @Inject constructor(
    private val api: QuizApi
) {

    suspend fun getQuizQuestions(activityId: String): Result<List<QuizQuestionDto>> {
        return try {
            val response = api.getQuizQuestions(activityId)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun createQuizQuestion(
        adminId: String,
        activityId: String,
        question: CreateQuizQuestionDto
    ): Result<QuizQuestionDto> {
        return try {
            val response = api.createQuizQuestion(adminId, activityId, question)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun getAdminQuizQuestions(
        adminId: String,
        activityId: String
    ): Result<List<AdminQuizQuestionDto>> {
        return try {
            val response = api.getAdminQuizQuestions(adminId, activityId)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun updateQuizQuestion(
        adminId: String,
        questionId: String,
        question: UpdateQuizQuestionDto
    ): Result<AdminQuizQuestionDto> {
        return try {
            val response = api.updateQuizQuestion(adminId, questionId, question)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun deleteQuizQuestion(
        adminId: String,
        questionId: String
    ): Result<Unit> {
        return try {
            val response = api.deleteQuizQuestion(adminId, questionId)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun submitQuizAttempt(
        enrollmentId: String,
        activityId: String,
        attempt: SubmitQuizAttemptDto
    ): Result<QuizAttemptDto> {
        return try {
            val response = api.submitQuizAttempt(enrollmentId, activityId, attempt)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }
}
