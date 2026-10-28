package com.example.blesearchum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blesearchum.data.FavoritesRepository
import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleTranslation
import com.example.blesearchum.model.FavoriteVerse
import com.example.blesearchum.network.BibleApiService
import com.example.blesearchum.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibleViewModel(
    private val apiService: BibleApiService = RetrofitInstance.api,
    private val favoritesRepository: FavoritesRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(BibleUiState())
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    init {
        fetchBooks()
        observeFavorites()
    }

    private fun observeFavorites() {
        favoritesRepository?.let { repo ->
            viewModelScope.launch {
                repo.favoritesFlow.collect { savedFavorites ->
                    _uiState.update { it.copy(favorites = savedFavorites) }
                }
            }
        }
    }

    fun onTranslationSelected(translation: BibleTranslation) {
        if (_uiState.value.selectedTranslation.code == translation.code) return
        _uiState.update {
            it.copy(
                selectedTranslation = translation,
                selectedBook = null,
                selectedChapter = null,
                selectedVerse = null,
                availableChapters = emptyList(),
                availableVerses = emptyList(),
                currentVerseContent = null,
                errorMessage = null
            )
        }
        fetchBooks(translation.code)
    }

    fun fetchBooks(translationCode: String = _uiState.value.selectedTranslation.code) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val bookList = apiService.getBooksByTranslation(translationCode)
                _uiState.update {
                    it.copy(books = bookList, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar libros: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onBookSelected(book: BibleBook) {
        val chaptersList = (1..book.chapters).toList()
        _uiState.update {
            it.copy(
                selectedBook = book,
                availableChapters = chaptersList,
                selectedChapter = null,
                selectedVerse = null,
                availableVerses = emptyList(),
                currentVerseContent = null
            )
        }
    }

    fun onChapterSelected(chapter: Int) {
        // Asumimos un máximo genérico de 150 versículos por capítulo para consulta
        val versesList = (1..150).toList()
        _uiState.update {
            it.copy(
                selectedChapter = chapter,
                availableVerses = versesList,
                selectedVerse = null,
                currentVerseContent = null
            )
        }
    }

    fun onVerseSelected(verse: Int) {
        val state = _uiState.value
        val currentBook = state.selectedBook ?: return
        val currentChapter = state.selectedChapter ?: return
        val currentTranslation = state.selectedTranslation.code

        _uiState.update { it.copy(selectedVerse = verse, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = apiService.getVerseByTranslation(
                    translation = currentTranslation,
                    bookId = currentBook.bookId,
                    chapter = currentChapter,
                    verse = verse
                )
                _uiState.update {
                    it.copy(currentVerseContent = result, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo obtener el versículo solicitado."
                    )
                }
            }
        }
    }

    fun toggleCurrentVerseFavorite() {
        val state = _uiState.value
        val book = state.selectedBook ?: return
        val chapter = state.selectedChapter ?: return
        val verseContent = state.currentVerseContent ?: return
        val translationCode = state.selectedTranslation.code

        val favorite = FavoriteVerse(
            id = "${translationCode}_${book.bookId}_${chapter}_${verseContent.verse}",
            translation = translationCode,
            bookId = book.bookId,
            bookName = book.name,
            chapter = chapter,
            verse = verseContent.verse,
            text = verseContent.text.trim()
        )

        if (favoritesRepository != null) {
            viewModelScope.launch {
                favoritesRepository.toggleFavorite(favorite)
            }
        } else {
            _uiState.update { current ->
                val exists = current.favorites.any { it.id == favorite.id }
                val updated = if (exists) {
                    current.favorites.filterNot { it.id == favorite.id }
                } else {
                    listOf(favorite) + current.favorites
                }
                current.copy(favorites = updated)
            }
        }
    }

    fun removeFavorite(id: String) {
        if (favoritesRepository != null) {
            viewModelScope.launch {
                favoritesRepository.removeFavorite(id)
            }
        } else {
            _uiState.update { current ->
                current.copy(favorites = current.favorites.filterNot { it.id == id })
            }
        }
    }

    fun clearSelection() {
        _uiState.update {
            BibleUiState(
                availableTranslations = it.availableTranslations,
                selectedTranslation = it.selectedTranslation,
                books = it.books,
                favorites = it.favorites
            )
        }
    }

    companion object {
        fun provideFactory(favoritesRepository: FavoritesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BibleViewModel(
                        apiService = RetrofitInstance.api,
                        favoritesRepository = favoritesRepository
                    ) as T
                }
            }
    }
}
