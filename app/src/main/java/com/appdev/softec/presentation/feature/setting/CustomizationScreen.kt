package com.appdev.softec.presentation.feature.setting


import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*

@Composable
fun CustomizationScreen(viewModel: CustomizationViewModel = hiltViewModel()) {
    val customizationState by viewModel.customizationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Customization Options",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Divider()

        // Dark Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Dark Mode", style = MaterialTheme.typography.titleMedium)

            Switch(
                checked = customizationState.isDarkMode,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )
        }

        Divider()

        // Font Size Selection
        Text(text = "Font Size", style = MaterialTheme.typography.titleMedium)

        Column(modifier = Modifier.fillMaxWidth()) {
            FontSize.entries.forEach { fontSize ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = customizationState.fontSize == fontSize,
                        onClick = { viewModel.setFontSize(fontSize) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = fontSize.name.lowercase().capitalize(),
                        fontSize = fontSize.size.sp
                    )
                }
            }
        }

        Divider()

        // Layout Type Selection
        Text(text = "Layout Type", style = MaterialTheme.typography.titleMedium)

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LayoutType.entries.toTypedArray()) { layoutType ->
                val isSelected = customizationState.layoutType == layoutType

                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .height(80.dp)
                        .clickable { viewModel.setLayoutType(layoutType) },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 8.dp else 2.dp
                    ),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Layout icon would go here in real app
                        Text(
                            text = layoutType.name.lowercase().capitalize(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Divider()

        // Notification Style Selection
        Text(text = "Notification Style", style = MaterialTheme.typography.titleMedium)

        Column(modifier = Modifier.fillMaxWidth()) {
            NotificationStyle.entries.forEach { style ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { viewModel.setNotificationStyle(style) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = customizationState.notificationStyle == style,
                        onClick = { viewModel.setNotificationStyle(style) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = getNotificationDescription(style),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get notification style descriptions
@Composable
fun getNotificationDescription(style: NotificationStyle): String {
    return when (style) {
        NotificationStyle.STANDARD -> "Normal notifications with sound and vibration"
        NotificationStyle.MINIMAL -> "Show only essential information"
        NotificationStyle.DETAILED -> "Show all available information"
        NotificationStyle.SILENT -> "No sound or vibration"
    }
}

// Extension function to capitalize first letter of a string
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}