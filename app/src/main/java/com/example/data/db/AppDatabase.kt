package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(HabitTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rulz_habit_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.habitDao(), database.habitLogDao())
                    }
                }
            }

            suspend fun populateInitialData(habitDao: HabitDao, logDao: HabitLogDao) {
                val allDays = DayOfWeek.entries.toList()
                val weekdays = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )

                val habit1 = Habit(
                    id = 1,
                    name = "Morning Hydration",
                    icon = "💧",
                    category = "Health",
                    targetTime = LocalTime.of(7, 0),
                    repeatDays = allDays,
                    reminderEnabled = true,
                    reminderMessage = "Drink 500ml water to kickstart your metabolism",
                    colorHex = "#34D399"
                )
                val habit2 = Habit(
                    id = 2,
                    name = "Morning Cardio & Run",
                    icon = "🏃",
                    category = "Fitness",
                    targetTime = LocalTime.of(7, 30),
                    repeatDays = allDays,
                    reminderEnabled = true,
                    reminderMessage = "Time for your 30-minute cardio session",
                    colorHex = "#10B981"
                )
                val habit3 = Habit(
                    id = 3,
                    name = "Deep Work Reading",
                    icon = "📚",
                    category = "Learning",
                    targetTime = LocalTime.of(9, 0),
                    repeatDays = weekdays,
                    reminderEnabled = true,
                    reminderMessage = "Read 20 pages of non-fiction book",
                    colorHex = "#68DDA9"
                )
                val habit4 = Habit(
                    id = 4,
                    name = "Mindful Meditation",
                    icon = "🧘",
                    category = "Mind",
                    targetTime = LocalTime.of(12, 30),
                    repeatDays = allDays,
                    reminderEnabled = true,
                    reminderMessage = "Take 10 minutes to breathe and center yourself",
                    colorHex = "#45DFA4"
                )
                val habit5 = Habit(
                    id = 5,
                    name = "Evening Review & Journal",
                    icon = "✍️",
                    category = "Productivity",
                    targetTime = LocalTime.of(20, 0),
                    repeatDays = allDays,
                    reminderEnabled = true,
                    reminderMessage = "Review your top 3 wins and plan for tomorrow",
                    colorHex = "#10B981"
                )

                habitDao.insertHabits(listOf(habit1, habit2, habit3, habit4, habit5))

                // Populate past 5 days of realistic completion data
                val today = LocalDate.now()

                for (i in 5 downTo 1) {
                    val date = today.minusDays(i.toLong())
                    logDao.insertOrUpdateLog(HabitLog(habitId = 1, date = date, completed = true))
                    logDao.insertOrUpdateLog(HabitLog(habitId = 2, date = date, completed = true))
                    if (date.dayOfWeek.value <= 5) {
                        logDao.insertOrUpdateLog(HabitLog(habitId = 3, date = date, completed = true))
                    }
                    if (i != 2) {
                        logDao.insertOrUpdateLog(HabitLog(habitId = 4, date = date, completed = true))
                    }
                    logDao.insertOrUpdateLog(HabitLog(habitId = 5, date = date, completed = true))
                }

                // For today, mark first two completed
                logDao.insertOrUpdateLog(HabitLog(habitId = 1, date = today, completed = true))
                logDao.insertOrUpdateLog(HabitLog(habitId = 2, date = today, completed = true))
            }
        }
    }
}
