package com.example.blesearchum.model

import com.google.gson.annotations.SerializedName

data class BibleBook(
    @SerializedName("bookid") val bookId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("chapters") val chapters: Int
)

data class BibleVerse(
    @SerializedName("pk") val pk: Int,
    @SerializedName("verse") val verse: Int,
    @SerializedName("text") val text: String
)

data class BibleTranslation(
    val code: String,
    val name: String
)

data class FavoriteVerse(
    val id: String,
    val translation: String,
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)
