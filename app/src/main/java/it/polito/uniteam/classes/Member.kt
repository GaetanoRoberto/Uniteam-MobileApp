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
    var permissionrole: permissionRole = permissionRole.USER
    // key teamId value role inside it
    var teamsInfo: HashMap<Int,MemberTeamInfo>? = null
    var chats: List<Chat> = emptyList()
}

class MemberTeamInfo {
    var role: categoryRole = categoryRole.NONE
    var weeklyAvailabilityTimes: Int = 0
    var weeklyAvailabilityHours: Pair<Int,Int> = Pair(0,0)
}
enum class permissionRole {
    ADMIN, USER
}
