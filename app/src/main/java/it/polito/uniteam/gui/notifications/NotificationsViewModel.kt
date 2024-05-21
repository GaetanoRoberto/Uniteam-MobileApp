package it.polito.uniteam.gui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.messageStatus
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    var teamsHistories by mutableStateOf(model.getAllHistories())
        private set

    var teamsMessages by mutableStateOf(listOf(
        Message(
            id = 1,
            message = "",
            senderId = 1,
            creationDate = LocalDateTime.now(),
            membersUnread = listOf(model.loggedMember!!),
            status = messageStatus.UNREAD
        ),
        Message(
            id = 2,
            message = "",
            senderId = 2,
            creationDate = LocalDateTime.now(),
            membersUnread = listOf(model.loggedMember),
            status = messageStatus.UNREAD
        ),
        Message(
            id = 3,
            message = "",
            senderId = 3,
            creationDate = LocalDateTime.now(),
            membersUnread = listOf(model.loggedMember),
            status = messageStatus.UNREAD
        )
    ))
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