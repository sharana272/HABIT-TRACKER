package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.NavigationTab
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.components.RulzBottomBar
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CoachScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.RulzHabitTheme
import com.example.ui.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RulzHabitApp()
        }
    }
}

@Composable
fun RulzHabitApp(
    viewModel: HabitViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()

    val habitsForSelectedDate by viewModel.habitsForSelectedDate.collectAsStateWithLifecycle()
    val monthCompletions by viewModel.monthCompletions.collectAsStateWithLifecycle()
    val progressStats by viewModel.progressStats.collectAsStateWithLifecycle()
    val milestoneBadges by viewModel.milestoneBadges.collectAsStateWithLifecycle()

    val isAddEditSheetOpen by viewModel.isAddEditSheetOpen.collectAsStateWithLifecycle()
    val editingHabit by viewModel.editingHabit.collectAsStateWithLifecycle()
    val smartRemindersEnabled by viewModel.smartRemindersMaster.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val selectedPersona by viewModel.selectedPersona.collectAsStateWithLifecycle()
    val isAiStreaming by viewModel.isAiStreaming.collectAsStateWithLifecycle()

    // Handle back button: if not on TODAY, return to TODAY
    BackHandler(enabled = currentTab != NavigationTab.TODAY) {
        viewModel.setTab(NavigationTab.TODAY)
    }

    RulzHabitTheme(themeMode = themeMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                RulzBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentTab) {
                    NavigationTab.TODAY -> {
                        TodayScreen(
                            selectedDate = selectedDate,
                            onSelectDate = { viewModel.selectDate(it) },
                            habits = habitsForSelectedDate,
                            onToggleHabit = { viewModel.toggleHabitCompletion(it) },
                            onEditHabit = { viewModel.openEditHabit(it) },
                            onDeleteHabit = { viewModel.deleteHabit(it) },
                            onAddHabitClick = { viewModel.openAddHabit() },
                            onOpenCoach = { viewModel.setTab(NavigationTab.COACH) },
                            onJumpToToday = { viewModel.jumpToToday() }
                        )
                    }

                    NavigationTab.CALENDAR -> {
                        CalendarScreen(
                            currentMonth = calendarMonth,
                            selectedDate = selectedDate,
                            onSelectDate = { viewModel.selectDate(it) },
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onJumpToToday = { viewModel.jumpToToday() },
                            completions = monthCompletions,
                            habitsForSelectedDate = habitsForSelectedDate,
                            onToggleHabit = { habitId, date -> viewModel.toggleHabitCompletion(habitId, date) },
                            onEditHabit = { viewModel.openEditHabit(it) },
                            onDeleteHabit = { viewModel.deleteHabit(it) }
                        )
                    }

                    NavigationTab.PROGRESS -> {
                        val daysInMonthList = monthCompletions.values.toList()
                        ProgressScreen(
                            stats = progressStats,
                            dayCompletions = daysInMonthList,
                            badges = milestoneBadges
                        )
                    }

                    NavigationTab.COACH -> {
                        CoachScreen(
                            messages = chatMessages,
                            selectedPersona = selectedPersona,
                            onSelectPersona = { viewModel.selectCoachPersona(it) },
                            isStreaming = isAiStreaming,
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            onClearHistory = { viewModel.clearChatHistory() }
                        )
                    }

                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            currentTheme = themeMode,
                            onSelectTheme = { viewModel.setThemeMode(it) },
                            smartRemindersEnabled = smartRemindersEnabled,
                            onToggleSmartReminders = { viewModel.toggleSmartRemindersMaster(it) },
                            onTestNotification = { title, msg -> viewModel.triggerTestNotification(title, msg) }
                        )
                    }
                }
            }

            // Modal Bottom Sheet for Add/Edit Habit
            if (isAddEditSheetOpen) {
                AddEditHabitDialog(
                    initialHabit = editingHabit,
                    onDismiss = { viewModel.closeAddEditSheet() },
                    onSave = { viewModel.saveHabit(it) },
                    onDelete = { viewModel.deleteHabit(it) }
                )
            }
        }
    }
}
