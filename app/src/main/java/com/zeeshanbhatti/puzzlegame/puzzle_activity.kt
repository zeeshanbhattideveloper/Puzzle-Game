package com.zeeshanbhatti.puzzlegame

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlin.math.min
import kotlin.random.Random
import android.animation.ValueAnimator




class PuzzleActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var categoryIndex = 1
    private var imageNumber = 1
    private lateinit var prefix: String

    private val gridSize = 4
    private val totalImages = 20

    private lateinit var gridPuzzle: GridLayout
    private lateinit var imgPreview: ImageView
    private lateinit var txtTaps: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtCoins: TextView

    private lateinit var tileRotations: Array<Array<Int>>
    private lateinit var puzzleTiles: Array<Array<ImageView?>>

    private var rotationCount = 0
    private var dialogShown = false

    private var seconds = 50
    private var timerRunning = false
    private var timerStarted = false
    private val handler = Handler(Looper.getMainLooper())

    private var rewardedAd: RewardedAd? = null
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        MobileAds.initialize(this)

        adView = findViewById(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())

        prefs = getSharedPreferences("game_progress", MODE_PRIVATE)

        categoryIndex = intent.getIntExtra("categoryIndex", 1)
        imageNumber = intent.getIntExtra("imageNumber", 1)
        prefix = intent.getStringExtra("prefix") ?: "img"

        gridPuzzle = findViewById(R.id.gridPuzzle)
        imgPreview = findViewById(R.id.imgPreview)
        txtTaps = findViewById(R.id.txtTaps)
        txtTimer = findViewById(R.id.txtTimer)
        txtCoins = findViewById(R.id.txtCoins)

        updateCoinsUI()
        loadRewardedAd()
        loadPuzzle()
    }

    private fun loadPuzzle() {
        dialogShown = false
        rotationCount = 0
        txtTaps.text = "0"

        seconds = 50
        timerRunning = false
        timerStarted = false
        txtTimer.text = formatTime(seconds)

        val resId = resources.getIdentifier("${prefix}$imageNumber", "drawable", packageName)
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        imgPreview.setImageBitmap(bitmap)

        gridPuzzle.post {
            createGrid(bitmap)
            shufflePuzzle()
        }
    }

    private fun createGrid(originalBitmap: Bitmap) {
        gridPuzzle.removeAllViews()
        gridPuzzle.columnCount = gridSize
        gridPuzzle.rowCount = gridSize

        val bitmap = cropToSquare(originalBitmap)

        puzzleTiles = Array(gridSize) { arrayOfNulls(gridSize) }
        tileRotations = Array(gridSize) { Array(gridSize) { 0 } }

        val gridSizePx = min(gridPuzzle.width, gridPuzzle.height)
        val cellSize = gridSizePx / gridSize
        val tileSize = bitmap.width / gridSize

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {

                val tileBitmap = Bitmap.createBitmap(
                    bitmap,
                    col * tileSize,
                    row * tileSize,
                    tileSize,
                    tileSize
                )

                val tile = ImageView(this).apply {
                    setImageBitmap(tileBitmap)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSize
                        height = cellSize
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                        setMargins(1, 1, 1, 1)
                    }

                    setOnClickListener {
                        if (!timerStarted) {
                            timerStarted = true
                            startTimer()
                        }
                        rotateTile(row, col)
                    }
                }

                gridPuzzle.addView(tile)
                puzzleTiles[row][col] = tile
            }
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun shufflePuzzle() {
        val random = Random(System.currentTimeMillis())
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val rotation = random.nextInt(4) * 90
                tileRotations[i][j] = rotation
                puzzleTiles[i][j]?.rotation = rotation.toFloat()
            }
        }
    }

    private fun rotateTile(x: Int, y: Int) {
        tileRotations[x][y] = (tileRotations[x][y] + 90) % 360
        puzzleTiles[x][y]?.rotation = tileRotations[x][y].toFloat()

        rotationCount++
        txtTaps.text = rotationCount.toString()

        if (!dialogShown && isSolved()) {
            dialogShown = true
            stopTimer()

            unlockNextImage()

            val oldCoins = CoinsManager.getCoins(this)

            CoinsManager.addCoins(this, 10)

            val newCoins = CoinsManager.getCoins(this)

            animateCoins(oldCoins, newCoins)

            if (imageNumber == totalImages) unlockNextCategory()
            showResultDialog(true)
        }
    }

    private fun isSolved(): Boolean {
        for (i in 0 until gridSize)
            for (j in 0 until gridSize)
                if (tileRotations[i][j] != 0) return false
        return true
    }

    private fun showResultDialog(solved: Boolean) {

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_result, null)

        val txtTitle = view.findViewById<TextView>(R.id.txtResultTitle)
        val txtMsg = view.findViewById<TextView>(R.id.txtResultMessage)
        val txtReward = view.findViewById<TextView>(R.id.txtRewardMessage)
        val btnReplay = view.findViewById<Button>(R.id.btnReplay)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val btnWatchAd = view.findViewById<Button?>(R.id.btnWatchAd)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        if (solved) {

            txtTitle.text = "You Win!"
            txtMsg.text = "Time left: ${formatTime(seconds)}\nRotations: $rotationCount"
            txtReward.text = "You got 10 🪙"
            txtReward.visibility = View.VISIBLE

            btnNext.visibility = View.VISIBLE
            btnWatchAd?.visibility = View.GONE

        } else {

            txtTitle.text = "Time Up!"
            txtMsg.text = "You have failed to solve the Puzzle!"
            txtReward.visibility = View.GONE
            btnNext.visibility = View.GONE

            btnWatchAd?.visibility = View.VISIBLE
            btnWatchAd?.setOnClickListener {

                btnWatchAd.isEnabled = false

                if (rewardedAd == null) {
                    Toast.makeText(this, "Ad not ready", Toast.LENGTH_SHORT).show()
                    btnWatchAd.isEnabled = true
                    loadRewardedAd()
                    return@setOnClickListener
                }

                rewardedAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {

                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            loadRewardedAd()

                            dialog.dismiss()

                            seconds += 10
                            txtTimer.text = formatTime(seconds)

                            timerStarted = false
                            timerRunning = false
                            dialogShown = false   // ✅ MOST IMPORTANT FIX
                        }
                    }

                rewardedAd?.show(this) { }
            }
        }

        btnReplay.setOnClickListener {
            dialog.dismiss()
            loadPuzzle()
        }

        btnNext.setOnClickListener {
            dialog.dismiss()
            imageNumber++
            loadPuzzle()
        }

        dialog.show()
    }

    private fun unlockNextImage() {
        val unlocked = prefs.getInt("unlockedImage_$categoryIndex", 1)
        if (imageNumber >= unlocked) {
            prefs.edit().putInt("unlockedImage_$categoryIndex", imageNumber + 1).apply()
        }
    }

    private fun unlockNextCategory() {
        val unlocked = prefs.getInt("unlockedCategory", 1)
        if (categoryIndex >= unlocked) {
            prefs.edit().putInt("unlockedCategory", categoryIndex + 1).apply()
        }
    }

    private fun startTimer() {
        timerRunning = true
        handler.post(object : Runnable {
            override fun run() {
                if (timerRunning) {
                    seconds--
                    txtTimer.text = formatTime(seconds)

                    if (seconds <= 0 && !dialogShown) {
                        dialogShown = true
                        stopTimer()
                        showResultDialog(false)
                        return
                    }
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun stopTimer() {
        timerRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onResume() {
        super.onResume()
        updateCoinsUI()
    }

    private fun animateCoins(oldCoins: Int, newCoins: Int) {

        val animator = ValueAnimator.ofInt(oldCoins, newCoins)

        animator.duration = 1500

        animator.addUpdateListener {
            txtCoins.text = "🪙 ${it.animatedValue}"
        }

        animator.start()

        txtCoins.animate()
            .scaleX(1.4f)
            .scaleY(1.4f)
            .setDuration(250)
            .withEndAction {
                txtCoins.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(250)
                    .start()
            }
            .start()
    }

    private fun updateCoinsUI() {
        txtCoins.text = "🪙 ${CoinsManager.getCoins(this)}"
    }

    private fun loadRewardedAd() {
        RewardedAd.load(
            this,
            "ca-app-pub-7496903880919477/8437474357", //Rewarded Ad
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

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}
