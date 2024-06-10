package it.polito.uniteam.gui.calendar

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.TaskDBFinal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors
import java.util.stream.Stream

class Calendar(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {

    val teamId: String = "8Kil7wNz6eF1ZD2Lf19K"//checkNotNull(savedStateHandle["teamId"])
    //fun getTeam(teamId: Int) = model.getTeam(teamId)
    //val teamName = getTeam(teamId.toInt()).name
    var loggedMember = model.loggedMemberFinal.id // TODO hardcoded
        private set

    fun scheduleTask(task: TaskDBFinal, scheduleDate: LocalDate, hoursToSchedule: Pair<Int,Int>) = model.scheduleTask(task, scheduleDate, hoursToSchedule)
    fun unScheduleTask(task: TaskDBFinal, scheduleDate: LocalDate) = model.unscheduleTask(task,scheduleDate)
    var selectedShowDialog by mutableStateOf(showDialog.none)
        private set

    fun openDialog(showDialog: showDialog) {
        selectedShowDialog = showDialog
    }

    fun closeDialog() {
        selectedShowDialog = showDialog.none
    }

    fun checkDialogs(task: TaskDBFinal, sourceDate:LocalDate? = null, targetDate: LocalDate, isNewSchedule: Boolean = true) {
        // check permissions
        Log.i("diooo",task.schedules.keys.contains(Pair(loggedMember,targetDate)).toString())
        if ((task.schedules.keys.contains(Pair(loggedMember,sourceDate)) &&!isNewSchedule) || isNewSchedule) {
            // if permissions, check if back in time
            if(targetDate.isBefore(LocalDate.now())) {
                selectedShowDialog = showDialog.schedule_in_past
            } else if(targetDate.isAfter(task.deadline)) {
                selectedShowDialog = showDialog.after_deadline
            } else if(isNewSchedule) {
                selectedShowDialog = showDialog.schedule_task
            } else {
                selectedShowDialog = showDialog.none
            }
        } else {
            selectedShowDialog = showDialog.no_permission
        }
    }

    // task to schedule, old date (if moving from an already scheduled), new date
    var taskToSchedule by mutableStateOf<Triple<TaskDBFinal, LocalDate?, LocalDate>?>(null)
        private set

    fun assignTaskToSchedule(task: Triple<TaskDBFinal, LocalDate?, LocalDate>?) {
        taskToSchedule = task
    }

    var tasksToAssign = mutableStateListOf<TaskDBFinal>()

    var allScheduledTasks = mutableStateListOf<TaskDBFinal>()

    var viewedScheduledTasks = mutableStateListOf<TaskDBFinal>()

    fun filterScheduledTasks(filterByMyTask: Boolean) {
        if (filterByMyTask) {
            viewedScheduledTasks.removeIf { !it.members.contains(loggedMember) }
        } else {
            viewedScheduledTasks.clear()
            viewedScheduledTasks.addAll(allScheduledTasks)
        }
    }

    fun subtractTimes(time1: Pair<Int, Int>, time2: Pair<Int, Int>): Pair<Int, Int> {
        val totalMinutes1 = time1.first * 60 + time1.second
        val totalMinutes2 = time2.first * 60 + time2.second

        val differenceMinutes = totalMinutes1 - totalMinutes2
        val hours = differenceMinutes / 60
        val minutes = differenceMinutes % 60

        return Pair(hours, minutes)
    }


    /*fun scheduleTask(task: TaskDBFinal, scheduleDate: LocalDate, hoursToSchedule: Pair<Int,Int>) {
        // schedule task on scheduleDate with hoursToSchedule
        // initialize this variable to then store the updated version of this task
        updateTask(task,scheduleDate,hoursToSchedule)
        var updated_task = task;
        // get the list from the state
        var viewedTask = viewedScheduledTasks.toMutableList()
        if(viewedTask.any { it.id == task.id }) {
            // if task already present, update it with the new schedule
            viewedTask = viewedTask.map {
                if(it.id == task.id) {
                    if (it.schedules.containsKey(Pair(memberId,scheduleDate))) {
                        val prevHours = it.schedules.remove(Pair(memberId,scheduleDate))
                        if (prevHours != null) {
                            it.schedules.put(Pair(memberId!!,scheduleDate), model.sumTimes(prevHours,hoursToSchedule))
                        }
                    } else {
                        it.schedules.put(Pair(memberId!!,scheduleDate), hoursToSchedule)
                    }
                    // get the updated instance for this task
                    updated_task = it.copy()
                    it
                } else {
                    it
                }
            }.toMutableList()
        } else {
            // if task not present, add it with the new schedule
            if (task.schedules.containsKey(Pair(memberId,scheduleDate))) {
                val prevHours = task.schedules.remove(Pair(memberId,scheduleDate))
                if (prevHours != null) {
                    task.schedules.put(Pair(memberId!!,scheduleDate), model.sumTimes(prevHours,hoursToSchedule))
                }
            } else {
                task.schedules.put(Pair(memberId!!,scheduleDate), hoursToSchedule)
            }
            // get the updated instance for this task
            updated_task = task.copy()
            viewedTask.add(task)
        }
        // ASSIGN LIST TO STATE AND MAINTAIN BOTH COPIES ALIGNED
        viewedScheduledTasks.clear()
        viewedScheduledTasks.addAll(viewedTask)
        allScheduledTasks.clear()
        allScheduledTasks.addAll(viewedTask)
        // update the task in taskstoassign to see the updated hours
        var taskToAssignList = tasksToAssign.toMutableList()
        taskToAssignList = taskToAssignList.map {
            if(it.id == task.id) {
                // return the updated task to avoid add scheduled hours 2 times (not idempotent like remove in unScheduleTask)
                updated_task
            } else {
                it
            }
        }.toMutableStateList()
        tasksToAssign.clear()
        tasksToAssign.addAll(taskToAssignList)
    }*/

    /*fun unScheduleTask(task: TaskDBFinal, scheduleDate: LocalDate) {
        // remove the specific instance of the task from the scheduled ones
        // get the list from the state
        var viewedTask = viewedScheduledTasks.toMutableList()
        if(viewedTask.any { it.id == task.id }) {
            // if task present, update it by removing the schedule, otherwise do nothing since nothing to remove
            // after also filter by number of schedules (if task after unscheduling has no more schedule remove it)
            viewedTask = viewedTask.map {
                if(it.id == task.id) {
                    it.schedules.remove(Pair(memberId,scheduleDate))
                    it
                } else {
                    it
                }
            }.filter { it.schedules.isNotEmpty() }.toMutableStateList()
        }
        // ASSIGN LIST TO STATE AND MAINTAIN BOTH COPIES ALIGNED
        viewedScheduledTasks.clear()
        viewedScheduledTasks.addAll(viewedTask)
        allScheduledTasks.clear()
        allScheduledTasks.addAll(viewedTask)
        // update the task in taskstoassign to see the updated hours
        var taskToAssignList = tasksToAssign.toMutableList()
        taskToAssignList = taskToAssignList.map {
            if(it.id == task.id) {
                // here we can remove 2 times since it works anyway idempotent
                it.schedules.remove(Pair(memberId,scheduleDate))
                it
            } else {
                it
            }
        }.toMutableStateList()
        tasksToAssign.clear()
        tasksToAssign.addAll(taskToAssignList)
    }*/

    var calendarUiModel by mutableStateOf<CalendarUiModel>(getData(lastSelectedDate = today))
    val today: LocalDate
        get() {
            return LocalDate.now()
        }


    fun getData(startDate: LocalDate = today, lastSelectedDate: LocalDate): CalendarUiModel {
        val firstDayOfWeek = startDate.with(DayOfWeek.MONDAY)
        val endDayOfWeek = firstDayOfWeek.plusDays(7)
        val visibleDates = getDatesBetween(firstDayOfWeek, endDayOfWeek)
        return toUiModel(visibleDates, lastSelectedDate)
    }

    private fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val numOfDays = ChronoUnit.DAYS.between(startDate, endDate)
        return Stream.iterate(startDate) { date ->
            date.plusDays(/* daysToAdd = */ 1)
        }
            .limit(numOfDays)
            .collect(Collectors.toList())
    }

    private fun toUiModel(
        dateList: List<LocalDate>,
        lastSelectedDate: LocalDate
    ): CalendarUiModel {
        return CalendarUiModel(
            selectedDate = toItemUiModel(lastSelectedDate, true),
            visibleDates = dateList.map {
                toItemUiModel(it, it.isEqual(lastSelectedDate))
            },
        )
    }

    private fun toItemUiModel(date: LocalDate, isSelectedDate: Boolean) = CalendarUiModel.Date(
        isSelected = isSelectedDate,
        isToday = date.isEqual(today),
        date = date,
    )


}

enum class showDialog {
    schedule_task,
    no_permission,
    schedule_in_past,
    after_deadline,
    task_detail,
    none
}
