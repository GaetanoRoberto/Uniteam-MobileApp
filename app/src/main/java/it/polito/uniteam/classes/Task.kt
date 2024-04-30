package it.polito.uniteam.classes

import java.time.LocalDate

class Task {
    var id: Int = 0
    var name: String = ""
    var description: String = ""
    var category: String = ""
    var priority: String = ""
    var creationDate: LocalDate = LocalDate.now()
    var deadline: LocalDate? = null
    var estimatedHours: Int = 0
    var spentHours: Int = 0
    var status: Status = Status.TODO
    var repetition: Repetition = Repetition.NONE
    var members: List<Member> = emptyList()
    var schedules: HashMap<LocalDate,Int> = hashMapOf()
}