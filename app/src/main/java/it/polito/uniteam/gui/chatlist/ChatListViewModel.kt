package it.polito.uniteam.gui.chatlist

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.messageStatus
import java.time.LocalDateTime

class ChatListViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    val loggedMember = model.loggedMemberFinal
    val teamId: String = checkNotNull(savedStateHandle["teamId"])

    fun getUnreadMessagesUserDB(messages: List<MessageDB>) = model.getUnreadMessagesUserDB(messages, loggedMember.id)
    fun getUnreadMessagesTeamDB(messages: List<MessageDB>) = model.getUnreadMessagesTeamDB(messages, loggedMember.id)

}
