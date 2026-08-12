package com.sagrd.mentorly.di

import com.sagrd.mentorly.data.remote.api.AnalyticsApi
import com.sagrd.mentorly.data.remote.remotedatasource.AnalyticsRemoteDataSource
import com.sagrd.mentorly.data.repository.analytics.AnalyticsRepositoryImpl
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
    fun provideAnalyticsRepository(remoteDataSource: AnalyticsRemoteDataSource): AnalyticsRepository {
        return AnalyticsRepositoryImpl(remoteDataSource)
    }
}
