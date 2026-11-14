package com.example.data.repository

import com.example.data.model.ChatMessage
import com.example.data.model.CoachPersona
import com.example.data.model.MessageRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AiRepository {
    fun isLiveFirebaseConnected(): Boolean
    suspend fun sendMessage(
        userMessage: String,
        persona: CoachPersona,
        history: List<ChatMessage>
    ): Flow<String>
}

class LocalFallbackAiCoachRepository : AiRepository {

    override fun isLiveFirebaseConnected(): Boolean = false

    override suspend fun sendMessage(
        userMessage: String,
        persona: CoachPersona,
        history: List<ChatMessage>
    ): Flow<String> = flow {
        // Simulate streaming tokens to deliver realistic multi-turn conversation feeling
        val promptLower = userMessage.lowercase().trim()

        val fullResponse = when (persona) {
            CoachPersona.QUICK_TACTICS -> generateFastTacticResponse(promptLower)
            CoachPersona.DEEP_STRATEGIST -> generateDeepStrategistResponse(promptLower)
            CoachPersona.HABIT_COACH -> generateHabitCoachResponse(promptLower)
        }

        // Stream in chunks
        val words = fullResponse.split(" ")
        val buffer = StringBuilder()
        for (i in words.indices) {
            buffer.append(words[i]).append(" ")
            if (i % 3 == 0 || i == words.size - 1) {
                delay(35)
                emit(buffer.toString())
            }
        }
    }

    private fun generateHabitCoachResponse(prompt: String): String {
        return when {
            prompt.contains("morning") || prompt.contains("wake") ->
                "### 🌅 Morning Routine Calibration\n\n" +
                "To cement a high-yield morning routine:\n\n" +
                "1. **Anchor with Water & Sunlight:** Place your 500ml water bottle next to your phone before bed. Drink it before touching screens, and get 5-10 minutes of direct light.\n" +
                "2. **The 2-Minute Activation Rule:** Scale down the first step of your morning workout. Put on running shoes the second you leave bed—momentum will take care of the rest.\n" +
                "3. **Habit Stack:** Pair a new habit immediately following an existing automatic behavior: *'After I pour my morning coffee, I will sit down for 10 minutes of deep reading.'*"

            prompt.contains("streak") || prompt.contains("miss") || prompt.contains("fail") || prompt.contains("broken") ->
                "### 🔄 The Never-Miss-Twice Rule\n\n" +
                "A broken streak is data, not defeat. Elite performers differentiate themselves not by never falling off, but by their speed of recovery.\n\n" +
                "- **Forgive Fast:** Missing 1 day has a statistical 0% impact on long-term habit consolidation—*unless* it cascades into day 2.\n" +
                "- **Emergency Minimum:** If you cannot do a 45-minute workout today, do a 3-minute stretch or 10 pushups to preserve your identity as an active person.\n" +
                "- **Audit the Friction:** What roadblock tripped you up? Remove that specific obstacle tonight."

            prompt.contains("procrastinat") || prompt.contains("lazy") || prompt.contains("tired") || prompt.contains("start") ->
                "### ⚡ Breaking Inertia: Behavioral Activation\n\n" +
                "Action precedes motivation—not the other way around.\n\n" +
                "1. **Lower Activation Energy:** Reduce the hurdle to start until it is laughably easy. If writing feels daunting, open the doc and write one sentence.\n" +
                "2. **5-Second Countdown:** Count 5-4-3-2-1 and physically stand up before your brain rationalizes delay.\n" +
                "3. **Focus Sprint:** Set a timer for 15 minutes of uninterrupted focus. After 15 minutes, give yourself full permission to stop. 85% of the time, you'll choose to keep going."

            prompt.contains("evening") || prompt.contains("night") || prompt.contains("sleep") ->
                "### 🌙 Evening Wind-Down Blueprint\n\n" +
                "Quality mornings are forged the night before:\n\n" +
                "1. **Digital Sunset (T-60 min):** Dim screens and switch to warm lighting 1 hour prior to sleep.\n" +
                "2. **Brain Dump:** Spend 3 minutes jotting down your top 3 needle-moving priorities for tomorrow. Eliminating morning decision fatigue preserves cognitive willpower.\n" +
                "3. **Environment Staging:** Lay out your gym gear or book on the nightstand."

            else ->
                "### 🎯 Strategic Habit Focus\n\n" +
                "Every habit you track in Rulz Habit is a vote for the person you wish to become.\n\n" +
                "- **Clarity Over Willpower:** Have a specific time, cue, and location for this routine.\n" +
                "- **Friction Reduction:** Make good habits obvious and easy; make distractions invisible and friction-heavy.\n" +
                "- **Track & Celebrate:** Ticking that circle in the app triggers a dopamine micro-reward that locks in the neural pathway. What specific routine would you like to optimize next?"
        }
    }

    private fun generateFastTacticResponse(prompt: String): String {
        return "⚡ **Quick Tactic (2-Min Action):**\n\n" +
            "• **Immediate Cue:** Do the first 120 seconds of your scheduled habit right now.\n" +
            "• **Friction Slash:** Eliminate one distraction from your immediate field of view.\n" +
            "• **Anchor:** Say out loud: *'I am doing this because I value consistency.'*\n\n" +
            "Execute now, reflect later."
    }

    private fun generateDeepStrategistResponse(prompt: String): String {
        return "### 🏛️ Deep Routine Architecture (System Analysis)\n\n" +
            "#### Phase 1: Neurological Loop Diagnostic\n" +
            "Every routine operates on Cue ➔ Craving ➔ Response ➔ Reward. When routines fail, the breakdown almost always occurs in the cue salience or the delay in reward delivery.\n\n" +
            "#### Phase 2: Systematic Stacking Matrix\n" +
            "1. **Tier-1 Keystone:** Morning Hydration & Light Exposure (regulates cortisol and circadian timing).\n" +
            "2. **Tier-2 Deep Focus Block:** Schedule cognitive output during your peak ultradian cycle (typically 90-120 minutes post-waking).\n" +
            "3. **Tier-3 Kinetic Release:** Physical movement resets adenosine accumulation and sharpens evening neuroplasticity.\n\n" +
            "#### Phase 3: Contingency Protocol\n" +
            "Pre-commit to an 'If-Then' contract: *'If unexpected friction interrupts my standard routine, then I execute my 10-minute condensed micro-routine without compromise.'*"
    }
}
