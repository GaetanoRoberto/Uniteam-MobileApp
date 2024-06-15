package it.polito.uniteam.gui.chat

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.handleInputString
import it.polito.uniteam.classes.messageStatus
import it.polito.uniteam.firebase.addTeamMessage
import it.polito.uniteam.firebase.addMessage
import it.polito.uniteam.firebase.markTeamMessageAsReadDB
import it.polito.uniteam.firebase.markUserMessageAsReadDB
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class ChatViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {

    val chatId: String = checkNotNull(savedStateHandle["chatId"])
    val loggedMember = model.loggedMemberFinal
    val db = model.db

    fun addTeamMessage(senderId: String, messageText: String, teamMembers: MutableList<String>) {
        viewModelScope.launch {
            try {
                val messTrimmed = handleInputString(messageText)
                addTeamMessage(db, chatId, senderId, messTrimmed, teamMembers)
            } catch (e: Exception) {
                // Gestisci l'errore
                Log.e("ChatViewModel", "Error adding team message", e)
            }
        }
    }
    fun addMessage(senderId: String, messageText: String/*, receiverId: String*/) {
        viewModelScope.launch {
            try {
                val messTrimmed = handleInputString(messageText)
                addMessage(db, chatId, senderId, messTrimmed)
            } catch (e: Exception) {
                // Gestisci l'errore
                Log.e("ChatViewModel", "Error adding direct message", e)
            }
        }
    }

    fun markTeamMessageAsRead(memberId: String, message: MessageDB) {
        viewModelScope.launch {
            try {
                markTeamMessageAsReadDB(db, memberId, message.id)
            } catch (e: Exception) {
                // Gestisci l'errore
                Log.e("ChatViewModel", "Error team set Message Read", e)
            }
        }
    }

    fun markUserMessageAsRead(message: MessageDB) {
        viewModelScope.launch {
            try {
                markUserMessageAsReadDB(db, message.id)
            } catch (e: Exception) {
                // Gestisci l'errore
                Log.e("ChatViewModel", "Error user set Message Read", e)
            }
        }
    }




//    fun addMessage(message: Message) {
//        //messages.add(message)
//        chat.messages.add(message.copy(message = handleInputString(message.message)))
//        chat.messages[chat.messages.size - 1].status = messageStatus.UNREAD
//    }
//    fun addTeamMessage(senderId: Int, messageText: String, teamId: Int) {
//        val team = getTeam(teamId)
//        val membersUnread = team.members.map { it.id }
//
//        val newMessage = Message(
//            id = chat.messages.size + 1, // Assicurati di gestire gli ID dei messaggi in modo appropriato
//            senderId = senderId,
//            message = messageText,
//            creationDate = LocalDateTime.now(),
//            membersUnread = membersUnread.toMutableList()
//        )
//
//        chat.messages.add(newMessage)
//    }

//    fun markTeamMessageAsRead(memberId: String, message: MessageDB) {TODO

//        // Trova il messaggio nella lista dei messaggi della chat
//        /*val mes = chat.messages.find { it.id == message.id } ?: return
//
//        // Se il membro è presente nella lista membersUnread del messaggio trovato, rimuovilo
//        if (memberId in mes.membersUnread) {
//            mes.membersUnread.remove(memberId)
//        }*/
//        chat.messages.forEach {
//            if (it.id == message.id) {
//                it.membersUnread.remove(memberId)
//            }
//        }
//        //Log.d("ChatViewModel" ,"${message.membersUnread}" ,     )
//        //chat.messages.find { it.id == message.id }?.membersUnread?.remove(memberId)
//        //Log.d("ChatViewModel ", "${message.membersUnread}" , )
//
//    }
//
//    fun markUserMessageAsRead(message: MessageDB) {TODO
//        if (message.status == messageStatus.UNREAD) {
//            message.status = messageStatus.READ
//        }
//    }
    /*fun markUserMessageAsRead(memberId: Int, message: Message) {
        if (message.status == messageStatus.UNREAD/* && message.senderId == memberId*/) {
            message.status = messageStatus.READ
        }
    }*/


}



