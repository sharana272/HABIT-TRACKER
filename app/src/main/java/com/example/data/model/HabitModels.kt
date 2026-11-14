package com.example.data.model

import com.example.data.db.Habit
import java.time.LocalDate

data class HabitItem(
    val habit: Habit,
    val isCompleted: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val isScheduledForSelectedDate: Boolean,
    val formattedTime: String,
    val repeatDaysFormatted: String,
    val formattedTimes: List<String> = listOf(formattedTime)
)

data class DayCompletion(
    val date: LocalDate,
    val totalScheduled: Int,
    val completedCount: Int,
    val isAllCompleted: Boolean,
    val isPartial: Boolean,
    val isMissed: Boolean,
    val isFuture: Boolean
)

data class HabitProgressStat(
    val habit: Habit,
    val totalCompletions: Int,
    val completionPercentage: Float, // 0.0f - 100.0f
    val currentStreak: Int,
    val bestStreak: Int
)

data class MilestoneBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val progress: Float, // 0f to 1f
    val progressLabel: String
)

enum class ThemeMode {
    OBSIDIAN_DARK,
    REFINED_LIGHT,
    SYSTEM
}

enum class NavigationTab {
    TODAY,
    CALENDAR,
    PROGRESS,
    COACH,
    SETTINGS
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String = "gemini-3.8-flash"
)

enum class MessageRole {
    USER,
    ASSISTANT
}

enum class CoachPersona(
    val title: String,
    val iconEmoji: String,
    val modelName: String,
    val systemInstruction: String,
    val subtitle: String
) {
    HABIT_COACH(
        title = "Habit Coach",
        iconEmoji = "🧠",
        modelName = "gemini-3.8-flash",
        subtitle = "General behavioral & streak mentor (gemini-3.8-flash)",
        systemInstruction = "You are Rulz Habit Coach, an elite behavioral psychologist and personal accountability mentor. You give concise, actionable, motivating advice on building sustainable daily routines, overcoming procrastination, habit stacking, and recovering from broken streaks. Keep advice structured, empathetic, and grounded in behavioral science."
    ),
    QUICK_TACTICS(
        title = "Quick Tactics",
        iconEmoji = "⚡",
        modelName = "gemini-3.1-flash-lite",
        subtitle = "Fast micro-tips & 2-minute steps (gemini-3.1-flash-lite)",
        systemInstruction = "You are Rulz Quick Tactics, specialized in lightning-fast, high-impact productivity micro-steps. Give 2-3 punchy bullet points to break inertia immediately."
    ),
    DEEP_STRATEGIST(
        title = "Deep Strategist",
        iconEmoji = "🏛️",
        modelName = "gemini-4-argon",
        subtitle = "Complex system & routine architect (gemini-4-argon)",
        systemInstruction = "You are Rulz Deep Routine Architect. You specialize in complex life system design, circadian rhythm optimization, multi-phase habit loops, deep work scheduling, and holistic lifestyle architecture. Provide analytical, structured blueprints."
    )
}
