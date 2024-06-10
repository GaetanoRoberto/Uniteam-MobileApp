package it.polito.uniteam.gui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.messageStatus
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    val loggedMember = model.loggedMemberFinal.id // TODO hardcoded
    fun getUsersChat(member: Member) = model.getUsersChat(member)
    fun getAllHistories() = model.getAllHistories()
    fun getAllTeamMessages() = 2//= model.getAllTeamMessagesCount()
    fun getAllMembersMessages() = 3//= model.getAllMembersMessagesCount()

    fun getUnreadMessagesUserDB(messages: List<MessageDB>, loggedMemberId: String): Int = model.getUnreadMessagesUserDB(messages,loggedMemberId)
    fun getUnreadMessagesTeamDB(messages: List<MessageDB>, loggedMemberId: String): Int = model.getUnreadMessagesTeamDB(messages,loggedMemberId)

    var teamsHistories: List<Pair<TeamDBFinal, List<HistoryDBFinal>>> = listOf()

    var teamsMessages: List<Pair<TeamDBFinal, Int>> = listOf()

    // Each Element: Member,chatId with the member, N_unread_messages
    var membersChatMessages: List<Triple<MemberDBFinal, String, Int>> = listOf()

    var tabState by mutableIntStateOf(0)
        private set
    fun switchTab(index: Int) {
        tabState = index
    }

    var selectedSection by mutableStateOf(notificationsSection.MESSAGES)
        private set
    fun changeSection(section: String) {
        selectedSection = notificationsSection.valueOf(section)
    }
}

enum class notificationsSection {
    MESSAGES,ACTIVITIES
}