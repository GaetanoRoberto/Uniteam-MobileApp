package it.polito.uniteam.gui.chat

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.ChatDB
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberDB
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.TeamDB
import it.polito.uniteam.classes.handleInputString
import it.polito.uniteam.classes.messageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class ChatViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    var chat by mutableStateOf<ChatDB?>(null)
        private set

    var teamName by mutableStateOf<String?>(null)
        private set


    /*
    init {
        fetchChat()
    }

    private fun fetchChat() {
        viewModelScope.launch {
            val chat = model.getChatById( chatId)
            setTeamName(chat)
        }
    }

    private fun setTeamName(chat: ChatDB) {
        chat.teamId?.let { teamId ->
            viewModelScope.launch {
                model.getTeamById( teamId).collect { team ->
                    teamName = team.name
                }
            }
        }
    }*/

    fun getTeam(teamId: String): Flow<TeamDB> = model.getTeamById( teamId)
    fun getLoggedMember() = model.loggedMember.value
    fun getMemberById(memberId: String): Flow<MemberDB> = getMemberById(memberId)

    fun fetchChat(chatId: String) {
        viewModelScope.launch {
            try {
                val chatDB = model.getChatByIdTest(chatId)
                chat = chatDB
                // Use the retrieved chat object
            } catch (e: Exception) {
                println(e)
                // Handle the error
            }
        }
    }

    fun getMemberByIdTest(memberId: String) {
        viewModelScope.launch {
            try {
                val member = model.getMemberByIdTest(memberId)

                // Use the retrieved member object, e.g., update LiveData
                // _memberLiveData.postValue(member)
            } catch (e: Exception) {
                // Handle the error
                // _errorLiveData.postValue(e.message)
            }

        }

    }
    fun fetchMessagesFromChat(chatId: String) {
        viewModelScope.launch {
            try {
                val messages = model.getMessagesFromChat(chatId)
                chat?.messages?.addAll(messages)
                // Use the retrieved messages
            } catch (e: Exception) {
                // Handle the error
                println(e)
            }
        }
    }
    fun addMessage(message: MessageDB) {
        chat?.let {
            it.messages.add(message.copy(message = handleInputString(message.message)))
            it.messages[it.messages.size - 1].status = messageStatus.UNREAD
        }
    }

    fun addTeamMessage(senderId: String, messageText: String, teamId: String) {
        viewModelScope.launch {
            /*val teamDeferred = model.getTeamById( teamId)
            val team = teamDeferred.first()
            val membersUnread = team.members.map { it.id }

            val newMessage = MessageDB(
                id = chat?.messages?.size?.plus(1) ?: 1, // Ensure proper ID management
                senderId = senderId,
                message = messageText,
                creationDate = LocalDateTime.now(),
                membersUnread = membersUnread.toMutableList()
            )

            chat?.messages?.add(newMessage)*/
        }
    }

    fun markTeamMessageAsRead(memberId: String, message: MessageDB) {
        chat?.messages?.forEach {
            if (it.id == message.id) {
                it.membersUnread.remove(memberId)
            }
        }
    }

    fun markUserMessageAsRead(message: MessageDB) {
        if (message.status == messageStatus.UNREAD) {
            message.status = messageStatus.READ
        }
    }
}



