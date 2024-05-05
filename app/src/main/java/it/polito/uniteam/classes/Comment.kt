package it.polito.uniteam.classes

data class Comment(
    var user: Member,
    var commentValue: String,
    var date: String,
    var hour: String
)
