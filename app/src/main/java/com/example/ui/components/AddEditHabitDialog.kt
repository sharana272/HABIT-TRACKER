package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.Habit
import com.example.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitDialog(
    initialHabit: Habit? = null,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: ((Habit) -> Unit)? = null
) {
    val extendedColors = LocalExtendedColors.current
    val isEditing = initialHabit != null

    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedIcon by remember { mutableStateOf(initialHabit?.icon ?: "🏃") }
    var selectedCategory by remember { mutableStateOf(initialHabit?.category ?: "Fitness") }

    // Time: primary + optional additional scheduled timings
    val initialAllTimes = initialHabit?.allTimes?.ifEmpty { listOf(LocalTime.of(8, 0)) }
        ?: listOf(LocalTime.of(8, 0))
    val initialPrimary = initialAllTimes.first()
    var selectedHour by remember { mutableIntStateOf(initialPrimary.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialPrimary.minute) }
    var extraTimes by remember { mutableStateOf(initialAllTimes.drop(1)) }
    var showClockPicker by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.US) }
    val quickPresetTimes = remember {
        listOf(
            LocalTime.of(6, 0),
            LocalTime.of(7, 0),
            LocalTime.of(8, 0),
            LocalTime.of(9, 0),
            LocalTime.of(12, 30),
            LocalTime.of(16, 0),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            LocalTime.of(21, 30)
        )
    }

    // Repeat days
    val initialDays = initialHabit?.repeatDays?.toSet()
        ?: DayOfWeek.entries.toSet()
    var selectedDays by remember { mutableStateOf(initialDays) }
    var daysError by remember { mutableStateOf<String?>(null) }

    var reminderEnabled by remember { mutableStateOf(initialHabit?.reminderEnabled ?: true) }
    var reminderMessage by remember {
        mutableStateOf(initialHabit?.reminderMessage ?: "")
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val availableIcons = listOf(
        "🏃", "💧", "📚", "🧘", "✍️", "💼", "🥗", "💤", "🎯", "🏋️", "💊", "🚴", "🧠", "🌿", "🚶", "🎨"
    )
    val categories = listOf("Fitness", "Health", "Learning", "Mind", "Productivity", "Lifestyle")
    val daysOfWeek = listOf(
        DayOfWeek.MONDAY to "Mon",
        DayOfWeek.TUESDAY to "Tue",
        DayOfWeek.WEDNESDAY to "Wed",
        DayOfWeek.THURSDAY to "Thu",
        DayOfWeek.FRIDAY to "Fri",
        DayOfWeek.SATURDAY to "Sat",
        DayOfWeek.SUNDAY to "Sun"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = extendedColors.cardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("add_edit_habit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Habit" else "Add New Habit",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.textPrimary
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = extendedColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Name Input
            Text(
                text = "Habit Name",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = null
                },
                placeholder = { Text("e.g. Read 20 pages, Morning Jog", color = extendedColors.textMuted) },
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extendedColors.cardBorder,
                    focusedTextColor = extendedColors.textPrimary,
                    unfocusedTextColor = extendedColors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Picker
            Text(
                text = "Choose Icon",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableIcons.forEach { icon ->
                    val isSelected = selectedIcon == icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else extendedColors.surfaceRaised
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.cardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedIcon = icon }
                            .testTag("icon_choice_$icon"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = extendedColors.surfaceRaised,
                            labelColor = extendedColors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = extendedColors.cardBorder,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(99.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Selector & Multiple Timings
            val currentPickerTime = LocalTime.of(selectedHour, selectedMinute)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit Timings",
                    style = MaterialTheme.typography.labelMedium,
                    color = extendedColors.textSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .clickable { showClockPicker = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("open_clock_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Open clock picker",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clock Picker",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val isPm = selectedHour >= 12
            val displayHour12 = when {
                selectedHour == 0 -> 12
                selectedHour > 12 -> selectedHour - 12
                else -> selectedHour
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(extendedColors.surfaceRaised)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Hour selector
                    TimeStepper(
                        value = displayHour12,
                        label = "Hour",
                        onIncrement = {
                            val new12 = if (displayHour12 == 12) 1 else displayHour12 + 1
                            selectedHour = if (isPm) (if (new12 == 12) 12 else new12 + 12) else (if (new12 == 12) 0 else new12)
                        },
                        onDecrement = {
                            val new12 = if (displayHour12 == 1) 12 else displayHour12 - 1
                            selectedHour = if (isPm) (if (new12 == 12) 12 else new12 + 12) else (if (new12 == 12) 0 else new12)
                        }
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = extendedColors.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minute selector
                    TimeStepper(
                        value = selectedMinute,
                        label = "Min",
                        step = 5,
                        onIncrement = {
                            selectedMinute = (selectedMinute + 5) % 60
                        },
                        onDecrement = {
                            selectedMinute = if (selectedMinute < 5) 55 else selectedMinute - 5
                        }
                    )
                }

                // AM / PM Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(extendedColors.cardBackground)
                        .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(99.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (!isPm) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                if (isPm) selectedHour -= 12
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "AM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!isPm) MaterialTheme.colorScheme.onPrimary else extendedColors.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isPm) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                if (!isPm) selectedHour += 12
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "PM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isPm) MaterialTheme.colorScheme.onPrimary else extendedColors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Time Presets ("this time, this time, this time")
            Text(
                text = "Quick Timings (tap to set, or + to add another)",
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPresetTimes.forEach { preset ->
                    val isPrimary = currentPickerTime == preset
                    val isExtra = extraTimes.contains(preset)
                    val isActive = isPrimary || isExtra
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else extendedColors.surfaceRaised
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else extendedColors.cardBorder,
                                shape = RoundedCornerShape(99.dp)
                            )
                            .clickable {
                                selectedHour = preset.hour
                                selectedMinute = preset.minute
                            }
                            .padding(horizontal = 11.dp, vertical = 7.dp)
                            .testTag("quick_time_${preset.hour}_${preset.minute}")
                    ) {
                        Text(
                            text = preset.format(timeFormatter),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isActive) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                        )
                    }
                }

                // Button to append current picked time as an additional timing slot
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            if (!extraTimes.contains(currentPickerTime)) {
                                extraTimes = (extraTimes + currentPickerTime).distinct().sorted()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("add_another_time_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add timing slot",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add ${currentPickerTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            if (extraTimes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val combinedDisplayTimes = (listOf(currentPickerTime) + extraTimes).distinct().sorted()
                Text(
                    text = "Scheduled Timings (${combinedDisplayTimes.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    combinedDisplayTimes.forEachIndexed { idx, slotTime ->
                        val canRemove = combinedDisplayTimes.size > 1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(extendedColors.surfaceRaised)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(99.dp))
                                .clickable(enabled = canRemove) {
                                    if (slotTime == currentPickerTime && extraTimes.isNotEmpty()) {
                                        val nextPrimary = extraTimes.first()
                                        selectedHour = nextPrimary.hour
                                        selectedMinute = nextPrimary.minute
                                        extraTimes = extraTimes.drop(1)
                                    } else {
                                        extraTimes = extraTimes.filterNot { it == slotTime }
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("scheduled_timing_chip_$idx")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🕒 ${slotTime.format(timeFormatter)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = extendedColors.textPrimary
                                )
                                if (canRemove) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "×",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = extendedColors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showClockPicker) {
                val pickerState = rememberTimePickerState(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    is24Hour = false
                )
                AlertDialog(
                    onDismissRequest = { showClockPicker = false },
                    containerColor = extendedColors.cardBackground,
                    title = {
                        Text(
                            text = "Select Exact Time",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = extendedColors.textPrimary
                        )
                    },
                    text = {
                        TimePicker(state = pickerState)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedHour = pickerState.hour
                                selectedMinute = pickerState.minute
                                showClockPicker = false
                            }
                        ) {
                            Text("Set Time", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClockPicker = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.labelMedium,
                    color = extendedColors.textSecondary
                )

                // Presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Everyday",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                selectedDays = DayOfWeek.entries.toSet()
                                daysError = null
                            }
                            .padding(4.dp)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textMuted
                    )
                    Text(
                        text = "Weekdays",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                selectedDays = setOf(
                                    DayOfWeek.MONDAY,
                                    DayOfWeek.TUESDAY,
                                    DayOfWeek.WEDNESDAY,
                                    DayOfWeek.THURSDAY,
                                    DayOfWeek.FRIDAY
                                )
                                daysError = null
                            }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { (day, label) ->
                    val isDaySelected = selectedDays.contains(day)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDaySelected) MaterialTheme.colorScheme.primary
                                else extendedColors.surfaceRaised
                            )
                            .border(
                                1.dp,
                                if (isDaySelected) MaterialTheme.colorScheme.primary else extendedColors.cardBorder,
                                CircleShape
                            )
                            .clickable {
                                selectedDays = if (isDaySelected) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                                if (selectedDays.isNotEmpty()) daysError = null
                            }
                            .testTag("day_toggle_${label.uppercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.take(1),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDaySelected) MaterialTheme.colorScheme.onPrimary else extendedColors.textSecondary
                        )
                    }
                }
            }
            if (daysError != null) {
                Text(
                    text = daysError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Smart Reminders Switch & Custom Message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(extendedColors.surfaceRaised)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Reminders",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Send notification alert on scheduled days",
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textMuted
                    )
                }

                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedTrackColor = extendedColors.cardBorder
                    ),
                    modifier = Modifier.testTag("reminder_switch")
                )
            }

            if (reminderEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reminderMessage,
                    onValueChange = { reminderMessage = it },
                    label = { Text("Reminder Prompt / Message") },
                    placeholder = { Text("e.g. You have a meeting today at 4 PM") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_message_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = extendedColors.cardBorder,
                        focusedTextColor = extendedColors.textPrimary,
                        unfocusedTextColor = extendedColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions: Save / Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditing && onDelete != null && initialHabit != null) {
                    OutlinedButton(
                        onClick = { onDelete(initialHabit) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("delete_habit_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        // Validation
                        if (name.isBlank()) {
                            nameError = "Habit name cannot be empty"
                            return@Button
                        }
                        if (selectedDays.isEmpty()) {
                            daysError = "Please select at least one repeat day"
                            return@Button
                        }

                        val sortedTimes = (listOf(LocalTime.of(selectedHour, selectedMinute)) + extraTimes)
                            .distinct()
                            .sorted()
                        val targetTime = sortedTimes.first()
                        val additionalTimes = sortedTimes.drop(1)
                        val daysList = selectedDays.sortedBy { it.value }
                        val promptMsg = if (reminderMessage.isNotBlank()) {
                            reminderMessage.trim()
                        } else {
                            "Time for $name!"
                        }

                        val habitToSave = (initialHabit ?: Habit(
                            name = name.trim(),
                            icon = selectedIcon,
                            category = selectedCategory,
                            targetTime = targetTime,
                            repeatDays = daysList,
                            reminderEnabled = reminderEnabled,
                            reminderMessage = promptMsg,
                            colorHex = "#10B981",
                            additionalTimes = additionalTimes
                        )).copy(
                            name = name.trim(),
                            icon = selectedIcon,
                            category = selectedCategory,
                            targetTime = targetTime,
                            repeatDays = daysList,
                            reminderEnabled = reminderEnabled,
                            reminderMessage = promptMsg,
                            additionalTimes = additionalTimes
                        )

                        onSave(habitToSave)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("save_habit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isEditing) "Save Changes" else "Create Habit",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeStepper(
    value: Int,
    label: String,
    step: Int = 1,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(extendedColors.cardBackground)
                .clickable { onIncrement() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = extendedColors.textPrimary, fontWeight = FontWeight.Bold)
        }

        Text(
            text = String.format(Locale.US, "%02d", value),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = extendedColors.textPrimary,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(extendedColors.cardBackground)
                .clickable { onDecrement() },
            contentAlignment = Alignment.Center
        ) {
            Text("-", color = extendedColors.textPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
