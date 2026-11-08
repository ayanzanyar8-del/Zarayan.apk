package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminUsersScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    usersList: List<AppUser>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Security Gate: Check if user is Admin
    if (!uiState.isAdmin) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "بەشی بەڕێوەبەرایەتی",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("admin_users_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "گەڕانەوە"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "پارێزراوە",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "تەنها ئەدمین دەتوانێت ئەم لیستە ببینێت",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "لیستی کەسانی تۆماربووی ئەپڵیکەیشنی زەڕایەن پارێزراوە بە ئاسایشی ئەدمین. تکایە سەرەتا بە هەژماری ئەدمین بچۆ ژوورەوە بۆ بینینی تەواوی ناوەکان و ژمارەکان.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.openAdminLoginDialog() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_gate_login_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("چوونەژوورەوەی ئەدمین")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("گەڕانەوە بۆ لاپەڕەی سەرەکی")
                        }
                    }
                }
            }
        }
        return
    }

    // Filter list by search query and neighborhood
    val filteredUsers = remember(usersList, uiState.userSearchQuery, uiState.userFilterNeighborhood) {
        usersList.filter { user ->
            val matchesQuery = uiState.userSearchQuery.isBlank() ||
                    user.fullName.contains(uiState.userSearchQuery, ignoreCase = true) ||
                    user.phoneNumber.contains(uiState.userSearchQuery, ignoreCase = true) ||
                    user.neighborhood.contains(uiState.userSearchQuery, ignoreCase = true) ||
                    user.email.contains(uiState.userSearchQuery, ignoreCase = true)

            val matchesNeighborhood = uiState.userFilterNeighborhood == "هەموو گەڕەکەکان" ||
                    user.neighborhood.contains(uiState.userFilterNeighborhood, ignoreCase = true)

            matchesQuery && matchesNeighborhood
        }
    }

    val totalAdmins = remember(usersList) {
        usersList.count { it.role.contains("ئەدمین", ignoreCase = true) }
    }

    val neighborhoods = listOf(
        "هەموو گەڕەکەکان",
        "گەڕەکی مەولەوی",
        "گەڕەکی کانی ئاسکان",
        "گەڕەکی سۆران",
        "گەڕەکی ڕزگاری",
        "گەڕەکی نەورۆز",
        "سەنتەری زەڕایەن"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "لیستی ئەندامان و تۆماربووان",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "تایبەت بە ئەدمینی شارۆچکەی زەڕایەن",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_users_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "گەڕانەوە"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.openAddUserDialog() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("admin_add_user_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "زیادکردن",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("زیادکردنی ئەندام", fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DeepPineGreen.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = DeepPineGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = DeepPineGreen,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "ئاماری تۆماربووانی ئەپی زەڕایەن",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DeepPineGreen.copy(alpha = 0.15f),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "ئەدمین: پارێزراو",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepPineGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Users
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${usersList.size}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepPineGreen
                                    )
                                    Text(
                                        text = "کۆی تۆماربووان",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Total Admins
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$totalAdmins",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AntiqueGold
                                    )
                                    Text(
                                        text = "دەستەی ئەدمین",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Active Community
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${usersList.size - totalAdmins}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "هاووڵاتیان",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.userSearchQuery,
                    onValueChange = { viewModel.setUserSearchQuery(it) },
                    placeholder = { Text("گەڕان بەپێی ناو، ژمارەی تەلەفۆن، گەڕەک...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "گەڕان")
                    },
                    trailingIcon = {
                        if (uiState.userSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setUserSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاککردنەوە")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_user_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // Filter Chips
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    neighborhoods.forEach { area ->
                        val isSelected = uiState.userFilterNeighborhood == area
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setUserFilterNeighborhood(area) },
                            label = { Text(area, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Result count label
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ئەنجامی گەڕان: (${filteredUsers.size}) کەس",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state
            if (filteredUsers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "هیچ بەکارهێنەرێک نەدۆزرایەوە",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تکایە گەڕانەکەت بگۆڕە یان فلتەرەکان لابدە",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // User Cards List
            items(filteredUsers, key = { it.id }) { user ->
                UserCardItem(
                    user = user,
                    onCallClick = { phone ->
                        if (phone.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    },
                    onEmailClick = { email ->
                        if (email.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                            context.startActivity(intent)
                        }
                    },
                    onDeleteClick = {
                        viewModel.openDeleteUserDialog(user)
                    }
                )
            }
        }
    }
}

@Composable
fun UserCardItem(
    user: AppUser,
    onCallClick: (String) -> Unit,
    onEmailClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val isAdmin = user.role.contains("ئەدمین", ignoreCase = true)
    val initialLetter = user.fullName.firstOrNull()?.toString() ?: "U"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdmin) AntiqueGold.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar + Name + Role Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isAdmin) AntiqueGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initialLetter,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isAdmin) AntiqueGold else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isAdmin) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "ئەدمین",
                                tint = AntiqueGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isAdmin) AntiqueGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (isAdmin) "ئەدمینی سەرەکی" else user.role,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isAdmin) AntiqueGold else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Delete Button (if not super admin)
                if (user.id > 1 || !user.role.contains("سەرەکی")) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_user_btn_${user.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "سڕینەوەی بەکارهێنەر",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Neighborhood
                if (user.neighborhood.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ناونیشان / گەڕەک: ${user.neighborhood}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Signup Date
                if (user.signupDateFormatted.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بەرواری تۆماربوون: ${user.signupDateFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Notes / Occupation
                if (user.notes.isNotBlank()) {
                    Text(
                        text = "تێبینی: ${user.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Call / Email)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.phoneNumber.isNotBlank()) {
                    Button(
                        onClick = { onCallClick(user.phoneNumber) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("call_user_btn_${user.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "پەیوەندی",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = user.phoneNumber, fontSize = 13.sp)
                    }
                }

                if (user.email.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onEmailClick(user.email) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "ئیمەیڵ",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = user.email.take(16) + if (user.email.length > 16) "..." else "",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
