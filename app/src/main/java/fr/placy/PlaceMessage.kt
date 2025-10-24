package fr.placy

import kotlinx.serialization.SerialName
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.ExperimentalTime

@Serializable
data class PlaceMessage(
    @SerialName("id")
    val id: Long,

    @Serializable(with = UUIDSerializer::class)
    @SerialName("place_id")
    val placeId: UUID,

    @SerialName("chat_type")
    val chatType: ChatRoomType,

    @Serializable(with = UUIDSerializer::class)
    @SerialName("sender_id")
    val senderId: UUID,

    @SerialName("body")
    val body: String,

    @SerialName("inserted_at")
    @OptIn(ExperimentalTime::class)
    @Serializable(with = InstantSerializer::class)
    val insertedAt: Instant,
)

