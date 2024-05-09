package it.polito.uniteam.gui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Task
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors
import java.util.stream.Stream

class Calendar : ViewModel() {

    var memberProfile by mutableStateOf<Member?>(null)
        private set

    var selectedShowDialog by mutableStateOf(showDialog.none)
        private set

    fun openDialog(showDialog: showDialog) {
        selectedShowDialog = showDialog
    }

    fun closeDialog() {
        selectedShowDialog = showDialog.none
    }

    fun checkDialogs(task: Task, date: LocalDate, isNewSchedule: Boolean = true) {
        // check permissions
        if (task.members.contains(memberProfile)) {
            // if permissions, check if back in time
            if(date.isBefore(LocalDate.now())) {
                selectedShowDialog = showDialog.schedule_in_past
            } else if(date.isAfter(task.deadline)) {
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
    var taskToSchedule by mutableStateOf<Triple<Task, LocalDate?, LocalDate>?>(null)
        private set

    fun assignTaskToSchedule(task: Triple<Task, LocalDate?, LocalDate>?) {
        taskToSchedule = task
    }

    var tasksToAssign by mutableStateOf(emptyList<Task>())
        private set

    var allScheduledTasks = mutableStateListOf<Task>()
        private set

    var viewedScheduledTasks = mutableStateListOf<Task>()
        private set

    fun filterScheduledTasks(filterByMyTask: Boolean) {
        if (filterByMyTask) {
            viewedScheduledTasks.removeIf { !it.members.contains(memberProfile) }
        } else {
            viewedScheduledTasks.clear()
            viewedScheduledTasks.addAll(allScheduledTasks)
        }
    }

    init {
        // get logged in user
        memberProfile = DummyDataProvider.getDummyProfile()
        // get task to assign and filter them based on the user
        tasksToAssign = DummyDataProvider.getTasksToAssign()
        tasksToAssign = tasksToAssign.filter { it.members.contains(memberProfile) }
        val tasks = DummyDataProvider.getScheduledTasks()
        tasks.forEach { task ->
            allScheduledTasks.add(task)
            viewedScheduledTasks.add(task)
        }
    }

    fun sumTimes(time1: Pair<Int, Int>, time2: Pair<Int, Int>): Pair<Int, Int> {
        val totalMinutes = time1.second + time2.second
        val minutesOverflow = totalMinutes / 60
        val minutes = totalMinutes % 60

        val totalHours = time1.first + time2.first + minutesOverflow

        return Pair(totalHours, minutes)
    }

    fun getTotalTime(task: Task) {
        val pair = task.schedules.values.reduce { acc, pair ->
            sumTimes(acc, pair)
        }
    }

    fun scheduleTask(task: Task, scheduleDate: LocalDate, hoursToSchedule: Pair<Int,Int>) {
        // schedule task on scheduleDate with hoursToSchedule
        // if task with that scheduleDate already present remove it
        // MAINTAIN BOTH COPIES ALIGNED
        viewedScheduledTasks.remove(task)
        allScheduledTasks.remove(task)
        // add the schedule to the task schedules
        if (task.schedules.containsKey(scheduleDate)) {
            val prevHours = task.schedules.remove(scheduleDate)
            if (prevHours != null) {
                task.schedules.put(scheduleDate, sumTimes(prevHours,hoursToSchedule))
            }
        } else {
            task.schedules.put(scheduleDate, hoursToSchedule)
        }
        // MAINTAIN BOTH COPIES ALIGNED
        viewedScheduledTasks.add(task)
        allScheduledTasks.add(task)
        // Remove the task from tasksToAssign if completely scheduled
        //if (task.schedules.values.sumOf { it } == task.estimatedHours)
        //   tasksToAssign = tasksToAssign.filter { it.id != task.id }

    }

    fun unScheduleTask(task: Task, scheduleDate: LocalDate) {
        // remove the specific instance of the task from the scheduled ones
        // MAINTAIN BOTH COPIES ALIGNED
        viewedScheduledTasks.removeIf { it.schedules.containsKey(scheduleDate) && it.id == task.id }
        allScheduledTasks.removeIf { it.schedules.containsKey(scheduleDate) && it.id == task.id }
        // remove the previous instance of this task
        tasksToAssign = tasksToAssign.filter { it.id != task.id }
        // remove the scheduled information
        task.schedules.remove(scheduleDate)
        // Add the task back to tasksToAssign
        tasksToAssign = tasksToAssign + task
    }

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

enum class handleDialog {
    schedule_task,
    no_permission,
    schedule_in_past,
    none
}