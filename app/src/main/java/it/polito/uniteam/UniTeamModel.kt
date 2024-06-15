package it.polito.uniteam

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import com.auth0.android.jwt.JWT
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.uniteam.classes.CategoryRole
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.ChatDBFinal
import it.polito.uniteam.classes.Comment
import it.polito.uniteam.classes.CommentDBFinal
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.File
import it.polito.uniteam.classes.FileDBFinal
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberDB
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.TaskDBFinal
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.TeamDB
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.messageStatus
import it.polito.uniteam.classes.permissionRole
import it.polito.uniteam.firebase.changeAdminRole
//import it.polito.uniteam.firebase.addTaskHistory
import it.polito.uniteam.firebase.getAllTeamsMembersHome
import it.polito.uniteam.firebase.getMemberByEmail
import it.polito.uniteam.firebase.getMemberById
import it.polito.uniteam.firebase.getMemberFlowById
import it.polito.uniteam.firebase.getTeamById
import it.polito.uniteam.firebase.getTeams
import it.polito.uniteam.firebase.joinTeam
import it.polito.uniteam.firebase.leaveTeam
import it.polito.uniteam.firebase.updateLoggedMemberTeamInfo
import it.polito.uniteam.firebase.updateTaskAssignee
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import kotlin.collections.HashMap


