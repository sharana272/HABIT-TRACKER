package com.example.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.db.Habit
import com.example.data.db.HabitTypeConverters
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"
    private val converters = HabitTypeConverters()

    private const val MAX_TIME_SLOTS_PER_HABIT = 12

    fun scheduleHabitReminder(context: Context, habit: Habit) {
        cancelHabitReminder(context, habit.id)
        if (!habit.reminderEnabled) return

        val repeatDaysStr = converters.fromDayOfWeekList(habit.repeatDays)
        habit.allTimes.take(MAX_TIME_SLOTS_PER_HABIT).forEachIndexed { slotIndex, time ->
            val timeStr = time.format(DateTimeFormatter.ofPattern("hh:mm a"))
            val message = if (habit.reminderMessage.isNotBlank()) {
                habit.reminderMessage
            } else {
                "Time for your habit: ${habit.name} ($timeStr)"
            }
            val timeMinutes = time.hour * 60 + time.minute

            scheduleNextReminder(
                context = context,
                habitId = habit.id,
                habitName = habit.name,
                habitMessage = message,
                habitTime = timeStr,
                timeMinutes = timeMinutes,
                repeatDays = repeatDaysStr,
                slotIndex = slotIndex
            )
        }
    }

    fun scheduleNextReminder(
        context: Context,
        habitId: Long,
        habitName: String,
        habitMessage: String,
        habitTime: String,
        timeMinutes: Int,
        repeatDays: String,
        slotIndex: Int = 0
    ) {
        val targetDayTimes = getNextScheduledDateTime(timeMinutes, repeatDays) ?: return

        val epochMillis = targetDayTimes.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val requestCode = habitId.toInt() * 100 + slotIndex
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = "com.example.rulzhabit.ACTION_REMINDER"
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_MESSAGE, habitMessage)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_TIME, habitTime)
            putExtra(HabitReminderReceiver.EXTRA_TIME_MINUTES, timeMinutes)
            putExtra(HabitReminderReceiver.EXTRA_REPEAT_DAYS, repeatDays)
            putExtra(HabitReminderReceiver.EXTRA_SLOT_INDEX, slotIndex)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    epochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    epochMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for $habitName at $targetDayTimes")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted, falling back to inexact alarm", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                epochMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    fun cancelHabitReminder(context: Context, habitId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        for (slotIndex in 0 until MAX_TIME_SLOTS_PER_HABIT) {
            val requestCode = habitId.toInt() * 100 + slotIndex
            val intent = Intent(context, HabitReminderReceiver::class.java).apply {
                action = "com.example.rulzhabit.ACTION_REMINDER"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager?.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun triggerTestNotification(context: Context, title: String = "⚡ Smart Reminder Test", message: String = "You have a session scheduled today at 4 PM! Stay consistent.") {
        HabitReminderReceiver.createNotificationChannel(context)
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, 999999L)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, title)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_MESSAGE, message)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_TIME, "Now")
        }
        val receiver = HabitReminderReceiver()
        receiver.onReceive(context, intent)
    }

    private fun getNextScheduledDateTime(timeMinutes: Int, repeatDaysStr: String): LocalDateTime? {
        val daysList = repeatDaysStr.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
        if (daysList.isEmpty()) return null

        val dayOfWeekMap = mapOf(
            "MON" to DayOfWeek.MONDAY,
            "TUE" to DayOfWeek.TUESDAY,
            "WED" to DayOfWeek.WEDNESDAY,
            "THU" to DayOfWeek.THURSDAY,
            "FRI" to DayOfWeek.FRIDAY,
            "SAT" to DayOfWeek.SATURDAY,
            "SUN" to DayOfWeek.SUNDAY
        )

        val scheduledDays = daysList.mapNotNull { dayOfWeekMap[it] }.toSet()
        if (scheduledDays.isEmpty()) return null

        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(timeMinutes / 60, timeMinutes % 60)

        // Check next 14 days for the nearest matching day
        for (i in 0..14) {
            val candidateDate = now.plusDays(i.toLong()).toLocalDate()
            val candidateDateTime = LocalDateTime.of(candidateDate, targetTime)
            if (scheduledDays.contains(candidateDate.dayOfWeek)) {
                if (candidateDateTime.isAfter(now.plusSeconds(30))) {
                    return candidateDateTime
                }
            }
        }
        return null
    }
}
