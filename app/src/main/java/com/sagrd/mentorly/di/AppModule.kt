package com.sagrd.mentorly.di

import com.sagrd.mentorly.data.remote.api.CourseApi
import com.sagrd.mentorly.data.remote.api.EnrollmentApi
import com.sagrd.mentorly.data.remote.api.EnrollmentProgressApi
import com.sagrd.mentorly.data.remote.api.StudentApi
import com.sagrd.mentorly.data.remote.api.SubmissionApi
import com.sagrd.mentorly.data.remote.remotedatasource.CourseRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentProgressRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.StudentRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.SubmissionRemoteDataSource
import com.sagrd.mentorly.data.local.session.SessionPreferences
import com.sagrd.mentorly.data.repository.course.CourseRepositoryImpl
import com.sagrd.mentorly.data.repository.enrollment.EnrollmentRepositoryImpl
import com.sagrd.mentorly.data.repository.progress.EnrollmentProgressRepositoryImpl
import com.sagrd.mentorly.data.repository.session.SessionRepositoryImpl
import com.sagrd.mentorly.data.repository.student.StudentRepositoryImpl
import com.sagrd.mentorly.data.repository.submission.SubmissionRepositoryImpl
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL =
        "https://mentorlyapi-ap2-f8gfgwh3efchgzfn.eastus2-01.azurewebsites.net/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideCourseApi(retrofit: Retrofit): CourseApi {
        return retrofit.create(CourseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStudentApi(retrofit: Retrofit): StudentApi {
        return retrofit.create(StudentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEnrollmentApi(retrofit: Retrofit): EnrollmentApi {
        return retrofit.create(EnrollmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEnrollmentProgressApi(retrofit: Retrofit): EnrollmentProgressApi {
        return retrofit.create(EnrollmentProgressApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSubmissionApi(retrofit: Retrofit): SubmissionApi {
        return retrofit.create(SubmissionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseRemoteDataSource(
        courseApi: CourseApi
    ): CourseRemoteDataSource {
        return CourseRemoteDataSource(courseApi)
    }

    @Provides
    @Singleton
    fun provideStudentRemoteDataSource(
        studentApi: StudentApi
    ): StudentRemoteDataSource {
        return StudentRemoteDataSource(studentApi)
    }

    @Provides
    @Singleton
    fun provideEnrollmentRemoteDataSource(
        enrollmentApi: EnrollmentApi
    ): EnrollmentRemoteDataSource {
        return EnrollmentRemoteDataSource(enrollmentApi)
    }

    @Provides
    @Singleton
    fun provideEnrollmentProgressRemoteDataSource(
        enrollmentProgressApi: EnrollmentProgressApi
    ): EnrollmentProgressRemoteDataSource {
        return EnrollmentProgressRemoteDataSource(enrollmentProgressApi)
    }

    @Provides
    @Singleton
    fun provideSubmissionRemoteDataSource(
        submissionApi: SubmissionApi
    ): SubmissionRemoteDataSource {
        return SubmissionRemoteDataSource(submissionApi)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(
        remoteDataSource: CourseRemoteDataSource
    ): CourseRepository {
        return CourseRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideStudentRepository(
        remoteDataSource: StudentRemoteDataSource
    ): StudentRepository {
        return StudentRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideEnrollmentRepository(
        remoteDataSource: EnrollmentRemoteDataSource
    ): EnrollmentRepository {
        return EnrollmentRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideEnrollmentProgressRepository(
        remoteDataSource: EnrollmentProgressRemoteDataSource
    ): EnrollmentProgressRepository {
        return EnrollmentProgressRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideSubmissionRepository(
        remoteDataSource: SubmissionRemoteDataSource
    ): SubmissionRepository {
        return SubmissionRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(
        sessionPreferences: SessionPreferences
    ): SessionRepository {
        return SessionRepositoryImpl(sessionPreferences)
    }
}
