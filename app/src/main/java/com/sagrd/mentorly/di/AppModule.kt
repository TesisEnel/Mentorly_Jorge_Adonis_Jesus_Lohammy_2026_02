package com.sagrd.mentorly.di

import com.sagrd.mentorly.data.remote.api.CourseApi
import com.sagrd.mentorly.data.remote.api.StudentApi
import com.sagrd.mentorly.data.remote.remotedatasource.CourseRemoteDataSource
import com.sagrd.mentorly.data.remote.remotedatasource.StudentRemoteDataSource
import com.sagrd.mentorly.data.repository.course.CourseRepositoryImpl
import com.sagrd.mentorly.data.repository.student.StudentRepositoryImpl
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
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
}