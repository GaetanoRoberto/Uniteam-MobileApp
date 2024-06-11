package it.polito.uniteam.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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

fun deleteFile(db: FirebaseFirestore, fileId: String, fileName: String, taskId: String) {
    val fileRef = db.collection("File").document(fileId)
    val taskRef = db.collection("Task").document(taskId)
    val storage = FirebaseStorage.getInstance()
    val fileDataRef = storage.reference.child("files/$fileName")
    db.runTransaction { transaction ->
        // Delete the file document
        transaction.delete(fileRef)
        // Update the task with the deleted file ID in taskHistory
        transaction.update(taskRef, "taskFiles", FieldValue.arrayRemove(fileId))
        // Delete The Actual File
        fileDataRef.delete()
    }
}
