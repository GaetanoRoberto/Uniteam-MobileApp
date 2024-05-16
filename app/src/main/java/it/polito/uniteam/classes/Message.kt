package it.polito.uniteam.classes

import java.time.LocalDate

data class Message (
    var id: Int = 0,
    var message: String = "",
    var creationDate: LocalDate = LocalDate.now(),
    var membersUnread: List<Member> = emptyList(),
    var status: messageStatus = messageStatus.UNREAD
    )

enum class messageStatus {
    READ,UNREAD
}