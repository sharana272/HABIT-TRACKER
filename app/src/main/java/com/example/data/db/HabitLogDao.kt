package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE dateString = :date")
    fun getLogsForDateFlow(date: LocalDate): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE dateString = :date")
    suspend fun getLogsForDate(date: LocalDate): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE dateString LIKE :monthPrefix || '%'")
    fun getLogsForMonthFlow(monthPrefix: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE dateString LIKE :monthPrefix || '%'")
    suspend fun getLogsForMonth(monthPrefix: String): List<HabitLog>

    @Query("SELECT * FROM habit_logs")
    fun getAllLogsFlow(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateString DESC")
    fun getLogsForHabitFlow(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND completed = 1 ORDER BY dateString DESC")
    suspend fun getCompletedLogsForHabit(habitId: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE completed = 1 ORDER BY dateString DESC")
    suspend fun getAllCompletedLogs(): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLog): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :date")
    suspend fun deleteLog(habitId: Long, date: LocalDate)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun clearAllLogs()
}
