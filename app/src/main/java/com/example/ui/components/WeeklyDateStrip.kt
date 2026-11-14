package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyDateStrip(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    dayCompletions: Map<LocalDate, Boolean>, // whether day was fully completed or partially
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val today = LocalDate.now()

    // Determine the 7 days of current selected week (Monday to Sunday)
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("weekly_date_strip"),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEach { date ->
            val isSelected = date.isEqual(selectedDate)
            val isToday = date.isEqual(today)
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase().take(3)
            val dayNumber = date.dayOfMonth.toString()
            val isCompleted = dayCompletions[date] == true

            val pillBg = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                extendedColors.cardBackground
            }

            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else if (isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                extendedColors.textSecondary
            }

            val borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else if (isToday) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                extendedColors.cardBorder
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillBg)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onSelectDate(date) }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .testTag("date_pill_${date}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else extendedColors.textMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dayNumber,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Completion status dot
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}
