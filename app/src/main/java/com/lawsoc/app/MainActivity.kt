package com.lawsoc.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            checkSession()
        }, 2000)
    }

    private fun checkSession() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        val intent = if (authToken != null) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}