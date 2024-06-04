package it.polito.uniteam.firebase

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.ChatDB
import it.polito.uniteam.classes.CommentDB
import it.polito.uniteam.classes.FileDB
import it.polito.uniteam.classes.HistoryDB
import it.polito.uniteam.classes.MemberDB
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.TaskDB
import it.polito.uniteam.classes.TeamDB
import it.polito.uniteam.classes.messageStatus
import it.polito.uniteam.classes.parseReturnType
import it.polito.uniteam.classes.parseToLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime

fun getCommentById(db: FirebaseFirestore, coroutineScope: CoroutineScope, commentId: String): Deferred<CommentDB> {
    return coroutineScope.async {
        val comment = db.collection("Comment")
            .document(commentId)
            .get()
            .await()
        val memberId = comment.getString("user") ?: ""
        val c = CommentDB(
            id = comment.id,
            user = getMemberById(db,coroutineScope,memberId).await(),
            commentValue = comment.getString("commentValue") ?: "",
            date = comment.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                parseReturnType.DATE) } as LocalDate,
            hour = comment.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                parseReturnType.TIME) } as String,
        )
        c
    }
}

fun getFileById(db: FirebaseFirestore, coroutineScope: CoroutineScope, fileId: String): Deferred<FileDB> {
    return coroutineScope.async {
        val file = db.collection("File")
            .document(fileId)
            .get()
            .await()
        val memberId = file.getString("user") ?: ""
        val f = FileDB(
            id = file.id,
            user = getMemberById(db,coroutineScope,memberId).await(),
            filename = file.getString("filename") ?: "",
            date = file.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                parseReturnType.DATE) } as LocalDate,
            uri = Uri.EMPTY
        )
        f
    }
}

fun getHistoryById(db: FirebaseFirestore, coroutineScope: CoroutineScope, historyId: String): Deferred<HistoryDB> {
    return coroutineScope.async {
        val history = db.collection("History")
            .document(historyId)
            .get()
            .await()
        val userId = history.getString("user") ?: ""
        val h = HistoryDB(
            id = history.id,
            comment = history.getString("comment") ?: "",
            date = history.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                parseReturnType.DATE) } as LocalDate,
            user = getMemberById(db,coroutineScope,userId).await()
        )
        h
    }
}

