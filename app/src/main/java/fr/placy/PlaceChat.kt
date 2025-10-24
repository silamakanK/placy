package fr.placy

import fr.placy.SupabaseManager.supabase
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.UUID
import kotlin.time.ExperimentalTime

class PlaceChat {

    lateinit var chatFlow: Flow<List<PlaceMessage>>
        private set

    constructor()

    @OptIn(SupabaseExperimental::class, ExperimentalTime::class)
    suspend fun init(placeId: UUID) {
        val singleFlow: Flow<PlaceMessage> = supabase
            .from("place_messages")
            .selectSingleValueAsFlow(
                primaryKey = PlaceMessage::id,
            ) {
                and {
                    eq("chat_type", ChatRoomType.visitors)
                    eq("place_id", placeId)
                }
            }

        // On accumule les messages successifs dans une liste
        chatFlow = singleFlow
            .scan(emptyList<PlaceMessage>()) { acc, msg ->
                if (acc.any { it.id == msg.id }) acc
                else (acc + msg).sortedBy { it.insertedAt }
            }
            .distinctUntilChanged()
    }
}
