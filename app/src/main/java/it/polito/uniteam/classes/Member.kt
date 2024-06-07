package it.polito.uniteam.classes

import android.net.Uri
import java.math.BigInteger
import java.security.MessageDigest

data class Member (
    var id: Int = 0,
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var location: String = "",
    var description: String = "",
    var kpi: String = "",
    var profileImage: Uri = Uri.EMPTY,
    // key teamId value role inside it
    var teamsInfo: HashMap<Int,MemberTeamInfo>? = null,
    var chats: MutableList<Int> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}

data class MemberTeamInfo (
    var role: CategoryRole = CategoryRole.NONE,
    var weeklyAvailabilityTimes: Int = 0,
    var weeklyAvailabilityHours: Pair<Int,Int> = Pair(0,0),
    var permissionrole: permissionRole = permissionRole.USER
    )
enum class permissionRole {
    ADMIN, USER
}

data class MemberDB (
    var id: String = "",
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var location: String = "",
    var description: String = "",
    var kpi: String = "",
    var profileImage: Uri = Uri.EMPTY,
    // key teamId value role inside it
    var teamsInfo: HashMap<String,MemberTeamInfo>? = null,
    var chats: MutableList<ChatDB> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemberDB) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(id.toByteArray(Charsets.UTF_8))

        // Convert the first 4 bytes of the hash to an integer
        return BigInteger(1, hashBytes.sliceArray(0..3)).toInt()
    }
}
data class MemberDBFinal (
    var id: String = "",
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var location: String = "",
    var description: String = "",
    var kpi: String = "",
    var profileImage: Uri = Uri.EMPTY,
    // key teamId value role inside it
    var teamsInfo: HashMap<String,MemberTeamInfo>? = null,
    var chats: MutableList<String> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemberDBFinal) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(id.toByteArray(Charsets.UTF_8))

        // Convert the first 4 bytes of the hash to an integer
        return BigInteger(1, hashBytes.sliceArray(0..3)).toInt()
    }
}