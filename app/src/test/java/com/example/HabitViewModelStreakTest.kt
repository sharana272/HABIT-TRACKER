package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.Habit
import com.example.data.db.HabitLog
import com.example.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitViewModelStreakTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var application: Application
    private lateinit var viewModel: HabitViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = HabitViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `calculateCurrentStreak handles consecutive scheduled days and gaps`() {
        val referenceDate = LocalDate.of(2026, 11, 13) // Friday
        val habit = Habit(
            id = 10L,
            name = "MWF Strength",
            icon = "🏋️",
            category = "Fitness",
            targetTime = LocalTime.of(8, 0),
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        val history = listOf(
            HabitLog(habitId = 10L, date = LocalDate.of(2026, 11, 13), completed = true), // Fri
            HabitLog(habitId = 10L, date = LocalDate.of(2026, 11, 11), completed = true), // Wed
            HabitLog(habitId = 10L, date = LocalDate.of(2026, 11, 9), completed = true),  // Mon
            // Missed Fri Nov 6
            HabitLog(habitId = 10L, date = LocalDate.of(2026, 11, 4), completed = true)   // Wed
        )

        val streak = viewModel.calculateCurrentStreak(habit, history, referenceDate)
        assertEquals(3, streak)
    }

    @Test
    fun `calculateAndUpdateStreaks fetches completion history from Room and updates state flows`() = runBlocking {
        val db = AppDatabase.getDatabase(application, this)
        val habitDao = db.habitDao()
        val habitLogDao = db.habitLogDao()

        // Ensure database creation & initial seed callback have completed before clearing
        habitDao.getAllHabits()
        delay(200)

        habitLogDao.clearAllLogs()
        habitDao.clearAllHabits()

        val referenceDate = LocalDate.of(2026, 11, 13)
        val habitId = habitDao.insertHabit(
            Habit(
                name = "Daily Reading",
                icon = "📚",
                category = "Learning",
                targetTime = LocalTime.of(9, 0),
                repeatDays = DayOfWeek.entries.toList()
            )
        )

        // 4 consecutive days ending on referenceDate
        for (offset in 0L..3L) {
            habitLogDao.insertOrUpdateLog(
                HabitLog(
                    habitId = habitId,
                    date = referenceDate.minusDays(offset),
                    completed = true
                )
            )
        }

        viewModel.calculateAndUpdateStreaks(referenceDate)

        val streaks = withTimeout(5_000L) {
            viewModel.currentStreaks.first { it[habitId] == 4 }
        }

        assertEquals(4, streaks[habitId])
        assertEquals(4, viewModel.maxCurrentStreak.value)
    }
}
