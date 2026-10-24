package com.zeeshanbhatti.puzzlegame

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object DailyRewardManager {

    private const val PREFS_NAME = "daily_rewards"
    private const val KEY_LAST_CLAIM = "last_claim"
    private const val KEY_CURRENT_DAY = "current_day"

    private val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    // =================================================
    // Reset ONLY if user missed more than 1 day
    // =================================================
    fun resetIfMissed(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val lastClaimDate = prefs.getString(KEY_LAST_CLAIM, null) ?: return
        val lastDate = sdf.parse(lastClaimDate) ?: return
        val todayDate = sdf.parse(sdf.format(Date())) ?: return

        val diffDays =
            abs((todayDate.time - lastDate.time) / (1000 * 60 * 60 * 24))

        if (diffDays > 1) {
            prefs.edit()
                .putInt(KEY_CURRENT_DAY, 1)
                .apply()
        }
    }

    // =================================================
    // Get current reward day
    // =================================================
    fun getDay(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT_DAY, 1)
    }

    // =================================================
    // Reward amount per day
    // =================================================
    fun getRewardForDay(day: Int): Int {
        val rewards = listOf(10, 20, 30, 40, 50, 70, 100)
        return rewards.getOrElse(day - 1) { 10 }
    }

    // =================================================
    // Claim reward
    // =================================================
    fun claimReward(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val currentDay = getDay(context)
        val nextDay = if (currentDay >= 7) 1 else currentDay + 1

        prefs.edit()
            .putInt(KEY_CURRENT_DAY, nextDay)
            .putString(KEY_LAST_CLAIM, sdf.format(Date()))
            .apply()
    }

    // =================================================
    // Check if today reward is claimed
    // =================================================
    fun hasClaimedToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_CLAIM, "") == sdf.format(Date())
    }
}
