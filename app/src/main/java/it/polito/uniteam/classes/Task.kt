package it.polito.uniteam.classes

import java.time.LocalDate

data class Task (
    var id: Int = 0,
    var name: String = "",
    var description: String? = null,
    var category: Category? = null,
    var priority: Priority = Priority.LOW,
    var creationDate: LocalDate = LocalDate.now(),
    var deadline: LocalDate? = null,
    var estimatedTime: Pair<Int,Int> = Pair(0,0),
    var spentTime: HashMap<Member,Pair<Int,Int>> = hashMapOf(),
    var status: Status = Status.TODO,
    var repetition: Repetition = Repetition.NONE,
    var members: List<Member> = emptyList(),
    var schedules: HashMap<Pair<Member,LocalDate>,Pair<Int,Int>> = hashMapOf(),
    var taskFiles: List<File> = emptyList(),
    var taskComments: List<Comment> = emptyList(),
    var taskHistory: MutableList<History> = mutableListOf()
) {
    override fun toString(): String {
        return "Task(id=$id, name='$name', schedules=$schedules)"
    }
}

data class TaskForCalendar(
    val team: String,
    val name: String,
    val date: LocalDate,
    val estimatedTime: Pair<Int,Int> = Pair(0,0)
    )
