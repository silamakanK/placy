package fr.placy

import kotlinx.serialization.Serializable

@Serializable
enum class ChatRoomType {
    visitors,
    interested
    // adapte selon les valeurs de ton enum SQL
}

