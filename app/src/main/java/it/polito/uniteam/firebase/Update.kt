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
import it.polito.uniteam.classes.HistoryDBFinal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
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

fun uploadImageToFirebase(db: FirebaseFirestore, fileName: String,fileUri: Uri = Uri.EMPTY, isMember: Boolean = true) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val fileRef = storageRef.child("images/${fileName}.jpg")

    if (fileUri != Uri.EMPTY) {
        fileRef.putFile(fileUri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val newPhotoUri = uri.toString()

                // Start the transaction after getting the download URL
                db.runTransaction { transaction ->
                    if (isMember) {
                        val memberRef = db.collection("Member").document(fileName)
                        val snapshot = transaction.get(memberRef)
                        val updatedMember = snapshot.data?.toMutableMap() ?: mutableMapOf()
                        updatedMember["image"] = newPhotoUri
                        transaction.set(memberRef, updatedMember)
                    } else {
                        val teamRef = db.collection("Team").document(fileName)
                        val snapshot = transaction.get(teamRef)
                        val updatedTeam = snapshot.data?.toMutableMap() ?: mutableMapOf()
                        updatedTeam["image"] = newPhotoUri
                        transaction.set(teamRef, updatedTeam)
                    }
                }.addOnSuccessListener {
                    Log.i("DB", "Image Updated Successfully.")
                }.addOnFailureListener {
                    Log.i("DB", "Image Update Failed.")
                }
            }.addOnFailureListener {
                Log.i("DB", "Failed to get download URL.")
            }
        }.addOnFailureListener {
            Log.i("DB", "Image Upload Failed.")
        }
    } else {
        // delete image case
        fileRef.delete()
        db.runTransaction { transaction ->
            if (isMember) {
                val memberRef = db.collection("Member").document(fileName)
                val snapshot = transaction.get(memberRef)
                val updatedMember = snapshot.data?.toMutableMap() ?: mutableMapOf()
                updatedMember["image"] = ""
                transaction.set(memberRef, updatedMember)
            } else {
                val teamRef = db.collection("Team").document(fileName)
                val snapshot = transaction.get(teamRef)
                val updatedTeam = snapshot.data?.toMutableMap() ?: mutableMapOf()
                updatedTeam["image"] = ""
                transaction.set(teamRef, updatedTeam)
            }
        }.addOnSuccessListener {
            Log.i("DB", "Image Updated Successfully.")
        }.addOnFailureListener {
            Log.i("DB", "Image Update Failed.")
        }
    }
}

fun compareDatesByDayMonthYear(date1: Date, date2: Date): Int {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()

    cal1.time = date1
    cal2.time = date2

    val year1 = cal1.get(Calendar.YEAR)
    val month1 = cal1.get(Calendar.MONTH)
    val day1 = cal1.get(Calendar.DAY_OF_MONTH)

    val year2 = cal2.get(Calendar.YEAR)
    val month2 = cal2.get(Calendar.MONTH)
    val day2 = cal2.get(Calendar.DAY_OF_MONTH)

    return when {
        year1 != year2 -> year1.compareTo(year2)
        month1 != month2 -> month1.compareTo(month2)
        else -> day1.compareTo(day2)
    }
}

