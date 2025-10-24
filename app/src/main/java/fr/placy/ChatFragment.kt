package fr.placy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import fr.placy.SupabaseManager.supabase
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresListDataFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

class ChatFragment : Fragment() {

    private val adapter = ChatAdapter()

    private var recyclerView: RecyclerView? = null
    private var messageInput: TextInputEditText? = null
    private var sendButton: MaterialButton? = null

    private var realtimeChannel: RealtimeChannel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.rvChat).also { rv ->
            rv.adapter = adapter
            rv.layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            rv.itemAnimator = null
        }

        messageInput = view.findViewById(R.id.etMessage)
        sendButton = view.findViewById<MaterialButton>(R.id.btnSend)?.apply {
            isEnabled = false
            setOnClickListener { handleSendMessage() }
        }

        messageInput?.doOnTextChanged { text, _, _, _ ->
            sendButton?.isEnabled = !text.isNullOrBlank()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            startChatListener()
        }
    }

    private fun handleSendMessage() {
        val text = messageInput?.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) return

        // TODO: connect to Supabase mutation when send is implemented.
        messageInput?.text = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        messageInput = null
        sendButton = null
        realtimeChannel = null
    }

    @OptIn(SupabaseExperimental::class)
    private suspend fun startChatListener() {
        val place = supabase.from("places").select {
            limit(count = 1)
        }.decodeSingle<Place>()

        val channel = supabase.channel("placeMessages").also {
            realtimeChannel = it
        }

        channel.postgresListDataFlow(
            schema = "public",
            table = "place_messages",
            primaryKey = PlaceMessage::id,
            filter = FilterOperation("place_id", FilterOperator.EQ, place.id)
        ).onEach { updateMessages(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        channel.subscribe()
    }

    @OptIn(ExperimentalTime::class)
    private fun updateMessages(messages: List<PlaceMessage>) {
        val chatMessages = messages
            .sortedBy { it.insertedAt }
            .map {
                ChatMessage(
                    id = it.id,
                    text = it.body,
                    isMine = false,
                    timestamp = it.insertedAt.epochSeconds
                )
            }

        adapter.submitList(chatMessages) {
            val rv = recyclerView ?: return@submitList
            if (chatMessages.isNotEmpty()) {
                rv.scrollToPosition(chatMessages.lastIndex)
            }
        }
    }
}
