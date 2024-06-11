package it.polito.uniteam.firebase
import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.Timestamp
import it.polito.uniteam.classes.TaskDBFinal
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import it.polito.uniteam.R
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

fun scheduleTask(db: FirebaseFirestore, task: TaskDBFinal, scheduleDate: LocalDate, hoursToSchedule: Pair<Int,Int>, memberId: String) {
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

    db.collection("Task").document(task.id)
        .update("schedules", FieldValue.arrayUnion(newSchedule))
        .addOnSuccessListener {
            Log.d("DB","Task Updated Successfully.")
        }
        .addOnFailureListener {
            Log.d("DB","Failed to Add Schedule: $it")
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