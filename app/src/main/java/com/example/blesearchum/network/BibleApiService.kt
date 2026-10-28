package com.example.blesearchum.network

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleVerse
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
}

object RetrofitInstance {
    private const val BASE_URL = "https://bolls.life/"
    val api: BibleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BibleApiService::class.java)
    }
}
