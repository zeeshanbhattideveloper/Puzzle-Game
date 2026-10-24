package com.zeeshanbhatti.puzzlegame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This line makes the splash screen appear instantly
        setContentView(R.layout.activity_splash_screen)

        // Show the splash screen for 2 seconds (2000 milliseconds)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)  // Changed delay from 3000 to 2000
    }
}