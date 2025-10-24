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
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fr.placy.SupabaseManager.supabase
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresListDataFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.ExperimentalTime

class ChatFragment : Fragment() {

    private val adapter = ChatAdapter()

    private var recyclerView: RecyclerView? = null
    private var messageInput: TextInputEditText? = null
    private var sendButton: MaterialButton? = null
    private var placeSelector: MaterialAutoCompleteTextView? = null
    private var placeInputLayout: TextInputLayout? = null
    private var chatTypeChipGroup: ChipGroup? = null

    private var realtimeChannel: RealtimeChannel? = null
    private var messagesJob: Job? = null

    private var places: List<Place> = emptyList()
    private var cachedMessages: List<PlaceMessage> = emptyList()
    private var selectedPlaceId: UUID? = null
    private var selectedChatType: ChatRoomType = ChatRoomType.visitors

    private val chatTypeChipIds = mapOf(
        ChatRoomType.visitors to R.id.chipChatTypeVisitors,
        ChatRoomType.interested to R.id.chipChatTypeInterested
    )

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
        placeInputLayout = view.findViewById(R.id.placeInputLayout)
        placeSelector = view.findViewById(R.id.placeSelector)
        chatTypeChipGroup = view.findViewById(R.id.chatTypeChipGroup)

        setupPlaceSelector()
        setupChatTypeSelector()

        messageInput?.doOnTextChanged { text, _, _, _ ->
            updateSendButtonState(text)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loadPlaces()
        }
    }

    private fun handleSendMessage() {
        val text = messageInput?.text?.toString()?.trim().orEmpty()
        if (selectedPlaceId == null) return
        if (text.isBlank()) return

        // TODO: connect to Supabase mutation when send is implemented.
        messageInput?.text = null
        updateSendButtonState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesJob?.cancel()
        messagesJob = null
        realtimeChannel?.let { channel ->
            viewLifecycleOwner.lifecycleScope.launch {
                supabase.realtime.removeChannel(channel)
            }
        }
        realtimeChannel = null
        recyclerView?.adapter = null
        recyclerView = null
        messageInput = null
        sendButton = null
        placeSelector = null
        placeInputLayout = null
        chatTypeChipGroup = null
    }

    @OptIn(SupabaseExperimental::class)
    private suspend fun loadPlaces() {
        val fetchedPlaces = supabase
            .from("places")
            .select()
            .decodeList<Place>()

        places = fetchedPlaces
        val labels = places.map { it.name.ifBlank { getString(R.string.chat_place_placeholder_name) } }

        if (labels.isEmpty()) {
            placeInputLayout?.isEnabled = false
            placeSelector?.setSimpleItems(emptyArray())
            placeSelector?.setText("", false)
            selectedPlaceId = null
            cachedMessages = emptyList()
            displayMessagesForCurrentSelection()
            updateSendButtonState()
            return
        }

        placeInputLayout?.isEnabled = true
        placeSelector?.setSimpleItems(labels.toTypedArray())

        val currentIndex = places.indexOfFirst { it.id == selectedPlaceId }
        val resolvedIndex = currentIndex.takeIf { it >= 0 } ?: 0
        val resolvedPlace = places[resolvedIndex]
        selectedPlaceId = resolvedPlace.id
        placeSelector?.setText(labels[resolvedIndex], false)

        updateSendButtonState()
        startChatListener(resolvedPlace.id)
    }

    private fun setupPlaceSelector() {
        placeSelector?.setOnItemClickListener { _, _, position, _ ->
            val place = places.getOrNull(position) ?: return@setOnItemClickListener
            if (place.id != selectedPlaceId) {
                selectedPlaceId = place.id
                cachedMessages = emptyList()
                displayMessagesForCurrentSelection()
                updateSendButtonState()
                viewLifecycleOwner.lifecycleScope.launch {
                    startChatListener(place.id)
                }
            }
        }
    }

    private fun setupChatTypeSelector() {
        chatTypeChipGroup?.setOnCheckedStateChangeListener { _, checkedIds ->
            val chipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val newType = chatTypeChipIds.entries.firstOrNull { it.value == chipId }?.key ?: return@setOnCheckedStateChangeListener
            if (newType != selectedChatType) {
                selectedChatType = newType
                displayMessagesForCurrentSelection()
            }
        }
        chatTypeChipIds[selectedChatType]?.let { defaultId ->
            chatTypeChipGroup?.check(defaultId)
        }
    }

    private fun updateSendButtonState(currentText: CharSequence? = messageInput?.text) {
        val hasText = !currentText.isNullOrBlank()
        sendButton?.isEnabled = hasText && selectedPlaceId != null
    }

    @OptIn(SupabaseExperimental::class)
    private suspend fun startChatListener(placeId: UUID) {
        messagesJob?.cancel()
        messagesJob = null

        realtimeChannel?.let { channel ->
            viewLifecycleOwner.lifecycleScope.launch {
                supabase.realtime.removeChannel(channel)
            }
        }
        realtimeChannel = null

        cachedMessages = emptyList()
        displayMessagesForCurrentSelection()

        val channel = supabase.channel("placeMessages-$placeId").also {
            realtimeChannel = it
        }

        messagesJob = channel.postgresListDataFlow(
            schema = "public",
            table = "place_messages",
            primaryKey = PlaceMessage::id,
            filter = FilterOperation("place_id", FilterOperator.EQ, placeId)
        ).onEach { updateMessages(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        channel.subscribe()
    }

    private fun updateMessages(messages: List<PlaceMessage>) {
        cachedMessages = messages
        displayMessagesForCurrentSelection()
    }

    @OptIn(ExperimentalTime::class)
    private fun displayMessagesForCurrentSelection() {
        val chatMessages = cachedMessages
            .filter { it.chatType == selectedChatType }
            .sortedBy { it.insertedAt }
            .map {
                ChatMessage(
                    id = it.id,
                    text = it.body,
                    isMine = false,
                    timestamp = it.insertedAt.epochSeconds
                )
            }

        applyMessages(chatMessages)
    }

    private fun applyMessages(chatMessages: List<ChatMessage>) {
        adapter.submitList(chatMessages) {
            val rv = recyclerView ?: return@submitList
            if (chatMessages.isNotEmpty()) {
                rv.scrollToPosition(chatMessages.lastIndex)
            }
        }
    }
}
