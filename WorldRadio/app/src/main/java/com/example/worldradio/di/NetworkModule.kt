package com.example.worldradio.di

import com.example.worldradio.data.remote.RadioApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt DI Module
 *
 * 앱 전역에서 사용할 네트워크 객체들을 제공한다.
 *
 * @InstallIn(SingletonComponent::class)
 *  → 앱이 실행되는 동안 단 하나의 인스턴스만 유지
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * 반드시 '/' 로 끝나야 한다.
     *
     * Radio Browser 공식 JSON API 경로 포함
     */
    private const val BASE_URL =
        "https://all.api.radio-browser.info/json/"

    /**
     * OkHttpClient 생성
     *
     * - 타임아웃 30초
     * - 연결 실패 시 자동 재시도
     * - HTTP 로그 출력 (디버깅용)
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        // 네트워크 요청/응답 로그 확인용
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)   // 서버 연결 대기 시간
            .readTimeout(30, TimeUnit.SECONDS)      // 서버 응답 대기 시간
            .writeTimeout(30, TimeUnit.SECONDS)     // 요청 전송 대기 시간
            .retryOnConnectionFailure(true)         // 네트워크 실패 시 자동 재시도
            .addInterceptor(logging)
            .build()
    }

    /**
     * Retrofit 생성
     *
     * - BASE_URL 설정
     * - OkHttpClient 연결
     * - Gson 변환기 추가
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * RadioApi 구현체 제공
     *
     * Retrofit이 인터페이스를 기반으로
     * 실제 네트워크 구현 객체를 생성한다.
     */
    @Provides
    @Singleton
    fun provideRadioApi(retrofit: Retrofit): RadioApi {
        return retrofit.create(RadioApi::class.java)
    }
}