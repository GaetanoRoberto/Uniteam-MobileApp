package it.polito.uniteam.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.polito.uniteam.classes.FileDBFinal
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun deleteComment(db: FirebaseFirestore, commentId: String, taskId: String) {
    val commentRef = db.collection("Comment").document(commentId)
    val taskRef = db.collection("Task").document(taskId)
    db.runTransaction { transaction ->
        // Delete the comment document
        transaction.delete(commentRef)
        // Update the task with the deleted comment ID
        transaction.update(taskRef, "taskComments", FieldValue.arrayRemove(commentId))
    }
}

fun deleteFile(db: FirebaseFirestore, file: FileDBFinal, taskId: String) {
    val fileHistoryData = mapOf(
        "comment" to "File ${file.filename} deleted.",
        "date" to Timestamp(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())),
        "user" to file.user
    )
    val fileRef = db.collection("File").document(file.id)
    val historyRef = db.collection("History").document()
    val taskRef = db.collection("Task").document(taskId)
    val storage = FirebaseStorage.getInstance()
    val fileDataRef = storage.reference.child("files/${file.id + "." + file.filename.split(".")[1]}")
    db.runTransaction { transaction ->
        // Delete the file document
        transaction.delete(fileRef)
        // Update the task with the deleted file ID in taskHistory
        transaction.update(taskRef, "taskFiles", FieldValue.arrayRemove(file.id))
        // Set the new history document
        transaction.set(historyRef, fileHistoryData)
        // Get the new history document ID
        val fileHistoryId = historyRef.id
        // Update the task with the new history ID
        transaction.update(taskRef, "taskHistory", FieldValue.arrayUnion(fileHistoryId))
        // Delete The Actual File
        fileDataRef.delete()
    }
}

fun deleteTask(db: FirebaseFirestore, files:List<FileDBFinal>, taskId: String, teamId: String) {
    val taskRef = db.collection("Task").document(taskId)
    db.runTransaction { transaction ->
        val task = transaction.get(taskRef)
        // delete comments, files and history related to the task
        val taskComments = task.get("taskComments") as List<String>
        taskComments.forEach { comment->
            val commentRef = db.collection("Comment").document(comment)
            transaction.delete(commentRef)
        }
        val taskFiles = task.get("taskFiles") as List<String>
        // db file entry
        taskFiles.forEach { file ->
            val fileRef = db.collection("File").document(file)
            transaction.delete(fileRef)
        }
        // db fileStorage
        files.filter { taskFiles.contains(it.id) }.forEach { file ->
            deleteFile(db, file, taskId)
        }
        val taskHistory = task.get("taskHistory") as List<String>
        taskHistory.forEach { history ->
            val historyRef = db.collection("History").document(history)
            transaction.delete(historyRef)
        }
        // remove the task from the team
        val teamRef = db.collection("Team").document(teamId)
        transaction.update(teamRef,"tasks",FieldValue.arrayRemove(taskId))
        // delete the task
        transaction.delete(taskRef)
    }
}

fun deleteTeam(db: FirebaseFirestore, teamId: String, files:List<FileDBFinal>, /*members: List<String>,*/ user: String) {

    db.runTransaction { transaction ->
        val teamRef = db.collection("Team").document(teamId)
        // Get the team data
        val team = transaction.get(teamRef)
        val chatId = team.getString("chat") ?: ""
        val chatRef = db.collection("Chat").document(chatId)
        val chat = transaction.get(chatRef)
        val messagesId = chat.get("messages") as? List<String> ?: listOf()
        val tasks = team.get("tasks") as List<String>
        val members = team.get("members") as List<String>
        val historyRef = db.collection("History").document()

        // Remove members' teamsInfo
        val membersToRemove = members.map {
            val memberRef = db.collection("Member").document(it)
            val member = transaction.get(memberRef)
            Pair(memberRef,member)
        }
        membersToRemove.forEach { (memberRef,member) ->
            val teamsInfo = member.get("teamsInfo") as? List<Map<String, Any>> ?: emptyList()

            // Filter out the teamsInfo entry associated with the teamId
            val updatedTeamsInfo = teamsInfo.filterNot { it["teamId"] == teamId }

            transaction.update(memberRef, "teamsInfo", updatedTeamsInfo)
        }
        // Delete chat and messages
        messagesId.forEach { messageId ->
            val messageRef = db.collection("Message").document(messageId)
            transaction.delete(messageRef)
        }
        transaction.delete(chatRef)

        // Delete tasks and related files
        tasks.forEach { taskId ->
            deleteTask(db, files, taskId, teamId)
        }
        // Add to history
        val historyData = mapOf(
            "comment" to "Team ${team.get("name")} deleted.",
            "date" to Timestamp(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "user" to user,
            "oldMembers" to members
        )
        transaction.set(historyRef, historyData)
        // Delete the team
        transaction.delete(teamRef)
        // delete teamImage
        val storage = FirebaseStorage.getInstance()
        val teamImageRef = storage.reference.child("images/${teamId}.jpg")
        teamImageRef.delete()
    }.addOnSuccessListener {
        Log.d("DB", "Team deleted successfully")
    }.addOnFailureListener { e ->
        Log.w("DB", "Error deleting team", e)
    }
}