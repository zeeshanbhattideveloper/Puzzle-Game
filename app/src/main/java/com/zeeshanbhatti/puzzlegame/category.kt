package com.zeeshanbhatti.puzzlegame

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {

    private lateinit var txtCoins: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        txtCoins = findViewById(R.id.txtCoins)
        initializeFirstLaunch()
    }

    override fun onResume() {
        super.onResume()
        updateCoinsUI()
        setupCategories()
    }

    private fun initializeFirstLaunch() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        if (prefs.getBoolean("first_launch", true)) {

            prefs.edit()
                .putBoolean("first_launch", false)
                .putBoolean("category_Cartoons_unlocked", true)
                .apply()

            getSharedPreferences("game_progress", MODE_PRIVATE)
                .edit()
                .putInt("unlockedImage_1", 1)
                .apply()
        }
    }

    private fun updateCoinsUI() {
        txtCoins.text = "🪙 ${CoinsManager.getCoins(this)}"
    }

    private fun setupCategories() {
        setupCategory(R.id.cardCartoons, R.id.lockCartoons, "Cartoons", "cart", 1, 0)
        setupCategory(R.id.cardAnimals, R.id.lockAnimals, "Animals", "a", 2, 400)
        setupCategory(R.id.cardBirds, R.id.lockBirds, "Birds", "b", 3, 400)
        setupCategory(R.id.cardFishes, R.id.lockFishes, "Fishes", "fi", 4, 500)
        setupCategory(R.id.cardFruits, R.id.lockFruits, "Fruits", "fr", 5, 600)
        setupCategory(R.id.cardCars, R.id.lockCars, "Cars", "car", 6, 700)
        setupCategory(R.id.cardBikes, R.id.lockBikes, "Bikes", "bik", 7, 800)
    }

    private fun setupCategory(
        cardId: Int,
        lockId: Int,
        categoryName: String,
        prefix: String,
        categoryIndex: Int,
        cost: Int
    ) {
        val card = findViewById<FrameLayout>(cardId)
        val lock = findViewById<ImageView>(lockId)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        val isUnlocked = prefs.getBoolean("category_${categoryName}_unlocked", false)

        if (isUnlocked) {
            lock.visibility = ImageView.GONE
            card.setOnClickListener {
                startActivity(
                    Intent(this, ShowImageActivity::class.java).apply {
                        putExtra("categoryIndex", categoryIndex)
                        putExtra("prefix", prefix)
                    }
                )
            }
        } else {
            lock.visibility = ImageView.VISIBLE
            card.setOnClickListener {
                showUnlockDialog(categoryName, categoryIndex, cost)
            }
        }
    }

    private fun showUnlockDialog(
        categoryName: String,
        categoryIndex: Int,
        requiredCoins: Int
    ) {
        AlertDialog.Builder(this)
            .setTitle("Unlock $categoryName")
            .setMessage("You need $requiredCoins coins to unlock this category.")
            .setPositiveButton("UNLOCK") { _, _ ->
                if (CoinsManager.spendCoins(this, requiredCoins)) {

                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("category_${categoryName}_unlocked", true)
                        .apply()

                    getSharedPreferences("game_progress", MODE_PRIVATE)
                        .edit()
                        .putInt("unlockedImage_$categoryIndex", 1)
                        .apply()

                    Toast.makeText(this, "$categoryName Unlocked 🎉", Toast.LENGTH_SHORT).show()
                    updateCoinsUI()
                    setupCategories()

                } else {
                    Toast.makeText(this, "Not enough coins 😢", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}
