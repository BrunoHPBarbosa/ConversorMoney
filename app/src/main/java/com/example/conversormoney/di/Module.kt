package com.example.conversormoney.di

import com.example.conversormoney.model.remote.ServiceApi
import com.example.conversormoney.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Singleton
    @Provides
    @Named("mainRetrofit")
    fun provideMainRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Singleton
    @Provides
    @Named("flagsRetrofit")
    fun provideFlagsRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_FLAGS)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Singleton
    @Provides
    @Named("mainServiceApi")
    fun provideMainServiceApi(
        @Named("mainRetrofit") retrofit: Retrofit
    ): ServiceApi {
        return retrofit.create(ServiceApi::class.java)
    }

    @Singleton
    @Provides
    @Named("flagsServiceApi")
    fun provideFlagsServiceApi(
        @Named("flagsRetrofit") retrofit: Retrofit
    ): ServiceApi {
        return retrofit.create(ServiceApi::class.java)
    }
}
