package it.polito.uniteam.classes

import java.time.LocalDate

class Message {
    var id: Int = 0
    var message: String = ""
    var creationDate: LocalDate = LocalDate.now()
}