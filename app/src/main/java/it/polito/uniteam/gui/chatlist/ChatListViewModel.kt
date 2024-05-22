package it.polito.uniteam.gui.chatlist

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.messageStatus
import java.time.LocalDateTime

class ChatListViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    val loggedMember = model.loggedMember
    fun getTeam(teamId: Int) = model.getTeam(teamId)
    val team = getTeam(1)
    fun getMembers() = getTeam(1).members

    val messages = mutableStateListOf<Message>(
        Message(1, 1, "Ciao!", LocalDateTime.now().minusDays(1), status = messageStatus.UNREAD),
        Message(2, 3, "Ciao a!", LocalDateTime.now().minusDays(1), status = messageStatus.UNREAD),
        Message(3, 2, "Ciao Lucaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!", LocalDateTime.now(), status = messageStatus.UNREAD),
        Message(4, 5, "Ciao bg!", LocalDateTime.now().minusDays(3), status = messageStatus.UNREAD),
        Message(5, 2, "Ciao Alice!", LocalDateTime.now(), status = messageStatus.UNREAD),
        Message(6, 4, "Ciao s!", LocalDateTime.now().minusMinutes(1), status = messageStatus.UNREAD)

    )

    val chat = DummyDataProvider.groupChat2
/*
    val chat = Chat(
        id = 123456,
        sender = model.loggedMember.value,
        receiver = model.getMemberById(2).first,
        messages = messages,
        // teamId = 2
    )*/
    fun getUnreadMessagesCount(memberId: Int) = model.getUnreadMessagesUser(memberId)
    fun getUnreadMessagesTeam(teamId: Int) = model.getUnreadMessagesTeam(teamId)
}