fun getTaskById(db: FirebaseFirestore, coroutineScope: CoroutineScope, taskId: String): Deferred<TaskDB> {
    return coroutineScope.async {
        val task = db.collection("Task")
            .document(taskId)
            .get()
            .await()
        val t = TaskDB()
        t.id = task.id
        t.name = task.getString("name") ?: ""
        t.description = task.getString("description")
        val category = task.getString("category") ?: ""
        t.category =  if(category.isNotEmpty()) Category.valueOf(category) else Category.NONE
        val priority = task.getString("priority") ?: ""
        t.priority = if(priority.isNotEmpty()) Priority.valueOf(priority) else Priority.LOW
        t.creationDate = task.getTimestamp("creationDate") ?.let { parseToLocalDate(it.toDate(),
            parseReturnType.DATE) } as LocalDate
        t.deadline = task.getTimestamp("deadline") ?.let { parseToLocalDate(it.toDate(),
            parseReturnType.DATE) } as LocalDate
        val estimatedTime = (task.data?.get("estimatedTime") as HashMap<String,Int>).values.toList()
        t.estimatedTime = Pair(estimatedTime[0],estimatedTime[1])
        val spentTimeMap = task.data?.get("spentTime") as List<HashMap<String,Any>>
        spentTimeMap.forEach { memberTimeMap ->
            var memberId: String = ""
            var spentTime: Pair<Int,Int> = Pair(0,0)
            memberTimeMap.forEach { dbKey, dbValue ->
                if(dbKey == "member") {
                    memberId = dbValue as String
                } else {
                    // spentTime key
                    val timeKeyValue = (dbValue as HashMap<String,Int>).values.toList()
                    spentTime = Pair(timeKeyValue[0],timeKeyValue[1])
                }
            }
            t.spentTime.put(getMemberById(db,coroutineScope,memberId).await(),spentTime)
        }
        val status = task.getString("status") ?: ""
        t.status = if(status.isNotEmpty()) Status.valueOf(status) else Status.TODO
        val repetition = task.getString("repetition") ?: ""
        t.repetition = if(repetition.isNotEmpty()) Repetition.valueOf(repetition) else Repetition.NONE
        val membersId = task.get("members") as List<String>
        membersId.forEach { memberId->
            t.members.add(getMemberById(db,coroutineScope,memberId).await())
        }
        val schedules = task.data?.get("schedules") as List<HashMap<String,HashMap<String,Any>>>
        schedules.forEach { scheduleInfoTimeMap ->
            var memberId = ""
            var scheduleDate = LocalDate.now()
            var hours = 0
            var minutes = 0
            scheduleInfoTimeMap.forEach { dbKey, dbValue ->
                if(dbKey == "scheduleInfo") {
                    val memberIdTimestampMap = dbValue as HashMap<String,Any>
                    memberIdTimestampMap.forEach { innerDBKey, innerDbValue ->
                        if(innerDBKey == "member") {
                            memberId = innerDbValue as String
                        } else {
                            // scheduleDate key
                            val timestamp = innerDbValue as Timestamp
                            scheduleDate = parseToLocalDate(timestamp.toDate(),
                                parseReturnType.DATE) as LocalDate
                        }
                    }
                } else {
                    // scheduleTime key
                    val hoursMinutesMap = dbValue as HashMap<String,Long>
                    hoursMinutesMap.forEach { dbKey, dbValue ->
                        if(dbKey == "hours") {
                            hours = dbValue.toInt()
                        } else {
                            // minutes key
                            minutes = dbValue.toInt()
                        }
                    }
                }
            }
            //Pair<MemberDB, LocalDate>, Pair<Int, Int>
            t.schedules.put(Pair(getMemberById(db,coroutineScope,memberId).await(),scheduleDate),Pair(hours,minutes))
        }
        val taskFilesId = task.get("taskFiles") as List<String>
        taskFilesId.forEach { taskFileId ->
            t.taskFiles.add(getFileById(db,coroutineScope,taskFileId).await())
        }
        val taskCommentsId = task.get("taskComments") as List<String>
        taskCommentsId.forEach { taskCommentId ->
            t.taskComments.add(getCommentById(db,coroutineScope,taskCommentId).await())
        }
        val taskHistoriesId = task.get("taskHistory") as List<String>
        taskHistoriesId.forEach { taskHistoryId ->
            t.taskHistory.add(getHistoryById(db,coroutineScope,taskHistoryId).await())
        }
        t
    }
}

fun getMessageById(db: FirebaseFirestore, coroutineScope: CoroutineScope, messageId: String): Deferred<MessageDB> {
    return coroutineScope.async {
        val message = db.collection("Message")
            .document(messageId)
            .get()
            .await()
        val status = message.getString("status") ?: ""
        val m = MessageDB(
            id = message.id,
            senderId = message.getString("senderId") ?: "",
            message = message.getString("message") ?: "",
            creationDate = message.getTimestamp("creationDate") ?.let { parseToLocalDate(it.toDate(),
                parseReturnType.DATETIME) } as LocalDateTime,
            membersUnread = message.get("membersUnread") as MutableList<String>,
            status = if(status.isNotEmpty()) messageStatus.valueOf(status) else messageStatus.UNREAD
        )
        m
    }
}

fun getChatById(db: FirebaseFirestore, coroutineScope: CoroutineScope, chatId: String): Deferred<ChatDB> {
    return coroutineScope.async {
        val chat = db.collection("Chat")
            .document(chatId)
            .get()
            .await()
        val c = ChatDB()
        c.id = chat.id
        val sender = chat.getString("sender") ?: ""
        c.sender = if(sender.isNotEmpty()) getMemberById(db,coroutineScope,sender).await() else null
        val receiver = chat.getString("receiver") ?: ""
        c.receiver = if(receiver.isNotEmpty()) getMemberById(db,coroutineScope,receiver).await() else null
        c.teamId = chat.getString("teamId") ?: ""
        val messages = chat.get("messages") as List<String>
        messages.forEach { messageId->
            c.messages.add(getMessageById(db,coroutineScope,messageId).await())
        }
        c
    }
}
fun getMemberById(db: FirebaseFirestore, coroutineScope: CoroutineScope, memberId: String): Deferred<MemberDB> {
    return coroutineScope.async {
        val member = db.collection("Member")
            .document(memberId)
            .get()
            .await()
        val m = MemberDB()
        m.id = member.id ?: ""
        m.fullName = member.getString("fullName") ?: ""
        m.username = member.getString("username") ?: ""
        m.email = member.getString("email") ?: ""
        m.location = member.getString("location") ?: ""
        m.description = member.getString("description") ?: ""
        m.kpi = member.getString("kpi") ?: ""
        m.profileImage = Uri.EMPTY
        m.teamsInfo = null//member.get("teamsInfo") as HashMap<String, MemberTeamInfo>?
        val chatsId = member.get("chats") as List<String>
        val jobs = chatsId.map { chatId ->
            getChatById(db,coroutineScope,chatId)
        }
        jobs.awaitAll().forEach { chat ->
            m.chats.add(chat)
        }
        //m.chats = mutableListOf()//member.get("chats") as MutableList<ChatDB>?
        m
    }
}

