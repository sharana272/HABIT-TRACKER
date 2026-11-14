package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.ThemeMode
import com.example.ui.components.AppShareHelper
import com.example.ui.components.SharanFlameEmblem
import com.example.ui.theme.LocalExtendedColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    onSelectTheme: (ThemeMode) -> Unit,
    smartRemindersEnabled: Boolean,
    onToggleSmartReminders: (Boolean) -> Unit,
    onTestNotification: (title: String, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    var notificationFeedback by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var installLink by remember { mutableStateOf(AppShareHelper.getInstallLink(context)) }
    var isEditingLink by remember { mutableStateOf(false) }
    var linkDraft by remember { mutableStateOf(installLink) }
    var shareFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SharanFlameEmblem(size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sharan Habits • Preferences",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.textMuted
                        )
                        Text(
                            text = "Settings & Alerts",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = extendedColors.textPrimary
                        )
                    }
                }

                IconButton(
                    onClick = { AppShareHelper.shareInstallLink(context, installLink) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(extendedColors.cardBackground)
                        .border(1.dp, extendedColors.cardBorder, CircleShape)
                        .testTag("header_share_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share Sharan Habits",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Appearance / Theme Section
        item {
            SettingsCard(
                icon = Icons.Outlined.ColorLens,
                title = "Appearance Theme",
                subtitle = "Switch between tactical Obsidian and organic light"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionPill(
                        label = "Obsidian Dark",
                        isSelected = currentTheme == ThemeMode.OBSIDIAN_DARK,
                        onClick = { onSelectTheme(ThemeMode.OBSIDIAN_DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionPill(
                        label = "Refined Light",
                        isSelected = currentTheme == ThemeMode.REFINED_LIGHT,
                        onClick = { onSelectTheme(ThemeMode.REFINED_LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionPill(
                        label = "System",
                        isSelected = currentTheme == ThemeMode.SYSTEM,
                        onClick = { onSelectTheme(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Smart Reminders Section
        item {
            SettingsCard(
                icon = Icons.Outlined.NotificationsActive,
                title = "Smart Reminders & Alarms",
                subtitle = "Scheduled alerts for your daily routines"
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Master Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Routine Reminders",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = extendedColors.textPrimary
                        )
                        Text(
                            text = "Triggers exact alarms or notifications when a habit is due",
                            style = MaterialTheme.typography.bodySmall,
                            color = extendedColors.textMuted
                        )
                    }

                    Switch(
                        checked = smartRemindersEnabled,
                        onCheckedChange = { onToggleSmartReminders(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = extendedColors.cardBorder
                        ),
                        modifier = Modifier.testTag("settings_reminders_master_switch")
                    )
                }

                // Permission Warning if needed
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Notification permission is required for alarms",
                                style = MaterialTheme.typography.bodySmall,
                                color = extendedColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Allow", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test Notification Button
                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onTestNotification("⚡ Smart Reminder", "You have a session scheduled today at 4 PM! Stay consistent.")
                            notificationFeedback = "Test notification sent to your device tray!"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("test_notification_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Test Reminder Notification", fontWeight = FontWeight.Bold)
                }

                if (notificationFeedback != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notificationFeedback ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Data Storage & Management Section
        item {
            SettingsCard(
                icon = Icons.Outlined.Storage,
                title = "Data & Storage",
                subtitle = "Local offline database on device"
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "All your habits, completion logs, and streaks are stored securely and locally on your device using Room persistence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )
            }
        }

        // Share & Install App Section
        item {
            SettingsCard(
                icon = Icons.Outlined.Share,
                title = "Share & Install Link",
                subtitle = "Let friends install Sharan Habits on their phone"
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Share this install link so someone else can open and install Sharan Habits, or send the app APK file directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Install Link Box + Copy Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(extendedColors.surfaceRaised)
                        .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = installLink,
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("install_link_text")
                    )
                    IconButton(
                        onClick = {
                            AppShareHelper.copyInstallLink(context, installLink)
                            shareFeedback = "Install link copied to clipboard!"
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("copy_install_link_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy install link",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            linkDraft = installLink
                            isEditingLink = !isEditingLink
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("edit_install_link_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit custom install link",
                            tint = extendedColors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (isEditingLink) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = linkDraft,
                        onValueChange = { linkDraft = it },
                        label = { Text("Custom Download / APK URL") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_install_link_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = extendedColors.cardBorder,
                            focusedTextColor = extendedColors.textPrimary,
                            unfocusedTextColor = extendedColors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val defaultUrl = context.getString(R.string.app_install_link)
                                AppShareHelper.saveCustomInstallLink(context, "")
                                installLink = defaultUrl
                                linkDraft = defaultUrl
                                isEditingLink = false
                                shareFeedback = "Reset to default app install link."
                            }
                        ) {
                            Text("Reset Default")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val cleaned = linkDraft.trim()
                                if (cleaned.isNotEmpty()) {
                                    AppShareHelper.saveCustomInstallLink(context, cleaned)
                                    installLink = cleaned
                                    isEditingLink = false
                                    shareFeedback = "Custom install link saved!"
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Link")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { AppShareHelper.shareInstallLink(context, installLink) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_app_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Install Link", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            shareFeedback = "Preparing APK file to share..."
                            val result = AppShareHelper.shareApkInstaller(context)
                            shareFeedback = if (result.isSuccess) {
                                "Choose an app to send Sharan_Habits.apk!"
                            } else {
                                "Could not export APK on this device. Use Share Install Link instead."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_apk_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.InstallMobile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send APK File to Install", fontWeight = FontWeight.Bold)
                }

                if (shareFeedback != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = shareFeedback ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // About Card
        item {
            SettingsCard(
                icon = Icons.Outlined.Info,
                title = "About Sharan Habits",
                subtitle = "Version 1.0.0 • Tactile Minimalist Routine Tracker"
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Designed for high-performing individuals building lasting momentum. Built with Jetpack Compose, Material 3, Room local database, and exact alarm notifications.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(extendedColors.surfaceRaised),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textMuted
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun ThemeOptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.surfaceRaised)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.cardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else extendedColors.textSecondary
        )
    }
}
