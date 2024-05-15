package it.polito.uniteam.classes

class Chat {
    var id:Int = 0
    var sender: Member = Member()
    // null if team chat
    var receiver: Member? = null
    // null if personal chat
    var teamId: Int? = null
    var messages: List<Message> = emptyList()
}