fun getTeamById(db: FirebaseFirestore, coroutineScope: CoroutineScope, teamId: String): Deferred<TeamDB> {
    return coroutineScope.async {
        val team = db.collection("Team")
            .document(teamId)
            .get()
            .await()
        val t = TeamDB()
        t.id = team.id
        t.name = team.getString("name") ?: ""
        t.description = team.getString("description") ?: ""
        t.image = Uri.EMPTY
        t.creationDate = team.getTimestamp("creationDate") ?.let { parseToLocalDate(it.toDate(),parseReturnType.DATE) } as LocalDate
        val membersId = team.get("members") as List<String>
        val membersJobs = membersId.map { memberId ->
            getMemberById(db,coroutineScope,memberId)
        }
        val tasksId = team.get("tasks") as List<String>
        val tasksJobs = tasksId.map { taskId ->
            getTaskById(db,coroutineScope,taskId)
        }
        tasksJobs.awaitAll().forEach { task ->
            t.tasks.add(task)
        }
        val teamHistories =  team.get("teamHistory") as List<String>
        val historiesJobs = teamHistories.map { historyId->
            getHistoryById(db,coroutineScope,historyId)
        }
        val chatId: String = team.getString("chat") ?: ""
        t.chat = getChatById(db,coroutineScope,chatId).await()
        membersJobs.awaitAll().forEach { m ->
            t.members.add(m)
        }
        historiesJobs.awaitAll().forEach { h->
            t.teamHistory.add(h)
        }
        t
    }
}

fun getTeams(db: FirebaseFirestore, coroutineScope: CoroutineScope): Flow<List<TeamDB>> = callbackFlow {
    // flow which provide in his lambda with the receiver a couple of method
    // try send that produce a value
    // await close so if nobody more interested in the flow perform an action(cancel subsription)
    val listener = db.collection("Team").addSnapshotListener {
        // whenever there is a change in this collection give me data (r query result, e error)
            r, e ->
        if(r!=null) {
            // try to map the result into user class
            val teams = mutableListOf<TeamDB>()
            val teamsJobs = r.map { entry->
                this.async {
                    val t = TeamDB()
                    t.id = entry.id
                    t.name = entry.getString("name") ?: ""
                    t.description = entry.getString("description") ?: ""
                    t.image = Uri.EMPTY
                    t.creationDate = entry.getTimestamp("creationDate") ?.let { parseToLocalDate(it.toDate(),parseReturnType.DATE) } as LocalDate
                    val membersId = entry.get("members") as List<String>
                    val membersJobs = membersId.map { memberId ->
                        getMemberById(db,coroutineScope,memberId)
                    }
                    val tasksId = entry.get("tasks") as List<String>
                    val tasksJobs = tasksId.map { taskId ->
                        getTaskById(db,coroutineScope,taskId)
                    }
                    tasksJobs.awaitAll().forEach { task ->
                        t.tasks.add(task)
                    }
                    val teamHistories =  entry.get("teamHistory") as List<String>
                    val historiesJobs = teamHistories.map { historyId->
                        getHistoryById(db,coroutineScope,historyId)
                    }
                    val chatId: String = entry.getString("chat") ?: ""
                    t.chat = getChatById(db,coroutineScope,chatId).await()
                    membersJobs.awaitAll().forEach { m ->
                        t.members.add(m)
                    }
                    historiesJobs.awaitAll().forEach { h->
                        t.teamHistory.add(h)
                    }
                    t
                }
            }
            this.launch {
                teamsJobs.awaitAll().forEach {
                    teams.add(it)
                }
                trySend(teams)
            }
        } else {
            // send empty list
            trySend(listOf())
        }
    }
    awaitClose {
        // when nobody more interested remove the listener, so remove database connection
        listener.remove()
    }
}