package it.polito.uniteam.classes

import android.net.Uri

class Member {
    var id: Int = 0
    var fullName: String = ""
    var username: String = ""
    var email: String = ""
    var location: String = ""
    var description: String = ""
    var kpi: String = ""
    var profileImage: Uri = Uri.EMPTY
}