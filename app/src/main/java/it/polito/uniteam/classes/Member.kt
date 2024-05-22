package it.polito.uniteam.classes

import android.net.Uri

data class Member (
    var id: Int = 0,
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var location: String = "",
    var description: String = "",
    var kpi: String = "",
    var profileImage: Uri = Uri.EMPTY,
    var permissionrole: permissionRole = permissionRole.USER,
    // key teamId value role inside it
    var teamsInfo: HashMap<Int,MemberTeamInfo>? = null,
    var chats: MutableList<Int> = mutableListOf()
)

data class MemberTeamInfo (
    var role: CategoryRole = CategoryRole.NONE,
    var weeklyAvailabilityTimes: Int = 0,
    var weeklyAvailabilityHours: Pair<Int,Int> = Pair(0,0)
)
enum class permissionRole {
    ADMIN, USER
}
