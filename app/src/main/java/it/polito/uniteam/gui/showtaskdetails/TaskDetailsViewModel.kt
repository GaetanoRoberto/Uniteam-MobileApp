package it.polito.uniteam.gui.showtaskdetails

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.Comment
import it.polito.uniteam.classes.File
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.isRepetition
import it.polito.uniteam.gui.calendar.DummyDataProvider
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@SuppressLint("MutableCollectionMutableState")
class taskDetails : ViewModel() {

    var taskName by mutableStateOf("Task Name value")
        private set
    var taskError by mutableStateOf("")
        private set

    fun changeTaskName(s: String) {
        taskName = s
    }

    private fun checkTaskName() {
        if (taskName.isBlank())
            taskError = "Task name cannot be blank!"
        else
            taskError = ""
    }


    var description by mutableStateOf("Description value")
        private set
    var descriptionError by mutableStateOf("")
        private set

    fun changeDescription(s: String) {
        description = s
    }

    private fun checkDescription() {
        if (description.isBlank())
            descriptionError = "Task description cannot be blank!"
        else
            descriptionError = ""
    }


    var category by mutableStateOf(Category.NONE.toString())
        private set
    val categoryValues = Category.entries.map { it.toString() }
    var categoryError by mutableStateOf("")
        private set

    fun changeCategory(s: String) {
        category = s
    }

    private fun checkCategory() {
        if (category.isBlank())
            categoryError = "Task category cannot be blank!"
        else
            categoryError = ""
    }


    var priority by mutableStateOf(Priority.LOW.toString())
        private set
    val priorityValues = Priority.entries.map { it.toString() }
    var priorityError by mutableStateOf("")
        private set

    fun changePriority(s: String) {
        priority = s
    }

    var deadline by mutableStateOf(LocalDate.now().toString())
        private set
    var deadlineError by mutableStateOf("")
        private set

    fun changeDeadline(s: String) {
        deadline = s
    }

    private fun checkDeadline() {
        val dateFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        sdf.isLenient = false
        try {
            sdf.parse(deadline)
            deadlineError = ""
        } catch (e: Exception) {
            deadlineError = "Invalid date"
        }
    }


    var status by mutableStateOf(Status.TODO.toString())
        private set
    var stateError by mutableStateOf("")
        private set

    fun changeState(s: String) {
        status = s
    }

    val possibleStates =
        listOf(Status.TODO.toString(), Status.IN_PROGRESS.toString(), Status.COMPLETED.toString())


    var estimatedHours by mutableStateOf("0")
        private set
    var estimatedHoursError by mutableStateOf("")
        private set

    fun changeEstimatedHours(h: String) {
        estimatedHours = h
    }

    private fun checkEstimatedHours() {
        try {
            if (estimatedHours == "") {
                estimatedHoursError = "Estimated hours cannot be blank!"
            } else if (estimatedHours.toInt() <= 0)
                estimatedHoursError = "Estimated hours must be greater than 0"
            else
                estimatedHoursError = ""
        } catch (e: RuntimeException) {
            estimatedHoursError = "A Valid Integer Value Must Be Provided."
        }
    }

    var spentHours by mutableStateOf("1")
        private set
    var spentHoursError by mutableStateOf("")
        private set

    fun changeSpentHours(h: String) {
        spentHours = h
    }

    private fun checkSpentHours() {
        try {
            if (spentHours == "") {
                spentHoursError = "Estimated hours cannot be blank!"
            } else if (spentHours.toInt() < 0)
                spentHoursError = "Estimated hours must be greater or equal than 0"
            else
                spentHoursError = ""
        } catch (e: RuntimeException) {
            spentHoursError = "A Valid Integer Value Must Be Provided."
        }
    }

    var possibleMembers = mutableStateListOf<Member>(
        DummyDataProvider.member1,
        DummyDataProvider.member2,
        DummyDataProvider.member3,
        DummyDataProvider.member4,
        DummyDataProvider.member5,
        DummyDataProvider.member6
    )
    var members = mutableStateListOf<Member>()

