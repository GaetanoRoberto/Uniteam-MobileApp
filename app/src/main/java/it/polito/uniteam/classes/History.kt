package it.polito.uniteam.classes

import java.time.LocalDate
import java.time.LocalDateTime

data class History (
    var id: Int = 0,
    var comment: String,
    var date: String,
    var user: Member
)

data class HistoryDB (
    var id: String = "",
    var comment: String,
    var date: LocalDate,
    var user: MemberDB
)
data class HistoryDBFinal (
    var id: String = "",
    var comment: String,
    var date: LocalDateTime,
    var user: String
)