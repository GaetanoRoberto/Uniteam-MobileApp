package it.polito.uniteam.firebase

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
    }
}