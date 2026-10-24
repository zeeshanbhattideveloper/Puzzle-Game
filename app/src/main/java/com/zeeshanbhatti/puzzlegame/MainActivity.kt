package com.zeeshanbhatti.puzzlegame

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd

class MainActivity : AppCompatActivity() {

    private lateinit var txtCoins: TextView
    private lateinit var adView: AdView
    private var rewardedAd: RewardedAd? = null

    private var isAnimatingCoins = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ---------------- AdMob Init ----------------
        MobileAds.initialize(this)

        adView = findViewById(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())
        loadRewardedAd()

        // ---------------- UI ----------------
        txtCoins = findViewById(R.id.txtCoins)
        val btnStart = findViewById<Button>(R.id.newgame)
        val btnDailyRewards = findViewById<Button>(R.id.btnDailyRewards)

        btnStart.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        btnDailyRewards.setOnClickListener {
            showDailyRewardsDialog()
        }

        // ---------------- Daily Reward Logic ----------------
        // Reset streak if user missed a day
        DailyRewardManager.resetIfMissed(this)

        updateCoins()

        // Auto show dialog if today reward not claimed
        if (!DailyRewardManager.hasClaimedToday(this)) {
            showDailyRewardsDialog()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isAnimatingCoins) {
            updateCoins()
        }
    }

    private fun updateCoins() {
        txtCoins.text = "🪙 ${CoinsManager.getCoins(this)}"
    }

    private fun animateCoins(oldCoins: Int, newCoins: Int) {

        val animator = ValueAnimator.ofInt(oldCoins, newCoins)

        animator.duration = 2000 // 2 seconds

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            txtCoins.text = "🪙 $value"
        }

        animator.start()

        txtCoins.animate()
            .cancel()

        txtCoins.scaleX = 1f
        txtCoins.scaleY = 1f

        txtCoins.animate()
            .scaleX(1.6f)
            .scaleY(1.6f)
            .setDuration(400)
            .withEndAction {
                txtCoins.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .start()
            }
            .start()
    }

    // =====================================================
    // Rewarded Ad
    // =====================================================
    private fun loadRewardedAd() {
        RewardedAd.load(
            this,
            "ca-app-pub-7496903880919477/8437474357",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }



    // =====================================================
    // Daily Rewards Dialog
    // =====================================================
    @SuppressLint("InflateParams")
    private fun showDailyRewardsDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_daily_rewards, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Animation
        dialogView.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .start()
        }

        val dayViews = listOf(
            dialogView.findViewById<TextView>(R.id.day1),
            dialogView.findViewById<TextView>(R.id.day2),
            dialogView.findViewById<TextView>(R.id.day3),
            dialogView.findViewById<TextView>(R.id.day4),
            dialogView.findViewById<TextView>(R.id.day5),
            dialogView.findViewById<TextView>(R.id.day6),
            dialogView.findViewById<TextView>(R.id.day7)
        )

        val btnCollect = dialogView.findViewById<Button>(R.id.btnCollect)
        val btnWatchAd = dialogView.findViewById<Button>(R.id.btnWatchAd)

        val currentDay = DailyRewardManager.getDay(this)

        // Mark completed days
        for (i in 0 until currentDay - 1) {
            dayViews[i].text = "${dayViews[i].text} ✅"
        }

        // ---------------- Collect Button ----------------
        if (DailyRewardManager.hasClaimedToday(this)) {
            btnCollect.isEnabled = false
            btnCollect.text = "Already Collected"
        } else {
            btnCollect.isEnabled = true
            btnCollect.text = "Collect Reward"
        }

        btnCollect.setOnClickListener {

            if (!DailyRewardManager.hasClaimedToday(this)) {

                val reward = DailyRewardManager.getRewardForDay(currentDay)

                val oldCoins = CoinsManager.getCoins(this)

                CoinsManager.addCoins(this, reward)

                val newCoins = CoinsManager.getCoins(this)

                DailyRewardManager.claimReward(this)

                animateCoins(oldCoins, newCoins)
            }

            dialog.dismiss()
        }

        // ---------------- Watch Ad Button ----------------
        btnWatchAd.visibility = Button.VISIBLE
        btnWatchAd.setOnClickListener {

            if (rewardedAd == null) {
                Toast.makeText(this, "Ad not ready", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadRewardedAd()
                return@setOnClickListener
            }

            rewardedAd?.show(this) {

                val oldCoins = CoinsManager.getCoins(this)

                CoinsManager.addCoins(this, 10)

                val newCoins = CoinsManager.getCoins(this)

                dialog.dismiss()

                txtCoins.postDelayed({
                    animateCoins(oldCoins, newCoins)
                }, 500)

                loadRewardedAd()
            }
        }

    }
}