    var openAssignDialog = mutableStateOf(false)
    var membersError by mutableStateOf("")
        private set

    fun addMembers(m: Member) {
        members.add(m)
        Log.i("diooo","member added")
    }

    fun removeMembers(m: Member) {
        members.remove(m)
    }

    private fun checkMembers() {
        if (members.size <= 0)
            membersError = "Almost a member should be assigned"
        else
            membersError = ""
    }


    var repeatable by mutableStateOf(Repetition.NONE.toString())
        private set
    val repeatableValues = Repetition.entries.map { it.toString() }

    fun changeRepetition(r: String) {
        if (r.isRepetition()) {
            repeatable = r
        }
    }

    fun hasChanged(oldValue: String, newValue: String): Boolean {
        return oldValue != newValue
    }

    fun handleHistory() {
        val entryToAdd: MutableList<History> = mutableListOf()
        val isEdit = taskNameBefore.isNotBlank()
        if (isEdit) {
            //general editing
            if(hasChanged(taskNameBefore,taskName) || hasChanged(descriptionBefore,description)
                || hasChanged(categoryBefore,category) || hasChanged(deadlineBefore,deadline)
                || hasChanged(estimateHoursBefore,estimatedHours) || hasChanged(spentHoursBefore,spentHours)
                || hasChanged(repeatableBefore,repeatable)) {
                entryToAdd.add(History(
                    comment = "Task Edited.",
                    date = LocalDate.now().toString(),
                    user = member
                ))
            }
            // members
            val removedComment = "Members removed: "
            var removedMembers = ""
            val addedComment = "Members Added: "
            var addedMembers = ""
            for (oldMember in membersBefore) {
                if(!members.contains(oldMember)) {
                    // deleted member
                    removedMembers += oldMember.username + " "
                }
            }
            for (member in members) {
                if(!membersBefore.contains(member)) {
                    // added member
                    addedMembers += member.username + " "
                }
            }
            if (removedMembers.isNotEmpty() && addedMembers.isNotEmpty()) {
                val comment = "Task " + removedComment + removedMembers + "\n" +
                        "Task " + addedComment + addedMembers + "\n"
                entryToAdd.add(History(
                    comment = comment,
                    date = LocalDate.now().toString(),
                    user = member
                ))
            } else if (removedMembers.isNotEmpty()) {
                val comment = "Task " + removedComment + removedMembers
                entryToAdd.add(History(
                    comment = comment,
                    date = LocalDate.now().toString(),
                    user = member
                ))
            } else if (addedMembers.isNotEmpty()) {
                val comment = "Task " + addedComment + addedMembers
                entryToAdd.add(History(
                    comment = comment,
                    date = LocalDate.now().toString(),
                    user = member
                ))
            }
            //status
            if (hasChanged(statusBefore,status)) {
                entryToAdd.add(History(
                    comment = "Task status changed from ${statusBefore} to ${status}.",
                    date = LocalDate.now().toString(),
                    user = member
                ))
            }
            //priority
            if (hasChanged(priorityBefore,priority)) {
                entryToAdd.add(History(
                    comment = "Task priority changed from ${priorityBefore} to ${priority}.",
                    date = LocalDate.now().toString(),
                    user = member
                ))
            }
            // add to history
            entryToAdd.forEach{ entry->
                history.add(entry)
            }
        }


    }

    fun validate() {
        checkTaskName()
        checkDescription()
        checkCategory()
        checkDeadline()
        checkEstimatedHours()
        checkSpentHours()
        //checkMembers()
    }

    var editing by mutableStateOf(false)
    fun changeEditing() {
        editing = !editing
    }

    // before states to cancel an edit
    var taskNameBefore = ""
    var descriptionBefore = ""
    var categoryBefore = ""
    var priorityBefore = ""
    var deadlineBefore = ""
    var estimateHoursBefore = ""
    var spentHoursBefore = ""
    var repeatableBefore = ""
    var statusBefore = ""
    var membersBefore = mutableListOf<Member>()

