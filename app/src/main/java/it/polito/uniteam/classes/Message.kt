package it.polito.uniteam.classes

import java.time.LocalDate
import java.time.LocalDateTime

data class Message (
    val id: Int = 0,
    val senderId : Int ,
    val message: String = "",
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val membersUnread: List<Member> = emptyList(),
    val status: messageStatus = messageStatus.UNREAD
    )

enum class messageStatus {
    READ,UNREAD
}