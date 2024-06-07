package it.polito.uniteam.classes

import androidx.compose.runtime.mutableStateListOf

data class Chat (
    val id: Int = 0,
    val sender: Member = Member(),
    // null if team chat
    val receiver: Member? = null,
    // null if personal chat
    val teamId: Int? = null,
    //var messages: List<Message> = emptyList()
    var messages : MutableList<Message> = mutableStateListOf()
)

data class ChatDB (
    var id: String = "",
    var sender: MemberDB? = null,
    // null if team chat
    var receiver: MemberDB? = null,
    // null if personal chat
    var teamId: String? = null,
    //var messages: List<Message> = emptyList()
    var messages : MutableList<MessageDB> = mutableStateListOf()
)
data class ChatDBFinal (
    var id: String = "",
    var sender: String? = null,
    // null if team chat
    var receiver: String? = null,
    // null if personal chat
    var teamId: String? = null,
    //var messages: List<Message> = emptyList()
    var messages : MutableList<String> = mutableStateListOf()
)
