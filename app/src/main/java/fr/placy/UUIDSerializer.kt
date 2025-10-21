package fr.placy

import java.util.UUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString()) // "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    }

    override fun deserialize(decoder: Decoder): UUID {
        val s = decoder.decodeString()
        return UUID.fromString(s) // lève IllegalArgumentException si format invalide
    }
}
