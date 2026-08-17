package com.sagrd.mentorly.data.repository.quiz

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.quiz.UpdateQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.CreateQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.SubmitQuizAttemptDto
import com.sagrd.mentorly.data.remote.remotedatasource.QuizRemoteDataSource
import com.sagrd.mentorly.domain.model.quiz.QuizAttempt
import com.sagrd.mentorly.domain.model.quiz.AdminQuizQuestion
import com.sagrd.mentorly.domain.model.quiz.QuizQuestion
import com.sagrd.mentorly.domain.repository.quiz.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuizRemoteDataSource
) : QuizRepository {

    override fun getQuizQuestions(
        activityId: String
    ): Flow<Resource<List<QuizQuestion>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getQuizQuestions(activityId)
            .onSuccess { questions ->
                emit(Resource.Success(questions.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las preguntas."))
            }
    }

    override fun createQuizQuestion(
        adminId: String,
        activityId: String,
        question: CreateQuizQuestionDto
    ): Flow<Resource<QuizQuestion>> = flow {
        emit(Resource.Loading())

        remoteDataSource.createQuizQuestion(adminId, activityId, question)
            .onSuccess { createdQuestion ->
                emit(Resource.Success(createdQuestion.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear la pregunta."))
            }
    }

    override fun getAdminQuizQuestions(
        adminId: String,
        activityId: String
    ): Flow<Resource<List<AdminQuizQuestion>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getAdminQuizQuestions(adminId, activityId)
            .onSuccess { questions ->
                emit(Resource.Success(questions.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las preguntas."))
            }
    }

    override fun updateQuizQuestion(
        adminId: String,
        questionId: String,
        question: UpdateQuizQuestionDto
    ): Flow<Resource<AdminQuizQuestion>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateQuizQuestion(adminId, questionId, question)
            .onSuccess { updatedQuestion ->
                emit(Resource.Success(updatedQuestion.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo actualizar la pregunta."))
            }
    }

    override fun deleteQuizQuestion(
        adminId: String,
        questionId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.deleteQuizQuestion(adminId, questionId)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo eliminar la pregunta."))
            }
    }

    override fun submitQuizAttempt(
        enrollmentId: String,
        activityId: String,
        attempt: SubmitQuizAttemptDto
    ): Flow<Resource<QuizAttempt>> = flow {
        emit(Resource.Loading())

        remoteDataSource.submitQuizAttempt(enrollmentId, activityId, attempt)
            .onSuccess { quizAttempt ->
                emit(Resource.Success(quizAttempt.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo enviar el intento."))
            }
    }
}
