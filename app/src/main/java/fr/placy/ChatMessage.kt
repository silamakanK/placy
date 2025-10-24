package fr.placy

data class ChatMessage(
    val id: Long,
    val text: String,
    val isMine: Boolean,
    val timestamp: Long
)

