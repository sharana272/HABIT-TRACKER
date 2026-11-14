package com.example.data.repository

import android.content.Context
import com.example.data.db.Habit
import com.example.data.db.HabitDao
import com.example.data.db.HabitLog
import com.example.data.db.HabitLogDao
import com.example.data.model.DayCompletion
import com.example.data.model.HabitItem
import com.example.data.model.HabitProgressStat
import com.example.data.model.MilestoneBadge
import com.example.data.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val context: Context
) {
    fun getHabitItemsForDate(selectedDate: LocalDate): Flow<List<HabitItem>> {
        return combine(
            habitDao.getAllHabitsFlow(),
            habitLogDao.getAllLogsFlow()
        ) { habits, allLogs ->
            val logsByHabitId = allLogs.groupBy { it.habitId }
            val logsForSelectedDate = allLogs.filter { it.date == selectedDate }.associateBy { it.habitId }

            habits.map { habit ->
                val isCompleted = logsForSelectedDate[habit.id]?.completed == true
                val habitLogs = logsByHabitId[habit.id] ?: emptyList()
                val streaks = calculateHabitStreaks(habit, habitLogs)

                HabitItem(
                    habit = habit,
                    isCompleted = isCompleted,
                    currentStreak = streaks.first,
                    bestStreak = streaks.second,
                    isScheduledForSelectedDate = isHabitScheduledOn(habit, selectedDate.dayOfWeek),
                    formattedTime = formatTime(habit.targetTime),
                    repeatDaysFormatted = formatRepeatDays(habit.repeatDays),
                    formattedTimes = habit.allTimes.map { formatTime(it) }
                )
            }
        }
    }

    fun getMonthCompletions(yearMonth: YearMonth): Flow<Map<LocalDate, DayCompletion>> {
        val monthPrefix = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return combine(
            habitDao.getAllHabitsFlow(),
            habitLogDao.getLogsForMonthFlow(monthPrefix)
        ) { habits, logs ->
            val today = LocalDate.now()
            val daysInMonth = yearMonth.lengthOfMonth()
            val resultMap = mutableMapOf<LocalDate, DayCompletion>()
            val logsByDate = logs.filter { it.completed }.groupBy { it.date }

            for (day in 1..daysInMonth) {
                val date = yearMonth.atDay(day)
                val scheduledHabits = habits.filter { isHabitScheduledOn(it, date.dayOfWeek) }
                val completedLogs = logsByDate[date] ?: emptyList()
                val completedCount = completedLogs.size
                val totalScheduled = scheduledHabits.size

                val isFuture = date.isAfter(today)
                val isAllCompleted = totalScheduled > 0 && completedCount >= totalScheduled
                val isPartial = completedCount > 0 && completedCount < totalScheduled
                val isMissed = !isFuture && totalScheduled > 0 && completedCount == 0

                resultMap[date] = DayCompletion(
                    date = date,
                    totalScheduled = totalScheduled,
                    completedCount = completedCount,
                    isAllCompleted = isAllCompleted,
                    isPartial = isPartial,
                    isMissed = isMissed,
                    isFuture = isFuture
                )
            }
            resultMap
        }
    }

    fun getProgressStats(): Flow<List<HabitProgressStat>> {
        return combine(
            habitDao.getAllHabitsFlow(),
            habitLogDao.getAllLogsFlow()
        ) { habits, allLogs ->
            val logsByHabit = allLogs.filter { it.completed }.groupBy { it.habitId }
            val now = LocalDate.now()

            habits.map { habit ->
                val completedLogs = logsByHabit[habit.id] ?: emptyList()
                val totalCompletions = completedLogs.size
                val streaks = calculateHabitStreaks(habit, allLogs.filter { it.habitId == habit.id })

                // Calculate percentage over the last 30 days
                var scheduledDaysInLast30 = 0
                var completedDaysInLast30 = 0
                val completedDatesSet = completedLogs.map { it.date }.toSet()

                for (i in 0 until 30) {
                    val d = now.minusDays(i.toLong())
                    if (isHabitScheduledOn(habit, d.dayOfWeek)) {
                        scheduledDaysInLast30++
                        if (completedDatesSet.contains(d)) {
                            completedDaysInLast30++
                        }
                    }
                }

                val percentage = if (scheduledDaysInLast30 > 0) {
                    (completedDaysInLast30.toFloat() / scheduledDaysInLast30.toFloat()) * 100f
                } else {
                    0f
                }

                HabitProgressStat(
                    habit = habit,
                    totalCompletions = totalCompletions,
                    completionPercentage = percentage,
                    currentStreak = streaks.first,
                    bestStreak = streaks.second
                )
            }
        }
    }

    fun getMilestoneBadges(): Flow<List<MilestoneBadge>> {
        return combine(
            habitDao.getAllHabitsFlow(),
            habitLogDao.getAllLogsFlow()
        ) { habits, logs ->
            val completedLogs = logs.filter { it.completed }
            val totalCompletions = completedLogs.size

            var maxStreak = 0
            for (habit in habits) {
                val streaks = calculateHabitStreaks(habit, logs.filter { it.habitId == habit.id })
                if (streaks.first > maxStreak) maxStreak = streaks.first
                if (streaks.second > maxStreak) maxStreak = streaks.second
            }

            // Check perfect days
            val logsByDate = completedLogs.groupBy { it.date }
            var perfectDaysCount = 0
            logsByDate.forEach { (date, dateLogs) ->
                val scheduled = habits.filter { isHabitScheduledOn(it, date.dayOfWeek) }
                if (scheduled.isNotEmpty() && dateLogs.size >= scheduled.size) {
                    perfectDaysCount++
                }
            }

            // Morning habits
            val morningHabitIds = habits.filter { it.targetTime.hour < 8 }.map { it.id }.toSet()
            val morningCompletions = completedLogs.count { morningHabitIds.contains(it.habitId) }

            // Evening habits
            val eveningHabitIds = habits.filter { it.targetTime.hour >= 19 }.map { it.id }.toSet()
            val eveningCompletions = completedLogs.count { eveningHabitIds.contains(it.habitId) }

            listOf(
                MilestoneBadge(
                    id = "first_step",
                    title = "First Step",
                    description = "Log your first completed habit",
                    iconEmoji = "🌱",
                    isUnlocked = totalCompletions >= 1,
                    progress = (totalCompletions / 1f).coerceIn(0f, 1f),
                    progressLabel = if (totalCompletions >= 1) "Completed" else "0 / 1"
                ),
                MilestoneBadge(
                    id = "momentum_3",
                    title = "Consistency Starter",
                    description = "Achieve a 3-day active streak",
                    iconEmoji = "🔥",
                    isUnlocked = maxStreak >= 3,
                    progress = (maxStreak / 3f).coerceIn(0f, 1f),
                    progressLabel = "$maxStreak / 3 days"
                ),
                MilestoneBadge(
                    id = "unstoppable_7",
                    title = "Unstoppable",
                    description = "Reach a 7-day streak milestone",
                    iconEmoji = "⚡",
                    isUnlocked = maxStreak >= 7,
                    progress = (maxStreak / 7f).coerceIn(0f, 1f),
                    progressLabel = "$maxStreak / 7 days"
                ),
                MilestoneBadge(
                    id = "master_30",
                    title = "Habit Master",
                    description = "Maintain a stellar 30-day streak",
                    iconEmoji = "👑",
                    isUnlocked = maxStreak >= 30,
                    progress = (maxStreak / 30f).coerceIn(0f, 1f),
                    progressLabel = "$maxStreak / 30 days"
                ),
                MilestoneBadge(
                    id = "perfect_day",
                    title = "Flawless Day",
                    description = "Complete 100% of habits in a single day",
                    iconEmoji = "⭐",
                    isUnlocked = perfectDaysCount >= 1,
                    progress = (perfectDaysCount / 1f).coerceIn(0f, 1f),
                    progressLabel = if (perfectDaysCount >= 1) "Achieved ($perfectDaysCount)" else "0 / 1"
                ),
                MilestoneBadge(
                    id = "centurion",
                    title = "Centurion",
                    description = "Reach 100 lifetime habit completions",
                    iconEmoji = "🏆",
                    isUnlocked = totalCompletions >= 100,
                    progress = (totalCompletions / 100f).coerceIn(0f, 1f),
                    progressLabel = "$totalCompletions / 100"
                ),
                MilestoneBadge(
                    id = "early_bird",
                    title = "Early Bird",
                    description = "Complete a morning routine before 8 AM",
                    iconEmoji = "🌅",
                    isUnlocked = morningCompletions >= 1,
                    progress = (morningCompletions / 1f).coerceIn(0f, 1f),
                    progressLabel = if (morningCompletions >= 1) "Achieved" else "0 / 1"
                ),
                MilestoneBadge(
                    id = "night_owl",
                    title = "Evening Anchor",
                    description = "Complete an evening routine after 7 PM",
                    iconEmoji = "🌙",
                    isUnlocked = eveningCompletions >= 1,
                    progress = (eveningCompletions / 1f).coerceIn(0f, 1f),
                    progressLabel = if (eveningCompletions >= 1) "Achieved" else "0 / 1"
                )
            )
        }
    }

    suspend fun getAllHabits(): List<Habit> = habitDao.getAllHabits()

    suspend fun getCompletedLogsForHabit(habitId: Long): List<HabitLog> =
        habitLogDao.getCompletedLogsForHabit(habitId)

    suspend fun getAllCompletedLogs(): List<HabitLog> =
        habitLogDao.getAllCompletedLogs()

    suspend fun toggleHabitCompletion(habitId: Long, date: LocalDate): Boolean {
        val existingLogs = habitLogDao.getLogsForDate(date)
        val existing = existingLogs.firstOrNull { it.habitId == habitId }

        val newCompleted = if (existing != null) {
            !existing.completed
        } else {
            true
        }

        habitLogDao.insertOrUpdateLog(
            HabitLog(
                id = existing?.id ?: 0,
                habitId = habitId,
                date = date,
                completed = newCompleted,
                completedAt = Date()
            )
        )
        return newCompleted
    }

    suspend fun addHabit(habit: Habit): Long {
        val id = habitDao.insertHabit(habit)
        val createdHabit = habit.copy(id = id)
        ReminderScheduler.scheduleHabitReminder(context, createdHabit)
        return id
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
        ReminderScheduler.scheduleHabitReminder(context, habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        ReminderScheduler.cancelHabitReminder(context, habit.id)
        habitLogDao.deleteLogsForHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun resetToSampleData() {
        habitLogDao.clearAllLogs()
        habitDao.clearAllHabits()
        val allDays = DayOfWeek.entries.toList()
        val weekdays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )

        val habit1 = Habit(1, "Morning Hydration", "💧", "Health", LocalTime.of(7, 0), allDays, true, "Drink 500ml fresh water", "#34D399")
        val habit2 = Habit(2, "Morning Cardio & Run", "🏃", "Fitness", LocalTime.of(7, 30), allDays, true, "30-min morning workout", "#10B981")
        val habit3 = Habit(3, "Deep Work Reading", "📚", "Learning", LocalTime.of(9, 0), weekdays, true, "Read 20 pages non-fiction", "#68DDA9")
        val habit4 = Habit(4, "Mindful Meditation", "🧘", "Mind", LocalTime.of(12, 30), allDays, true, "10-min breathwork & reset", "#45DFA4")
        val habit5 = Habit(5, "Evening Review & Journal", "✍️", "Productivity", LocalTime.of(20, 0), allDays, true, "Daily wins & tomorrow preview", "#10B981")

        habitDao.insertHabits(listOf(habit1, habit2, habit3, habit4, habit5))

        ReminderScheduler.scheduleHabitReminder(context, habit1)
        ReminderScheduler.scheduleHabitReminder(context, habit2)
        ReminderScheduler.scheduleHabitReminder(context, habit3)
        ReminderScheduler.scheduleHabitReminder(context, habit4)
        ReminderScheduler.scheduleHabitReminder(context, habit5)

        val today = LocalDate.now()
        for (i in 5 downTo 1) {
            val date = today.minusDays(i.toLong())
            habitLogDao.insertOrUpdateLog(HabitLog(habitId = 1, date = date, completed = true))
            habitLogDao.insertOrUpdateLog(HabitLog(habitId = 2, date = date, completed = true))
            if (date.dayOfWeek.value <= 5) {
                habitLogDao.insertOrUpdateLog(HabitLog(habitId = 3, date = date, completed = true))
            }
            if (i != 2) {
                habitLogDao.insertOrUpdateLog(HabitLog(habitId = 4, date = date, completed = true))
            }
            habitLogDao.insertOrUpdateLog(HabitLog(habitId = 5, date = date, completed = true))
        }

        habitLogDao.insertOrUpdateLog(HabitLog(habitId = 1, date = today, completed = true))
        habitLogDao.insertOrUpdateLog(HabitLog(habitId = 2, date = today, completed = true))
    }

    // Helpers
    private fun isHabitScheduledOn(habit: Habit, dayOfWeek: DayOfWeek): Boolean {
        return habit.repeatDays.contains(dayOfWeek)
    }

    private fun calculateHabitStreaks(habit: Habit, logs: List<HabitLog>): Pair<Int, Int> {
        val completedDates = logs.filter { it.completed }.map { it.date }.toSet()
        val today = LocalDate.now()

        // Calculate current streak
        var currentStreak = 0
        var checkDate = today

        val todayCompleted = completedDates.contains(today)
        if (todayCompleted) {
            currentStreak++
        }
        checkDate = today.minusDays(1)

        var missed = false
        var daysBack = 0
        while (!missed && daysBack < 365) {
            if (isHabitScheduledOn(habit, checkDate.dayOfWeek)) {
                if (completedDates.contains(checkDate)) {
                    currentStreak++
                } else {
                    missed = true
                }
            }
            checkDate = checkDate.minusDays(1)
            daysBack++
        }

        // Calculate best streak
        var bestStreak = currentStreak
        var runningStreak = 0
        val earliestDate = today.minusDays(180)
        var scanDate = earliestDate

        while (!scanDate.isAfter(today)) {
            if (isHabitScheduledOn(habit, scanDate.dayOfWeek)) {
                if (completedDates.contains(scanDate)) {
                    runningStreak++
                    if (runningStreak > bestStreak) bestStreak = runningStreak
                } else {
                    runningStreak = 0
                }
            }
            scanDate = scanDate.plusDays(1)
        }

        return Pair(currentStreak, maxOf(currentStreak, bestStreak))
    }

    private fun formatTime(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
    }

    private fun formatRepeatDays(repeatDays: List<DayOfWeek>): String {
        val days = repeatDays.distinct()
        return when {
            days.size == 7 -> "Everyday"
            days.size == 5 && days.containsAll(
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
            ) -> "Weekdays"
            days.size == 2 && days.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) -> "Weekends"
            else -> days.sortedBy { it.value }.joinToString(", ") {
                it.getDisplayName(TextStyle.SHORT, Locale.US)
            }
        }
    }
}
