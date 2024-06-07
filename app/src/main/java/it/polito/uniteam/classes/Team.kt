package it.polito.uniteam.classes

import android.net.Uri
import java.time.LocalDate

data class Team (
    var id: Int = 0,
    var name: String = "",
    var description: String = "",
    var image: Uri = Uri.EMPTY,
    var creationDate: LocalDate = LocalDate.now(),
    var members: MutableList<Member> = mutableListOf(),
    var tasks: MutableList<Task> = mutableListOf(),
    var teamHistory: MutableList<History> = mutableListOf(),
    var chat: Chat? = null
    )

data class TeamDB (
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var image: Uri = Uri.EMPTY,
    var creationDate: LocalDate = LocalDate.now(),
    var members: MutableList<MemberDB> = mutableListOf(),
    var tasks: MutableList<TaskDB> = mutableListOf(),
    var teamHistory: MutableList<HistoryDB> = mutableListOf(),
    var chat: ChatDB? = null
)
data class TeamDBFinal (
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var image: Uri = Uri.EMPTY,
    var creationDate: LocalDate = LocalDate.now(),
    var members: MutableList<String> = mutableListOf(),
    var tasks: MutableList<String> = mutableListOf(),
    var teamHistory: MutableList<String> = mutableListOf(),
    var chat: String? = null
)