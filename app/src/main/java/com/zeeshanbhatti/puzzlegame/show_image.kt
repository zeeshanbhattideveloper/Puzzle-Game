package com.zeeshanbhatti.puzzlegame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShowImageActivity : AppCompatActivity() {

    // ---------------- Preferences ----------------
    private lateinit var prefs: SharedPreferences

    // ---------------- State ----------------
    private var categoryIndex = 1
    private lateinit var prefix: String
    private var totalImages = 20
    private var unlockedImages = 1

    // ---------------- UI ----------------
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private lateinit var txtCoins: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)

        prefs = getSharedPreferences("game_progress", MODE_PRIVATE)
        txtCoins = findViewById(R.id.txtCoins)

        categoryIndex = intent.getIntExtra("categoryIndex", 1)
        prefix = intent.getStringExtra("prefix") ?: "img"

        totalImages = when (categoryIndex) {
            1, 2, 3 -> 20
            else -> 8
        }

        updateCoinsUI()

        recyclerView = findViewById(R.id.recyclerImages)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        loadImages()
    }

    // =================================================
    // Load Images
    // =================================================
    private fun loadImages() {
        unlockedImages = prefs.getInt("unlockedImage_$categoryIndex", 1)

        val images = mutableListOf<Int>()
        for (i in 1..totalImages) {
            val resId = resources.getIdentifier(
                "${prefix}$i",
                "drawable",
                packageName
            )
            if (resId != 0) {
                images.add(resId)
            }
        }

        adapter = ImageAdapter(images, unlockedImages) { position, _ ->
            if (position < unlockedImages) {
                startActivity(
                    Intent(this, PuzzleActivity::class.java).apply {
                        putExtra("categoryIndex", categoryIndex)
                        putExtra("imageNumber", position + 1)
                        putExtra("prefix", prefix)
                    }
                )
            } else {
                Toast.makeText(
                    this,
                    "Complete previous puzzle 🔒",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        unlockedImages = prefs.getInt("unlockedImage_$categoryIndex", 1)
        adapter.updateUnlockedCount(unlockedImages)
        updateCoinsUI()
    }

    // =================================================
    // Coins
    // =================================================
    private fun updateCoinsUI() {
        txtCoins.text = "🪙 ${CoinsManager.getCoins(this)}"
    }
}
