package it.polito.uniteam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.classes.ChatDBFinal
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.messageStatus
import it.polito.uniteam.firebase.getAllMessagesUnread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeneralViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    val teams = model.getAllTeams2()
    val members = model.getAllMembers2()
    val tasks = model.getAllTasks2()
    val histories = model.getAllHistories2()
    val chats = model.getAllChats2()
    val files = model.getAllFiles2()
    val comments = model.getAllComments2()
    val messages = model.getAllMessages2()
    //val loggedMember = model.getLoggedUserFlow()


    /*fun getTeams() = model.getTeams()
    fun getAllTeamsMembersHome() = model.getAllTeamsMembersHome()
    fun getTeamById(id: String) = model.getTeamById(id)*/
}