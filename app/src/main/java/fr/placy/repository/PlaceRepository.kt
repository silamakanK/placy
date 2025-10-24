package fr.placy.repository

import android.util.Log
import fr.placy.SupabaseManager
import fr.placy.model.Place
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlaceRepository {

    private const val TABLE_NAME = "places"

    suspend fun getLimitedPlaces(limit: Long = 10): List<Place> = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.supabase.postgrest
                .from(TABLE_NAME)
                .select() {
                    filter {
                        eq("is_active", true)
                    }
                    limit(limit)
                }
                .decodeList<Place>()
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Erreur getLimitedPlaces: ${e.message}")
            emptyList()
        }
    }

    /**
     * 🔹 Lieux recommandés (basés sur popularité ou récence)
     */
    suspend fun getRecommendedPlaces(): List<Place> = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.supabase.postgrest
                .from(TABLE_NAME)
                .select() {
                    filter {
                        eq("is_active", true)
                    }
                    order("created_at", order = Order.DESCENDING)
                    limit(3)
                }
                .decodeList<Place>()
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Erreur getRecommendedPlaces: ${e.message}")
            emptyList()
        }
    }


    /**
     * 🔹 Recherche par nom ou tag
     */
    suspend fun searchPlaces(keyword: String): List<Place> = withContext(Dispatchers.IO) {
        try {
            val supabase = SupabaseManager.supabase
            supabase.from(TABLE_NAME)
                .select {
                    filter {
                        or {
                            ilike("name", "%$keyword%")
                            ilike("city", "%$keyword%")
                            ilike("description", "%$keyword%")
                        }
                    }
                    limit(10)
                }
                .decodeList<Place>()
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Erreur searchPlaces: ${e.message}")
            emptyList()
        }
    }

    /**
     * 🔹 Récupère un lieu par son ID
     */
    suspend fun getPlaceById(placeId: String): Place? = withContext(Dispatchers.IO) {
        try {
            val supabase = SupabaseManager.supabase
            supabase.from(TABLE_NAME)
                .select {
                    filter {
                        eq("id", placeId)
                    }
                }
                .decodeSingleOrNull<Place>()
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Erreur getPlaceById: ${e.message}")
            null
        }
    }

    /**
     * 🔹 Crée un nouveau lieu
     */
    suspend fun createPlace(place: Place): Boolean = withContext(Dispatchers.IO) {
        try {
            val supabase = SupabaseManager.supabase
            supabase.from(TABLE_NAME).insert(place)
            true
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Erreur createPlace: ${e.message}")
            false
        }
    }

    /**
     * 🔹 Supprime un lieu
     */
    suspend fun deletePlace(placeId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val supabase = SupabaseManager.supabase
            supabase.from(TABLE_NAME).delete {
                filter { eq("id", placeId) }
            }
            true
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Erreur deletePlace: ${e.message}")
            false
        }
    }
}
