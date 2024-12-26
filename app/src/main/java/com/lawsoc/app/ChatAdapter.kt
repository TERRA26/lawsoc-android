package com.lawsoc.app

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<ChatMessage>()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private lateinit var markwon: Markwon
    private val disclaimerText = "Please note: JusAsk is an AI assistant and cannot provide legal advice. I can provide general information based on my knowledge base, but for specific legal advice, please consult a qualified legal professional."

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val timeText: TextView = view.findViewById(R.id.timeText)
        val botAvatar: ShapeableImageView? = view.findViewById(R.id.botAvatar)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        markwon = Markwon.builder(recyclerView.context)
            .usePlugin(LinkifyPlugin.create())
            .build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_chat_message_user
        } else {
            R.layout.item_chat_message_bot
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.messageText.apply {
            movementMethod = LinkMovementMethod.getInstance()

            if (message.isUser) {
                text = message.content
            } else {
                // Remove duplicate disclaimer if it exists
                var content = message.content
                if (content.contains(disclaimerText)) {
                    content = content.replace(disclaimerText, "").trim()
                }

                // Keep markdown links as is
                markwon.setMarkdown(this, content)
            }
        }

        holder.timeText.text = dateFormat.format(Date(message.timestamp))

        // Animate bot avatar if present
        if (!message.isUser) {
            holder.botAvatar?.let { avatar ->
                val pulseAnimation = AnimationUtils.loadAnimation(avatar.context, R.anim.pulse)
                avatar.startAnimation(pulseAnimation)
            }
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }
}