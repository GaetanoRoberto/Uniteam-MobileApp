package it.polito.uniteam.classes

import java.time.LocalDate
import java.time.LocalDateTime

data class Message (
    val id: Int = 0,
    val senderId : Int,
    val message: String = "",
    val creationDate: LocalDateTime = LocalDateTime.now(),
    var membersUnread: List<Int> = mutableListOf(),
    val status: messageStatus = messageStatus.UNREAD
    )

enum class messageStatus {
    READ,UNREAD
}