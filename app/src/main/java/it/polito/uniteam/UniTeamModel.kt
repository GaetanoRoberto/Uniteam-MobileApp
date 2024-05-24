package it.polito.uniteam

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.messageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.log


class UniTeamModel {

    private val _loggedMember = MutableStateFlow<Member>(DummyDataProvider.member1)
    val loggedMember: StateFlow<Member> = _loggedMember
    fun setLoggedMember(member: Member) {
        _loggedMember.value = member
    }

    // To update the teamsInfo of the loggedMember
    fun updateTeamsInfo(newTeamsInfo: HashMap<Int, MemberTeamInfo>) {
        _loggedMember.let {
            val updatedMember = it.value.copy(teamsInfo = newTeamsInfo)
            Log.i("updateTeamsInfo", updatedMember.toString())
            setLoggedMember(updatedMember)
        }
    }

    private val _teams = MutableStateFlow<MutableList<Team>>(mutableListOf<Team>())
    val teams: StateFlow<MutableList<Team>> = _teams

    private var _selectedTeam = mutableStateOf(Team(name= "default", description = "default" ))// team selected to show its details
    var selectedTeam= _selectedTeam.value


    // USER CHAT UNREAD
    fun getUnreadMessagesUser(memberId: Int): Int {
        // Trova il membro con l'ID specificato
        val member = getMemberById(memberId).first

        // Se il membro non esiste, ritorna 0
        if (member == null) {
            return 0
        }
        val chat = getUsersChat(member)

        return chat?.messages?.filter { it.status == messageStatus.UNREAD }?.count() ?: 0
    }
    // TEAM CHAT UNREAD
    fun isMessageUnreadByMember(memberId: Int, message: Message): Boolean {
        return memberId in message.membersUnread
    }
    // Funzione per ottenere il numero di messaggi non letti da un membro
    fun getUnreadMessagesCount(memberId: Int, chat: Chat): Int {
        return chat.messages.count { isMessageUnreadByMember(memberId, it) }
    }

    fun getUnreadMessagesTeam(teamId: Int): Int? {
        val team = getTeam(teamId)
        return _loggedMember.value?.id.let { team.chat?.let { it1 -> getUnreadMessagesCount(it!!, it1) } }
    }

    fun selectTeam(id: Int){ // click on team to set the selected team to show
        val team = getTeam(id)
        if(team != null){
            _selectedTeam.value = team
        }

    }



    fun newTeam(){
        var newId: Int

        if(_teams.value.size <1 ){
            newId = 0
        }else{
            newId = _teams.value.map { it.id }.max() +1
        }
        _selectedTeam = mutableStateOf(Team(id = newId, name= "", description = ""))
        selectedTeam= _selectedTeam.value
    }

    fun changeSelectedTeamName(s:String){
        _selectedTeam.value.name= s
    }
    fun changeSelectedTeamDescription(s:String){
        _selectedTeam.value.description = s
    }
    fun changeSelectedTeamImage(u: Uri){
        _selectedTeam.value.image = u
    }

    fun changeSelectedTeamMembers(members: List<Member>){
        _selectedTeam.value.members.apply {
            clear()
            addAll(members.toMutableList())
        }    }

    init {
        // get dummy data
        _teams.value = DummyDataProvider.getTeams().toMutableStateList()
    }

    fun getAllTasks() :List<Task>{
        val ret = mutableListOf<Task>()
        _teams.value.forEach { team->
            val tasks = team.tasks.filter { it.members.contains(_loggedMember.value) }
            ret.addAll(tasks)
        }
        return ret
    }

    fun getAllMembers() :List<Member>{
        val ret = mutableListOf<Member>()
        _teams.value.forEach { team->
            ret.addAll(team.members)
        }
        return ret// TODO( DESELECT FOR PRODUCTION )
        //return only for testing
        /*return(listOf(DummyDataProvider.member1,
            DummyDataProvider.member2,
            DummyDataProvider.member3,
            DummyDataProvider.member4,
            DummyDataProvider.member5,
            DummyDataProvider.member6) )*/
    }

    fun getAllDistinctMembers(): List<Member> {
        val members = mutableListOf<Member>()
        _teams.value.forEach {
            members.addAll(it.members.toList())
        }
        return members.toSet().toList()
    }
    fun getAllHistories(): List<Pair<Team,List<History>>> {
        // TODO here no history of the single tasks so they will not be visible
        return _teams.value.map { Pair(it,it.teamHistory) }
    }

