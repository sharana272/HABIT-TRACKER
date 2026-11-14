package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.Habit
import com.example.data.model.HabitItem
import com.example.ui.components.AppShareHelper
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.HabitCard
import com.example.ui.components.SharanHabitsBrandBadge
import com.example.ui.components.WeeklyDateStrip
import com.example.ui.theme.LocalExtendedColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    habits: List<HabitItem>,
    onToggleHabit: (Long) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onAddHabitClick: () -> Unit,
    onOpenCoach: () -> Unit,
    onJumpToToday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val context = LocalContext.current
    val today = LocalDate.now()
    val isToday = selectedDate.isEqual(today)

    val scheduledHabits = habits.filter { it.isScheduledForSelectedDate }
    val completedCount = scheduledHabits.count { it.isCompleted }
    val totalScheduled = scheduledHabits.size
    val progressRatio = if (totalScheduled > 0) completedCount.toFloat() / totalScheduled.toFloat() else 0f

    val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0

    // Greeting
    val currentHour = LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good morning"
        currentHour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SharanHabitsBrandBadge(
                            onClick = onJumpToToday,
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Streak Pill
                            if (maxStreak > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(extendedColors.surfaceRaised)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(99.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("today_streak_pill")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🔥",
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${maxStreak}d",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { AppShareHelper.shareInstallLink(context) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(extendedColors.cardBackground)
                                    .border(1.dp, extendedColors.cardBorder, CircleShape)
                                    .testTag("today_share_app_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share Sharan Habits install link",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column {
                        Text(
                            text = if (isToday) greeting else "Habits Schedule",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.textMuted
                        )
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US)),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = extendedColors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly Date Strip
                    val dayCompletionsMap = habits.groupBy { selectedDate }
                        .mapValues { (_, items) -> items.isNotEmpty() && items.all { it.isCompleted } }

                    WeeklyDateStrip(
                        selectedDate = selectedDate,
                        onSelectDate = onSelectDate,
                        dayCompletions = dayCompletionsMap
                    )
                }
            }

            // Daily Progress Hero Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(extendedColors.cardBackground)
                        .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                        .testTag("daily_progress_hero_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Momentum",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = extendedColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    totalScheduled == 0 -> "No habits scheduled for this day"
                                    completedCount == totalScheduled -> "All habits completed! Fantastic job! 🎉"
                                    completedCount > 0 -> "$completedCount of $totalScheduled routines completed. Keep the flow going!"
                                    else -> "Ready to check off your routines for today?"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = extendedColors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        CircularProgressRing(
                            progress = progressRatio,
                            completedCount = completedCount,
                            totalCount = totalScheduled,
                            size = 86.dp,
                            strokeWidth = 7.dp
                        )
                    }
                }
            }

            // Quick AI Coach Advice Pill / Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(extendedColors.surfaceRaised)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { onOpenCoach() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("today_coach_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🧠", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gemini Habit Coach",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = extendedColors.textPrimary
                                )
                                Text(
                                    text = "Ask for custom habit stacks & streak accountability",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = extendedColors.textMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = extendedColors.textPrimary
                    )

                    Text(
                        text = "Time order",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textMuted
                    )
                }
            }

            // Empty State
            if (scheduledHabits.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(extendedColors.cardBackground)
                            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(20.dp))
                            .padding(32.dp)
                            .testTag("empty_habits_state"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(extendedColors.surfaceRaised),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (habits.isEmpty()) "No Habits Yet" else "No Habits Scheduled",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = extendedColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (habits.isEmpty())
                                "Start building consistency. Add your first routine with a target time and reminder."
                            else
                                "You don't have habits scheduled on this day of the week. Tap below to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extendedColors.textMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onAddHabitClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("empty_add_habit_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Habit")
                        }
                    }
                }
            } else {
                // Group habits by time of day in chronological order
                val morningHabits = scheduledHabits.filter { it.habit.targetTime.hour < 12 }
                val afternoonHabits = scheduledHabits.filter { it.habit.targetTime.hour in 12..16 }
                val eveningHabits = scheduledHabits.filter { it.habit.targetTime.hour >= 17 }

                val timeSections = listOf(
                    "🌅 Morning (Before 12 PM)" to morningHabits,
                    "☀️ Afternoon (12 PM – 5 PM)" to afternoonHabits,
                    "🌙 Evening (After 5 PM)" to eveningHabits
                ).filter { it.second.isNotEmpty() }

                timeSections.forEach { (sectionTitle, sectionHabits) ->
                    item(key = "section_$sectionTitle") {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = extendedColors.textMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(sectionHabits, key = { it.habit.id }) { item ->
                        HabitCard(
                            habitItem = item,
                            onToggleCheck = { onToggleHabit(item.habit.id) },
                            onEditHabit = { onEditHabit(item.habit) },
                            onDeleteHabit = { onDeleteHabit(item.habit) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Habit
        FloatingActionButton(
            onClick = onAddHabitClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("fab_add_habit"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Habit", modifier = Modifier.size(24.dp))
        }
    }
}
