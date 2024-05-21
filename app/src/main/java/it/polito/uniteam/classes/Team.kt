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
    var teamHistory: List<History> = emptyList(),
    var chat: Chat? = null
    )