package it.polito.uniteam.classes

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate
import java.time.LocalDateTime

data class Message (
    val id: Int = 0,
    val senderId : Int,
    val message: String = "",
    val creationDate: LocalDateTime = LocalDateTime.now(),
    var membersUnread: MutableList<Int> = mutableStateListOf(),
    var status: messageStatus = messageStatus.UNREAD
    )

data class MessageDB (
    var id: String = "",
    var senderId : String,
    var message: String = "",
    var creationDate: LocalDateTime = LocalDateTime.now(),
    var membersUnread: MutableList<String> = mutableStateListOf(),
    var status: messageStatus = messageStatus.UNREAD
)

enum class messageStatus {
    READ,UNREAD
}