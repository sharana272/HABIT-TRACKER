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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.DayCompletion
import com.example.data.model.HabitProgressStat
import com.example.data.model.MilestoneBadge
import com.example.ui.components.MilestoneBadgeCard
import com.example.ui.components.MonthlyBarChart
import com.example.ui.theme.LocalExtendedColors
import java.time.LocalDate

@Composable
fun ProgressScreen(
    stats: List<HabitProgressStat>,
    dayCompletions: List<DayCompletion>,
    badges: List<MilestoneBadge>,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

    val currentStreak = stats.maxOfOrNull { it.currentStreak } ?: 0
    val longestStreak = stats.maxOfOrNull { it.bestStreak } ?: 0
    val totalCompletions = stats.sumOf { it.totalCompletions }
    val activeHabitsCount = stats.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Personal Mastery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textMuted
                )
                Text(
                    text = "Consistency Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.textPrimary
                )
            }
        }

        // Top 4 Metrics 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricOverviewCard(
                        icon = "🔥",
                        label = "Current Streak",
                        value = "$currentStreak Days",
                        modifier = Modifier.weight(1f)
                    )
                    MetricOverviewCard(
                        icon = "⚡",
                        label = "Longest Streak",
                        value = "$longestStreak Days",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricOverviewCard(
                        icon = "🏆",
                        label = "Total Sessions",
                        value = "$totalCompletions Done",
                        modifier = Modifier.weight(1f)
                    )
                    MetricOverviewCard(
                        icon = "🌱",
                        label = "Active Routines",
                        value = "$activeHabitsCount Habits",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Monthly Bar Chart
        item {
            MonthlyBarChart(dayCompletions = dayCompletions)
        }

        // Habit Completion Percentages
        item {
            Column {
                Text(
                    text = "Habit Completion Rate",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.textPrimary
                )
                Text(
                    text = "Calculated across scheduled days in the past 30 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )
            }
        }

        if (stats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(extendedColors.cardBackground)
                        .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits tracked yet. Add habits on the Today tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.textMuted
                    )
                }
            }
        } else {
            items(stats, key = { it.habit.id }) { stat ->
                HabitProgressPercentageCard(stat = stat)
            }
        }

        // Milestone Badges Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                val unlockedCount = badges.count { it.isUnlocked }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Milestone Badges",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = extendedColors.textPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(extendedColors.surfaceRaised)
                            .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$unlockedCount / ${badges.size} Unlocked",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Earn badges by building streaks and hitting consistency marks",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )
            }
        }

        items(badges, key = { it.id }) { badge ->
            MilestoneBadgeCard(badge = badge)
        }
    }
}

@Composable
private fun MetricOverviewCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                .background(extendedColors.surfaceRaised),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = extendedColors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun HabitProgressPercentageCard(stat: HabitProgressStat) {
    val extendedColors = LocalExtendedColors.current
    val percentageInt = stat.completionPercentage.toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("progress_card_${stat.habit.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(extendedColors.surfaceRaised),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stat.habit.icon.ifBlank { "⭐" }, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stat.habit.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "${stat.totalCompletions} total completions • Best ${stat.bestStreak}d streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textMuted
                    )
                }

                Text(
                    text = "$percentageInt%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (stat.completionPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = extendedColors.cardBorder
            )
        }
    }
}
