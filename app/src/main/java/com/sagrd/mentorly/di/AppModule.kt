package com.sagrd.mentorly.di

import com.sagrd.mentorly.data.remote.api.*
import com.sagrd.mentorly.data.remote.remotedatasource.*
import com.sagrd.mentorly.data.repository.*
import com.sagrd.mentorly.data.repository.analytics.AnalyticsRepositoryImpl
import com.sagrd.mentorly.domain.repository.*
import com.sagrd.mentorly.domain.repository.analytics.AnalyticsRepository
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
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
        val baseUrl = "https://mentorlyapi-ap2-f8gfgwh3efchgzfn.eastus2-01.azurewebsites.net/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideCourseMentorlyApi(retrofit: Retrofit): CourseMentorlyApi {
        return retrofit.create(CourseMentorlyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSubmissionApi(retrofit: Retrofit): SubmissionMentorlyApi {
        return retrofit.create(SubmissionMentorlyApi::class.java)
    }

    @Provides
    @Singleton
    fun providePeerReviewApi(retrofit: Retrofit): PeerReviewMentorlyApi {
        return retrofit.create(PeerReviewMentorlyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEnrollmentApi(retrofit: Retrofit): EnrollmentMentorlyApi {
        return retrofit.create(EnrollmentMentorlyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStudentApi(retrofit: Retrofit): StudentMentorlyApi {
        return retrofit.create(StudentMentorlyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAnalyticsApi(retrofit: Retrofit): AnalyticsApi {
        return retrofit.create(AnalyticsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAnalyticsRemoteDataSource(api: AnalyticsApi): AnalyticsRemoteDataSource {
        return AnalyticsRemoteDataSource(api)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(remoteDataSource: CourseRemoteDataSource): CourseRepository {
        return CourseRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun providePeerReviewRepository(remoteDataSource: PeerReviewRemoteDataSource): PeerReviewRepository {
        return PeerReviewRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideSubmissionRepository(remoteDataSource: SubmissionRemoteDataSource): SubmissionRepository {
        return SubmissionRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideEnrollmentRepository(remoteDataSource: EnrollmentRemoteDataSource): EnrollmentRepository {
        return EnrollmentRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideStudentRepository(api: StudentMentorlyApi): StudentRepository {
        return StudentRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(remoteDataSource: AnalyticsRemoteDataSource): AnalyticsRepository {
        return AnalyticsRepositoryImpl(remoteDataSource)
    }
}
