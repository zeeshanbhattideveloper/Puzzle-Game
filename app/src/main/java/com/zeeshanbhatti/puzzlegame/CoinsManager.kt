package com.zeeshanbhatti.puzzlegame

import android.content.Context
import java.util.concurrent.TimeUnit

object CoinsManager {

    // ---------------- Preferences ----------------
    private const val PREFS_NAME = "coins_prefs"
    private const val KEY_COINS = "coins"
    private const val KEY_LAST_DAILY = "last_daily_reward_time"

    // ---------------- Get Coins ----------------
    fun getCoins(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COINS, 0)
    }

    // ---------------- Set Coins ----------------
    fun setCoins(context: Context, coins: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_COINS, coins)
            .apply()
    }

    // ---------------- Spend Coins ----------------
    fun spendCoins(context: Context, amount: Int): Boolean {
        val currentCoins = getCoins(context)
        return if (currentCoins >= amount) {
            setCoins(context, currentCoins - amount)
            true
        } else {
            false
        }
    }

    // ---------------- Add Coins ----------------
    fun addCoins(context: Context, amount: Int) {
        val currentCoins = getCoins(context)
        setCoins(context, currentCoins + amount)
    }

    // ---------------- Daily Reward Check ----------------
    fun canClaimDailyReward(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastClaimTime = prefs.getLong(KEY_LAST_DAILY, 0L)
        val now = System.currentTimeMillis()

        return now - lastClaimTime >= TimeUnit.HOURS.toMillis(24)
    }

    // ---------------- Claim Daily Reward ----------------
    fun claimDailyReward(context: Context, amount: Int) {
        addCoins(context, amount)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_LAST_DAILY, System.currentTimeMillis())
            .apply()
    }
}
