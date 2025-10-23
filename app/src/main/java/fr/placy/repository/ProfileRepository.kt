package fr.placy.repository

import fr.placy.SupabaseManager
import fr.placy.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

object ProfileRepository {
    suspend fun getProfileById(userId: String): Profile? {
        return try {
            SupabaseManager.supabase
                .from("profiles")
                .select() {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            println("Erreur lors de la récupération du profil : ${e.message}")
            null
        }
    }

    // 🧑‍💻 Récupère le profil du user actuellement connecté
    suspend fun getCurrentUserProfile(): Profile? {
        return try {
            val session = SupabaseManager.supabase.auth.currentSessionOrNull()
            val user = session?.user ?: return null
            getProfileById(user.id)
        } catch (e: Exception) {
            println("Erreur lors de la récupération du profil connecté : ${e.message}")
            null
        }
    }
}
