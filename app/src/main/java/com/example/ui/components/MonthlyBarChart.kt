package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayCompletion
import com.example.ui.theme.LocalExtendedColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MonthlyBarChart(
    dayCompletions: List<DayCompletion>,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val maxCompletions = dayCompletions.maxOfOrNull { it.completedCount }?.coerceAtLeast(1) ?: 5

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("monthly_bar_chart")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Consistency Flow",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.textPrimary
                )
                Text(
                    text = "Habits accomplished per day",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )
            }

            val totalCompletedInPeriod = dayCompletions.sumOf { it.completedCount }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(extendedColors.surfaceRaised)
                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$totalCompletedInPeriod total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bars Container (showing last 14 days)
        val displayItems = dayCompletions.takeLast(14)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            displayItems.forEach { item ->
                val ratio = (item.completedCount.toFloat() / maxCompletions.toFloat()).coerceIn(0.05f, 1f)
                val animatedRatio by animateFloatAsState(
                    targetValue = if (item.completedCount > 0) ratio else 0.04f,
                    animationSpec = tween(durationMillis = 600),
                    label = "barHeightAnim"
                )

                val isToday = item.date.isEqual(LocalDate.now())
                val isAllDone = item.isAllCompleted

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (item.completedCount > 0) {
                        Text(
                            text = item.completedCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isToday) MaterialTheme.colorScheme.primary else extendedColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Bar Track & Fill
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(90.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(extendedColors.surfaceRaised),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedRatio)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isAllDone -> MaterialTheme.colorScheme.primary
                                        item.completedCount > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                                        else -> extendedColors.cardBorder
                                    }
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date Label
                    Text(
                        text = item.date.format(DateTimeFormatter.ofPattern("d", Locale.US)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = if (isToday) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                    )
                }
            }
        }
    }
}
