package com.lawsoc.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.lawsoc.app.databinding.ActivityChatBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ChatActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatRepository = ChatRepository()
    private lateinit var dbHelper: ChatDatabaseHelper
    private lateinit var toggle: ActionBarDrawerToggle
    private var currentSessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = ChatDatabaseHelper(this)
        setupToolbar()
        setupDrawer()
        setupRecyclerView()
        setupMessageInput()
        setupNewChatButton()

        // Create new session if none exists
        if (currentSessionId == -1L) {
            currentSessionId = dbHelper.createChatSession()
            loadChatHistory(currentSessionId)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    navigateToHome()
                }
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.chatbot)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(this)
        updateNavigationDrawer()
    }

    private fun setupNewChatButton() {
        val headerView = binding.navigationView.getHeaderView(0)
        headerView.findViewById<MaterialButton>(R.id.newChatButton).setOnClickListener {
            createNewSession()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = false
                reverseLayout = false
            }
            adapter = chatAdapter
        }
    }

    private fun setupMessageInput() {
        binding.messageInput.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    binding.sendButton.performClick()
                    true
                } else false
            }
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageInput.text?.clear()
            }
        }
    }

    private fun createNewSession() {
        currentSessionId = dbHelper.createChatSession()
        chatAdapter.clearMessages()
        displayWelcomeMessage()
        updateNavigationDrawer()
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun updateNavigationDrawer() {
        val menu = binding.navigationView.menu
        menu.clear()

        // Add static menu items
        menu.add(0, R.id.nav_clear_history, 0, getString(R.string.clear_history))
            .setIcon(R.drawable.ic_delete)

        // Add chat sessions
        val sessions = dbHelper.getChatSessions()
        val chatSessionsGroup = menu.addSubMenu("Chat History")
        sessions.forEach { session ->
            // Find first user message for the title
            val firstUserMessage = dbHelper.getMessages(session.id)
                .firstOrNull { it.isUser }?.content ?: "New Chat"

            val truncatedTitle = if (firstUserMessage.length > 30)
                "${firstUserMessage.take(27)}..."
            else
                firstUserMessage

            chatSessionsGroup.add(1, session.id.toInt(), 0, truncatedTitle)
                .setIcon(R.drawable.ic_chat)
        }
    }

    private fun loadChatHistory(sessionId: Long) {
        currentSessionId = sessionId
        val messages = dbHelper.getMessages(sessionId)
        if (messages.isEmpty()) {
            displayWelcomeMessage()
        } else {
            chatAdapter.setMessages(messages)
        }
    }

    private fun displayWelcomeMessage() {
        val welcomeMessage = """
            Hello, I am JusAsk, the Law Society of Singapore's virtual AI assistant. I can assist you with:
            
            • Membership requirements and benefits
            • Ethics, Compliance and Regulatory matters
            
            If you have any questions, feel free to ask.
            
            Please note, JusAsk is not a lawyer. I can provide information, but not legal advice.
        """.trimIndent()

        val message = ChatMessage(welcomeMessage, false)
        chatAdapter.addMessage(message)
        dbHelper.addMessage(currentSessionId, message)
    }

    private fun sendMessage(message: String) {
        val userMessage = ChatMessage(message, true)
        chatAdapter.addMessage(userMessage)
        dbHelper.addMessage(currentSessionId, userMessage)

        // Update navigation drawer if this is the first user message
        if (dbHelper.getMessages(currentSessionId).count { it.isUser } == 1) {
            updateNavigationDrawer()
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = chatRepository.sendMessage(message)
                val botMessage = ChatMessage(response.response, false)
                chatAdapter.addMessage(botMessage)
                dbHelper.addMessage(currentSessionId, botMessage)
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> {
                        // Log the detailed error response
                        val errorBody = e.response()?.errorBody()?.string()
                        Log.e("ChatActivity", "HTTP Error: ${e.code()}, Body: $errorBody", e)
                        "Server error (${e.code()}). Please try again."
                    }
                    is IOException -> {
                        Log.e("ChatActivity", "Network Error", e)
                        "Network error. Please check your internet connection."
                    }
                    else -> {
                        Log.e("ChatActivity", "Unknown Error", e)
                        "Error: ${e.message}"
                    }
                }
                Toast.makeText(this@ChatActivity, errorMessage, Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_clear_history -> {
                AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear all chat history?")
                    .setPositiveButton("Yes") { _, _ ->
                        dbHelper.clearAllHistory()
                        currentSessionId = dbHelper.createChatSession()
                        chatAdapter.clearMessages()
                        displayWelcomeMessage()
                        updateNavigationDrawer()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
            else -> {
                // Load selected chat session
                loadChatHistory(item.itemId.toLong())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToHome()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}