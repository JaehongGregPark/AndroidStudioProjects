package com.example.worldradio.di

import com.example.worldradio.data.remote.RadioApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt 네트워크 모듈
 *
 * Retrofit, API 객체를
 * 앱 전역에서 주입 가능하도록 제공
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://de1.api.radio-browser.info/json/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideRadioApi(retrofit: Retrofit): RadioApi =
        retrofit.create(RadioApi::class.java)
}