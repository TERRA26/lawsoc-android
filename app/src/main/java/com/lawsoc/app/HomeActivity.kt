package com.lawsoc.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.lawsoc.app.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupClickListeners() {
        binding.homepageCard.setOnClickListener {
            openWebsite()
        }

        binding.chatbotCard.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        binding.crmCard.setOnClickListener {
            // Coming soon
            android.widget.Toast.makeText(this, "Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.lawsociety.org.sg/"))
        startActivity(intent)
    }

    private fun logout() {
        // Clear session data
        getSharedPreferences("auth_prefs", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Return to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}