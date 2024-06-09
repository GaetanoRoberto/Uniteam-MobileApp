package it.polito.uniteam.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.messageStatus
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

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