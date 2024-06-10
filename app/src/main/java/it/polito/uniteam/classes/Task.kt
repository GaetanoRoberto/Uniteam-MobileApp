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
    var taskFiles: MutableList<File> = mutableListOf(),
    var taskComments: MutableList<Comment> = mutableListOf(),
    var taskHistory: MutableList<History> = mutableListOf()
) {
    override fun toString(): String {
        return "Task(id=$id, name='$name', schedules=$schedules)"
    }
}

data class TaskDB (
    var id: String = "",
    var name: String = "",
    var description: String? = null,
    var category: Category = Category.NONE,
    var priority: Priority = Priority.LOW,
    var creationDate: LocalDate = LocalDate.now(),
    var deadline: LocalDate = LocalDate.now(),
    var estimatedTime: Pair<Int,Int> = Pair(0,0),
    var spentTime: HashMap<MemberDB,Pair<Int,Int>> = hashMapOf(),
    var status: Status = Status.TODO,
    var repetition: Repetition = Repetition.NONE,
    var members: MutableList<MemberDB> = mutableListOf(),
    var schedules: HashMap<Pair<MemberDB,LocalDate>,Pair<Int,Int>> = hashMapOf(),
    var taskFiles: MutableList<FileDB> = mutableListOf(),
    var taskComments: MutableList<CommentDB> = mutableListOf(),
    var taskHistory: MutableList<HistoryDB> = mutableListOf()
)
data class TaskDBFinal (
    var id: String = "",
    var name: String = "",
    var description: String? = null,
    var category: Category = Category.NONE,
    var priority: Priority = Priority.LOW,
    var creationDate: LocalDate = LocalDate.now(),
    var deadline: LocalDate = LocalDate.now(),
    var estimatedTime: Pair<Int,Int> = Pair(0,0),
    var spentTime: HashMap<String,Pair<Int,Int>> = hashMapOf(),
    var status: Status = Status.TODO,
    var repetition: Repetition = Repetition.NONE,
    var members: MutableList<String> = mutableListOf(),
    var schedules: HashMap<Pair<String,LocalDate>,Pair<Int,Int>> = hashMapOf(),
    var taskFiles: MutableList<String> = mutableListOf(),
    var taskComments: MutableList<String> = mutableListOf(),
    var taskHistory: MutableList<String> = mutableListOf()
)

data class TaskForCalendar(
    val id: String,
    val team: String,
    val name: String,
    val date: LocalDate,
    val deadline: LocalDate,
    val scheduledTime: Pair<Int,Int> = Pair(0,0)
    )
