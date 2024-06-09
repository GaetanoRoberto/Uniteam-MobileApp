package it.polito.uniteam.firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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