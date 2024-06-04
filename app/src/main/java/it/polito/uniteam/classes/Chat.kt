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
    val id: String = "",
    val sender: Member = Member(),
    // null if team chat
    val receiver: Member? = null,
    // null if personal chat
    val teamId: String? = null,
    //var messages: List<Message> = emptyList()
    var messages : MutableList<Message> = mutableStateListOf()
)
