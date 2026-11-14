package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayCompletion
import com.example.ui.theme.LocalExtendedColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MonthCalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    completions: Map<LocalDate, DayCompletion>,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val today = LocalDate.now()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("month_calendar_card")
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.textPrimary
                )
                Text(
                    text = if (currentMonth == YearMonth.from(today)) "Current Month" else "Historical Archive",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentMonth != YearMonth.from(today)) {
                    OutlinedButton(
                        onClick = onJumpToToday,
                        modifier = Modifier
                            .height(34.dp)
                            .padding(end = 6.dp)
                            .testTag("calendar_today_button"),
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Text("Today", style = MaterialTheme.typography.labelSmall)
                    }
                }

                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("calendar_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = extendedColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("calendar_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = extendedColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
            weekdays.forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.textMuted,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Days Grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 for Monday .. 7 for Sunday
        val daysInMonth = currentMonth.lengthOfMonth()

        val totalCells = ((firstDayOfWeek - 1 + daysInMonth + 6) / 7) * 7
        val rows = totalCells / 7

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 1..7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - (firstDayOfWeek - 1)

                        if (dayNumber in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNumber)
                            val completion = completions[date]
                            val isSelected = date.isEqual(selectedDate)
                            val isToday = date.isEqual(today)

                            CalendarDayCell(
                                dayNumber = dayNumber,
                                date = date,
                                isToday = isToday,
                                isSelected = isSelected,
                                completion = completion,
                                onClick = { onSelectDate(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Blank cell outside month
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = MaterialTheme.colorScheme.primary, label = "All Done")
            LegendItem(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), label = "Partial")
            LegendItem(color = Color(0xFFEF4444).copy(alpha = 0.7f), label = "Missed")
            LegendItem(color = extendedColors.cardBorder, label = "Rest/Empty")
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    completion: DayCompletion?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

    val isAllCompleted = completion?.isAllCompleted == true
    val isPartial = completion?.isPartial == true
    val isMissed = completion?.isMissed == true

    val cellBg = when {
        isAllCompleted -> MaterialTheme.colorScheme.primary
        isPartial -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        isMissed -> Color(0xFFEF4444).copy(alpha = 0.2f)
        isSelected -> extendedColors.surfaceRaised
        else -> Color.Transparent
    }

    val textColor = when {
        isAllCompleted -> MaterialTheme.colorScheme.onPrimary
        isMissed -> Color(0xFFEF4444)
        isToday -> MaterialTheme.colorScheme.primary
        else -> extendedColors.textPrimary
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(cellBg)
            .border(
                width = if (isSelected || isToday) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .testTag("calendar_day_$dayNumber"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isToday || isSelected || isAllCompleted) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                ),
                color = textColor
            )

            if (isAllCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(10.dp)
                )
            } else if (isPartial) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else if (isMissed) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    val extendedColors = LocalExtendedColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = extendedColors.textMuted
        )
    }
}
