package it.polito.uniteam.gui.calendar

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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

    var haveNoPermission by mutableStateOf(false)
        private set

    fun closePermissionDialog() {
        haveNoPermission = false
    }

    fun checkPermission(task: Task) {
        if (task.members.contains(memberProfile)) {
            haveNoPermission = false
        } else {
            haveNoPermission = true
        }
    }

    var tasksToAssign by mutableStateOf(emptyList<Task>())
        private set

    var taskToSchedule by mutableStateOf<Pair<Task, LocalDate?>?>(null)
        private set

    fun assignTaskToSchedule(task: Pair<Task, LocalDate?>?) {
        taskToSchedule = task
    }

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

    fun scheduleTask(task: Task, scheduleDate: LocalDate, hoursToSchedule: Int) {
        // schedule task on scheduleDate with hoursToSchedule
        // if task with that scheduleDate already present remove it
        // MAINTAIN BOTH COPIES ALIGNED
        viewedScheduledTasks.remove(task)
        allScheduledTasks.remove(task)
        // add the schedule to the task schedules
        if (task.schedules.containsKey(scheduleDate)) {
            val prevHours = task.schedules.remove(scheduleDate)
            if (prevHours != null) {
                task.schedules.put(scheduleDate, prevHours.plus(hoursToSchedule))
            }
        } else {
            task.schedules.put(scheduleDate, hoursToSchedule)
        }
        // MAINTAIN BOTH COPIES ALIGNED
        viewedScheduledTasks.add(task)
        allScheduledTasks.add(task)
        // Remove the task from tasksToAssign if completely scheduled
        if (task.schedules.values.sumOf { it } == task.estimatedHours)
            tasksToAssign = tasksToAssign.filter { it.id != task.id }

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
