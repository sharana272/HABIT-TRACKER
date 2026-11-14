package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitItem
import com.example.ui.theme.LocalExtendedColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitCard(
    habitItem: HabitItem,
    onToggleCheck: () -> Unit,
    onEditHabit: () -> Unit,
    onDeleteHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    var showMenu by remember { mutableStateOf(false) }

    val isCompleted = habitItem.isCompleted
    val habit = habitItem.habit

    val cardBg by animateColorAsState(
        targetValue = if (isCompleted) extendedColors.surfaceRaised else extendedColors.cardBackground,
        label = "cardBgAnim"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "checkScaleAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else extendedColors.cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onEditHabit() }
            .testTag("habit_card_${habit.id}")
    ) {
        // Completed Left Accent Indicator Strip
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.surfaceRaised)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.icon.ifBlank { "⭐" },
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Habit Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isCompleted) TextDecoration.None else TextDecoration.None
                        ),
                        color = extendedColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (habit.reminderEnabled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.Alarm,
                            contentDescription = "Reminder Active",
                            tint = extendedColors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val timesToShow = habitItem.formattedTimes.ifEmpty { listOf(habitItem.formattedTime) }
                    timesToShow.forEach { timeLabel ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(extendedColors.surfaceRaised)
                                .border(0.5.dp, extendedColors.cardBorder, RoundedCornerShape(99.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = timeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Repeat schedule
                    Text(
                        text = habitItem.repeatDaysFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textMuted,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    // Streak Badge
                    if (habitItem.currentStreak > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(extendedColors.streakBadgeBg)
                                .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔥 ${habitItem.currentStreak}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = extendedColors.streakBadgeText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Options",
                        tint = extendedColors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(extendedColors.surfaceRaised)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Habit", color = extendedColors.textPrimary) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = extendedColors.textPrimary) },
                        onClick = {
                            showMenu = false
                            onEditHabit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Habit", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDeleteHabit()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Interactive Check Ring Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onToggleCheck() }
                    .testTag("habit_check_button_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) extendedColors.checkRingFill else extendedColors.cardBackground
                        )
                        .border(
                            width = if (isCompleted) 0.dp else 1.5.dp,
                            color = if (isCompleted) Color.Transparent else extendedColors.checkRingBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = extendedColors.checkRingIcon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
