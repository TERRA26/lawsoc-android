package com.lawsoc.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ChatDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "chat_history.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_CHATS = "chats"
        private const val TABLE_CHAT_SESSIONS = "chat_sessions"

        private const val COLUMN_ID = "id"
        private const val COLUMN_MESSAGE = "message"
        private const val COLUMN_IS_USER = "is_user"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_SESSION_ID = "session_id"
        private const val COLUMN_SESSION_NAME = "session_name"
        private const val COLUMN_LAST_MESSAGE = "last_message"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSessionsTable = """
            CREATE TABLE $TABLE_CHAT_SESSIONS (
                $COLUMN_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SESSION_NAME TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER,
                $COLUMN_LAST_MESSAGE TEXT
            )
        """.trimIndent()

        val createChatsTable = """
            CREATE TABLE $TABLE_CHATS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SESSION_ID INTEGER,
                $COLUMN_MESSAGE TEXT,
                $COLUMN_IS_USER INTEGER,
                $COLUMN_TIMESTAMP INTEGER,
                FOREIGN KEY($COLUMN_SESSION_ID) REFERENCES $TABLE_CHAT_SESSIONS($COLUMN_SESSION_ID)
            )
        """.trimIndent()

        db.execSQL(createSessionsTable)
        db.execSQL(createChatsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHATS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT_SESSIONS")
        onCreate(db)
    }

    fun createChatSession(sessionName: String = "New Chat"): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SESSION_NAME, sessionName)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        return db.insert(TABLE_CHAT_SESSIONS, null, values)
    }

    fun getChatSessions(): List<ChatSession> {
        val sessions = mutableListOf<ChatSession>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CHAT_SESSIONS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                sessions.add(
                    ChatSession(
                        id = getLong(getColumnIndexOrThrow(COLUMN_SESSION_ID)),
                        name = getString(getColumnIndexOrThrow(COLUMN_SESSION_NAME)),
                        timestamp = getLong(getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        lastMessage = getString(getColumnIndexOrThrow(COLUMN_LAST_MESSAGE))
                    )
                )
            }
        }
        cursor.close()
        return sessions
    }

    fun addMessage(sessionId: Long, message: ChatMessage) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SESSION_ID, sessionId)
            put(COLUMN_MESSAGE, message.content)
            put(COLUMN_IS_USER, if (message.isUser) 1 else 0)
            put(COLUMN_TIMESTAMP, message.timestamp)
        }
        db.insert(TABLE_CHATS, null, values)

        // Update session's last message
        val sessionValues = ContentValues().apply {
            put(COLUMN_LAST_MESSAGE, message.content)
            put(COLUMN_TIMESTAMP, message.timestamp)
        }
        db.update(TABLE_CHAT_SESSIONS, sessionValues, "$COLUMN_SESSION_ID = ?", arrayOf(sessionId.toString()))
    }

    fun getMessages(sessionId: Long): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CHATS,
            null,
            "$COLUMN_SESSION_ID = ?",
            arrayOf(sessionId.toString()),
            null,
            null,
            "$COLUMN_TIMESTAMP ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                messages.add(
                    ChatMessage(
                        content = getString(getColumnIndexOrThrow(COLUMN_MESSAGE)),
                        isUser = getInt(getColumnIndexOrThrow(COLUMN_IS_USER)) == 1,
                        timestamp = getLong(getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                )
            }
        }
        cursor.close()
        return messages
    }

    fun deleteChatSession(sessionId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_CHATS, "$COLUMN_SESSION_ID = ?", arrayOf(sessionId.toString()))
        db.delete(TABLE_CHAT_SESSIONS, "$COLUMN_SESSION_ID = ?", arrayOf(sessionId.toString()))
    }

    fun clearAllHistory() {
        val db = this.writableDatabase
        db.delete(TABLE_CHATS, null, null)
        db.delete(TABLE_CHAT_SESSIONS, null, null)
    }

    fun renameChatSession(sessionId: Long, newName: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SESSION_NAME, newName)
        }
        db.update(TABLE_CHAT_SESSIONS, values, "$COLUMN_SESSION_ID = ?", arrayOf(sessionId.toString()))
    }
}