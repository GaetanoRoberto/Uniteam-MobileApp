package it.polito.uniteam.firebase

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import it.polito.uniteam.classes.CommentDBFinal
import it.polito.uniteam.classes.FileDBFinal
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.classes.TaskDBFinal
import it.polito.uniteam.classes.TeamDBFinal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.time.ZoneId
import java.util.Date

suspend fun addTeamMessage(
    db: FirebaseFirestore,
    chatId: String,
    senderId: String,
    messageText: String,
    teamMembers: MutableList<String>
) {
    // Set membersUnread to all team members except the sender
    val membersUnread:MutableList<String> = teamMembers.filter { it != senderId }.toMutableList()

    val message = mapOf(
        //"id" to "",
        "senderId" to senderId,
        "message" to messageText,
        "creationDate" to Timestamp.now(),
        "membersUnread" to membersUnread,
        "status" to null
    )

    // Add the message to the "Messages" collection
    val messageRef = db.collection("Message").add(message).await()
    val messageId = messageRef.id

    // Update the message ID in the created message
    //db.collection("Message").document(messageId).update("id", messageId).await()

    // Update the chat to include the new message ID
    val chatRef = db.collection("Chat").document(chatId)
    chatRef.update("messages", FieldValue.arrayUnion(messageId)).await()
}
suspend fun addMessage(
    db: FirebaseFirestore,
    chatId: String,
    senderId: String,
    //receiverId: String,
    messageText: String
) {
    val message = mapOf(
        //"id" to "",
        "senderId" to senderId,
        "message" to messageText,
        "creationDate" to Timestamp.now(),
        "status" to "UNREAD",
        "membersUnread" to mutableListOf<String>()
    )

    // Add the message to the "Messages" collection
    val messageRef = db.collection("Message").add(message).await()
    val messageId = messageRef.id

    // Update the message ID in the created message
    //db.collection("Message").document(messageId).update("id", messageId).await()

    // Update the chat to include the new message ID
    val chatRef = db.collection("Chat").document(chatId)
    chatRef.update("messages", FieldValue.arrayUnion(messageId)).await()
}

fun addComment(db: FirebaseFirestore, comment: CommentDBFinal, taskId: String) {
    val data = mapOf(
        "commentValue" to comment.commentValue,
        "date" to Timestamp(Date.from(comment.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        "user" to comment.user
    )

    val commentRef = db.collection("Comment").document()
    val taskRef = db.collection("Task").document(taskId)
    db.runTransaction { transaction ->
        // Set the new comment document
        transaction.set(commentRef, data)
        // Get the new comment document ID
        val commentId = commentRef.id
        // Update the task with the new comment ID
        transaction.update(taskRef, "taskComments", FieldValue.arrayUnion(commentId))
    }
}

suspend fun uploadFile(context: Context, fileUri: Uri, fileStorageName: String) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        return // Return and wait for the permission to be granted
    }
    val storage = FirebaseStorage.getInstance()
    val fileRef = storage.reference.child("files/$fileStorageName")

    // Create the notification channel
    createNotificationChannel(context, "Upload Channel", "Channel for file upload notifications", "UPLOAD_CHANNEL")

    // Initialize the NotificationManager and NotificationCompat.Builder
    val notificationManager = NotificationManagerCompat.from(context)
    val builder = NotificationCompat.Builder(context, "UPLOAD_CHANNEL")
        .setContentTitle("Uploading File")
        .setContentText("Upload in progress")
        .setSmallIcon(android.R.drawable.stat_sys_upload)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setProgress(0, 0, true) // Indeterminate progress initially

    // Start the notification
    notificationManager.notify(2, builder.build())

    try {
        // Get the input stream from the URI
        val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
        val streamSize = inputStream?.available()?.toLong() ?: 0L
        val buffer = ByteArray(1024 * 4) // 4 KB buffer
        var totalBytesRead = 0L

        // Upload the file with progress updates
        val uploadTask: UploadTask = fileRef.putStream(inputStream!!)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val bytesTransferred = taskSnapshot.bytesTransferred
            totalBytesRead += bytesTransferred
            val progress = (100 * totalBytesRead / streamSize).toInt()
            builder.setProgress(100, progress, false)
            notificationManager.notify(2, builder.build())
        }.await()

        // Upload complete, update notification
        builder.setContentText("Upload complete")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
        notificationManager.notify(2, builder.build())

    } catch (e: Exception) {
        e.printStackTrace()

        // Update notification for failure
        builder.setContentText("Upload failed")
            .setProgress(0, 0, false)
            .setOngoing(false)
        notificationManager.notify(2, builder.build())
    }
}

