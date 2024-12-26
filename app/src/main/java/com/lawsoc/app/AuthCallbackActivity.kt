package com.lawsoc.app

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AuthCallbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri != null) {
            handleAuthResponse(uri)
        }
    }

    private fun handleAuthResponse(uri: Uri) {
        // Handle the auth response
        finish()
    }
}