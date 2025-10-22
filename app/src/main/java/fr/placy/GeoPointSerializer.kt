package fr.placy

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale

object GeoPointFlexibleSerializer : KSerializer<GeoPoint> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("GeoPoint") {
            element<Double>("latitude")
            element<Double>("longitude")
        }

    override fun deserialize(decoder: Decoder): GeoPoint {
        // On veut accéder au JSON brut pour pouvoir accepter string OU objet
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("GeoPointFlexibleSerializer must be used with Json")

        val elem = input.decodeJsonElement()
        return when (elem) {
            is JsonObject -> fromGeoJson(elem)
            is JsonPrimitive -> if (elem.isString) {
                val s = elem.content.trim()
                when {
                    s.startsWith("POINT", ignoreCase = true) -> fromWkt(s)
                    s.matches(Regex("^[0-9A-Fa-f]+$")) -> fromEwkbHex(s)
                    else -> throw SerializationException("Unsupported location string format")
                }
            } else error("Unsupported JSON for GeoPoint")
            else -> throw SerializationException("Unsupported JSON for GeoPoint")
        }
    }

    override fun serialize(encoder: Encoder, value: GeoPoint) {
        // On sérialise en GeoJSON pour rester standard
        val out = (encoder as? JsonEncoder)
            ?: throw SerializationException("GeoPointFlexibleSerializer must be used with Json")
        val obj = buildJsonObject {
            put("type", "Point")
            put("coordinates", buildJsonArray {
                // GeoJSON: [lon, lat]
                add(value.longitude)
                add(value.latitude)
            })
        }
        out.encodeJsonElement(obj)
    }

    // -------- Parsers --------

    private fun fromGeoJson(obj: JsonObject): GeoPoint {
        val type = obj["type"]?.jsonPrimitive?.contentOrNull
        if (!type.equals("Point", ignoreCase = true)) {
            throw SerializationException("Only GeoJSON Point supported")
        }
        val coords = obj["coordinates"] as? JsonArray
            ?: throw SerializationException("GeoJSON Point needs 'coordinates'")
        val lon = coords[0].jsonPrimitive.double
        val lat = coords[1].jsonPrimitive.double
        return GeoPoint(latitude = lat, longitude = lon)
    }

    private fun fromWkt(wkt: String): GeoPoint {
        // Formats acceptés: POINT(lon lat) ou POINT Z (...)
        val re = Regex("""POINT(?:\s+Z)?\s*\(\s*([-\d\.Ee+]+)\s+([-\d\.Ee+]+)\s*\)""", RegexOption.IGNORE_CASE)
        val m = re.find(wkt) ?: throw SerializationException("Invalid WKT POINT")
        val lon = m.groupValues[1].toDouble()
        val lat = m.groupValues[2].toDouble()
        return GeoPoint(latitude = lat, longitude = lon)
    }

    private fun fromEwkbHex(hex: String): GeoPoint {
        val bb = ByteBuffer.wrap(hexToBytes(hex))
        // 1) byte order
        val orderFlag = bb.get().toInt()
        bb.order(if (orderFlag == 1) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN)
        // 2) type (EWKB: SRID flag = 0x20000000)
        val type = bb.int
        val hasSrid = (type and 0x20000000.toInt()) != 0
        val baseType = type and 0x0FFF_FFFF // 1 = Point

        if (baseType != 1) throw SerializationException("Only POINT EWKB supported")

        // 3) SRID (optionnel)
        val srid = if (hasSrid) bb.int else null
        // PostGIS geography utilise souvent SRID 4326 (WGS84)
        if (srid != null && srid != 4326) {
            // On accepte, mais avertir serait sain en prod
        }

        // 4) coordinates (X=lon, Y=lat) en double
        val lon = bb.double
        val lat = bb.double
        return GeoPoint(latitude = lat, longitude = lon)
    }

    private fun hexToBytes(s: String): ByteArray {
        val clean = s.replace("\\s+".toRegex(), "").lowercase(Locale.US)
        require(clean.length % 2 == 0) { "Invalid hex length" }
        return ByteArray(clean.length / 2) { i ->
            val idx = i * 2
            ((clean[idx].digitToInt(16) shl 4) or clean[idx + 1].digitToInt(16)).toByte()
        }
    }
}
