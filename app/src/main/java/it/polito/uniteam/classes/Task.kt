package it.polito.uniteam.classes

import java.util.Date

class Task {
    var name: String = ""
    var description: String = ""
    var category: String = ""
    var priority: String = ""
    var deadline: Date = Date()
    var estimatedHours: Int = 0
    var spentHours: Int = 0
    var status: Status = Status.TODO
    var repetition: Repetition = Repetition.NONE
    var members: List<Member> = emptyList()
}