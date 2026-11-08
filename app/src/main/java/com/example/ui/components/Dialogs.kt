package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen

@Composable
fun UserRegistrationDialog(
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "بەخێربێیت",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "بەخێربێن بۆ ئەپی زەڕایەن",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "تۆمارکردنی ناوی بەکارهێنەر",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "تکایە تەنها ناوتان بنووسن بۆ دەستپێکردن و بەشداریکردن لە ئەپڵیکەیشنەکە:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Username only
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("ناوی بەکارهێنەر / ناوی سیانی") },
                    placeholder = { Text("بۆ نموونە: ئارام محەمەد") },
                    isError = isError && nameInput.isBlank(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_name_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    shape = RoundedCornerShape(14.dp)
                )
                if (isError && nameInput.isBlank()) {
                    Text(
                        text = "تکایە ناو بنووسن",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.trim().isNotBlank()) {
                        onSaveName(nameInput.trim())
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("submit_user_name_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("دەستپێکردن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("تێپەڕاندن (وەک میوان)")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String) -> Unit
) {
    var adminCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "ئەدمین",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "چوونەژوورەوەی ئەدمینی زەڕایەن",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "ئەم بەشە تایبەتە بە ئەدمینی سەرەکی بۆ بڵاوکردنەوە و بەڕێوەبردنی ئاگادارییە فەرمییەکان:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = adminCode,
                    onValueChange = {
                        adminCode = it
                        isError = false
                    },
                    label = { Text("کۆدی چوونەژوورەوەی ئەدمین") },
                    placeholder = { Text("کۆدی نهێنی بنووسە") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_code_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                if (isError) {
                    Text(
                        text = "کۆدی ئەدمین نادروستە!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (adminCode.trim().equals("zanyar2", ignoreCase = true)) {
                        onLogin(adminCode.trim())
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("admin_login_submit_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("چوونەژوورەوە")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("داخستن")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun NewJanazaDialog(
    onDismiss: () -> Unit,
    onPublish: (deceasedName: String, desc: String, mosque: String, cemetery: String, prayerTime: String, condolence: String, phone: String) -> Unit
) {
    var deceasedName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mosqueName by remember { mutableStateOf("مزگەوتی گەورەی زەڕایەن") }
    var cemeteryName by remember { mutableStateOf("گۆڕستانی گشتی زەڕایەن") }
    var prayerTime by remember { mutableStateOf("دوای نوێژی عەسر") }
    var condolenceLocation by remember { mutableStateOf("مزگەوتی گەورەی زەڕایەن") }
    var contactPhone by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = DeepPineGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "ئاگاداری نوێ",
                        tint = DeepPineGreen,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "بڵاوکردنەوەی نوێژی مردوو",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "زانیاری کۆچی دوایی پڕبکەرەوە بۆ بڵاوکردنەوە لە گروپی زەڕایەن:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Deceased Name
                OutlinedTextField(
                    value = deceasedName,
                    onValueChange = { deceasedName = it; isError = false },
                    label = { Text("ناوی خوالێخۆشبوو *") },
                    placeholder = { Text("بۆ نموونە: حاجی حەمە ئەمین") },
                    isError = isError && deceasedName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("janaza_name_input"),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("دەقی ئاگاداری و پرسەنامە") },
                    placeholder = { Text("بە داخ و پەژارەیەکی زۆرەوە...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("janaza_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mosque
                OutlinedTextField(
                    value = mosqueName,
                    onValueChange = { mosqueName = it },
                    label = { Text("مزگەوتی نوێژ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("janaza_mosque_input"),
                    leadingIcon = { Icon(Icons.Default.Mosque, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prayer Time
                OutlinedTextField(
                    value = prayerTime,
                    onValueChange = { prayerTime = it },
                    label = { Text("کاتی نوێژ") },
                    placeholder = { Text("دوای نوێژی نیوەڕۆ / عەسر...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("janaza_prayer_time_input"),
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Cemetery
                OutlinedTextField(
                    value = cemeteryName,
                    onValueChange = { cemeteryName = it },
                    label = { Text("گۆڕستان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("janaza_cemetery_input"),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Condolence Place
                OutlinedTextField(
                    value = condolenceLocation,
                    onValueChange = { condolenceLocation = it },
                    label = { Text("شوێنی پرسە (پیاوان و ژنان)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("janaza_condolence_input"),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phone
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("ژمارەی مۆبایل بۆ پرسە") },
                    placeholder = { Text("0770XXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("janaza_phone_input"),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deceasedName.trim().isNotBlank()) {
                        val desc = if (description.trim().isNotBlank()) description.trim() else "بە داخ و پەژارەیەکی زۆرەوە کۆچی دوایی خوالێخۆشبوو ڕادەگەیەنین. داواکارین لە پەروەردگار جێگەی بەهەشت بێت."
                        onPublish(
                            deceasedName.trim(),
                            desc,
                            mosqueName.trim(),
                            cemeteryName.trim(),
                            prayerTime.trim(),
                            condolenceLocation.trim(),
                            contactPhone.trim()
                        )
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("publish_janaza_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("بڵاوکردنەوە لە گروپ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("پەشیمانبوونەوە")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EditJanazaDialog(
    announcement: com.example.data.model.JanazaAnnouncement,
    onDismiss: () -> Unit,
    onSave: (deceasedName: String, desc: String, mosque: String, cemetery: String, prayerTime: String, condolence: String, phone: String) -> Unit
) {
    var deceasedName by remember { mutableStateOf(announcement.deceasedName) }
    var description by remember { mutableStateOf(announcement.description) }
    var mosqueName by remember { mutableStateOf(announcement.mosqueName) }
    var cemeteryName by remember { mutableStateOf(announcement.cemeteryName) }
    var prayerTime by remember { mutableStateOf(announcement.prayerTime) }
    var condolenceLocation by remember { mutableStateOf(announcement.condolenceLocation) }
    var contactPhone by remember { mutableStateOf(announcement.contactPhone) }
    var isError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = AntiqueGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "دەستکاریکردن",
                        tint = AntiqueGold,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "دەستکاریکردنی ئاگاداری نوێژی مردوو",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "دەستکاری زانیارییەکانی ئەم ئاگادارییە بکە وەک ئەدمین:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = deceasedName,
                    onValueChange = { deceasedName = it; isError = false },
                    label = { Text("ناوی خوالێخۆشبوو *") },
                    isError = isError && deceasedName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_janaza_name_input"),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("دەقی ئاگاداری") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("edit_janaza_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mosqueName,
                    onValueChange = { mosqueName = it },
                    label = { Text("مزگەوتی نوێژ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_janaza_mosque_input"),
                    leadingIcon = { Icon(Icons.Default.Mosque, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = prayerTime,
                    onValueChange = { prayerTime = it },
                    label = { Text("کاتی نوێژ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_janaza_prayer_time_input"),
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cemeteryName,
                    onValueChange = { cemeteryName = it },
                    label = { Text("گۆڕستان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_janaza_cemetery_input"),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = condolenceLocation,
                    onValueChange = { condolenceLocation = it },
                    label = { Text("شوێنی پرسە") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_janaza_condolence_input"),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("ژمارەی مۆبایل بۆ پرسە") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_janaza_phone_input"),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deceasedName.trim().isNotBlank()) {
                        onSave(
                            deceasedName.trim(),
                            description.trim(),
                            mosqueName.trim(),
                            cemeteryName.trim(),
                            prayerTime.trim(),
                            condolenceLocation.trim(),
                            contactPhone.trim()
                        )
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("save_edit_janaza_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("پاشەکەوتکردنی گۆڕانکارییەکان")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("پەشیمانبوونەوە")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteJanazaConfirmDialog(
    deceasedName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "دڵنیایت لە سڕینەوەی ئاگاداری؟",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "ئایا دڵنیایت دەتەوێت ئاگاداری نوێژی مردووی ($deceasedName) بسڕیتەوە؟ ئەم کردارە ناگەڕێتەوە.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_delete_janaza_btn")
            ) {
                Text("سڕینەوە", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("پەشیمانبوونەوە")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AddNewUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (name: String, phone: String, email: String, neighborhood: String, role: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("گەڕەکی مەولەوی") }
    var role by remember { mutableStateOf("بەکارهێنەر") }
    var notes by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "زیادکردنی بەکارهێنەر",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "زیادکردنی ئەندام / هاووڵاتی نوێ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "وەک ئەدمین دەتوانیت زانیاری ئەندام یاخود هاووڵاتی نوێ تۆمار بکەیت:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isError = false },
                    label = { Text("ناوی سیانی *") },
                    placeholder = { Text("بۆ نموونە: عومەر فەتاح حەمە") },
                    isError = isError && name.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_name_input"),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ژمارەی مۆبایل") },
                    placeholder = { Text("0770XXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_phone_input"),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = { Text("گەڕەک / ناونیشان") },
                    placeholder = { Text("مەولەوی، کانی ئاسکان، سۆران...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_neighborhood_input"),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ئیمەیڵ") },
                    placeholder = { Text("user@gmail.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("تێبینی یان پیشە (ئارەزوومەندانە)") },
                    placeholder = { Text("مامۆستا، کاسبکار، هتد...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_notes_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotBlank()) {
                        onAddUser(
                            name.trim(),
                            phone.trim(),
                            email.trim(),
                            neighborhood.trim().ifEmpty { "شارۆچکەی زەڕایەن" },
                            role.trim().ifEmpty { "بەکارهێنەر" },
                            notes.trim()
                        )
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("submit_add_user_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تۆمارکردنی ئەندام")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("پەشیمانبوونەوە")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteUserConfirmDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "دڵنیایت لە سڕینەوەی ئەم ئەندامە؟",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "ئایا دڵنیایت دەتەوێت تۆماری بەکارهێنەر ($userName) لە سیستەم بسڕیتەوە؟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_delete_user_btn")
            ) {
                Text("سڕینەوە", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("پەشیمانبوونەوە")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

