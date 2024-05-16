package it.polito.uniteam

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.Team
import it.polito.uniteam.gui.calendar.DummyDataProvider


class UniTeamModel {
    // Calendar View Model
    private var _loggedMember by mutableStateOf<Member?>(null)
    val loggedMember = _loggedMember
    fun setLoggedMember(member: Member) {
        _loggedMember = member
    }

    private var _teams = mutableStateListOf<Team>()
    val teams = _teams

    private var _selectedTeam = mutableStateOf<Team>(Team()) // team selected to show its details
    val selectedTeam = _selectedTeam

    fun selectTeam(id: Int){ // click on team to set the selected team to show
        val team = getTeam(id)
        if(team != null){
            _selectedTeam.value = team
        }

    }

    fun getAllTasks() :List<Task>{
        val ret = mutableListOf<Task>()
        _teams.forEach { team->
            ret.addAll(team.tasks)
        }
        return ret
    }

    fun getAllMembers() :List<Member>{
        val ret = mutableListOf<Member>()
        _teams.forEach { team->
            ret.addAll(team.members)
        }
        //return ret TODO( DESELECT FOR PRODUCTION )
        //return only for testing
        return(listOf(DummyDataProvider.member1,
            DummyDataProvider.member2,
            DummyDataProvider.member3,
            DummyDataProvider.member4,
            DummyDataProvider.member5,
            DummyDataProvider.member6) )
    }

    fun getAllHistories(): List<History> {
        // TODO here no history of the single tasks so they will not be visible
        return _teams.flatMap { it.teamHistory }
    }

    fun getTeam(teamId: Int): Team {
        return _teams.filter { it.id == teamId }[0]
    }

    fun addTeam(team: Team) {
        _teams.add(team)
    }

    fun editTeam(teamId: Int, team: Team) {
        _teams.replaceAll {
            if(it.id == teamId) {
                team
            } else {
                it
            }
        }
    }

    fun deleteTeam(teamId: Int) {
        _teams = _teams.filter { it.id != teamId }
            .toMutableStateList()
    }

    fun addTeamTask(teamId: Int, task: Task) {
        _teams.replaceAll {
            if (it.id == teamId) {
                it.tasks.add(task)
                it
            } else {
                it
            }
        }
    }

    fun editTeamTask(teamId: Int, task: Task) {
        _teams.replaceAll {
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
        _teams.replaceAll {
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
        _teams.replaceAll {
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
        _teams.forEach {team ->
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
        _teams.replaceAll {
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
        val chat = _teams.find { it.id == teamId }?.chat
        return chat!!
    }

    fun getUsersChat(memberToChatWith: Member): Chat {
        val chat = _loggedMember?.chats?.find { it.sender == _loggedMember && it.receiver == memberToChatWith }
        return chat!!
    }
}