    fun getAllTeamMessagesCount(): List<Pair<Team,Int>> {
        val teams = mutableListOf<Pair<Team,Int>>()
        _teams.value.filter { it.chat!=null }.forEach {
            val count = getUnreadMessagesTeam(it.id)
            if (count!= null && count > 0) {
                teams.add(Pair(it,count))
            }
        }
        return teams
    }

    fun getAllMembersMessagesCount(): List<Pair<Member,Int>> {
        val members = getAllDistinctMembers()
        val messages = mutableListOf<Pair<Member,Int>>()
        members.forEach { member ->
            val chat = getUsersChat(member)
            if(chat!=null) {
                val count = getUnreadMessagesCount(member.id,chat)
                if (count > 0) {
                    messages.add(Pair(member, count))
                }
            }
        }
        return messages
    }

    fun getTeam(teamId: Int): Team {
        Log.i("diooo",_teams.toString())
        return _teams.value.filter { it.id == teamId }[0]
    }
    fun getAllTeams(): List<Team> {
        return _teams.value
    }

    fun addTeam(team: Team) {
        _teams.value.add(team)
    }

    fun editTeam(teamId: Int, team: Team) {
        _teams.value.replaceAll {
            if(it.id == teamId) {
                team
            } else {
                it
            }
        }
    }

    fun deleteTeam(teamId: Int) {
        _teams.value = _teams.value.filter { it.id != teamId }
            .toMutableStateList()
    }

    fun addTeamTask(teamId: Int, task: Task) {
        _teams.value.replaceAll {
            if (it.id == teamId) {
                it.tasks.add(task)
                it
            } else {
                it
            }
        }
    }

    fun editTeamTask(teamId: Int, task: Task) {
        _teams.value.replaceAll {
            if (it.id == teamId) {
                it.tasks.replaceAll { innerTask ->
                    if (innerTask.id == task.id) {
                        task
                    } else {
                        innerTask
                    }
                }
                it
            } else {
                it
            }
        }
    }

    fun deleteTeamTask(teamId: Int, taskId: Int) {
        _teams.value.replaceAll {
            if (it.id == teamId) {
                it.tasks = it.tasks.filter { it.id != taskId }
                    .toMutableStateList()
                it
            } else {
                it
            }
        }
    }

    fun addTeamMember(teamId: Int, member: Member) {
        _teams.value.replaceAll {
            if (it.id == teamId) {
                it.members.add(member)
                it
            } else {
                it
            }
        }
    }

    fun getMemberById(memberId: Int): Pair<Member?,List<Team>> {
        var member: Member? = null;
        val commonTeam = mutableListOf<Team>()
        _teams.value.forEach {team ->
            var isCommon = false
            team.members.forEach {
                if(it.id == memberId) {
                    member = it
                    isCommon = true
                }
            }
            if (isCommon) {
                commonTeam.add(team)
            }
        }
        return Pair(member,commonTeam.toList())
    }

    fun deleteTeamMember(teamId: Int, memberId: Int) {
        _teams.value.replaceAll {
            if (it.id == teamId) {
                it.members = it.members.filter { it.id != memberId }
                    .toMutableStateList()
                it
            } else {
                it
            }
        }
    }

    fun getTeamChat(teamId: Int): Chat {
        val chat = _teams.value.find { it.id == teamId }?.chat
        return chat!!
    }

    fun getUsersChat(memberToChatWith: Member): Chat? {
        _loggedMember.value.chats.forEach{chat ->
            val c = getChat(chat)
            if (c.receiver == memberToChatWith && c.sender == loggedMember.value) {
                return c
            }
        }
        return null
    }
    fun getChat(chatId: Int): Chat {
        val allChats = DummyDataProvider.allChats
        return allChats.filter { it.id == chatId }[0]
    }

    fun sumTimes(time1: Pair<Int, Int>, time2: Pair<Int, Int>): Pair<Int, Int> {
        val totalMinutes = time1.second + time2.second
        val minutesOverflow = totalMinutes / 60
        val minutes = totalMinutes % 60

        val totalHours = time1.first + time2.first + minutesOverflow

        return Pair(totalHours, minutes)
    }
}