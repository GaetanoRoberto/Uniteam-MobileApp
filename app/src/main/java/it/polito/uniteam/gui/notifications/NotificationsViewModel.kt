package it.polito.uniteam.gui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.messageStatus
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    val loggedMember = model.loggedMemberFinal
    fun getUsersChat(member: Member) = model.getUsersChat(member)
    fun getAllHistories() = model.getAllHistories()
    fun getAllTeamMessages() = 2//= model.getAllTeamMessagesCount()
    fun getAllMembersMessages() = 3//= model.getAllMembersMessagesCount()
    var teamsHistories by mutableStateOf(getAllHistories())
        private set

    var teamsMessages by mutableStateOf(getAllTeamMessages())
        private set
    var membersMessages by mutableStateOf(getAllMembersMessages())
        private set

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