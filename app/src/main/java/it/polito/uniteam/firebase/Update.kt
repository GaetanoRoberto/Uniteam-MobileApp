package it.polito.uniteam.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.uniteam.classes.TaskDBFinal
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun scheduleTask(db: FirebaseFirestore, task: TaskDBFinal, scheduleDate: LocalDate, hoursToSchedule: Pair<Int,Int>) {
    val newSchedule = mapOf(
        "scheduleInfo" to mapOf(
            "member" to "2nm8PdGbk5CaROcyWjq7", //TODO hardcoded
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

fun unscheduleTask(db: FirebaseFirestore, task: TaskDBFinal, scheduleDate: LocalDate) {
    val scheduleTime = task.schedules[Pair("2nm8PdGbk5CaROcyWjq7",scheduleDate)]!! //TODO hardcoded
    val scheduleToRemove = mapOf(
        "scheduleInfo" to mapOf(
            "member" to "2nm8PdGbk5CaROcyWjq7", //TODO hardcoded
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
