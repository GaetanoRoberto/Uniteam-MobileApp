package it.polito.uniteam.classes

import android.net.Uri
import java.time.LocalDate

data class File (
    var id: Int = 0,
    var user: Member,
    var filename: String,
    var date: String,
    var uri: Uri
)

data class FileDB (
    var id: String = "",
    var user: MemberDB,
    var filename: String,
    var date: LocalDate,
    var uri: Uri
)
data class FileDBFinal (
    var id: String = "",
    var user: String,
    var filename: String,
    var date: LocalDate,
    var uri: Uri
)