fun scheduleTask(db: FirebaseFirestore, task: TaskDBFinal, scheduleDate: LocalDate, hoursToSchedule: Pair<Int, Int>, memberId: String) {
    val scheduleDateToDate = Timestamp(Date.from(scheduleDate.atStartOfDay(ZoneId.systemDefault()).toInstant())).toDate()
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
                Log.i("DB",(it["scheduleInfo"]?.get("scheduleDate")as Timestamp).toDate().toString())
                Log.i("DB",scheduleDateToDate.toString())
                it["scheduleInfo"]?.get("member") == memberId && (compareDatesByDayMonthYear((it["scheduleInfo"]?.get("scheduleDate")as Timestamp).toDate(),scheduleDateToDate) == 0)
            }
            if (existingScheduleIndex != -1) {
                val existingSchedule = schedules[existingScheduleIndex]
                val existingHours = (existingSchedule["scheduleTime"]?.get("hours") as Long).toInt()
                val existingMinutes = (existingSchedule["scheduleTime"]?.get("minutes") as Long).toInt()
                val newHours = newSchedule["scheduleTime"]?.get("hours") as Int
                val newMinutes = newSchedule["scheduleTime"]?.get("minutes") as Int
                val totalMinutes = existingMinutes + newMinutes
                val minutesOverflow = totalMinutes / 60
                val minutes = totalMinutes % 60
                val totalHours = existingHours + newHours + minutesOverflow
                val updatedSchedule = existingSchedule.toMutableMap()
                updatedSchedule["scheduleTime"] = mapOf("hours" to totalHours, "minutes" to minutes)
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
    val scheduleDateToDate = Timestamp(Date.from(scheduleDate.atStartOfDay(ZoneId.systemDefault()).toInstant())).toDate()

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

    val taskRef = db.collection("Task").document(task.id)

    db.runTransaction { transaction ->
        val taskDoc = transaction.get(taskRef)
        val schedules = taskDoc.get("schedules") as? List<Map<String, Map<String,Any>>>

        val updatedSchedules = schedules?.filter {
            Log.i("DB",(it["scheduleInfo"]?.get("scheduleDate")as Timestamp).toDate().toString())
            Log.i("DB",scheduleDateToDate.toString())
            !(it["scheduleInfo"]?.get("member") == memberId && (compareDatesByDayMonthYear((it["scheduleInfo"]?.get("scheduleDate")as Timestamp).toDate(),scheduleDateToDate) == 0))
        }

        transaction.update(taskRef, "schedules", updatedSchedules)
    }.addOnSuccessListener {
        Log.d("DB", "Task Updated Successfully.")
    }.addOnFailureListener { exception ->
        Log.d("DB", "Failed to Add Schedule: $exception")
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
        uploadImageToFirebase(db, memberId, profileImage)
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

fun updateTeam(db: FirebaseFirestore, teamId:String, teamName: String, teamDescription:String, teamImage: Uri, teamMembers:List<String>, teamHistory : List<HistoryDBFinal>) {

    db.runTransaction { transaction ->
        val teamRef = db.collection("Team").document(teamId)
        val teamDoc = transaction.get(teamRef)

        val oldTeamMembers = teamDoc.get("members") as List<String>
        val teamMembersToRemove = oldTeamMembers.filter { !teamMembers.contains(it) }

        teamMembersToRemove.forEach { memberId ->
            val memberRef = db.collection("Member").document(memberId)
            val historyRef = db.collection("History").document()
            val memberDoc = transaction.get(memberRef)
            transaction.update(teamRef, "members", FieldValue.arrayRemove(memberId))

            val taskIds = teamDoc.get("tasks") as? List<String> ?: emptyList()
            taskIds.forEach { taskId ->
                val taskRef = db.collection("Task").document(taskId)
                transaction.update(taskRef, "members", FieldValue.arrayRemove(memberId))
            }

            if (memberDoc.exists()) {
                val teamsInfo = memberDoc.get("teamsInfo") as? MutableList<Map<String, Any>>
                val teamInfoToRemove = teamsInfo?.find { it["teamId"] == teamId }
                transaction.update(memberRef, "teamsInfo", FieldValue.arrayRemove(teamInfoToRemove))
            }

        }
        transaction.update(teamRef, "name",teamName)
        transaction.update(teamRef, "description",teamDescription)
        uploadImageToFirebase(db, teamId, teamImage, isMember = false)
        teamHistory.forEach { history ->
            if(history.id.toIntOrNull() != null) {
                val historyDB = mapOf(
                    "comment" to history.comment,
                    "date" to Timestamp.now(),
                    "user" to history.user
                )
                val historyRef = db.collection("History").document()
                transaction.set(historyRef, historyDB)
                // Update the team document with the history ID
                transaction.update(teamRef, "teamHistory", FieldValue.arrayUnion(historyRef.id))
            }
        }
    }.addOnSuccessListener {
        Log.d("DB", "Team updated successfully")
    }.addOnFailureListener { e ->
        Log.w("DB", "Error updating team", e)
    }
}
fun updateTaskAssignee(db: FirebaseFirestore, taskId: String, members: List<String>, loggedUser: String, comment: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.runTransaction { transaction ->
        val taskRef = db.collection("Task").document(taskId)
        transaction.update(taskRef, "members", members)

        val history = mapOf(
            "comment" to comment,
            "date" to Timestamp.now(),
            "user" to loggedUser
        )
        val historyRef = db.collection("History").document()
        transaction.set(historyRef, history)

        transaction.update(taskRef, "taskHistory", FieldValue.arrayUnion(historyRef.id))
    }.addOnSuccessListener {
        onSuccess()
        Log.d("DB","Task updated successfully")
    }.addOnFailureListener { onFailure(it) }
}

fun joinTeam(db: FirebaseFirestore, memberId: String, teamId: String, newRole: String, newHours: Number, newMinutes: Number, newTimes: Number, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val teamRef = db.collection("Team").document(teamId)
    val memberRef = db.collection("Member").document(memberId)

    teamRef.get().addOnSuccessListener { documentSnapshot ->
        val members = documentSnapshot.get("members") as List<String>

        db.runTransaction { transaction ->
            transaction.update(teamRef, "members", FieldValue.arrayUnion(memberId))

            val newTeamInfo = mapOf(
                "permissionrole" to "USER",
                "role" to newRole,
                "teamId" to teamId,
                "weeklyAvailabilityHours" to mapOf("hours" to newHours, "minutes" to newMinutes),
                "weeklyAvailabilityTimes" to newTimes
            )
            transaction.update(memberRef, "teamsInfo", FieldValue.arrayUnion(newTeamInfo))

            val history = mapOf(
                "comment" to "Team Joined.",
                "date" to Timestamp.now(),
                "user" to memberId
            )
            val historyRef = db.collection("History").document()
            transaction.set(historyRef, history)

            transaction.update(teamRef, "teamHistory", FieldValue.arrayUnion(historyRef.id))

            for (teamMember in members) {
                if (teamMember != memberId) {
                    db.collection("Chat")
                        .whereIn("sender", listOf(memberId, teamMember))
                        .whereIn("receiver", listOf(memberId, teamMember))
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.documents.isEmpty()) {
                                val newChat = mapOf(
                                    "messages" to emptyList<String>(),
                                    "receiver" to teamMember,
                                    "sender" to memberId,
                                    "teamId" to null
                                )
                                db.collection("Chat").add(newChat)
                            }
                        }
                }
            }
        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }
}

fun changeAdminRole(db: FirebaseFirestore, loggedMemberId: String, memberId: String, teamId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val teamRef = db.collection("Team").document(teamId)
    val memberRef = db.collection("Member").document(memberId)
    val loggedMemberRef = db.collection("Member").document(loggedMemberId)
    val historyRef = db.collection("History").document()

    db.runTransaction { transaction ->
        val teamDoc = transaction.get(teamRef)
        val memberDoc = transaction.get(memberRef)
        val loggedMemberDoc = transaction.get(loggedMemberRef)

        if (!teamDoc.exists()) {
            onFailure(Exception("Team not found"))
        }

        if (memberDoc.exists()) {
            val teamsInfo = memberDoc.get("teamsInfo") as? MutableList<Map<String, Any>>
            teamsInfo?.let {
                val updatedTeamsInfo = it.map { teamInfo ->
                    if (teamInfo["teamId"] == teamId) {
                        teamInfo.toMutableMap().apply { put("permissionrole", "ADMIN") }
                    } else {
                        teamInfo
                    }
                }
                transaction.update(memberRef, "teamsInfo", updatedTeamsInfo)
            }
        }
        val newAdminHistory = mapOf(
            "comment" to "New team admin.",
            "date" to Timestamp.now(),
            "user" to memberId
        )
        transaction.set(historyRef, newAdminHistory)

        transaction.update(teamRef, "teamHistory", FieldValue.arrayUnion(historyRef.id))

        // Start of leaveTeam part
        transaction.update(teamRef, "members", FieldValue.arrayRemove(loggedMemberId))

        val taskIds = teamDoc.get("tasks") as? List<String> ?: emptyList()
        taskIds.forEach { taskId ->
            val taskRef = db.collection("Task").document(taskId)
            transaction.update(taskRef, "members", FieldValue.arrayRemove(loggedMemberId))
        }

        if (loggedMemberDoc.exists()) {
            val teamsInfo = loggedMemberDoc.get("teamsInfo") as? MutableList<Map<String, Any>>
            val teamInfoToRemove = teamsInfo?.find { it["teamId"] == teamId }
            transaction.update(loggedMemberRef, "teamsInfo", FieldValue.arrayRemove(teamInfoToRemove))
        }

        val leaveHistory = mapOf(
            "comment" to "Team Left.",
            "date" to Timestamp.now(),
            "user" to loggedMemberId
        )
        val leaveHistoryRef = db.collection("History").document()
        transaction.set(leaveHistoryRef, leaveHistory)

        transaction.update(teamRef, "teamHistory", FieldValue.arrayUnion(leaveHistoryRef.id))
    }.addOnSuccessListener {
        onSuccess()
        Log.d("DB","Admin role changed successfully")
    }.addOnFailureListener { onFailure(it) }
}

fun leaveTeam(db: FirebaseFirestore, memberId: String, teamId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val teamRef = db.collection("Team").document(teamId)
    val memberRef = db.collection("Member").document(memberId)
    val historyRef = db.collection("History").document()

    db.runTransaction { transaction ->
        val teamDoc = transaction.get(teamRef)
        val memberDoc = transaction.get(memberRef)

        if (!teamDoc.exists()) {
            onFailure(Exception("Team not found"))
        }
        transaction.update(teamRef, "members", FieldValue.arrayRemove(memberId))

        val taskIds = teamDoc.get("tasks") as? List<String> ?: emptyList()
        taskIds.forEach { taskId ->
            val taskRef = db.collection("Task").document(taskId)
            transaction.update(taskRef, "members", FieldValue.arrayRemove(memberId))
        }

        if (memberDoc.exists()) {
            val teamsInfo = memberDoc.get("teamsInfo") as? MutableList<Map<String, Any>>
            val teamInfoToRemove = teamsInfo?.find { it["teamId"] == teamId }
            transaction.update(memberRef, "teamsInfo", FieldValue.arrayRemove(teamInfoToRemove))
        }

        val history = mapOf(
            "comment" to "Team Left.",
            "date" to Timestamp.now(),
            "user" to memberId
        )
        transaction.set(historyRef, history)

        transaction.update(teamRef, "teamHistory", FieldValue.arrayUnion(historyRef.id))
    }.addOnSuccessListener {
        onSuccess()
        Log.d("DB","Team left successfully")
    }.addOnFailureListener { e ->
        Log.d("DB","Error leaving team: ${e.message}")
    }
}

fun updateLoggedMemberTeamInfo(db: FirebaseFirestore, memberId: String, teamId: String, newRole: String, newHours: Number, newMinutes: Number, newTimes: Number) {
    val memberRef = db.collection("Member").document(memberId)

    db.runTransaction { transaction ->
        val document = transaction.get(memberRef)
        if (document.exists()) {
            val teamsInfo = document.get("teamsInfo") as? MutableList<Map<String, Any>>
            teamsInfo?.let {
                val updatedTeamsInfo = it.map { teamInfo ->
                    if (teamInfo["teamId"] == teamId) {
                        teamInfo.toMutableMap().apply {
                            put("role", newRole)
                            put("weeklyAvailabilityHours", mapOf("hours" to newHours, "minutes" to newMinutes))
                            put("weeklyAvailabilityTimes", newTimes)
                        }
                    } else {
                        teamInfo
                    }
                }
                transaction.update(memberRef, "teamsInfo", updatedTeamsInfo)
            }
        } else {
            Log.d("DB","No such document")
        }
    }.addOnSuccessListener {
        Log.d("DB","Member's team info updated successfully")
    }.addOnFailureListener { e ->
        Log.d("DB","Error fetching document: ${e.message}")
    }
}
