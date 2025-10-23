package fr.placy.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val bio: String? = null,
    val status_message: String? = null,
    val status_expires_at: String? = null,
    val location_visibility: String? = null,
    val last_seen_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)