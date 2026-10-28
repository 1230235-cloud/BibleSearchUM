package com.example.blesearchum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BibleUiState())
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    init {
        fetchBooks()
    }

    fun fetchBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val bookList = RetrofitInstance.api.getBooks()
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
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: return
        _uiState.update { it.copy(selectedVerse = verse, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getVerse(currentBook.bookId, currentChapter, verse)
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

    fun clearSelection() {
        _uiState.update {
            BibleUiState(books = it.books)
        }
    }
}
