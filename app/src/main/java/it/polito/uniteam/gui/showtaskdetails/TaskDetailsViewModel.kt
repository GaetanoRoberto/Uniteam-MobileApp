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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.Comment
import it.polito.uniteam.classes.File
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.isRepetition
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.handleInputString
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@SuppressLint("MutableCollectionMutableState")
class taskDetails(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val taskId: String = checkNotNull(savedStateHandle["taskId"])
    val newTask = taskId.toInt() == 0

    fun getTask(taskId: Int) = model.getTask(taskId)
    fun getTeamRelatedToTask(taskId: Int) = model.getTeamRelatedToTask(taskId)

    private val task = getTask(taskId.toInt())
    var taskName by mutableStateOf(task?.name ?: "")
        private set
    var taskError by mutableStateOf("")
        private set

    fun changeTaskName(s: String) {
        taskName = handleInputString(s)
    }

    private fun checkTaskName() {
        if (taskName.isBlank())
            taskError = "Task name cannot be blank!"
        else
            taskError = ""
    }


    var description by mutableStateOf(task?.description ?: "")
        private set
    var descriptionError by mutableStateOf("")
        private set

    fun changeDescription(s: String) {
        description = handleInputString(s)
    }

    private fun checkDescription() {
        if (description.isBlank())
            descriptionError = "Task description cannot be blank!"
        else
            descriptionError = ""
    }


    var category by mutableStateOf(task?.category?.toString() ?: Category.NONE.toString())
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


    var priority by mutableStateOf(task?.priority?.toString() ?: Priority.LOW.toString())
        private set
    val priorityValues = Priority.entries.map { it.toString() }
    var priorityError by mutableStateOf("")
        private set

    fun changePriority(s: String) {
        priority = s
    }

    var deadline by mutableStateOf(task?.deadline?.toString() ?: LocalDate.now().toString())
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


    var status by mutableStateOf(task?.status?.toString() ?: Status.TODO.toString())
        private set
    var stateError by mutableStateOf("")
        private set

    fun changeState(s: String) {
        status = s
    }

    val possibleStates =
        listOf(Status.TODO.toString(), "IN PROGRESS", Status.COMPLETED.toString())


    var estimatedHours = mutableStateOf(task?.estimatedTime?.first.toString() ?: "0")
        private set
    var estimatedMinutes = mutableStateOf(task?.estimatedTime?.second.toString() ?: "0")
        private set
    var estimatedTimeError = mutableStateOf("")
        private set

    private fun checkEstimatedTime() {
        try {
            val hours = estimatedHours.value.toUInt().toInt()
            val minutes = estimatedMinutes.value.toUInt().toInt()
            if (hours == 0 && minutes == 0) {
                estimatedTimeError.value = "You Need To Schedule A Positive Time Interval."
            } else if (minutes >= 60) {
                estimatedTimeError.value = "Invalid Minute Value."
            } else {
                estimatedTimeError.value = ""
                estimatedHours.value = hours.toString()
                estimatedMinutes.value = minutes.toString()
            }
        } catch (e: RuntimeException) {
            estimatedTimeError.value = "Valid Positive Numbers Must Be Provided."
        }
    }

    var spentTime = task?.spentTime?.toMutableMap() ?: mutableMapOf()
        private set

    fun addSpentTime(member: Member, time: Pair<Int,Int>) {
        if(spentTime.contains(member)) {
            val prevHours = spentTime.remove(member)
            if (prevHours != null) {
                spentTime.put(member, model.sumTimes(prevHours,time))
            }
        } else {
            spentTime.put(member,time)
        }
        Log.i("diooo",spentTime.toString())
    }
    var spentHours = mutableStateOf(task?.spentTime?.values?.sumOf { it.first }?.toString() ?: "0")
        private set
    var spentMinutes = mutableStateOf(task?.spentTime?.values?.sumOf { it.second }?.toString() ?: "0")
        private set
    var spentTimeError = mutableStateOf("")
        private set

    private fun checkSpentTime() {
        try {
            val hours = spentHours.value.toUInt().toInt()
            val minutes = spentMinutes.value.toUInt().toInt()
            if (minutes >= 60) {
                spentTimeError.value = "Invalid Minute Value."
            } else {
                spentTimeError.value = ""
                spentHours.value = "0"
                spentMinutes.value = "0"
                if (!(hours == 0 && minutes == 0)) {
                    addSpentTime(member,Pair(hours,minutes))
                }
                /*spentHours.value = hours.toString()
                spentMinutes.value = minutes.toString()*/
            }
        } catch (e: RuntimeException) {
            spentTimeError.value = "Valid Positive Numbers Must Be Provided."
        }
    }

    var possibleMembers = getTeamRelatedToTask(taskId.toInt())?.members?.toMutableStateList() ?: DummyDataProvider.getMembers()
    var members = task?.members?.toMutableStateList() ?: mutableStateListOf()

    var openAssignDialog = mutableStateOf(false)
    var membersError by mutableStateOf("")
    var membersDialogError by mutableStateOf("")

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


    var repeatable by mutableStateOf(task?.repetition?.toString() ?: Repetition.NONE.toString())
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
                || hasChanged(estimateHoursBefore,estimatedHours.value) || hasChanged(estimateMinutesBefore,estimatedMinutes.value)
                || hasChanged(spentHoursBefore,spentHours.value) || hasChanged(spentMinutesBefore,spentMinutes.value)
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
        } else {
            // add creation task history
            history.add(History(
                comment = "Task ${taskName} created.",
                date = LocalDate.now().toString(),
                user = member
            ))
        }
    }

    fun validate() {
        checkTaskName()
        checkDescription()
        checkCategory()
        checkDeadline()
        checkEstimatedTime()
        checkSpentTime()
        checkMembers()
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
    var estimateHoursBefore = "0"
    var estimateMinutesBefore = "0"
    var spentHoursBefore = "0"
    var spentMinutesBefore = "0"
    var repeatableBefore = ""
    var statusBefore = ""
    var membersBefore = mutableListOf<Member>()

    fun enterEditingMode() {
        taskNameBefore = taskName
        descriptionBefore = description
        categoryBefore = category
        priorityBefore = priority
        deadlineBefore = deadline
        estimateHoursBefore = estimatedHours.value
        estimateMinutesBefore = estimatedMinutes.value
        spentHoursBefore = spentHours.value
        spentMinutesBefore = spentMinutes.value
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
        estimatedHours.value = estimateHoursBefore
        estimatedMinutes.value = estimateMinutesBefore
        spentHours.value = spentHoursBefore
        spentMinutes.value = spentMinutesBefore
        repeatable = repeatableBefore
        status = statusBefore
        members = membersBefore.toMutableStateList()
        taskError = ""
        descriptionError = ""
        categoryError = ""
        priorityError = ""
        deadlineError = ""
        estimatedTimeError.value = ""
        spentTimeError.value = ""
        membersError = ""
    }

    //var scrollTaskDetails by mutableIntStateOf(0)
    var tabState by mutableIntStateOf(0)
    fun switchTab(index: Int) {
        tabState = index
    }
    var commentHistoryFileSelection by mutableStateOf("comments")
    fun changeCommentHistoryFileSelection(s: String) {
        if (s.trim().lowercase() == "comments") {
            commentHistoryFileSelection = "comments"
        } else if (s.trim().lowercase() == "files") {
            commentHistoryFileSelection = "files"

        } else if (s.trim().lowercase() == "history") {
            commentHistoryFileSelection = "history"

        }
        //scrollTaskDetails = Int.MAX_VALUE
    }

    var localId by mutableIntStateOf(0)
    val member = model.loggedMember.value
    val dummyMembers = DummyDataProvider.getMembers()

    var comments = task?.taskComments?.toMutableStateList() ?: mutableStateListOf()
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
            addComment.commentValue = handleInputString(addComment.commentValue)
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
        estimatedHours.value = "0"
        estimatedMinutes.value = "0"
        spentHours.value = "0"
        spentMinutes.value = "0"
        repeatable = Repetition.NONE.toString()
        status = Status.TODO.toString()
        members = mutableStateListOf<Member>()
        comments = mutableStateListOf()
        files = mutableStateListOf()
        history = mutableStateListOf()
    }

    var openDeleteTaskDialog by mutableStateOf(false)

    init {
        if(newTask) {
            changeEditing()
            enterEditingMode()
            newTask()
        }
    }
}