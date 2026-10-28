package com.example.blesearchum.network

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleVerse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface BibleApiService {
    @GET("get-books/RVR1960/")
    suspend fun getBooks(): List<BibleBook>

    @GET("get-verse/RVR1960/{bookId}/{chapter}/{verse}/")
    suspend fun getVerse(
        @Path("bookId") bookId: Int,
        @Path("chapter") chapter: Int,
        @Path("verse") verse: Int
    ): BibleVerse

    // Reto 3: Endpoints dinámicos por traducción (RV1960, NVI, LBLA)
    @GET("get-books/{translation}/")
    suspend fun getBooksByTranslation(
        @Path("translation") translation: String
    ): List<BibleBook>

    @GET("get-verse/{translation}/{bookId}/{chapter}/{verse}/")
    suspend fun getVerseByTranslation(
        @Path("translation") translation: String,
        @Path("bookId") bookId: Int,
        @Path("chapter") chapter: Int,
        @Path("verse") verse: Int
    ): BibleVerse
}

object RetrofitInstance {
    private const val BASE_URL = "https://bolls.life/"

    // Bolls Bible uses "RV1960" internally; normalize "RVR1960" so the lab guide endpoint works seamlessly
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val normalizedPath = originalRequest.url().encodedPath()
                    .replace("/RVR1960/", "/RV1960/")
                val newUrl = originalRequest.url().newBuilder()
                    .encodedPath(normalizedPath)
                    .build()
                chain.proceed(originalRequest.newBuilder().url(newUrl).build())
            }
            .build()
    }

    val api: BibleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BibleApiService::class.java)
    }
}
