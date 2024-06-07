package it.polito.uniteam.classes

import java.time.LocalDate

data class Comment(
    var id: Int = 0,
    var user: Member,
    var commentValue: String,
    var date: String,
    var hour: String
)

data class CommentDB(
    var id: String = "",
    var user: MemberDB,
    var commentValue: String,
    var date: LocalDate,
    var hour: String
)
data class CommentDBFinal(
    var id: String = "",
    var user: String,
    var commentValue: String,
    var date: LocalDate,
    var hour: String
)