package it.polito.uniteam.firebase

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.auth0.android.jwt.JWT
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.CategoryRole
import it.polito.uniteam.classes.ChatDB
import it.polito.uniteam.classes.ChatDBFinal
import it.polito.uniteam.classes.CommentDB
import it.polito.uniteam.classes.CommentDBFinal
import it.polito.uniteam.classes.FileDB
import it.polito.uniteam.classes.FileDBFinal
import it.polito.uniteam.classes.HistoryDB
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.MemberDB
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.TaskDB
import it.polito.uniteam.classes.TaskDBFinal
import it.polito.uniteam.classes.TeamDB
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.messageStatus
import it.polito.uniteam.classes.parseReturnType
import it.polito.uniteam.classes.parseToLocalDate
import it.polito.uniteam.classes.permissionRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime

suspend fun getImageToFirebaseStorage(fileName: String): Uri {
    val storage = FirebaseStorage.getInstance()
    val imageRef = storage.reference.child("images/${fileName}.jpg")
    return try {
        imageRef.downloadUrl.await()
    } catch (e: Exception) {
        e.printStackTrace()
        Uri.EMPTY
    }
}

fun getMemberByEmail(db: FirebaseFirestore, coroutineScope: CoroutineScope, jwt: JWT): Deferred<MemberDBFinal> {
    val email = jwt.getClaim("email").asString()
    return coroutineScope.async {
        val querySnapshot = db.collection("Member")
            .whereEqualTo("email", email)
            .get()
            .await()

        val memberDocument = querySnapshot.documents.firstOrNull()
        if (memberDocument != null) {
            val member = memberDocument
            val m = MemberDBFinal()
            m.id = member.id ?: ""
            m.fullName = member.getString("fullName") ?: ""
            m.username = member.getString("username") ?: ""
            m.email = member.getString("email") ?: ""
            m.location = member.getString("location") ?: ""
            m.description = member.getString("description") ?: ""
            m.kpi = member.getString("kpi") ?: ""
            m.profileImage = getImageToFirebaseStorage(member.id)
            val teamsInfo = member.get("teamsInfo")
            if (teamsInfo is List<*>) {
                m.teamsInfo = hashMapOf()
                (teamsInfo as List<HashMap<String, Any>>).forEach { teamInfoMap ->
                    val teamId = teamInfoMap["teamId"] as? String ?: return@forEach
                    val role = teamInfoMap["role"] as? String ?: "NONE"
                    val weeklyAvailabilityTimes = (teamInfoMap["weeklyAvailabilityTimes"] as? Number)?.toInt() ?: 0
                    val weeklyAvailabilityHoursMap = teamInfoMap["weeklyAvailabilityHours"] as? HashMap<String, Number>
                    var hours = 0
                    var minutes = 0
                    weeklyAvailabilityHoursMap?.forEach { (dbKey, dbValue) ->
                        if (dbKey == "hours")
                            hours = dbValue.toInt()
                        else
                            minutes = dbValue.toInt()
                    }
                    val weeklyAvailabilityHours = Pair(hours, minutes)
                    val permissionRole = teamInfoMap["permissionrole"] as? String ?: "USER"

                    m.teamsInfo?.put(teamId, MemberTeamInfo(
                        role = CategoryRole.valueOf(role),
                        weeklyAvailabilityTimes = weeklyAvailabilityTimes,
                        weeklyAvailabilityHours = weeklyAvailabilityHours,
                        permissionrole = it.polito.uniteam.classes.permissionRole.valueOf(permissionRole)
                    ))
                }
            } else {
                m.teamsInfo = hashMapOf()
            }

            m.chats = member.get("chats") as? MutableList<String> ?: mutableListOf()
            Log.d("MemberDB", m.toString())
            m
        } else {
            val email = jwt.getClaim("email").asString()?: ""
            val name = jwt.getClaim("name").asString()?: ""
            val username = jwt.getClaim("preferred_username").asString()?: jwt.getClaim("email").asString()!!  // Example claim for username
            val pictureUrl = jwt.getClaim("picture").asString()?: "" // Claim for profile image URL
            val pictureUri = Uri.parse(pictureUrl)?: Uri.EMPTY
            //create member db

            val member = mapOf(
                //"id" to "",
                "fullName" to name,
                "username" to username,
                "email" to email,
                "image" to pictureUri,
                // blank values (not in jwt)
                "location" to "",
                "description" to "",
                "teamsInfo" to listOf<String>(),
                "chats" to listOf<String>(),
            )

            val memberRef = db.collection("Member").document()
            db.runTransaction{transaction->
                transaction.set(memberRef, member)

                uploadImageToFirebase( memberRef.id, pictureUri)

            }
            //val savedMember = db.collection("Member").add(member).await()

            //TODO prova messaggi perchè cambiato
            Log.d("MemberDB","no member")

            val m = MemberDBFinal(id= memberRef.id, username= username, email = email, fullName= name, profileImage = pictureUri )
            Log.d("MemberDB","${m}")
            m
        }
    }
}
fun createNotificationChannel(context: Context, channelName: String, channelDescription: String, channelId: String) {
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel(channelId, channelName, importance).apply {
        description = channelDescription
    }
    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

suspend fun downloadFileAndSaveToDownloads(context: Context, fileStorageName: String, fileName: String) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        return // Return and wait for the permission to be granted
    }
    val storage = FirebaseStorage.getInstance()
    val fileRef = storage.reference.child("files/$fileStorageName")

    // Create the notification channel
    createNotificationChannel(context, "Download Channel","Channel for file download notifications","DOWNLOAD_CHANNEL")

    // Initialize the NotificationManager and NotificationCompat.Builder
    val notificationManager = NotificationManagerCompat.from(context)
    val builder = NotificationCompat.Builder(context, "DOWNLOAD_CHANNEL")
        .setContentTitle("Downloading File")
        .setContentText("Download in progress")
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setProgress(0, 0, true) // Indeterminate progress initially

    // Start the notification

    try {
        // Download the file as bytes with progress updates
        val MAX_DOWNLOAD_SIZE = 1024 * 1024 * 100L // Adjust as needed
        val stream = fileRef.stream.await().stream

        // Get the Downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create a file in the Downloads directory
        val file = java.io.File(downloadsDir, fileName)
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { fos ->
                val buffer = ByteArray(1024 * 400) // 400 KB buffer
                var totalBytesRead = 0L
                var bytesRead: Int = 0

                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    fos.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    // Update progress notification
                    val progress = (totalBytesRead * 100 / MAX_DOWNLOAD_SIZE).toInt()
                    builder.setProgress(100, progress, false)
                    notificationManager.notify(1, builder.build())
                }
            }
        }
        // Create intent to open the Downloads directory
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/document/primary:Download"))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Download complete, update notification
        builder.setContentText("$fileName Successfully Downloaded")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
        notificationManager.notify(1, builder.build())

    } catch (e: Exception) {
        // Update notification for failure
        builder.setContentText("Download failed")
            .setProgress(0, 0, false)
            .setOngoing(false)
        notificationManager.notify(1, builder.build())

    }
}

