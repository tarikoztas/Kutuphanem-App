package com.tarik.kutuphanem.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tarik.kutuphanem.BuildConfig
import com.tarik.kutuphanem.data.remote.api.GoogleBooksService
import com.tarik.kutuphanem.data.remote.api.OpenLibraryService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GOOGLE_BOOKS_URL = "https://www.googleapis.com/"
    private const val OPEN_LIBRARY_URL = "https://openlibrary.org/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val gunluk = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(gunluk)
            .addInterceptor { zincir ->
                val istek = zincir.request()
                val anahtar = BuildConfig.GOOGLE_BOOKS_API_KEY

                // Anahtar sadece Google isteklerine, sadece tanimliysa eklenir.
                val yeniIstek = if (
                    anahtar.isNotBlank() && istek.url.host.contains("googleapis.com")
                ) {
                    istek.newBuilder()
                        .url(istek.url.newBuilder().addQueryParameter("key", anahtar).build())
                        .build()
                } else {
                    istek
                }

                zincir.proceed(yeniIstek)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleBooksService(client: OkHttpClient, json: Json): GoogleBooksService =
        Retrofit.Builder()
            .baseUrl(GOOGLE_BOOKS_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GoogleBooksService::class.java)

    @Provides
    @Singleton
    fun provideOpenLibraryService(client: OkHttpClient, json: Json): OpenLibraryService =
        Retrofit.Builder()
            .baseUrl(OPEN_LIBRARY_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenLibraryService::class.java)
}
