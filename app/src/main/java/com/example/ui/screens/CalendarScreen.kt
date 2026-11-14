package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.Habit
import com.example.data.model.DayCompletion
import com.example.data.model.HabitItem
import com.example.ui.components.HabitCard
import com.example.ui.components.MonthCalendarView
import com.example.ui.theme.LocalExtendedColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    completions: Map<LocalDate, DayCompletion>,
    habitsForSelectedDate: List<HabitItem>,
    onToggleHabit: (Long, LocalDate) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val today = LocalDate.now()

    // Calculate month stats
    val monthDays = completions.values.filter { !it.isFuture }
    val totalScheduledInMonth = monthDays.sumOf { it.totalScheduled }
    val totalCompletedInMonth = monthDays.sumOf { it.completedCount }
    val monthCompletionRate = if (totalScheduledInMonth > 0) {
        ((totalCompletedInMonth.toFloat() / totalScheduledInMonth.toFloat()) * 100).toInt()
    } else 0

    val perfectDaysInMonth = monthDays.count { it.isAllCompleted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Habit Calendar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textMuted
                )
                Text(
                    text = "Monthly Overview",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.textPrimary
                )
            }
        }

        // Calendar Component
        item {
            MonthCalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onSelectDate = onSelectDate,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onJumpToToday = onJumpToToday,
                completions = completions
            )
        }

        // Month Highlights Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rate card
                MonthStatCard(
                    title = "Monthly Rate",
                    value = "$monthCompletionRate%",
                    subtitle = "$totalCompletedInMonth completed",
                    modifier = Modifier.weight(1f)
                )

                // Perfect days card
                MonthStatCard(
                    title = "Perfect Days",
                    value = "$perfectDaysInMonth days",
                    subtitle = "100% routines done",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Selected Day Details Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedDate.isEqual(today)) "Today's Log" else "Log for " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Tap circle to toggle completion for this date",
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textMuted
                    )
                }
            }
        }

        // Selected Day Habits
        val scheduledOnDay = habitsForSelectedDate.filter { it.isScheduledForSelectedDate }
        if (scheduledOnDay.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(extendedColors.cardBackground)
                        .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No routines scheduled for this date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.textMuted
                    )
                }
            }
        } else {
            items(scheduledOnDay, key = { it.habit.id }) { item ->
                HabitCard(
                    habitItem = item,
                    onToggleCheck = { onToggleHabit(item.habit.id, selectedDate) },
                    onEditHabit = { onEditHabit(item.habit) },
                    onDeleteHabit = { onDeleteHabit(item.habit) }
                )
            }
        }
    }
}

@Composable
private fun MonthStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("month_stat_${title.lowercase().replace(" ", "_")}")
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = extendedColors.textSecondary
            )
        }
    }
}
