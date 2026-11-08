package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsSoccer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaceCategory
import com.example.data.model.ZarayenPlace
import com.example.ui.theme.DeepPineGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val places = viewModel.getFilteredPlaces()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ڕێبەری شوێن و خزمەتگوزارییەکانی زەڕایەن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("places_back_btn")
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

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.placesSearchQuery,
                    onValueChange = { viewModel.setPlacesSearchQuery(it) },
                    placeholder = { Text("گەڕان بە ناوی مزگەوت، قوتابخانە، نۆرینگە، مارکێت...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("places_search_input")
                )
            }

            // Categories Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val isAllSelected = uiState.selectedPlacesCategory == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { viewModel.selectPlacesCategory(null) },
                            label = { Text("هەموو شوێنەکان") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepPineGreen,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    items(PlaceCategory.values()) { category ->
                        val isSelected = category == uiState.selectedPlacesCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectPlacesCategory(category) },
                            label = { Text(category.titleKurdish) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepPineGreen,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Places List
            if (places.isEmpty()) {
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
                                text = "هیچ شوێنێک نەدۆزرایەوە",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(places, key = { it.id }) { place ->
                    PlaceItemCard(
                        place = place,
                        onCall = { phone ->
                            if (phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        },
                        onOpenMap = { url ->
                            viewModel.openUrl(context, url)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PlaceItemCard(
    place: ZarayenPlace,
    onCall: (String) -> Unit,
    onOpenMap: (String) -> Unit
) {
    val categoryIcon: ImageVector = when (place.category) {
        PlaceCategory.MOSQUES -> Icons.Default.Mosque
        PlaceCategory.SCHOOLS -> Icons.Default.School
        PlaceCategory.HEALTH -> Icons.Default.LocalHospital
        PlaceCategory.SPORTS -> Icons.Default.SportsSoccer
        PlaceCategory.MARKETS -> Icons.Default.ShoppingCart
        PlaceCategory.FINANCE_FUEL -> Icons.Default.LocalGasStation
        PlaceCategory.PARK_GOV -> Icons.Default.Park
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("place_card_${place.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = place.name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = place.category.titleKurdish,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (place.phone.isNotBlank()) {
                        IconButton(
                            onClick = { onCall(place.phone) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .testTag("call_place_btn_${place.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "پەیوەندی",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (place.googleMapsUrl.isNotBlank()) {
                        IconButton(
                            onClick = { onOpenMap(place.googleMapsUrl) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("map_place_btn_${place.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "نەخشە",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = place.description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