fun addFile(db: FirebaseFirestore, coroutineScope: CoroutineScope, context: Context, file: FileDBFinal, taskId: String) {
    val fileData = mapOf(
        "filename" to file.filename,
        "date" to Timestamp(Date.from(file.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        "user" to file.user
    )
    val fileHistoryData = mapOf(
        "comment" to "File ${file.filename} uploaded.",
        "date" to Timestamp(Date.from(file.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        "user" to file.user
    )
    val fileRef = db.collection("File").document()
    val historyRef = db.collection("History").document()
    val taskRef = db.collection("Task").document(taskId)
    db.runTransaction { transaction ->
        // Set the new file document
        transaction.set(fileRef, fileData)
        // Get the new file document ID
        val fileId = fileRef.id
        // Update the task with the new file ID
        transaction.update(taskRef, "taskFiles", FieldValue.arrayUnion(fileId))
        // Set the new history document
        transaction.set(historyRef, fileHistoryData)
        // Get the new history document ID
        val fileHistoryId = historyRef.id
        // Update the task with the new history ID
        transaction.update(taskRef, "taskHistory", FieldValue.arrayUnion(fileHistoryId))
        // upload the corresponding file
        coroutineScope.launch {
            uploadFile(context,file.uri,fileId + "." + file.filename.split(".")[1])
        }
    }
}

fun addTaskHistories(db: FirebaseFirestore, histories: List<HistoryDBFinal>, taskId: String) {
    val taskRef = db.collection("Task").document(taskId)
    db.runTransaction { transaction ->
        histories.forEach { history ->
            if(history.id.toIntOrNull() != null) {
                // if parse successfully, int ids so entry to add into the db
                val data = mapOf(
                    "comment" to history.comment,
                    "date" to Timestamp(
                        Date.from(
                            history.date.atZone(ZoneId.systemDefault()).toInstant()
                        )
                    ),
                    "user" to history.user
                )
                val historyRef = db.collection("History").document()
                // Set the new history document
                transaction.set(historyRef, data)
                // Get the new history document ID
                val historyId = historyRef.id
                // Update the task with the new history ID
                transaction.update(taskRef, "taskHistory", FieldValue.arrayUnion(historyId))
            }
        }
    }
}

fun addTask(db: FirebaseFirestore, coroutineScope: CoroutineScope, context: Context, teamId: String, task: TaskDBFinal, comments: List<CommentDBFinal>, files: List<FileDBFinal>, histories: List<HistoryDBFinal>) {
    val fieldsToAdd = mapOf(
        "name" to task.name,
        "description" to task.description,
        "category" to task.category,
        "priority" to task.priority,
        "deadline" to Timestamp(Date.from(task.deadline.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        "estimatedTime" to task.estimatedTime,
        "spentTime" to task.spentTime.map { entry ->
            hashMapOf(
                "member" to entry.key,
                "spentTime" to hashMapOf(
                    "hours" to entry.value.first,
                    "minutes" to entry.value.second
                )
            )
        },
        "repetition" to task.repetition,
        "status" to task.status,
        "creationDate" to Timestamp(Date.from(task.creationDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        "members" to task.members,
        "schedules" to emptyList<Any>(),
        "taskComments" to emptyList<String>(),
        "taskFiles" to emptyList<String>(),
        "taskHistory" to emptyList<String>()
    )
    val taskRef = db.collection("Task").document()
    val teamRef = db.collection("Team").document(teamId)
    db.runTransaction { transaction ->
        transaction.set(taskRef,fieldsToAdd)
        // Add Comments
        comments.forEach { comment ->
            val data = mapOf(
                "commentValue" to comment.commentValue,
                "date" to Timestamp(Date.from(comment.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                "user" to comment.user
            )

            val commentRef = db.collection("Comment").document()
            // Set the new comment document
            transaction.set(commentRef, data)
            // Get the new comment document ID
            val commentId = commentRef.id
            // Update the task with the new comment ID
            transaction.update(taskRef, "taskComments", FieldValue.arrayUnion(commentId))
        }
        // Add files
        files.forEach { file ->
            val fileData = mapOf(
                "filename" to file.filename,
                "date" to Timestamp(Date.from(file.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                "user" to file.user
            )
            val fileHistoryData = mapOf(
                "comment" to "File ${file.filename} uploaded.",
                "date" to Timestamp(Date.from(file.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                "user" to file.user
            )
            val fileRef = db.collection("File").document()
            val historyRef = db.collection("History").document()
            // Set the new file document
            transaction.set(fileRef, fileData)
            // Get the new file document ID
            val fileId = fileRef.id
            // Update the task with the new file ID
            transaction.update(taskRef, "taskFiles", FieldValue.arrayUnion(fileId))
            // Set the new history document
            transaction.set(historyRef, fileHistoryData)
            // Get the new history document ID
            val fileHistoryId = historyRef.id
            // Update the task with the new history ID
            transaction.update(taskRef, "taskHistory", FieldValue.arrayUnion(fileHistoryId))
            // upload the corresponding file
            coroutineScope.launch {
                uploadFile(context,file.uri,fileId + "." + file.filename.split(".")[1])
            }
        }
        // Add histories
        histories.forEach { history ->
            if(history.id.toIntOrNull() != null) {
                // if parse successfully, int ids so entry to add into the db
                val data = mapOf(
                    "comment" to history.comment,
                    "date" to Timestamp(Date.from(history.date.atZone(ZoneId.systemDefault()).toInstant())),
                    "user" to history.user
                )
                val historyRef = db.collection("History").document()
                // Set the new history document
                transaction.set(historyRef, data)
                // Get the new history document ID
                val historyId = historyRef.id
                // Update the task with the new history ID
                transaction.update(taskRef, "taskHistory", FieldValue.arrayUnion(historyId))
            }
        }
        // link task to team
        transaction.update(teamRef, "tasks", FieldValue.arrayUnion(taskRef.id))
    }
}

fun createTeam(db: FirebaseFirestore, team: TeamDBFinal, memberInfo : MemberTeamInfo, history: HistoryDBFinal) {

    db.runTransaction { transaction ->
        val teamData = mapOf(
            "name" to team.name,
            "description" to team.description,
            "creationDate" to Timestamp.now(),
            "members" to team.members,
            "tasks" to team.tasks,
            "teamHistory" to team.teamHistory
        )

        val teamRef = db.collection("Team").document()
        transaction.set(teamRef, teamData)
        team.id = teamRef.id


        // Create the chat document for the team
        val chatRef = db.collection("Chat").document()
        val chatData = mapOf(
            "teamId" to teamRef.id,
            "sender" to null,
            "receiver" to null,
            "messages" to mutableListOf<String>()
        )
        transaction.set(chatRef, chatData)

        // Update the team document with the chat ID
        transaction.update(teamRef, "chat", chatRef.id)

        //ADD TEAM HISTORY
        val historyDB = mapOf(
            "comment" to history.comment,
            "date" to Timestamp.now(),
            "user" to history.user
        )
        val histRef = db.collection("History").document()
        transaction.set(histRef, historyDB)
        history.id = histRef.id

        // Update the team document with the history ID
        transaction.update(teamRef, "teamHistory", FieldValue.arrayUnion(histRef.id))


        team.members.forEach { memberId ->
            val memberRef = db.collection("Member").document(memberId)
            //ADD TEAM INFO TO MEMBER
            val teamsInfo = mapOf(
                "permissionrole" to memberInfo.permissionrole,
                "role" to memberInfo.role,
                "teamId" to teamRef.id,
                "weeklyAvailabilityHours" to mapOf(
                    "hours" to memberInfo.weeklyAvailabilityHours.first,
                    "minutes" to memberInfo.weeklyAvailabilityHours.second
                ),
                "weeklyAvailabilityTimes" to memberInfo.weeklyAvailabilityTimes
            )
            transaction.update(memberRef, "teamsInfo", FieldValue.arrayUnion(teamsInfo))
        }

        uploadImageToFirebase(teamRef.id, team.image)
    }.addOnSuccessListener {
        Log.d("DB", "Team created with ID: ${team.id}")
    }.addOnFailureListener { e ->
        Log.w("DB", "Error adding team", e)
    }
}