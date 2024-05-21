package it.polito.uniteam.gui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.Team


class ChatViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {

    val messages = mutableStateListOf<Message>(
        Message(1, 1, "Ciao!"),
        Message(2, 3, "Ciao a!"),
        Message(3, 2, "Ciao Lucaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!"),
        Message(4, 4, "Ciao bg!"),
        Message(5, 2, "Ciao Alice!")
    )
    val chat = Chat(
        id = 123456,
        sender = model.loggedMember!!,
        receiver = model.getMemberById(2).first,
        messages = messages,
       // teamId = 2
    )


    // ERRORE QUANDO FACCIO getTeam in ChatHeader 1 TODO
    var teamName by mutableStateOf(chat.teamId?.let { model.getTeam(it).name })
        private set



    fun getTeam(teamId: Int): Team = model.getTeam(teamId)
    fun getLoggedMember() = model.loggedMember
    fun getMemberById(senderId: Int): Member? = model.getMemberById(senderId).first

    /*var teamName = "Team 1 Cambia Nome"
        private set*/

    fun addMessage(message: Message) {
        //chat.messages += Message(chat.messages.size + 1, chat.sender.id, message)
        messages.add(message)
    }

}

// DA QUI


