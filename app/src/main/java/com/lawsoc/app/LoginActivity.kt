package com.lawsoc.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.lawsoc.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userRepository = UserRepository()
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInputValidation()

        binding.loginButton.setOnClickListener {
            performLogin()
        }

        binding.passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                performLogin()
                true
            } else false
        }
    }

    private fun setupInputValidation() {
        binding.usernameInput.doAfterTextChanged {
            binding.usernameLayout.error = null
            binding.errorText.visibility = View.GONE
        }

        binding.passwordInput.doAfterTextChanged {
            binding.passwordLayout.error = null
            binding.errorText.visibility = View.GONE
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val email = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        if (email.isEmpty()) {
            binding.usernameLayout.error = getString(R.string.email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.usernameLayout.error = getString(R.string.invalid_email)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.password_required)
            isValid = false
        }

        return isValid
    }

    private fun performLogin() {
        if (!validateInput()) {
            return
        }

        val email = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        setLoadingState(true)

        // Check if test user with test password
        if (TestUsers.isValidTestCredentials(email, password)) {
            handleSuccessfulTestLogin(email)
            return
        }

        // If test user, but not using test password, try normal login
        if (TestUsers.isAllowedUser(email)) {
            performNormalLogin(email, password)
            return
        }

        // Not a test user
        showError(getString(R.string.unauthorized_access))
        setLoadingState(false)
    }

    private fun performNormalLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Attempting normal login for email: $email")
                val result = userRepository.login(email, password)

                when (result) {
                    is NetworkResult.Success -> {
                        result.data.data?.let { authData ->
                            Log.d(TAG, "Login successful")
                            saveAuthData(authData)
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        } ?: run {
                            Log.e(TAG, "Login failed: No auth data")
                            showError(getString(R.string.invalid_credentials))
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Login failed: ${result.message}")
                        handleLoginError(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                showError(getString(R.string.network_error))
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun handleSuccessfulTestLogin(email: String) {
        // Create mock auth data for test phase
        val authData = AuthData(
            token = "test-token",
            user = UserData(
                id = 1,
                email = email,
                display_name = email.substringBefore("@"),
                roles = listOf("user")
            )
        )

        saveAuthData(authData)
        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        finish()
        setLoadingState(false)
    }

    private fun handleLoginError(errorMessage: String) {
        when {
            errorMessage.contains("invalid", ignoreCase = true) ||
                    errorMessage.contains("incorrect", ignoreCase = true) ||
                    errorMessage.contains("failed", ignoreCase = true) -> {
                showInvalidCredentialsError()
            }
            errorMessage.contains("network", ignoreCase = true) -> {
                showError(getString(R.string.network_error))
            }
            else -> {
                showError(getString(R.string.login_failed_try_again))
            }
        }
    }

    private fun showInvalidCredentialsError() {
        binding.usernameLayout.error = " "  // Space to show the error state
        binding.passwordLayout.error = getString(R.string.invalid_credentials)
        binding.errorText.text = getString(R.string.invalid_credentials)
        binding.errorText.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
        binding.usernameInput.isEnabled = !isLoading
        binding.passwordInput.isEnabled = !isLoading
    }

    private fun saveAuthData(authData: AuthData) {
        getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().apply {
            putString("auth_token", authData.token)
            putString("user_display_name", authData.user.display_name)
            putString("user_email", authData.user.email)
            apply()
        }
    }
}