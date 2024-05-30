package it.polito.uniteam.gui.chat

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.messageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime


class ChatViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    val chat = model.getChat(chatId.toInt())//DummyDataProvider.directChat1

    /*var messages = mutableStateListOf<Message>().apply { addAll(chat.messages) }
        private set
*/
    val emptyChat = Chat()

    var teamName by mutableStateOf(chat.teamId?.let { model.getTeam(it).name })
        private set



    fun getTeam(teamId: Int): Team = model.getTeam(teamId)
    fun getLoggedMember() = model.loggedMember.value
    fun getMemberById(senderId: Int): Member? = model.getMemberById(senderId).first

    /*var teamName = "Team 1 Cambia Nome"
        private set*/

    fun addMessage(message: Message) {
        //messages.add(message)
        chat.messages.add(message)
        chat.messages[chat.messages.size - 1].status = messageStatus.UNREAD
    }
    fun addTeamMessage(senderId: Int, messageText: String, teamId: Int) {
        val team = getTeam(teamId)
        val membersUnread = team.members.map { it.id }

        val newMessage = Message(
            id = chat.messages.size + 1, // Assicurati di gestire gli ID dei messaggi in modo appropriato
            senderId = senderId,
            message = messageText,
            creationDate = LocalDateTime.now(),
            membersUnread = membersUnread.toMutableList()
        )

        chat.messages.add(newMessage)
    }

    fun markTeamMessageAsRead(memberId: Int, message: Message) {
        // Trova il messaggio nella lista dei messaggi della chat
        /*val mes = chat.messages.find { it.id == message.id } ?: return

        // Se il membro è presente nella lista membersUnread del messaggio trovato, rimuovilo
        if (memberId in mes.membersUnread) {
            mes.membersUnread.remove(memberId)
        }*/
        chat.messages.forEach {
            if (it.id == message.id) {
                it.membersUnread.remove(memberId)
            }
        }
        //Log.d("ChatViewModel" ,"${message.membersUnread}" ,     )
        //chat.messages.find { it.id == message.id }?.membersUnread?.remove(memberId)
        //Log.d("ChatViewModel ", "${message.membersUnread}" , )

    }

    fun markUserMessageAsRead( message: Message) {
        if (message.status == messageStatus.UNREAD) {
            message.status = messageStatus.READ
        }
    }
    /*fun markUserMessageAsRead(memberId: Int, message: Message) {
        if (message.status == messageStatus.UNREAD/* && message.senderId == memberId*/) {
            message.status = messageStatus.READ
        }
    }*/


}



