package fr.placy

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) = old.id == new.id
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) = old == new
        }
    }

    inner class VH(private val v: View) : RecyclerView.ViewHolder(v) {
        private val container = v.findViewById<LinearLayout>(R.id.messageContainer)
        private val tv = v.findViewById<TextView>(R.id.tvMessage)

        fun bind(item: ChatMessage) {
            tv.text = item.text

            val context = v.context
            val largeMargin = context.resources.getDimensionPixelSize(R.dimen.chat_message_horizontal_margin_large)
            val smallMargin = context.resources.getDimensionPixelSize(R.dimen.chat_message_horizontal_margin_small)
            val verticalMargin = context.resources.getDimensionPixelSize(R.dimen.chat_message_vertical_margin)

            (v.layoutParams as RecyclerView.LayoutParams).apply {
                val startMargin = if (item.isMine) smallMargin else largeMargin
                val endMargin = if (item.isMine) largeMargin else smallMargin
                setMargins(startMargin, verticalMargin, endMargin, verticalMargin)
                v.layoutParams = this
            }

            container.gravity = if (item.isMine) Gravity.END else Gravity.START

            val bubbleBackground = if (item.isMine) R.drawable.bg_message_self else R.drawable.bg_message_other
            val textColor = if (item.isMine) R.color.chat_bubble_text_on_self else R.color.chat_bubble_text_primary

            tv.background = ContextCompat.getDrawable(context, bubbleBackground)
            tv.setTextColor(ContextCompat.getColor(context, textColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
