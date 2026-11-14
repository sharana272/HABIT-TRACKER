package com.example.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String, // Emoji or symbol representation
    val category: String, // Health, Fitness, Mind, Learning, Productivity, etc.
    @ColumnInfo(name = "timeMinutes")
    val targetTime: LocalTime, // Stored via TypeConverter as minutes from midnight
    val repeatDays: List<DayOfWeek>, // Stored via TypeConverter as comma-separated day codes
    val reminderEnabled: Boolean = true,
    val reminderMessage: String = "", // e.g. "Time for your morning workout!"
    val colorHex: String = "#10B981",
    val tags: List<String> = emptyList(), // Stored via TypeConverter as JSON array
    val additionalTimes: List<LocalTime> = emptyList(), // Extra reminder/schedule times
    @ColumnInfo(name = "createdAtTimestamp")
    val createdAt: Date = Date() // Stored via TypeConverter as epoch millis
) {
    val timeMinutes: Int
        get() = targetTime.hour * 60 + targetTime.minute

    val allTimes: List<LocalTime>
        get() = (listOf(targetTime) + additionalTimes).distinct().sorted()
}

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "dateString"], unique = true)
    ]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    @ColumnInfo(name = "dateString")
    val date: LocalDate, // Stored via TypeConverter as ISO-8601 "YYYY-MM-DD"
    val completed: Boolean,
    @ColumnInfo(name = "completedAtTimestamp")
    val completedAt: Date = Date() // Stored via TypeConverter as epoch millis
)
