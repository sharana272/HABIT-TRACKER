package com.example.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Habit Reminder"
        val habitMessage = intent.getStringExtra(EXTRA_HABIT_MESSAGE) ?: "Time to complete your habit!"
        val habitTime = intent.getStringExtra(EXTRA_HABIT_TIME) ?: ""

        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "TODAY")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = if (habitTime.isNotEmpty()) {
            "⚡ $habitName • $habitTime"
        } else {
            "⚡ $habitName"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(habitMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(habitMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val slotIndex = intent.getIntExtra(EXTRA_SLOT_INDEX, 0)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = if (habitId > 0) habitId.toInt() * 100 + slotIndex else System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        // Reschedule if needed
        if (habitId > 0) {
            val repeatDays = intent.getStringExtra(EXTRA_REPEAT_DAYS) ?: ""
            val timeMinutes = intent.getIntExtra(EXTRA_TIME_MINUTES, 480)
            ReminderScheduler.scheduleNextReminder(context, habitId, habitName, habitMessage, habitTime, timeMinutes, repeatDays, slotIndex)
        }
    }

    companion object {
        const val CHANNEL_ID = "rulz_habit_reminders"
        const val CHANNEL_NAME = "Habit Reminders"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_HABIT_MESSAGE = "extra_habit_message"
        const val EXTRA_HABIT_TIME = "extra_habit_time"
        const val EXTRA_TIME_MINUTES = "extra_time_minutes"
        const val EXTRA_REPEAT_DAYS = "extra_repeat_days"
        const val EXTRA_SLOT_INDEX = "extra_slot_index"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for scheduled daily and weekly habits"
                    enableVibration(true)
                    setShowBadge(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
