package com.example.blesearchum.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blesearchum.model.FavoriteVerse
import com.example.blesearchum.ui.components.DropdownSelector

private fun shareVerseText(context: Context, reference: String, text: String) {
    val shareContent = "$reference\n\"$text\""
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, reference)
        putExtra(Intent.EXTRA_TEXT, shareContent)
    }
    val chooser = Intent.createChooser(sendIntent, "Compartir versículo")
    context.startActivity(chooser)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleSearchScreen(viewModel: BibleViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "BibleSearchUM",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reiniciar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Buscar") },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Favoritos (${uiState.favorites.size})") },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reto 3: Selector de Traducción
                DropdownSelector(
                    label = "Traducción",
                    options = uiState.availableTranslations,
                    selectedItem = uiState.selectedTranslation,
                    onItemSelected = { viewModel.onTranslationSelected(it) },
                    itemLabelProvider = { it.name }
                )

                // Controles de Selección
                DropdownSelector(
                    label = "Libro",
                    options = uiState.books,
                    selectedItem = uiState.selectedBook,
                    onItemSelected = { viewModel.onBookSelected(it) },
                    itemLabelProvider = { it.name }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownSelector(
                        label = "Capítulo",
                        options = uiState.availableChapters,
                        selectedItem = uiState.selectedChapter,
                        onItemSelected = { viewModel.onChapterSelected(it) },
                        itemLabelProvider = { it.toString() },
                        enabled = uiState.selectedBook != null,
                        modifier = Modifier.weight(1f)
                    )
                    DropdownSelector(
                        label = "Versículo",
                        options = uiState.availableVerses,
                        selectedItem = uiState.selectedVerse,
                        onItemSelected = { viewModel.onVerseSelected(it) },
                        itemLabelProvider = { it.toString() },
                        enabled = uiState.selectedChapter != null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Indicadores de Estado
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                uiState.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Visualización del Versículo
                uiState.currentVerseContent?.let { verseData ->
                    val reference =
                        "${uiState.selectedBook?.name} ${uiState.selectedChapter}:${verseData.verse} (${uiState.selectedTranslation.code})"
                    val cleanText = verseData.text.trim()

                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = reference,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "\"$cleanText\"",
                                fontSize = 16.sp,
                                fontStyle = FontStyle.Italic,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reto 2: Guardar en Favoritos
                                OutlinedButton(
                                    onClick = { viewModel.toggleCurrentVerseFavorite() }
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isCurrentVerseFavorite) {
                                            Icons.Default.Bookmark
                                        } else {
                                            Icons.Default.BookmarkBorder
                                        },
                                        contentDescription = "Favorito"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (uiState.isCurrentVerseFavorite) "Guardado" else "Favorito"
                                    )
                                }

                                // Reto 1: Compartir Versículo con Intent.ACTION_SEND
                                Button(
                                    onClick = { shareVerseText(context, reference, cleanText) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Compartir"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Compartir")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            FavoritesListContent(
                favorites = uiState.favorites,
                onShare = { fav ->
                    val ref = "${fav.bookName} ${fav.chapter}:${fav.verse} (${fav.translation})"
                    shareVerseText(context, ref, fav.text)
                },
                onDelete = { fav -> viewModel.removeFavorite(fav.id) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun FavoritesListContent(
    favorites: List<FavoriteVerse>,
    onShare: (FavoriteVerse) -> Unit,
    onDelete: (FavoriteVerse) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Aún no tienes versículos favoritos guardados.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        favorites.forEach { fav ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${fav.bookName} ${fav.chapter}:${fav.verse} (${fav.translation})",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            IconButton(onClick = { onShare(fav) }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir favorito"
                                )
                            }
                            IconButton(onClick = { onDelete(fav) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar favorito"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${fav.text}\"",
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
