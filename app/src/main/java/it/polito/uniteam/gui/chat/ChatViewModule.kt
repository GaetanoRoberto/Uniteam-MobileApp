package it.polito.uniteam.gui.chat

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


class ChatViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {

    val chat = DummyDataProvider.directChat1
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
    }

    fun markTeamMessageAsRead(memberId: Int, message: Message) {
        if (memberId in message.membersUnread) {
            message.membersUnread = message.membersUnread.toMutableList().apply {
                remove(memberId)
            }
        }
    }
    fun markUserMessageAsRead(memberId: Int, message: Message) {
        if (message.status == messageStatus.UNREAD/* && message.senderId == memberId*/) {
            message.status = messageStatus.READ
        }
    }
    /*
    fun addMessage(message:Message) {
        val member = getMemberById(message.senderId)
        val chat = model.getUsersChat(member!!)
        chat?.messages?.add(message)
    }*/


}

// DA QUI