    fun enterEditingMode() {
        taskNameBefore = taskName
        descriptionBefore = description
        categoryBefore = category
        priorityBefore = priority
        deadlineBefore = deadline
        estimateHoursBefore = estimatedHours
        spentHoursBefore = spentHours
        repeatableBefore = repeatable
        statusBefore = status
        membersBefore = members.toMutableStateList()
    }

    fun cancelEdit() {
        taskName = taskNameBefore
        description = descriptionBefore
        category = categoryBefore
        priority = priorityBefore
        deadline = deadlineBefore
        estimatedHours = estimateHoursBefore
        spentHours = spentHoursBefore
        repeatable = repeatableBefore
        status = statusBefore
        members = membersBefore.toMutableStateList()
        taskError = ""
        descriptionError = ""
        categoryError = ""
        priorityError = ""
        deadlineError = ""
        estimatedHoursError = ""
        spentHoursError = ""
        membersError = ""
    }

    var scrollTaskDetails by mutableIntStateOf(0)
    var commentHistoryFileSelection by mutableStateOf("comments")
    fun changeCommentHistoryFileSelection(s: String) {
        if (s.trim().lowercase() == "comments") {
            commentHistoryFileSelection = "comments"
        } else if (s.trim().lowercase() == "files") {
            commentHistoryFileSelection = "files"

        } else if (s.trim().lowercase() == "history") {
            commentHistoryFileSelection = "history"

        }
        scrollTaskDetails = Int.MAX_VALUE
    }

    var localId by mutableIntStateOf(0)
    val member = DummyDataProvider.getDummyProfile()
    val dummyMembers = DummyDataProvider.getMembers()

    var comments = mutableStateListOf(
        Comment(++localId,dummyMembers[0], "Ciao", "2024-02-05", "18:31"),
        Comment(++localId,dummyMembers[1], "Ciao", "2024-02-05", "18:40"),
        Comment(++localId,dummyMembers[2], "Ciao", "2024-02-06", "18:31"),
        Comment(++localId,dummyMembers[3], "Ciao", "2024-02-07", "18:50")
    )
    var addComment by mutableStateOf(Comment(++localId,member, "", "", ""))

    fun changeAddComment(s: String) {
        addComment = Comment(
            ++localId,
            addComment.user,
            s,
            LocalDate.now().toString(),
            LocalTime.now().hour.toString() + ":" + LocalTime.now().minute.toString()
        )
    }

    fun addNewComment() {
        if (addComment.commentValue.trim() != "") {
            comments.add(addComment)
        }
        addComment = Comment(
            ++localId,
            addComment.user,
            "",
            LocalDate.now().toString(),
            LocalTime.now().hour.toString() + ":" + LocalTime.now().minute.toString()
        )
    }

    fun deleteComment(c: Comment) {
        comments.remove(c)
        //commentsBefore.remove(c)
    }

    var files = mutableStateListOf(File(++localId,dummyMembers[3], "filename", "2024-02-05", Uri.EMPTY))
    fun addFile(f: File) {
        files.add(f)
        // add history entry
        history.add(History(
            comment = "File ${f.filename} uploaded.",
            date = LocalDate.now().toString(),
            user = member
        ))
    }

    fun removeFile(f: File) {
        files.remove(f)
        // add history entry
        history.add(History(
            comment = "File ${f.filename} deleted.",
            date = LocalDate.now().toString(),
            user = member
        ))
    }

    var history = mutableStateListOf<History>()

    fun newTask() {
        taskName = ""
        description = ""
        category = ""
        priority = ""
        //deadline =""
        estimatedHours = ""
        spentHours = ""
        repeatable = Repetition.NONE.toString()
        status = Status.TODO.toString()
        members = mutableStateListOf<Member>()
        comments = mutableStateListOf()
        files = mutableStateListOf()
        history = mutableStateListOf()
    }
}