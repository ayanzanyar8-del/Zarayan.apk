package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NameOfAllah
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesOfAllahScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    namesList: List<NameOfAllah>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredNames = remember(namesList, uiState.namesSearchQuery, uiState.namesFavoritesOnly) {
        namesList.filter { name ->
            val matchesQuery = uiState.namesSearchQuery.isBlank() ||
                name.arabicName.contains(uiState.namesSearchQuery, ignoreCase = true) ||
                name.kurdishName.contains(uiState.namesSearchQuery, ignoreCase = true) ||
                name.kurdishMeaning.contains(uiState.namesSearchQuery, ignoreCase = true) ||
                name.number.toString() == uiState.namesSearchQuery.trim()

            val matchesFav = !uiState.namesFavoritesOnly || name.isFavorite

            matchesQuery && matchesFav
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "٩٩ ناوی پیرۆزی خودای گەورە",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("names_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "گەڕانەوە"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hadith Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepPineGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "« إِنَّ لِلَّهِ تِسْعَةً وَتِسْعِينَ اسْمًا، مِائَةً إِلَّا وَاحِدًا، مَنْ أَحْصَاهَا دَخَلَ الْجَنَّةَ »",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AntiqueGold
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "پێغەمبەری خودا (ﷺ) دەفەرموێت: بەڕاستی خودای گەورە ٩٩ ناوی هەیە، هەرکەسێک لەبەریان بکات و مانایان بزانێت دەچێتە بەهەشتەوە.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Search Bar and Favorites Chip Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.namesSearchQuery,
                        onValueChange = { viewModel.setNamesSearchQuery(it) },
                        placeholder = { Text("گەڕان بە ناو یان واتا...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("names_search_input")
                    )

                    FilterChip(
                        selected = uiState.namesFavoritesOnly,
                        onClick = { viewModel.toggleNamesFavoritesOnly() },
                        label = { Text("دڵخوازەکان") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (uiState.namesFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (uiState.namesFavoritesOnly) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("favorites_filter_chip")
                    )
                }
            }

            // Names List
            if (filteredNames.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هیچ ناوێک نەدۆزرایەوە",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredNames, key = { it.number }) { name ->
                    NameOfAllahCard(
                        name = name,
                        onToggleFavorite = { viewModel.toggleFavoriteName(name) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun NameOfAllahCard(
    name: NameOfAllah,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("name_card_${name.number}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Number Badge
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${name.number}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = name.arabicName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("fav_btn_${name.number}")
                ) {
                    Icon(
                        imageVector = if (name.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "دڵخواز",
                        tint = if (name.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ناوەکە بە کوردی: ${name.kurdishName}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = name.kurdishMeaning,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