fun getAllTeams(db: FirebaseFirestore, coroutineScope: CoroutineScope, isLoading: MutableState<Boolean>, isLoading2: MutableState<Boolean>): Flow<List<TeamDBFinal>> = callbackFlow {
    //Stato di caricamento dati dal db
    isLoading.value = true
    isLoading2.value = true
    val listener = db.collection("Team").addSnapshotListener {
        // whenever there is a change in this collection give me data (r query result, e error)
            r, e ->
        if (r != null) {
            var teams = mutableListOf<TeamDBFinal>()
            coroutineScope.launch {
                r.forEach {
                    val t = TeamDBFinal()
                    t.id = it.id
                    t.name = it.getString("name") ?: ""
                    t.description = it.getString("description") ?: ""
                    t.image = getImageToFirebaseStorage(it.id)
                    t.creationDate = it.getTimestamp("creationDate")
                        ?.let {
                            parseToLocalDate(
                                it.toDate(),
                                parseReturnType.DATE
                            )
                        } as LocalDate
                    t.chat = it.getString("chat")
                    t.tasks = it.get("tasks") as MutableList<String>
                    t.members = it.get("members") as MutableList<String>
                    t.teamHistory = it.get("teamHistory") as MutableList<String>

                    teams.add(t)
                }
                //val teams = r.toObjects(TeamDBFinal::class.java)
                Log.i("TEAMS", teams.toString())
                trySend(teams)
            }
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}
fun getAllMembers(db: FirebaseFirestore, coroutineScope: CoroutineScope): Flow<List<MemberDBFinal>> = callbackFlow {
    val listener = db.collection("Member").addSnapshotListener {
        // whenever there is a change in this collection give me data (r query result, e error)
            r, e ->
        if (r != null) {
            var members = mutableListOf<MemberDBFinal>()
            coroutineScope.launch {
                r.forEach {
                    val m = MemberDBFinal()
                    m.id = it.id
                    m.fullName = it.getString("fullName") ?: ""
                    m.username = it.getString("username") ?: ""
                    m.email = it.getString("email") ?: ""
                    m.location = it.getString("location") ?: ""
                    m.description = it.getString("description") ?: ""
                    m.kpi = it.getString("kpi") ?: ""
                    m.profileImage = getImageToFirebaseStorage(it.id)!!
                    val teamsInfo = it.get("teamsInfo")
                    if (teamsInfo is List<*>) {
                        m.teamsInfo = hashMapOf()
                        (teamsInfo as List<HashMap<String, Any>>).forEach { teamInfoMap ->
                            val teamId = teamInfoMap["teamId"] as String ?: return@forEach
                            val role = teamInfoMap["role"] as String ?: "NONE"
                            val weeklyAvailabilityTimes =
                                (teamInfoMap["weeklyAvailabilityTimes"] as Number).toInt() ?: 0
                            val weeklyAvailabilityHoursMap =
                                teamInfoMap["weeklyAvailabilityHours"] as HashMap<String, Number>
                            var hours = 0
                            var minutes = 0
                            weeklyAvailabilityHoursMap.forEach { (dbKey, dbValue) ->
                                if (dbKey == "hours")
                                    hours = dbValue.toInt()
                                else
                                    minutes = dbValue.toInt()
                            }
                            val weeklyAvailabilityHours = Pair(hours, minutes)
                            val permissionrole = teamInfoMap["permissionRole"] as? String ?: "USER"

                            m.teamsInfo?.put(
                                teamId, MemberTeamInfo(
                                    role = CategoryRole.valueOf(role),
                                    weeklyAvailabilityTimes = weeklyAvailabilityTimes,
                                    weeklyAvailabilityHours = weeklyAvailabilityHours,
                                    permissionrole = permissionRole.valueOf(permissionrole)
                                )
                            )
                        }
                    } else {
                        m.teamsInfo = hashMapOf()
                    }
                    m.chats = it.get("chats") as MutableList<String>

                    members.add(m)
                }

                //val teams = r.toObjects(TeamDBFinal::class.java)
                Log.i("MEMBERS", members.toString())
                trySend(members)
            }
        }else{
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}
fun getAllTasks(db: FirebaseFirestore): Flow<List<TaskDBFinal>> = callbackFlow {
    val listener = db.collection("Task").addSnapshotListener { r, e ->

        if (r != null) {
            val tasks = mutableListOf<TaskDBFinal>()
            r.forEach { task ->
                val t = TaskDBFinal()
                t.id = task.id
                t.name = task.getString("name") ?: ""
                t.description = task.getString("description")
                val category = task.getString("category") ?: ""
                t.category =
                    if (category.isNotEmpty()) Category.valueOf(category) else Category.NONE
                val priority = task.getString("priority") ?: ""
                t.priority = if (priority.isNotEmpty()) Priority.valueOf(priority) else Priority.LOW
                t.creationDate = task.getTimestamp("creationDate")?.let {
                    parseToLocalDate(
                        it.toDate(),
                        parseReturnType.DATE
                    )
                } as LocalDate
                t.deadline = task.getTimestamp("deadline")?.let {
                    parseToLocalDate(
                        it.toDate(),
                        parseReturnType.DATE
                    )
                } as LocalDate
                val estimatedTime =
                    (task.data?.get("estimatedTime") as HashMap<String, Int>).values.toList()
                t.estimatedTime = Pair(estimatedTime[0], estimatedTime[1])
                val spentTimeMap = task.data?.get("spentTime") as List<HashMap<String, Any>>
                spentTimeMap.forEach { memberTimeMap ->
                    var memberId: String = ""
                    var spentTime: Pair<Int, Int> = Pair(0, 0)
                    memberTimeMap.forEach { (dbKey, dbValue) ->
                        if (dbKey == "member") {
                            memberId = dbValue as String
                        } else {
                            // spentTime key
                            val timeKeyValue = (dbValue as HashMap<String, Int>).values.toList()
                            spentTime = Pair(timeKeyValue[0], timeKeyValue[1])
                        }
                    }
                    t.spentTime.put(memberId, spentTime)
                }
                val status = task.getString("status") ?: ""
                t.status = if(status == "IN PROGRESS") Status.IN_PROGRESS else if (status.isNotEmpty()) Status.valueOf(status) else Status.TODO
                val repetition = task.getString("repetition") ?: ""
                t.repetition =
                    if (repetition.isNotEmpty()) Repetition.valueOf(repetition) else Repetition.NONE
                val membersId = task.get("members") as List<String>
                membersId.forEach { memberId ->
                    t.members.add(memberId)
                }
                val schedules =
                    task.data?.get("schedules") as List<HashMap<String, HashMap<String, Any>>>
                schedules.forEach { scheduleInfoTimeMap ->
                    var memberId = ""
                    var scheduleDate = LocalDate.now()
                    var hours = 0
                    var minutes = 0
                    scheduleInfoTimeMap.forEach { (dbKey, dbValue) ->
                        if (dbKey == "scheduleInfo") {
                            val memberIdTimestampMap = dbValue as HashMap<String, Any>
                            memberIdTimestampMap.forEach { (innerDBKey, innerDbValue) ->
                                if (innerDBKey == "member") {
                                    memberId = innerDbValue as String
                                } else {
                                    // scheduleDate key
                                    val timestamp = innerDbValue as Timestamp
                                    scheduleDate = parseToLocalDate(
                                        timestamp.toDate(),
                                        parseReturnType.DATE
                                    ) as LocalDate
                                }
                            }
                        } else {
                            // scheduleTime key
                            val hoursMinutesMap = dbValue as HashMap<String, Long>
                            hoursMinutesMap.forEach { (dbKey, dbValue) ->
                                if (dbKey == "hours") {
                                    hours = dbValue.toInt()
                                } else {
                                    // minutes key
                                    minutes = dbValue.toInt()
                                }
                            }
                        }
                    }
                    //Pair<MemberDB, LocalDate>, Pair<Int, Int>
                    t.schedules.put(Pair(memberId, scheduleDate), Pair(hours, minutes))
                }
                val taskFilesId = task.get("taskFiles") as List<String>
                taskFilesId.forEach { taskFileId ->
                    t.taskFiles.add(taskFileId)
                }
                val taskCommentsId = task.get("taskComments") as List<String>
                taskCommentsId.forEach { taskCommentId ->
                    t.taskComments.add(taskCommentId)
                }
                val taskHistoriesId = task.get("taskHistory") as List<String>
                taskHistoriesId.forEach { taskHistoryId ->
                    t.taskHistory.add(taskHistoryId)
                }
                tasks.add(t)
            }
            trySend(tasks)
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}
fun getAllHistories(db: FirebaseFirestore): Flow<List<HistoryDBFinal>> = callbackFlow {
    val listener = db.collection("History").addSnapshotListener { r, e ->
        if (r != null) {
            val histories = mutableListOf<HistoryDBFinal>()
            r.forEach { history ->
                val userId = history.getString("user") ?: ""
                val h = HistoryDBFinal(
                    id = history.id,
                    comment = history.getString("comment") ?: "",
                    date = history.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                        parseReturnType.DATE) } as LocalDate,
                    user = userId
                )
                histories.add(h)
            }
            trySend(histories)
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}
fun getAllHistoriesFinal(db: FirebaseFirestore): Flow<List<HistoryDBFinal>> = callbackFlow {
    val listener = db.collection("History").addSnapshotListener { r, e ->
        if (r != null) {
            val histories = mutableListOf<HistoryDBFinal>()
            r.forEach { history ->
                val userId = history.getString("user") ?: ""
                val h = HistoryDBFinal(
                    id = history.id,
                    comment = history.getString("comment") ?: "",
                    date = history.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                        parseReturnType.DATE) } as LocalDate,
                    user = userId
                )
                histories.add(h)
            }
            trySend(histories)
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}
fun getAllChats(db: FirebaseFirestore): Flow<List<ChatDBFinal>> = callbackFlow {
    val listener = db.collection("Chat").addSnapshotListener { r, e ->
        if (r != null) {
            val chats = r.documents.mapNotNull { document ->
                document.toObject(ChatDBFinal::class.java)?.copy(id = document.id)
            }
            trySend(chats)
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}

fun getAllFiles(db: FirebaseFirestore): Flow<List<FileDBFinal>> = callbackFlow {
    val listener = db.collection("File").addSnapshotListener { r, e ->
        if(r != null) {
            val files = mutableListOf<FileDBFinal>()
            r.forEach { file ->
                val memberId = file.getString("user") ?: ""
                val f = FileDBFinal(
                    id = file.id,
                    user = memberId,
                    filename = file.getString("filename") ?: "",
                    date = file.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                        parseReturnType.DATE) } as LocalDate,
                    uri = Uri.EMPTY
                )
                files.add(f)
            }
            trySend(files)
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}
fun getAllComments(db: FirebaseFirestore): Flow<List<CommentDBFinal>> = callbackFlow {
    val listener = db.collection("Comment").addSnapshotListener{ r,e ->
        if(r != null) {
            val comments = mutableListOf<CommentDBFinal>()
            r.forEach { comment ->
                val memberId = comment.getString("user") ?: ""
                val c = CommentDBFinal(
                    id = comment.id,
                    user = memberId,
                    commentValue = comment.getString("commentValue") ?: "",
                    date = comment.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                        parseReturnType.DATE) } as LocalDate,
                    hour = comment.getTimestamp("date") ?.let { parseToLocalDate(it.toDate(),
                        parseReturnType.TIME) } as String,
                )
                comments.add(c)
            }
            trySend(comments)
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}
fun getAllMessages(db: FirebaseFirestore): Flow<List<MessageDB>> = callbackFlow {
    val listener = db.collection("Message").addSnapshotListener { r,e  ->
        if(r != null) {
            val messages = mutableListOf<MessageDB>()
            r.forEach {message ->
                val status = message.getString("status") ?: ""
                val m = MessageDB(
                    id = message.id,
                    senderId = message.getString("senderId") ?: "",
                    message = message.getString("message") ?: "",
                    creationDate = message.getTimestamp("creationDate") ?.let { parseToLocalDate(it.toDate(),
                        parseReturnType.DATETIME) } as LocalDateTime,
                    membersUnread = (message.get("membersUnread") as? MutableList<String>) ?: mutableListOf(),
                    status = if(status.isNotEmpty()) messageStatus.valueOf(status) else messageStatus.UNREAD
                )
                messages.add(m)
            }
            trySend(messages)
        } else {
            trySend(listOf())
        }
    }
    awaitClose {
        listener.remove()
    }
}

fun getMemberFlowById(db: FirebaseFirestore, coroutineScope: CoroutineScope, memberId: String): Flow<MemberDBFinal> = callbackFlow {
    val listener = db.collection("Member").document(memberId).addSnapshotListener { r, e ->
        if (r != null) {
            val m = MemberDBFinal().apply {
                id = r.id ?: ""
                fullName = r.getString("fullName") ?: ""
                username = r.getString("username") ?: ""
                email = r.getString("email") ?: ""
                location = r.getString("location") ?: ""
                description = r.getString("description") ?: ""
                kpi = r.getString("kpi") ?: ""
                profileImage = Uri.EMPTY
            }

            val teamsInfo = r.get("teamsInfo")
            if (teamsInfo is List<*>) {
                m.teamsInfo = hashMapOf()
                (teamsInfo as List<HashMap<String, Any>>).forEach { teamInfoMap ->
                    val teamId = teamInfoMap["teamId"] as String ?: return@forEach
                    val role = teamInfoMap["role"] as String ?: "NONE"
                    val weeklyAvailabilityTimes = (teamInfoMap["weeklyAvailabilityTimes"] as Number).toInt() ?: 0
                    val weeklyAvailabilityHoursMap = teamInfoMap["weeklyAvailabilityHours"] as HashMap<String, Number>
                    var hours = 0
                    var minutes = 0
                    weeklyAvailabilityHoursMap.forEach { (dbKey, dbValue) ->
                        if(dbKey == "hours")
                            hours = dbValue.toInt()
                        else
                            minutes = dbValue.toInt()
                    }
                    val weeklyAvailabilityHours = Pair(hours, minutes)
                    val permissionrole = teamInfoMap["permissionRole"] as? String ?: "USER"

                    m.teamsInfo?.put(teamId, MemberTeamInfo(
                        role = CategoryRole.valueOf(role),
                        weeklyAvailabilityTimes = weeklyAvailabilityTimes,
                        weeklyAvailabilityHours = weeklyAvailabilityHours,
                        permissionrole = permissionRole.valueOf(permissionrole)
                    ))
                }
            } else {
                m.teamsInfo = hashMapOf()
            }
            m.chats = r.get("chats") as MutableList<String>

            Log.i("prova",m.toString())
            trySend(m)
        } else {
            trySend(MemberDBFinal())
        }
    }
    awaitClose {
        listener.remove()
    }
}
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
//        val estimatedTime = (task.data?.get("estimatedTime") as HashMap<String,Int>).values.toList()
//        t.estimatedTime = Pair(estimatedTime[0],estimatedTime[1])
//        val spentTimeMap = task.data?.get("spentTime") as List<HashMap<String,Any>>
//        spentTimeMap.forEach { memberTimeMap ->
//            var memberId: String = ""
//            var spentTime: Pair<Int,Int> = Pair(0,0)
//            memberTimeMap.forEach { (dbKey, dbValue) ->
//                if(dbKey == "member") {
//                    memberId = dbValue as String
//                } else {
//                    // spentTime key
//                    val timeKeyValue = (dbValue as HashMap<String,Int>).values.toList()
//                    spentTime = Pair(timeKeyValue[0],timeKeyValue[1])
//                }
//            }
//            t.spentTime.put(getMemberById(db,coroutineScope,memberId).await(),spentTime)
//        }
        val status = task.getString("status") ?: ""
        t.status = if(status.isNotEmpty()) Status.valueOf(status) else Status.TODO
        val repetition = task.getString("repetition") ?: ""
        t.repetition = if(repetition.isNotEmpty()) Repetition.valueOf(repetition) else Repetition.NONE
        val membersId = task.get("members") as List<String>
        membersId.forEach { memberId->
            t.members.add(getMemberById(db,coroutineScope,memberId).await())
        }
//        val schedules = task.data?.get("schedules") as List<HashMap<String,HashMap<String,Any>>>
//        schedules.forEach { scheduleInfoTimeMap ->
//            var memberId = ""
//            var scheduleDate = LocalDate.now()
//            var hours = 0
//            var minutes = 0
//            scheduleInfoTimeMap.forEach { (dbKey, dbValue) ->
//                if(dbKey == "scheduleInfo") {
//                    val memberIdTimestampMap = dbValue as HashMap<String,Any>
//                    memberIdTimestampMap.forEach { (innerDBKey, innerDbValue) ->
//                        if(innerDBKey == "member") {
//                            memberId = innerDbValue as String
//                        } else {
//                            // scheduleDate key
//                            val timestamp = innerDbValue as Timestamp
//                            scheduleDate = parseToLocalDate(timestamp.toDate(),
//                                parseReturnType.DATE) as LocalDate
//                        }
//                    }
//                } else {
//                    // scheduleTime key
//                    val hoursMinutesMap = dbValue as HashMap<String,Long>
//                    hoursMinutesMap.forEach { (dbKey, dbValue) ->
//                        if(dbKey == "hours") {
//                            hours = dbValue.toInt()
//                        } else {
//                            // minutes key
//                            minutes = dbValue.toInt()
//                        }
//                    }
//                }
//            }
//            //Pair<MemberDB, LocalDate>, Pair<Int, Int>
//            t.schedules.put(Pair(getMemberById(db,coroutineScope,memberId).await(),scheduleDate),Pair(hours,minutes))
//        }
//        val taskFilesId = task.get("taskFiles") as List<String>
//        taskFilesId.forEach { taskFileId ->
//            t.taskFiles.add(getFileById(db,coroutineScope,taskFileId).await())
//        }
//        val taskCommentsId = task.get("taskComments") as List<String>
//        taskCommentsId.forEach { taskCommentId ->
//            t.taskComments.add(getCommentById(db,coroutineScope,taskCommentId).await())
//        }
//        val taskHistoriesId = task.get("taskHistory") as List<String>
//        taskHistoriesId.forEach { taskHistoryId ->
//            t.taskHistory.add(getHistoryById(db,coroutineScope,taskHistoryId).await())
//        }
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
        val teamsInfo = member.get("teamsInfo")
        if (teamsInfo is List<*>) {
            m.teamsInfo = hashMapOf()
            (teamsInfo as List<HashMap<String, Any>>).forEach { teamInfoMap ->
                val teamId = teamInfoMap["teamId"] as String ?: return@forEach
                val role = teamInfoMap["role"] as String ?: "NONE"
                val weeklyAvailabilityTimes = (teamInfoMap["weeklyAvailabilityTimes"] as Number).toInt() ?: 0
                val weeklyAvailabilityHoursMap = teamInfoMap["weeklyAvailabilityHours"] as HashMap<String, Number>
                var hours = 0
                var minutes = 0
                weeklyAvailabilityHoursMap.forEach { (dbKey, dbValue) ->
                    if(dbKey == "hours")
                        hours = dbValue.toInt()
                    else
                        minutes = dbValue.toInt()
                }
                val weeklyAvailabilityHours = Pair(hours, minutes)
                val permissionrole = teamInfoMap["permissionRole"] as? String ?: "USER"

                m.teamsInfo?.put(teamId, MemberTeamInfo(
                    role = CategoryRole.valueOf(role),
                    weeklyAvailabilityTimes = weeklyAvailabilityTimes,
                    weeklyAvailabilityHours = weeklyAvailabilityHours,
                    permissionrole = permissionRole.valueOf(permissionrole)
                ))
            }
        } else {
            m.teamsInfo = hashMapOf()
        }
        Log.d("Member1", m.toString())
        val chatsId = member.get("chats") as List<String>
        val jobs = chatsId.map { chatId ->
            getChatById(db,coroutineScope,chatId)
        }
        Log.d("Member2", m.toString())
        jobs.awaitAll().forEach { chat ->
            m.chats.add(chat)
        }
        //m.chats = mutableListOf()//member.get("chats") as MutableList<ChatDB>?
        Log.d("MemberDB", m.toString())
        m
    }
}

//fun getMemberFlowById(db: FirebaseFirestore, memberId: String): Flow<MemberDB> = callbackFlow {
//    val memberRef = db.collection("Member").document(memberId)
//
//    val listener = memberRef.addSnapshotListener { snapshot, e ->
//        if (e != null) {
//            close(e)
//            return@addSnapshotListener
//        }
//
//        if (snapshot != null && snapshot.exists()) {
//            val m = MemberDB().apply {
//                id = snapshot.id ?: ""
//                fullName = snapshot.getString("fullName") ?: ""
//                username = snapshot.getString("username") ?: ""
//                email = snapshot.getString("email") ?: ""
//                location = snapshot.getString("location") ?: ""
//                description = snapshot.getString("description") ?: ""
//                kpi = snapshot.getString("kpi") ?: ""
//                profileImage = Uri.EMPTY
//            }
//
//            val teamsInfo = snapshot.get("teamsInfo")
//            if (teamsInfo is List<*>) {
//                m.teamsInfo = hashMapOf()
//                (teamsInfo as List<HashMap<String, Any>>).forEach { teamInfoMap ->
//                    val teamId = teamInfoMap["teamId"] as String ?: return@forEach
//                    val role = teamInfoMap["role"] as String ?: "NONE"
//                    val weeklyAvailabilityTimes = (teamInfoMap["weeklyAvailabilityTimes"] as Number).toInt() ?: 0
//                    val weeklyAvailabilityHoursMap = teamInfoMap["weeklyAvailabilityHours"] as HashMap<String, Number>
//                    var hours = 0
//                    var minutes = 0
//                    weeklyAvailabilityHoursMap.forEach { (dbKey, dbValue) ->
//                        if(dbKey == "hours")
//                            hours = dbValue.toInt()
//                        else
//                            minutes = dbValue.toInt()
//                    }
//                    val weeklyAvailabilityHours = Pair(hours, minutes)
//                    val permissionrole = teamInfoMap["permissionRole"] as? String ?: "USER"
//
//                    m.teamsInfo?.put(teamId, MemberTeamInfo(
//                        role = CategoryRole.valueOf(role),
//                        weeklyAvailabilityTimes = weeklyAvailabilityTimes,
//                        weeklyAvailabilityHours = weeklyAvailabilityHours,
//                        permissionrole = permissionRole.valueOf(permissionrole)
//                    ))
//                }
//            } else {
//                m.teamsInfo = hashMapOf()
//            }
////    val chatsId = member.get("chats") as List<String>
////    val jobs = chatsId.map { chatId ->
////        getChatById(db,coroutineScope,chatId)
////    }
////    jobs.awaitAll().forEach { chat ->
////        m.chats.add(chat)
////    }
//            Log.i("prova",m.toString())
//            trySend(m)
//        }
//    }
//    awaitClose {
//        listener.remove()
//    }
//}



fun getTeamById(db: FirebaseFirestore, coroutineScope: CoroutineScope, teamId: String): Flow<TeamDB> = flow {
    val team = db.collection("Team").document(teamId).get().await()

    val t = TeamDB().apply {
        id = team.id
        name = team.getString("name") ?: ""
        description = team.getString("description") ?: ""
        image = Uri.EMPTY
        creationDate = team.getTimestamp("creationDate")?.let { parseToLocalDate(it.toDate(), parseReturnType.DATE) } as LocalDate
    }

    val membersId = team.get("members") as List<String>
    val tasksId = team.get("tasks") as List<String>
//    val teamHistories = team.get("teamHistory") as List<String>
//    val chatId: String = team.getString("chat") ?: ""

    coroutineScope {
        // Fetch members, tasks, histories, and chat in parallel
        val membersDeferred = membersId.map { memberId ->
            async { getMemberById(db, coroutineScope, memberId).await() }
        }
        val tasksDeferred = tasksId.map { taskId ->
            async { getTaskById(db, coroutineScope, taskId).await() }
        }
//        val historiesDeferred = teamHistories.map { historyId ->
//            async { getHistoryById(db, coroutineScope,  historyId).await() }
//        }
//        val chatDeferred = async {
//            getChatById(db, coroutineScope, chatId).await()
//        }

        // Await all parallel operations
        t.members.addAll(membersDeferred.awaitAll())
        t.tasks.addAll(tasksDeferred.awaitAll())
//        t.teamHistory.addAll(historiesDeferred.awaitAll())
//        t.chat = chatDeferred.await()

        emit(t)
    }
}


fun getTeams(db: FirebaseFirestore, coroutineScope: CoroutineScope, loggedUser: Deferred<MemberDB>, isLoading: MutableState<Boolean>): Flow<List<TeamDB>> = callbackFlow {
    //Stato di caricamento dati dal db
    isLoading.value = true
    // flow which provide in his lambda with the receiver a couple of method
    // try send that produce a value
    // await close so if nobody more interested in the flow perform an action(cancel subsription)
    val listener = db.collection("Team").addSnapshotListener { r, e ->
        if(r!=null) {
            val teams = mutableListOf<TeamDB>()
            val teamsJobs = r.map { entry ->
                this.async {
                    val membersId = entry.get("members") as List<String>
                    if (membersId.contains(loggedUser.await().id)) {
                        val t = TeamDB()
                        t.id = entry.id
                        t.name = entry.getString("name") ?: ""
                        t.description = entry.getString("description") ?: ""
                        t.image = Uri.EMPTY
                        t.creationDate = entry.getTimestamp("creationDate") ?.let { parseToLocalDate(it.toDate(),parseReturnType.DATE) } as LocalDate
                        val membersJobs = membersId.map { memberId ->
                            getMemberById(db,coroutineScope,memberId)
                        }
//                        val tasksId = entry.get("tasks") as List<String>
//                        val tasksJobs = tasksId.map { taskId ->
//                            getTaskById(db,coroutineScope,taskId)
//                        }
//                        tasksJobs.awaitAll().forEach { task ->
//                            t.tasks.add(task)
//                        }
//                        val teamHistories =  entry.get("teamHistory") as List<String>
//                        val historiesJobs = teamHistories.map { historyId->
//                            getHistoryById(db,coroutineScope,historyId)
//                        }
//                        val chatId: String = entry.getString("chat") ?: ""
//                        t.chat = getChatById(db,coroutineScope,chatId).await()
                        membersJobs.awaitAll().forEach { m ->
                            t.members.add(m)
                        }
//                        historiesJobs.awaitAll().forEach { h->
//                            t.teamHistory.add(h)
//                        }
                        t
                    } else null
                }
            }
            this.launch {
                teamsJobs.awaitAll().forEach {
                    if (it != null)
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

fun getMembersExcludingLoggedUser(teams: List<TeamDB>, loggedUser: MemberDB): List<MemberDB> {
    val members = mutableSetOf<MemberDB>()
    teams.forEach { team ->
        team.members.filter { it.id != loggedUser.id }.forEach { member ->
            members.add(member)
        }
    }
    return members.toList()
}

fun getAllTeamsMembersHome(db: FirebaseFirestore, coroutineScope: CoroutineScope, loggedUser: Deferred<MemberDB>, isLoading: MutableState<Boolean>): Flow<List<MemberDB>> = callbackFlow {
    val teamsFlow = getTeams(db, coroutineScope, loggedUser, isLoading)
    coroutineScope.launch {
        teamsFlow.collect { teams ->
            val members = getMembersExcludingLoggedUser(teams, loggedUser.await())
            trySend(members).isSuccess
        }
    }
    awaitClose {}
}