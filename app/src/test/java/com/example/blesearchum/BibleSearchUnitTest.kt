package com.example.blesearchum

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleTranslation
import com.example.blesearchum.model.BibleVerse
import com.example.blesearchum.network.BibleApiService
import com.example.blesearchum.network.RetrofitInstance
import com.example.blesearchum.ui.BibleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BibleSearchUnitTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeBibleApiService : BibleApiService {
        override suspend fun getBooks(): List<BibleBook> = getBooksByTranslation("RVR1960")

        override suspend fun getVerse(bookId: Int, chapter: Int, verse: Int): BibleVerse =
            getVerseByTranslation("RVR1960", bookId, chapter, verse)

        override suspend fun getBooksByTranslation(translation: String): List<BibleBook> {
            return listOf(
                BibleBook(bookId = 1, name = "Génesis ($translation)", chapters = 50),
                BibleBook(bookId = 43, name = "Juan ($translation)", chapters = 21)
            )
        }

        override suspend fun getVerseByTranslation(
            translation: String,
            bookId: Int,
            chapter: Int,
            verse: Int
        ): BibleVerse {
            return BibleVerse(
                pk = 1001,
                verse = verse,
                text = "Texto de prueba para $translation libro $bookId $chapter:$verse"
            )
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun viewModel_cascadingSelectionAndFavorites_worksCorrectly() = runTest(testDispatcher) {
        val fakeApi = FakeBibleApiService()
        val viewModel = BibleViewModel(apiService = fakeApi, favoritesRepository = null)

        advanceUntilIdle()
        val initialState = viewModel.uiState.value
        assertFalse(initialState.isLoading)
        assertEquals(2, initialState.books.size)

        // Select Book
        val genesis = initialState.books.first()
        viewModel.onBookSelected(genesis)
        assertEquals(50, viewModel.uiState.value.availableChapters.size)
        assertNull(viewModel.uiState.value.selectedChapter)

        // Select Chapter
        viewModel.onChapterSelected(1)
        assertEquals(150, viewModel.uiState.value.availableVerses.size)
        assertEquals(1, viewModel.uiState.value.selectedChapter)

        // Select Verse
        viewModel.onVerseSelected(1)
        advanceUntilIdle()
        val verseContent = viewModel.uiState.value.currentVerseContent
        assertNotNull(verseContent)
        assertEquals(1, verseContent?.verse)

        // Toggle Favorite (Reto 2)
        assertFalse(viewModel.uiState.value.isCurrentVerseFavorite)
        viewModel.toggleCurrentVerseFavorite()
        assertTrue(viewModel.uiState.value.isCurrentVerseFavorite)
        assertEquals(1, viewModel.uiState.value.favorites.size)

        // Change Translation (Reto 3)
        viewModel.onTranslationSelected(BibleTranslation("NVI", "Nueva Versión Internacional"))
        advanceUntilIdle()
        assertEquals("NVI", viewModel.uiState.value.selectedTranslation.code)
        assertNull(viewModel.uiState.value.selectedBook)
        assertTrue(viewModel.uiState.value.books.first().name.contains("NVI"))
    }

    @Test
    fun liveApi_fetchesRvr1960BooksAndJohn3_16() = runTest {
        val books = RetrofitInstance.api.getBooks()
        assertTrue("Expected 66 books from RVR1960 API", books.size >= 66)

        val john316 = RetrofitInstance.api.getVerse(bookId = 43, chapter = 3, verse = 16)
        assertEquals(16, john316.verse)
        assertTrue(john316.text.contains("amó Dios al mundo"))
    }
}
