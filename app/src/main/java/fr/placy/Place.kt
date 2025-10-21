package fr.placy

import java.util.UUID
import java.time.OffsetDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class Place(
    @Serializable(with = UUIDSerializer::class) val id: UUID = UUID.randomUUID(),

    val name: String,
    val description: String? = null,
    val category: String? = null,

    val tags: List<String> = emptyList(),

    val address: String? = null,
    val city: String? = null,
    val country: String? = null,

    // PostGIS geography → simple lat/lon model here
    @Serializable(with = GeoPointFlexibleSerializer::class) val location: GeoPoint? = null,

    @Serializable(with = UUIDSerializer::class) val createdBy: UUID? = null,
    @Serializable(with = OffsetDateTimeSerializer::class) val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Serializable(with = OffsetDateTimeSerializer::class) val updatedAt: OffsetDateTime = OffsetDateTime.now(),

    val isActive: Boolean = true,

    val osmElementType: String? = null,
    val osmElementId: Long? = null,

    // jsonb columns
    val osmTags: JsonObject = buildJsonObject { },
    val nameTranslations: JsonObject = buildJsonObject { },
    val addressComponents: JsonObject = buildJsonObject { },
    val contactInfo: JsonObject = buildJsonObject { },

    val openingHours: String? = null,
    val wheelchairAccess: String? = null,
    val wikidataId: String? = null,
    val wikipediaUrl: String? = null,
    val brand: String? = null,
    val operator: String? = null,

    val cuisine: List<String> = emptyList(),

    val amenity: String? = null,
    val tourism: String? = null,
    val leisure: String? = null,

    val sport: List<String> = emptyList(),

    val sourceRef: String? = null,
    val imageUrl: String? = null,

    val housenumber: String? = null,
    val street: String? = null,
    val postcode: String? = null,
    val state: String? = null,

    val phone: String? = null,
    val email: String? = null,

    @Serializable(with = OffsetDateTimeSerializer::class) val lastImportedAt: OffsetDateTime? = null
)
