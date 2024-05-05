package it.polito.uniteam.classes

data class History (
    var id: Int = 0,
    var comment: String,
    var date: String,
    var user: Member
)
