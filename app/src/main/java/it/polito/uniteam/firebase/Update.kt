package it.polito.uniteam.firebase
import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.Timestamp
import it.polito.uniteam.classes.TaskDBFinal
import com.google.firebase.storage.FirebaseStorage
import it.polito.uniteam.classes.CommentDBFinal
import it.polito.uniteam.classes.FileDBFinal
import it.polito.uniteam.classes.HistoryDBFinal
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

suspend fun markTeamMessageAsReadDB(db: FirebaseFirestore, memberId: String, messageId: String) {
    val messageRef = db.collection("Message").document(messageId)

    // Aggiorna il campo membersUnread rimuovendo il memberId
    messageRef.update("membersUnread", FieldValue.arrayRemove(memberId)).await()
}

suspend fun markUserMessageAsReadDB(db: FirebaseFirestore, messageId: String) {
    val messageRef = db.collection("Message").document(messageId)

    // Aggiorna il campo status a "READ"
    messageRef.update("status", "READ").await()
}

fun uploadImageToFirebase(fileName: String,fileUri: Uri = Uri.EMPTY) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val fileRef = storageRef.child("images/${fileName}.jpg")

    fileRef.putFile(fileUri)
        .addOnSuccessListener {
            Log.i("DB", "File Uploaded Successfully.")
        }
        .addOnFailureListener {
            Log.i("DB", "File Uploaded Failed.")
        }
}

fun scheduleTask(db: FirebaseFirestore, task: TaskDBFinal, scheduleDate: LocalDate, hoursToSchedule: Pair<Int, Int>, memberId: String) {
    val newSchedule = mapOf(
        "scheduleInfo" to mapOf(
            "member" to memberId,
            "scheduleDate" to Timestamp(Date.from(scheduleDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        ),
        "scheduleTime" to mapOf(
            "hours" to hoursToSchedule.first,
            "minutes" to hoursToSchedule.second
        )
    )

    val taskRef = db.collection("Task").document(task.id)

    db.runTransaction { transaction ->
        val taskDoc = transaction.get(taskRef)
        val schedules = taskDoc.get("schedules") as? List<Map<String, Map<String,Any>>>

        val updatedSchedules = if (schedules != null) {
            val existingScheduleIndex = schedules.indexOfFirst {
                Log.i("DB",it["scheduleInfo"]?.get("member").toString())
                Log.i("DB",it["scheduleInfo"]?.get("scheduleDate").toString())
                Log.i("DB",newSchedule["scheduleInfo"]?.get("scheduleDate").toString())
                it["scheduleInfo"]?.get("member") == memberId && it["scheduleInfo"]?.get("scheduleDate") == newSchedule["scheduleInfo"]?.get("scheduleDate")
            }
            if (existingScheduleIndex != -1) {
                val existingSchedule = schedules[existingScheduleIndex]
                val existingHours = (existingSchedule["scheduleTime"]?.get("hours") as Long).toInt()
                val existingMinutes = (existingSchedule["scheduleTime"]?.get("minutes") as Long).toInt()
                val newHours = existingHours + newSchedule["scheduleTime"]?.get("hours") as Int
                val newMinutes = existingMinutes + newSchedule["scheduleTime"]?.get("minutes") as Int

                val updatedSchedule = existingSchedule.toMutableMap()
                updatedSchedule["scheduleTime"] = mapOf("hours" to newHours, "minutes" to newMinutes)
                schedules.toMutableList().apply { set(existingScheduleIndex, updatedSchedule) }
            } else {
                schedules + newSchedule
            }
        } else {
            listOf(newSchedule)
        }

        transaction.update(taskRef, "schedules", updatedSchedules)
    }.addOnSuccessListener {
        Log.d("DB", "Task Updated Successfully.")
    }.addOnFailureListener { exception ->
        Log.d("DB", "Failed to Add Schedule: $exception")
    }
}



fun unscheduleTask(db: FirebaseFirestore, task: TaskDBFinal, scheduleDate: LocalDate, memberId: String) {
    val scheduleTime = task.schedules[Pair(memberId,scheduleDate)]!!
    val scheduleToRemove = mapOf(
        "scheduleInfo" to mapOf(
            "member" to memberId,
            "scheduleDate" to Timestamp(Date.from(scheduleDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        ),
        "scheduleTime" to mapOf(
            "hours" to scheduleTime.first,
            "minutes" to scheduleTime.second
        )
    )

    db.collection("Task").document(task.id)
        .update("schedules", FieldValue.arrayRemove(scheduleToRemove))
        .addOnSuccessListener {
            Log.d("DB","Task Unscheduled Successfully.")
        }
        .addOnFailureListener {
            Log.d("DB","Failed to Remove Schedule: $it")
        }
}

fun updateUserProfile(db: FirebaseFirestore, memberId: String, username: String, fullName: String, email: String, location: String, description: String, profileImage: Uri) {
    val fieldsToUpdate = mapOf(
        "username" to username,
        "fullName" to fullName,
        "email" to email,
        "location" to location,
        "description" to description
    )
    val memberRef = db.collection("Member").document(memberId)
    db.runTransaction { transaction ->
        uploadImageToFirebase(memberId, profileImage)
        transaction.update(memberRef, fieldsToUpdate)
    }.addOnSuccessListener {
        Log.d("DB", "Member Updated Successfully.")
    }.addOnFailureListener {
        Log.d("DB", "Failed to Update The Member: $it")
    }
}

fun updateTask(db: FirebaseFirestore, task: TaskDBFinal) {
    val fieldsToUpdate = mapOf(
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
        "members" to task.members
    )

    // here not care about comments, history, files (already added since edit)
    db.collection("Task").document(task.id).update(fieldsToUpdate)
}

fun updateComment(db: FirebaseFirestore, comment: CommentDBFinal, taskId: String) {
    val data = mapOf(
        "commentValue" to comment.commentValue,
        "date" to Timestamp(Date.from(comment.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        "user" to comment.user
    )

    db.collection("Comment").document(comment.id).update(data)
}