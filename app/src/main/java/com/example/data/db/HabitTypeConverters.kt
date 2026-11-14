package com.example.data.db

import androidx.room.TypeConverter
import org.json.JSONArray
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

class HabitTypeConverters {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String {
        if (days.isNullOrEmpty()) return ""
        return days.distinct().joinToString(",") { day ->
            when (day) {
                DayOfWeek.MONDAY -> "MON"
                DayOfWeek.TUESDAY -> "TUE"
                DayOfWeek.WEDNESDAY -> "WED"
                DayOfWeek.THURSDAY -> "THU"
                DayOfWeek.FRIDAY -> "FRI"
                DayOfWeek.SATURDAY -> "SAT"
                DayOfWeek.SUNDAY -> "SUN"
            }
        }
    }

    @TypeConverter
    fun toDayOfWeekList(value: String?): List<DayOfWeek> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",")
            .mapNotNull { token ->
                when (token.trim().uppercase()) {
                    "MON", "MONDAY" -> DayOfWeek.MONDAY
                    "TUE", "TUESDAY" -> DayOfWeek.TUESDAY
                    "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY
                    "THU", "THURSDAY" -> DayOfWeek.THURSDAY
                    "FRI", "FRIDAY" -> DayOfWeek.FRIDAY
                    "SAT", "SATURDAY" -> DayOfWeek.SATURDAY
                    "SUN", "SUNDAY" -> DayOfWeek.SUNDAY
                    else -> null
                }
            }
            .distinct()
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Int? {
        return time?.let { it.hour * 60 + it.minute }
    }

    @TypeConverter
    fun toLocalTime(minutesFromMidnight: Int?): LocalTime? {
        return minutesFromMidnight?.let { mins ->
            val clamped = mins.coerceIn(0, 23 * 60 + 59)
            LocalTime.of(clamped / 60, clamped % 60)
        }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(value)
            List(jsonArray.length()) { index -> jsonArray.getString(index) }
        } catch (_: Exception) {
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    @TypeConverter
    fun fromLocalTimeList(times: List<LocalTime>?): String {
        if (times.isNullOrEmpty()) return ""
        return times.distinct().sorted().joinToString(",") { time ->
            (time.hour * 60 + time.minute).toString()
        }
    }

    @TypeConverter
    fun toLocalTimeList(value: String?): List<LocalTime> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",")
            .mapNotNull { token -> token.trim().toIntOrNull()?.let { toLocalTime(it) } }
            .distinct()
            .sorted()
    }
}
