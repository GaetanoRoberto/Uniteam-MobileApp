package it.polito.uniteam.classes

import java.time.LocalDate

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