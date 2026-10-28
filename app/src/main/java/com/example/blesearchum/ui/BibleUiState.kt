package com.example.blesearchum.ui

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleTranslation
import com.example.blesearchum.model.BibleVerse
import com.example.blesearchum.model.FavoriteVerse

val SupportedTranslations = listOf(
    BibleTranslation("RVR1960", "Reina Valera 1960 (RVR1960)"),
    BibleTranslation("NVI", "Nueva Versión Internacional (NVI)"),
    BibleTranslation("LBLA", "La Biblia de las Américas (LBLA)")
)

data class BibleUiState(
    val availableTranslations: List<BibleTranslation> = SupportedTranslations,
    val selectedTranslation: BibleTranslation = SupportedTranslations.first(),
    val books: List<BibleBook> = emptyList(),
    val availableChapters: List<Int> = emptyList(),
    val availableVerses: List<Int> = emptyList(),
    val selectedBook: BibleBook? = null,
    val selectedChapter: Int? = null,
    val selectedVerse: Int? = null,
    val currentVerseContent: BibleVerse? = null,
    val favorites: List<FavoriteVerse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isCurrentVerseFavorite: Boolean
        get() {
            val book = selectedBook ?: return false
            val chapter = selectedChapter ?: return false
            val verse = currentVerseContent ?: return false
            val id = "${selectedTranslation.code}_${book.bookId}_${chapter}_${verse.verse}"
            return favorites.any { it.id == id }
        }
}