class UniTeamModel(val context: Context) {
    init {
        FirebaseApp.initializeApp(context)
        // TODO better with await in a suspend fun
        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
            user = it.user!!
            Log.i("User",user.uid.toString())
        }

    }

    var isUserLogged = mutableStateOf(false)
    fun setIsUserLogged(flag: Boolean){
        isUserLogged.value = flag
    }

    suspend fun setLoggedUser(jwtPayload: JWT){
        loggedMemberFinal = getMemberByEmail(db, coroutineScope, jwtPayload).await()
        //loggedMemberFinal = getMemberFlowByEmail(db, coroutineScope, jwtPayload).collectAsState(initial = MemberDBFinal()).value
        isUserLogged.value = true
        //Log.d("LOGIN", "logged member ${loggedUser}")

    }

    val db = Firebase.firestore
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    lateinit var user : FirebaseUser


    //Stato di caricamento dati dal db
    var isLoading = mutableStateOf(true)
    var isLoading2 = mutableStateOf(true)

    //var loggedUser = getMemberById(db, coroutineScope, "d67br0MqJf6Qs1tzKHhm")
    //fun getLoggedUserFlow(): Flow<MemberDBFinal> = getMemberFlowById(db, coroutineScope,loggedMemberFinal.id)
    //fun getMemberFlowByEmail(jwtPayload : JWT) :Flow<MemberDBFinal> = getMemberFlowByEmail(db, coroutineScope, jwtPayload)

    var loggedMemberFinal = MemberDBFinal()
    //fun getTeams(): Flow<List<TeamDB>> = getTeams(db,coroutineScope,loggedUser,isLoading)
    //fun getAllTeamsMembersHome(): Flow<List<MemberDB>> = getAllTeamsMembersHome(db,coroutineScope,loggedUser,isLoading)
    //fun getTeamById(id: String): Flow<TeamDB> = getTeamById(db,coroutineScope,id)
    fun updateTaskAssignee(taskId: String, members: List<String>, loggedUser: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = updateTaskAssignee(db,taskId,members,loggedUser,onSuccess,onFailure)
    fun joinTeam(memberId: String, teamId: String, newRole: String, newHours: Number, newMinutes: Number, newTimes: Number, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = joinTeam(db,memberId,teamId,newRole,newHours,newMinutes,newTimes,onSuccess,onFailure)
    fun changeAdminRole(loggedMemberId: String, memberId: String, teamId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = changeAdminRole(db,loggedMemberId,memberId,teamId,onSuccess,onFailure)
    fun leaveTeam(memberId: String, teamId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = leaveTeam(db,memberId,teamId,onSuccess,onFailure)
    fun updateLoggedMemberTeamInfo(memberId: String, teamId: String, newRole: String, newHours: Number, newMinutes: Number, newTimes: Number) = updateLoggedMemberTeamInfo(db,memberId,teamId,newRole,newHours,newMinutes,newTimes)

    fun getAllTeams2(): Flow<List<TeamDBFinal>> = it.polito.uniteam.firebase.getAllTeams(db,coroutineScope,isLoading,isLoading2)
    fun getAllMembers2(): Flow<List<MemberDBFinal>> = it.polito.uniteam.firebase.getAllMembers(db, coroutineScope)

    fun getAllTasks2(): Flow<List<TaskDBFinal>> = it.polito.uniteam.firebase.getAllTasks(db)

    fun getAllHistories2() : Flow<List<HistoryDBFinal>> = it.polito.uniteam.firebase.getAllHistoriesFinal(db)
    fun getAllChats2() : Flow<List<ChatDBFinal>> = it.polito.uniteam.firebase.getAllChats(db)

    fun getAllFiles2() : Flow<List<FileDBFinal>> = it.polito.uniteam.firebase.getAllFiles(db)
    fun getAllComments2() : Flow<List<CommentDBFinal>> = it.polito.uniteam.firebase.getAllComments(db)

    fun getAllMessages2() : Flow<List<MessageDB>> = it.polito.uniteam.firebase.getAllMessages(db)

    fun scheduleTask(task: TaskDBFinal, scheduleDate: LocalDate, hoursToSchedule: Pair<Int,Int>) = it.polito.uniteam.firebase.scheduleTask(db, task, scheduleDate, hoursToSchedule, memberId = loggedMemberFinal.id)
    fun unscheduleTask(task: TaskDBFinal, scheduleDate: LocalDate) = it.polito.uniteam.firebase.unscheduleTask(db,task,scheduleDate, memberId = loggedMemberFinal.id)
    fun updateUserProfile(memberId: String, username: String, fullName: String, email: String, location: String, description: String, profileImage: Uri) = it.polito.uniteam.firebase.updateUserProfile(db, memberId, username, fullName,email, location, description, profileImage)
    fun addTask(context: Context, teamId: String, task: TaskDBFinal, comments: List<CommentDBFinal>, files: List<FileDBFinal>, histories: List<HistoryDBFinal>) = it.polito.uniteam.firebase.addTask(db, coroutineScope, context, teamId, task, comments, files, histories)
    fun updateTask(task: TaskDBFinal) = it.polito.uniteam.firebase.updateTask(db, task)
    fun addComment(comment: CommentDBFinal, taskId: String) = it.polito.uniteam.firebase.addComment(db, comment, taskId)
    fun addFile(context: Context, file: FileDBFinal, taskId: String) = it.polito.uniteam.firebase.addFile(db, coroutineScope, context, file, taskId)
    fun addHistories(histories: List<HistoryDBFinal>, taskId: String) = it.polito.uniteam.firebase.addTaskHistories(db, histories, taskId)
    fun deleteComment(commentId: String, taskId: String) = it.polito.uniteam.firebase.deleteComment(db, commentId, taskId)
    fun deleteFile(file: FileDBFinal, taskId: String) = it.polito.uniteam.firebase.deleteFile(db, file, taskId)
    fun updateComment(comment: CommentDBFinal, taskId: String) = it.polito.uniteam.firebase.updateComment(db, comment, taskId)
    suspend fun downloadFileAndSaveToDownloads(context: Context, fileStorageName: String, fileName: String) = it.polito.uniteam.firebase.downloadFileAndSaveToDownloads(context, fileStorageName, fileName)
    fun deleteTask(files:List<FileDBFinal>, taskId: String, teamId: String) = it.polito.uniteam.firebase.deleteTask(db, files, taskId, teamId)
    fun createTeam(team: TeamDBFinal, memberInfo : MemberTeamInfo, history: HistoryDBFinal) = it.polito.uniteam.firebase.createTeam(db, team, memberInfo, history)
    fun updateTeam(teamId:String, teamName: String, teamDescription:String, teamImage: Uri, teamMembers:List<String>, teamHistory : List<HistoryDBFinal>) = it.polito.uniteam.firebase.updateTeam(db, teamId, teamName, teamDescription, teamImage, teamMembers, teamHistory)
    fun deleteTeam(teamId: String, files:List<FileDBFinal>, user: String) = it.polito.uniteam.firebase.deleteTeam(db, teamId, files, user)

    var timesError = mutableStateOf("")
    var timeError = mutableStateOf("")

    fun resetAvailabilitiesErrors() {
        timeError.value = ""
        timesError.value = ""
    }

    var membersList = mutableListOf<Member>(
        Member().apply {
            id = 1
            fullName = "John Doe"
            username = "johndoe"
            email = "johndoe@example.com"
            location = "New York"
            description = "Software Developer"
            kpi = "85"
        },
        Member().apply {
            id = 2
            fullName = "Jane Smith"
            username = "janesmith"
            email = "janesmith@example.com"
            location = "Los Angeles"
            description = "Product Manager"
            kpi = "90"
        },
        Member().apply {
            id = 3
            fullName = "Alice Johnson"
            username = "alicejohnson"
            email = "alicejohnson@example.com"
            location = "Chicago"
            description = "Data Analyst"
            kpi = "88"
        },
        Member().apply {
            id = 4
            fullName = "Bob Williams"
            username = "bobwilliams"
            email = "bobwilliams@example.com"
            location = "San Francisco"
            description = "UX Designer"
            kpi = "92"
        },
        Member().apply {
            id = 5
            fullName = "Charlie Brown"
            username = "charliebrown"
            email = "charliebrown@example.com"
            location = "Seattle"
            description = "Project Manager"
            kpi = "89"
        },
        Member().apply {
            id = 6
            fullName = "David Davis"
            username = "daviddavis"
            email = "daviddavis@example.com"
            location = "Austin"
            description = "QA Engineer"
            kpi = "91"
        },
        Member().apply {
            id = 7
            fullName = "Eve Evans"
            username = "eveevans"
            email = "eveevans@example.com"
            location = "Boston"
            description = "DevOps Engineer"
            kpi = "93"
        }
    )

    private val _loggedMember = MutableStateFlow<Member>(DummyDataProvider.member1)
    val loggedMember: StateFlow<Member> = _loggedMember
    fun setLoggedMember(member: Member) {
        _loggedMember.value = member
    }

    fun addTeamInfo(teamId: Int, newTeamInfo: MemberTeamInfo) {
        _loggedMember.value.teamsInfo?.put(teamId,newTeamInfo)
    }

    // To update the teamsInfo of the loggedMember
    fun updateTeamInfo(teamId: Int, newTeamInfo: MemberTeamInfo) {
        _loggedMember.value.let { member ->
            val updatedTeamsInfo = member.teamsInfo?.toMutableMap() ?: mutableMapOf()
            updatedTeamsInfo[teamId] = newTeamInfo
            val updatedMember = member.copy(teamsInfo = HashMap(updatedTeamsInfo))
            Log.i("updateTeamInfo", updatedMember.toString())
            setLoggedMember(updatedMember)
            Log.i("updateTeamInfo", loggedMember.value.toString())

        }
    }
    //To check if the logged member is already in a joining team
    fun isMemberInTeam(teamId: Int): Boolean {
        return _loggedMember.value.teamsInfo?.containsKey(teamId) ?: false
    }

    private val _teams = MutableStateFlow<MutableList<Team>>(mutableListOf<Team>(
        Team(id= 0, name = "Team1", description = "Description",
            members = membersList, teamHistory = mutableListOf(History(id = 0, comment = "Team created", date = "2024/11/11", user = DummyDataProvider.member1))
        )
    ))
    val teams: StateFlow<MutableList<Team>> = _teams

    private var _selectedTeam = MutableStateFlow<Team>(Team(name= "default", description = "default", image = Uri.EMPTY ))// team selected to show its details
    val selectedTeam: StateFlow<Team> = _selectedTeam


    // USER CHAT UNREAD
    fun getUnreadMessagesUserDB(messages: List<MessageDB>, loggedMemberId: String): Int {
        Log.i("getUnreadMessagesUserDB", messages.toString())
        return messages.filter { it.status == messageStatus.UNREAD && it.senderId != loggedMemberId }.count()
    }
    fun getUnreadMessagesTeamDB(messages: List<MessageDB>, loggedMemberId: String): Int {
        Log.i("getUnreadMessagesTeamDB", messages.toString())
        return messages.filter { it.membersUnread.contains(loggedMemberId) }.count()
    }

    /*
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
        return  chat.messages.count {loggedMember.value.id in it.membersUnread }
    }

    fun getUnreadMessagesTeam(teamId: Int): Int {
        val team = getTeam(teamId)
        val loggedMemberId = _loggedMember.value.id
        val chat = team.chat

        return if (chat != null) {
            getUnreadMessagesCount(loggedMemberId, chat)
        } else {
            0
        }
    }*/
    fun selectTeam(id: Int){ // click on team to set the selected team to show
        val team = getTeam(id)
        if(team != null){
            _selectedTeam.value = team
        }

    }



    fun newTeam(): Team{
        var newId: Int

        if(_teams.value.size <1 ){
            newId = 1
        }else{
            newId = _teams.value.map { it.id }.max() +1
        }
        _selectedTeam.value = Team(id = newId, name= "", description = "", image = Uri.EMPTY)
        return selectedTeam.value
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
        return ret// TODO ( DESELECT FOR PRODUCTION )
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

    fun addTeamHistory(teamId: Int, history: History) {
        getTeam(teamId).teamHistory.add(history)

    }

    fun addTaskHistory(taskId: Int, history: History) {
        getTask(taskId)?.taskHistory?.add(history)
    }

    fun addTaskComment(taskId: Int, comment: Comment) {
        getTask(taskId)?.taskComments?.add(comment)
    }

    fun addTaskFile(taskId: Int, file: File) {
        getTask(taskId)?.taskFiles?.add(file)
    }

    fun getTask(taskId: Int): Task? {
        _teams.value.forEach { team ->
            team.tasks.forEach { task ->
                if (task.id == taskId) {
                    return task
                }
            }
        }
        return null
    }

    fun getTeamRelatedToTask(taskId: Int): Team? {
        _teams.value.forEach { team->
            team.tasks.forEach { task->
                if(task.id == taskId)
                    return team
            }
        }
        return null
    }

//    fun getAllTeamMessagesCount(): List<Pair<Team,Int>> {
//        val teams = mutableListOf<Pair<Team,Int>>()
//        _teams.value.filter { it.chat!=null }.forEach {
//            val count = getUnreadMessagesTeam(it.id)
//            if (count!= null && count > 0) {
//                teams.add(Pair(it,count))
//            }
//        }
//        return teams
//    }
//
//    fun getAllMembersMessagesCount(): List<Pair<Member,Int>> {
//        val members = getAllDistinctMembers()
//        val messages = mutableListOf<Pair<Member,Int>>()
//        members.forEach { member ->
//            val chat = getUsersChat(member)
//            if(chat!=null) {
//                val count = getUnreadMessagesCount(member.id,chat)
//                if (count > 0) {
//                    messages.add(Pair(member, count))
//                }
//            }
//        }
//        return messages
//    }

    fun getTeam(teamId: Int): Team {
        return _teams.value.filter { it.id == teamId }[0]
    }

    fun getAllTeamsMembers(): List<Member> {
        val uniqueMembers = mutableSetOf<Member>()
        _teams.value.forEach { team ->
            team.members.forEach { member ->
                if (member.id != _loggedMember.value.id) {
                    uniqueMembers.add(member)
                }
            }
        }
        return uniqueMembers.toList()
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
                if (!it.members.any { it.id == member.id }) {
                    it.members.add(member)
                }
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
        _teams.value.forEach { team ->
            team.members.forEach { member ->
                if (member.id == memberId) {
                    member.teamsInfo?.remove(teamId)
                }
            }
        }
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

    fun getUsersChat(memberToChatWith: Member): Chat {
        _loggedMember.value.chats.forEach{chat ->
            val c = getChat(chat)
            if (c.receiver == memberToChatWith && c.sender == loggedMember.value) {
                return c
            }
        }
        // Se non troviamo una chat esistente, creiamo una nuova chat
        val newChat = createNewChat(memberToChatWith)
        _loggedMember.value.chats.add(newChat.id)
        return newChat
    }
    fun createNewChat(memberToChatWith: Member): Chat {
        val newChatId = DummyDataProvider.allChats.map { it.id }.max()!! + 1
        val newChat =  Chat(
            id = newChatId,
            sender = loggedMember.value,
            receiver = memberToChatWith,
            messages = mutableStateListOf()
        )
        DummyDataProvider.allChats.add(newChat)
        loggedMember.value.chats.add(newChatId)
        return newChat
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