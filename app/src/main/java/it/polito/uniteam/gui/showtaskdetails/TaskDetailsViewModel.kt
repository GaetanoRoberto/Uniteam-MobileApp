package it.polito.uniteam.gui.showtaskdetails

import android.annotation.SuppressLint
import android.content.Context
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
import it.polito.uniteam.classes.CommentDBFinal
import it.polito.uniteam.classes.File
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.isRepetition
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.FileDB
import it.polito.uniteam.classes.FileDBFinal
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.handleInputString
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("MutableCollectionMutableState")
class taskDetails(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    var loggedMember = ""
    val taskId: String = checkNotNull(savedStateHandle["taskId"])
    val teamId: String = checkNotNull(savedStateHandle["teamId"])
    val newTask = taskId.length == 1 // navigate with 0, so length 1
    var isTaskDeleted = false
    var temporaryId: Int = 1
    fun getTask(taskId: Int) = model.getTask(taskId)
    fun getTeamRelatedToTask(taskId: Int) = model.getTeamRelatedToTask(taskId)

    var taskName by mutableStateOf("")
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


    var description by mutableStateOf("")
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


    var category by mutableStateOf(Category.NONE.toString())
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
    val priorityValues = Priority.entries.map { it.toString() }
    var priorityError by mutableStateOf("")
        private set

    fun changePriority(s: String) {
        priority = s
    }

    var deadline by mutableStateOf(LocalDate.now().toString())
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
    var stateError by mutableStateOf("")
        private set

    fun changeState(s: String) {
        status = s
    }

    val possibleStates =
        listOf(Status.TODO.toString(), "IN PROGRESS", Status.COMPLETED.toString())


    var estimatedHours = mutableStateOf("0")
    var estimatedMinutes = mutableStateOf("0")
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

    var spentTime: MutableMap<String,Pair<Int,Int>> = mutableMapOf()

    fun addSpentTime(time: Pair<Int,Int>) {
        if(spentTime.contains(loggedMember)) {
            val prevHours = spentTime.remove(loggedMember)
            if (prevHours != null) {
                spentTime.put(loggedMember, model.sumTimes(prevHours,time))
            }
        } else {
            spentTime.put(loggedMember,time)
        }
        Log.i("diooo",spentTime.toString())
    }
    var spentHours = mutableStateOf("0")
        private set
    var spentMinutes = mutableStateOf("0")
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
                    addSpentTime(Pair(hours,minutes))
                }
                /*spentHours.value = hours.toString()
                spentMinutes.value = minutes.toString()*/
            }
        } catch (e: RuntimeException) {
            spentTimeError.value = "Valid Positive Numbers Must Be Provided."
        }
    }

    var possibleMembers = listOf<MemberDBFinal>()
    var members = mutableStateListOf<MemberDBFinal>()

    var openAssignDialog = mutableStateOf(false)
    var membersError by mutableStateOf("")
    var membersDialogError by mutableStateOf("")

    /*fun addMembers(m: MemberDBFinal) {
        members.add(m)
        Log.i("diooo","member added")
    }

    fun removeMembers(m: MemberDBFinal) {
        members.remove(m)
    }*/
    private fun checkMembers() {
        if (members.size <= 0)
            membersError = "At Least a member should be assigned"
        else
            membersError = ""
    }


    var repeatable by mutableStateOf(Repetition.NONE.toString())
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
        val entryToAdd: MutableList<HistoryDBFinal> = mutableListOf()
        val isEdit = taskNameBefore.isNotBlank()
        if (isEdit) {
            //general editing
            if(hasChanged(taskNameBefore,taskName) || hasChanged(descriptionBefore,description)
                || hasChanged(categoryBefore,category) || hasChanged(deadlineBefore,deadline)
                || hasChanged(estimateHoursBefore,estimatedHours.value) || hasChanged(estimateMinutesBefore,estimatedMinutes.value)
                || hasChanged(spentHoursBefore,spentHours.value) || hasChanged(spentMinutesBefore,spentMinutes.value)
                || hasChanged(repeatableBefore,repeatable)) {
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = "Task Edited.",
                    date = LocalDateTime.now(),
                    user = loggedMember
                ))
            }
            // members
            val removedComment = "Members removed: "
            var removedMembers = ""
            val addedComment = "Members added: "
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
                entryToAdd.add(
                    HistoryDBFinal(
                        id = (temporaryId++).toString(),
                        comment = comment,
                        date = LocalDateTime.now(),
                        user = loggedMember
                    )
                )
            } else if (removedMembers.isNotEmpty()) {
                val comment = "Task " + removedComment + removedMembers
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = comment,
                    date = LocalDateTime.now(),
                    user = loggedMember
                ))
            } else if (addedMembers.isNotEmpty()) {
                val comment = "Task " + addedComment + addedMembers
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = comment,
                    date = LocalDateTime.now(),
                    user = loggedMember
                ))
            }
            //status
            if (hasChanged(statusBefore,status)) {
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = "Task status changed from ${statusBefore} to ${status}.",
                    date = LocalDateTime.now(),
                    user = loggedMember
                ))
            }
            //priority
            if (hasChanged(priorityBefore,priority)) {
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = "Task priority changed from ${priorityBefore} to ${priority}.",
                    date = LocalDateTime.now(),
                    user = loggedMember
                ))
            }
            // add to history
            entryToAdd.forEach{ entry->
                history.add(entry)
            }
            // Add history directly in edit (I have firebase taskId)
            model.addHistories(history,taskId)
        } else {
            // task add creation task history
            // in View not reachable
            history.add(HistoryDBFinal(
                id = (temporaryId++).toString(),
                comment = "Task ${taskName} created.",
                date = LocalDateTime.now(),
                user = loggedMember
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
    var membersBefore = mutableListOf<MemberDBFinal>()

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

    val dummyMembers = DummyDataProvider.getMembers()

    var comments = mutableStateListOf<CommentDBFinal>()
    var addComment by mutableStateOf(CommentDBFinal(id = (temporaryId++).toString(), loggedMember, "", LocalDate.now(), ""))

    fun changeAddComment(s: String) {
        addComment = CommentDBFinal(
            id = (temporaryId++).toString(),
            addComment.user,
            s,
            LocalDate.now(),
            LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).slice(IntRange(0,4))
        )
    }

    fun addNewComment() {
        if (addComment.commentValue.trim() != "") {
            addComment.commentValue = handleInputString(addComment.commentValue)
            if(editing || !newTask) {
                // edit/view so call the db ( I know the taskId)
                model.addComment(addComment,taskId)
            } else {
                // add save into the state
                comments.add(addComment)
            }
        }
        addComment = CommentDBFinal(
            id = (temporaryId++).toString(),
            addComment.user,
            "",
            LocalDate.now(),
            LocalTime.now().hour.toString() + ":" + LocalTime.now().minute.toString()
        )
    }

    fun updateComment(c: CommentDBFinal) {
        if (editing || !newTask) {
            // edit view so have the taskId
            model.updateComment(c,taskId)
        } else {
            // remove from the state
            comments.replaceAll {
                if (it.id == c.id) {
                    c
                } else {
                    it
                }
            }
        }
    }
    fun deleteComment(c: CommentDBFinal) {
        if (editing || !newTask) {
            // edit view so have the taskId
            model.deleteComment(c.id,taskId)
        } else {
            // remove from the state
            comments.remove(c)
        }
    }

    var files = mutableStateListOf<FileDBFinal>()
    fun addFile(f: FileDBFinal, context: Context) {
        if(editing || !newTask) {
            // edit/view, so call the db (I have the taskId)
            model.addFile(context,f,taskId)
        } else {
            // add save in states
            files.add(f)
            // add history entry
            history.add(HistoryDBFinal(
                id = (temporaryId++).toString(),
                comment = "File ${f.filename} uploaded.",
                date = LocalDateTime.now(),
                user = loggedMember
            ))
        }
    }

    fun deleteFile(f: FileDBFinal) {
        if(editing || !newTask) {
            // edit/view, so call the db (I have the taskId)
            model.deleteFile(f,taskId)
        } else {
            // add save in states
            files.remove(f)
            // add history entry
            history.add(HistoryDBFinal(
                id = (temporaryId++).toString(),
                comment = "File ${f.filename} deleted.",
                date = LocalDateTime.now(),
                user = loggedMember
            ))
        }
    }

    var history = mutableStateListOf<HistoryDBFinal>()

    fun newTask() {
        taskName = ""
        description = ""
        category = Category.NONE.toString()
        priority = Priority.LOW.toString()
        //deadline =""
        estimatedHours.value = "0"
        estimatedMinutes.value = "0"
        spentHours.value = "0"
        spentMinutes.value = "0"
        repeatable = Repetition.NONE.toString()
        status = Status.TODO.toString()
        members = mutableStateListOf<MemberDBFinal>()
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