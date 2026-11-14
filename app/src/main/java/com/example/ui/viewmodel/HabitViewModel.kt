package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.Habit
import com.example.data.db.HabitLog
import com.example.data.model.ChatMessage
import com.example.data.model.CoachPersona
import com.example.data.model.DayCompletion
import com.example.data.model.HabitItem
import com.example.data.model.HabitProgressStat
import com.example.data.model.MessageRole
import com.example.data.model.MilestoneBadge
import com.example.data.model.NavigationTab
import com.example.data.model.ThemeMode
import com.example.data.reminder.ReminderScheduler
import com.example.data.repository.HabitRepository
import com.example.data.repository.LocalFallbackAiCoachRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = HabitRepository(database.habitDao(), database.habitLogDao(), application)
    private val aiCoachRepository = LocalFallbackAiCoachRepository()

    // Navigation & Selected Date
    private val _currentTab = MutableStateFlow(NavigationTab.TODAY)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _calendarMonth = MutableStateFlow(YearMonth.now())
    val calendarMonth: StateFlow<YearMonth> = _calendarMonth.asStateFlow()

    // Preferences
    private val _themeMode = MutableStateFlow(ThemeMode.OBSIDIAN_DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _smartRemindersMaster = MutableStateFlow(true)
    val smartRemindersMaster: StateFlow<Boolean> = _smartRemindersMaster.asStateFlow()

    // Add / Edit Sheet state
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen: StateFlow<Boolean> = _isAddEditSheetOpen.asStateFlow()

    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    // AI Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _selectedPersona = MutableStateFlow(CoachPersona.HABIT_COACH)
    val selectedPersona: StateFlow<CoachPersona> = _selectedPersona.asStateFlow()

    private val _isAiStreaming = MutableStateFlow(false)
    val isAiStreaming: StateFlow<Boolean> = _isAiStreaming.asStateFlow()

    // Current streaks keyed by habitId, computed from Room completion history
    private val _currentStreaks = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val currentStreaks: StateFlow<Map<Long, Int>> = _currentStreaks.asStateFlow()

    private val _maxCurrentStreak = MutableStateFlow(0)
    val maxCurrentStreak: StateFlow<Int> = _maxCurrentStreak.asStateFlow()

    init {
        calculateAndUpdateStreaks()
    }

    // Flow of habits for the selected date
    val habitsForSelectedDate: StateFlow<List<HabitItem>> = _selectedDate
        .flatMapLatest { date -> repository.getHabitItemsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month calendar completions
    val monthCompletions: StateFlow<Map<LocalDate, DayCompletion>> = _calendarMonth
        .flatMapLatest { yearMonth -> repository.getMonthCompletions(yearMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Progress stats
    val progressStats: StateFlow<List<HabitProgressStat>> = repository.getProgressStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Milestone badges
    val milestoneBadges: StateFlow<List<MilestoneBadge>> = repository.getMilestoneBadges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _calendarMonth.value = YearMonth.from(date)
    }

    fun previousMonth() {
        _calendarMonth.value = _calendarMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _calendarMonth.value = _calendarMonth.value.plusMonths(1)
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _calendarMonth.value = YearMonth.from(today)
    }

    fun toggleHabitCompletion(habitId: Long, date: LocalDate = _selectedDate.value) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, date)
            calculateAndUpdateStreaks()
        }
    }

    /**
     * Retrieves all habits and their completion history from the Room database,
     * calculates each habit's current streak, and updates [currentStreaks] and [maxCurrentStreak].
     */
    fun calculateAndUpdateStreaks(referenceDate: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val habits = repository.getAllHabits()
            val allCompletedLogs = repository.getAllCompletedLogs()
            val logsByHabitId = allCompletedLogs.groupBy { it.habitId }

            val updatedStreaks = habits.associate { habit ->
                val history = logsByHabitId[habit.id] ?: emptyList()
                habit.id to calculateCurrentStreak(habit, history, referenceDate)
            }

            _currentStreaks.value = updatedStreaks
            _maxCurrentStreak.value = updatedStreaks.values.maxOrNull() ?: 0
        }
    }

    /**
     * Calculates the current streak for a [habit] from its Room [completionHistory]
     * up to [referenceDate], honoring the habit's scheduled [Habit.repeatDays].
     */
    fun calculateCurrentStreak(
        habit: Habit,
        completionHistory: List<HabitLog>,
        referenceDate: LocalDate = LocalDate.now()
    ): Int {
        val completedDates = completionHistory
            .filter { it.completed }
            .map { it.date }
            .toSet()

        var streak = 0
        if (completedDates.contains(referenceDate)) {
            streak++
        }

        var checkDate = referenceDate.minusDays(1)
        var daysBack = 0
        while (daysBack < 365) {
            if (habit.repeatDays.contains(checkDate.dayOfWeek)) {
                if (completedDates.contains(checkDate)) {
                    streak++
                } else {
                    break
                }
            }
            checkDate = checkDate.minusDays(1)
            daysBack++
        }
        return streak
    }

    fun openAddHabit() {
        _editingHabit.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditHabit(habit: Habit) {
        _editingHabit.value = habit
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        _isAddEditSheetOpen.value = false
        _editingHabit.value = null
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            if (habit.id > 0) {
                repository.updateHabit(habit)
            } else {
                repository.addHabit(habit)
            }
            calculateAndUpdateStreaks()
            closeAddEditSheet()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            calculateAndUpdateStreaks()
            closeAddEditSheet()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleSmartRemindersMaster(enabled: Boolean) {
        _smartRemindersMaster.value = enabled
    }

    fun triggerTestNotification(title: String, message: String) {
        ReminderScheduler.triggerTestNotification(getApplication(), title, message)
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            calculateAndUpdateStreaks()
        }
    }

    // AI Chat Methods
    fun selectCoachPersona(persona: CoachPersona) {
        _selectedPersona.value = persona
    }

    fun sendChatMessage(text: String) {
        val persona = _selectedPersona.value
        val userMsg = ChatMessage(
            role = MessageRole.USER,
            text = text,
            modelName = persona.modelName
        )
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMsg)
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isAiStreaming.value = true
            val assistantMsgId = java.util.UUID.randomUUID().toString()
            var assistantMsg = ChatMessage(
                id = assistantMsgId,
                role = MessageRole.ASSISTANT,
                text = "",
                modelName = persona.modelName
            )
            // Add initial empty assistant bubble
            _chatMessages.value = currentList + assistantMsg

            aiCoachRepository.sendMessage(text, persona, currentList).collect { partialText ->
                assistantMsg = assistantMsg.copy(text = partialText)
                _chatMessages.value = currentList + assistantMsg
            }
            _isAiStreaming.value = false
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
    }
}
