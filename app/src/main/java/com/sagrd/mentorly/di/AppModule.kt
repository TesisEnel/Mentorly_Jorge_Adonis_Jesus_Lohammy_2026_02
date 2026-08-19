package com.sagrd.mentorly.di

import com.sagrd.mentorly.data.remote.api.ActivityApi
import com.sagrd.mentorly.data.remote.api.AnalyticsApi
import com.sagrd.mentorly.data.remote.api.CourseApi
import com.sagrd.mentorly.data.remote.api.CourseCommunityApi
import com.sagrd.mentorly.data.remote.api.EnrollmentApi
import com.sagrd.mentorly.data.remote.api.EnrollmentProgressApi
import com.sagrd.mentorly.data.remote.api.PeerReviewApi
import com.sagrd.mentorly.data.remote.api.QuizApi
import com.sagrd.mentorly.data.remote.api.StudentApi
import com.sagrd.mentorly.data.remote.api.SubmissionApi
import com.sagrd.mentorly.data.remote.api.ThemeApi
import com.sagrd.mentorly.data.remote.api.UnitApi
import com.sagrd.mentorly.data.remote.remotedatasource.ActivityRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.AnalyticsRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.CourseRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.CourseCommunityRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentProgressRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.PeerReviewRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.QuizRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.StudentRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.SubmissionRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.ThemeRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.UnitRemoteDataSource
import com.sagrd.mentorly.data.repository.activity.ActivityRepositoryImpl
import com.sagrd.mentorly.data.repository.analytics.AnalyticsRepositoryImpl
import com.sagrd.mentorly.data.repository.course.CourseRepositoryImpl
import com.sagrd.mentorly.data.repository.community.CourseCommunityRepositoryImpl
import com.sagrd.mentorly.data.repository.enrollment.EnrollmentRepositoryImpl
import com.sagrd.mentorly.data.repository.progress.EnrollmentProgressRepositoryImpl
import com.sagrd.mentorly.data.repository.peerreview.PeerReviewRepositoryImpl
import com.sagrd.mentorly.data.repository.quiz.QuizRepositoryImpl
import com.sagrd.mentorly.data.repository.session.SessionRepositoryImpl
import com.sagrd.mentorly.data.repository.student.StudentRepositoryImpl
import com.sagrd.mentorly.data.repository.submission.SubmissionRepositoryImpl
import com.sagrd.mentorly.data.repository.theme.ThemeRepositoryImpl
import com.sagrd.mentorly.data.repository.unit.UnitRepositoryImpl
import com.sagrd.mentorly.domain.repository.activity.ActivityRepository
import com.sagrd.mentorly.domain.repository.analytics.AnalyticsRepository
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.community.CourseCommunityRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
import com.sagrd.mentorly.domain.repository.quiz.QuizRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import com.sagrd.mentorly.domain.repository.theme.ThemeRepository
import com.sagrd.mentorly.domain.repository.unit.UnitRepository
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
    fun provideCourseApi(moshi: Moshi): CourseApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CourseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseCommunityApi(moshi: Moshi): CourseCommunityApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CourseCommunityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideActivityApi(moshi: Moshi): ActivityApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ActivityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAnalyticsApi(moshi: Moshi): AnalyticsApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AnalyticsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStudentApi(moshi: Moshi): StudentApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(StudentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEnrollmentApi(moshi: Moshi): EnrollmentApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EnrollmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEnrollmentProgressApi(moshi: Moshi): EnrollmentProgressApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EnrollmentProgressApi::class.java)
    }

    @Provides
    @Singleton
    fun providePeerReviewApi(moshi: Moshi): PeerReviewApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PeerReviewApi::class.java)
    }

    @Provides
    @Singleton
    fun provideQuizApi(moshi: Moshi): QuizApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuizApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSubmissionApi(moshi: Moshi): SubmissionApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SubmissionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideThemeApi(moshi: Moshi): ThemeApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ThemeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUnitApi(moshi: Moshi): UnitApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UnitApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(api: CourseApi): CourseRepository {
        return CourseRepositoryImpl(CourseRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideCourseCommunityRepository(api: CourseCommunityApi): CourseCommunityRepository {
        return CourseCommunityRepositoryImpl(CourseCommunityRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideActivityRepository(api: ActivityApi): ActivityRepository {
        return ActivityRepositoryImpl(ActivityRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(api: AnalyticsApi): AnalyticsRepository {
        return AnalyticsRepositoryImpl(AnalyticsRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideStudentRepository(api: StudentApi): StudentRepository {
        return StudentRepositoryImpl(StudentRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideEnrollmentRepository(api: EnrollmentApi): EnrollmentRepository {
        return EnrollmentRepositoryImpl(EnrollmentRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideEnrollmentProgressRepository(api: EnrollmentProgressApi): EnrollmentProgressRepository {
        return EnrollmentProgressRepositoryImpl(EnrollmentProgressRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun providePeerReviewRepository(api: PeerReviewApi): PeerReviewRepository {
        return PeerReviewRepositoryImpl(PeerReviewRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideQuizRepository(api: QuizApi): QuizRepository {
        return QuizRepositoryImpl(QuizRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideSubmissionRepository(api: SubmissionApi): SubmissionRepository {
        return SubmissionRepositoryImpl(SubmissionRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideThemeRepository(api: ThemeApi): ThemeRepository {
        return ThemeRepositoryImpl(ThemeRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideUnitRepository(api: UnitApi): UnitRepository {
        return UnitRepositoryImpl(UnitRemoteDataSource(api))
    }

    @Provides
    @Singleton
    fun provideSessionRepository(): SessionRepository {
        return SessionRepositoryImpl()
    }
}
