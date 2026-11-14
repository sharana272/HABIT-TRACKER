package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.Habit
import com.example.data.db.HabitDao
import com.example.data.db.HabitLog
import com.example.data.db.HabitLogDao
import com.example.data.db.HabitTypeConverters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var habitLogDao: HabitLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = database.habitDao()
        habitLogDao = database.habitLogDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `type converters round trip all domain types`() {
        val converters = HabitTypeConverters()

        val days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val daysEncoded = converters.fromDayOfWeekList(days)
        assertEquals("MON,WED,FRI", daysEncoded)
        assertEquals(days, converters.toDayOfWeekList(daysEncoded))

        val date = LocalDate.of(2026, 11, 13)
        val dateEncoded = converters.fromLocalDate(date)
        assertEquals("2026-11-13", dateEncoded)
        assertEquals(date, converters.toLocalDate(dateEncoded))

        val time = LocalTime.of(16, 45)
        val timeEncoded = converters.fromLocalTime(time)
        assertEquals(16 * 60 + 45, timeEncoded)
        assertEquals(time, converters.toLocalTime(timeEncoded))

        val timestamp = Date(1700000000000L)
        assertEquals(timestamp, converters.toDate(converters.fromDate(timestamp)))

        val tags = listOf("health", "morning", "cardio")
        assertEquals(tags, converters.toStringList(converters.fromStringList(tags)))

        val extraTimes = listOf(LocalTime.of(8, 0), LocalTime.of(13, 30), LocalTime.of(20, 15))
        assertEquals(extraTimes, converters.toLocalTimeList(converters.fromLocalTimeList(extraTimes)))
    }

    @Test
    fun `insert query update and delete habit with logs`() = runTest {
        val habit = Habit(
            name = "Evening Mobility",
            icon = "🧘",
            category = "Fitness",
            targetTime = LocalTime.of(19, 30),
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            reminderEnabled = true,
            reminderMessage = "Stretch for 15 minutes",
            tags = listOf("recovery", "evening"),
            additionalTimes = listOf(LocalTime.of(8, 0), LocalTime.of(13, 0))
        )

        val insertedId = habitDao.insertHabit(habit)
        assertTrue(insertedId > 0)

        val loaded = habitDao.getHabitById(insertedId)
        assertNotNull(loaded)
        assertEquals("Evening Mobility", loaded?.name)
        assertEquals(LocalTime.of(19, 30), loaded?.targetTime)
        assertEquals(listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), loaded?.repeatDays)
        assertEquals(listOf("recovery", "evening"), loaded?.tags)
        assertEquals(
            listOf(LocalTime.of(8, 0), LocalTime.of(13, 0), LocalTime.of(19, 30)),
            loaded?.allTimes
        )

        val logDate = LocalDate.of(2026, 11, 13)
        habitLogDao.insertOrUpdateLog(
            HabitLog(habitId = insertedId, date = logDate, completed = true)
        )

        val logsForDate = habitLogDao.getLogsForDate(logDate)
        assertEquals(1, logsForDate.size)
        assertTrue(logsForDate.first().completed)

        val monthLogs = habitLogDao.getLogsForMonth("2026-11")
        assertEquals(1, monthLogs.size)

        val updatedHabit = loaded!!.copy(name = "Night Mobility")
        habitDao.updateHabit(updatedHabit)
        assertEquals("Night Mobility", habitDao.getHabitByIdFlow(insertedId).first()?.name)

        // Deleting habit cascades to habit_logs
        habitDao.deleteHabit(updatedHabit)
        assertNull(habitDao.getHabitById(insertedId))
        assertTrue(habitLogDao.getLogsForDate(logDate).isEmpty())
    }
}
