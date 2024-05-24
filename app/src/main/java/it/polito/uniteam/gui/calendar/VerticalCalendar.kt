package it.polito.uniteam.gui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import it.polito.uniteam.Factory
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.TextTrim
import it.polito.uniteam.isVertical
import java.time.LocalDate

@Preview(showSystemUi = true)
@Composable
fun CalendarAppContainer(vm: Calendar = viewModel(factory = Factory(LocalContext.current))) {
    val dragAndDropState = rememberDragAndDropState<Pair<Task, LocalDate?>>()
    DragAndDropContainer(
        state = dragAndDropState,
    ) {
        if (isVertical()) {
            VerticalCalendarApp(
                modifier = Modifier.padding(1.dp),
                dragAndDropState = dragAndDropState,
                vm = vm
            )
        } else {
            HorizontalCalendarApp(
                modifier = Modifier.padding(1.dp),
                dragAndDropState = dragAndDropState,
                vm = vm
            )
        }
    }
    if (vm.taskToSchedule != null) {
        when (vm.selectedShowDialog) {
            showDialog.schedule_task -> {
                ScheduleTaskDialog(vm = vm)
            }
            showDialog.no_permission -> {
                NoPermissionDialog(vm = vm)
            }
            showDialog.schedule_in_past -> {
                ScheduleBackInTimeDialog(vm = vm)
            }
            showDialog.after_deadline -> {
                ScheduleAfterDeadlineDialog(vm = vm)
            }
            showDialog.task_detail -> {
                TaskDetailDialog(vm = vm)
            }
            showDialog.none -> {}
        }
    }
}

@Composable
fun VerticalCalendarApp(
    modifier: Modifier = Modifier,
    dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
    vm: Calendar = viewModel(factory = Factory(LocalContext.current))
) {
    // get CalendarUiModel from CalendarDataSource, and the lastSelectedDate is Today.
    var calendarUiModel by remember { mutableStateOf(vm.calendarUiModel) }
    Column(
        modifier = modifier
            .fillMaxSize()

    ) {
        VerticalHeader(vm = vm, startDate = calendarUiModel.startDate, endDate = calendarUiModel.endDate,
            onPrevClickListener = { startDate ->
                // refresh the CalendarUiModel with new data
                // by get data with new Start Date (which is the startDate-1 from the visibleDates)
                val finalStartDate = startDate.minusDays(1)
                calendarUiModel = vm.getData(
                    startDate = finalStartDate,
                    lastSelectedDate = calendarUiModel.selectedDate.date
                )
                vm.calendarUiModel = calendarUiModel
            },
            onNextClickListener = { endDate ->
                // refresh the CalendarUiModel with new data
                // by get data with new Start Date (which is the endDate+2 from the visibleDates)
                val finalStartDate = endDate.plusDays(2)
                calendarUiModel = vm.getData(
                    startDate = finalStartDate,
                    lastSelectedDate = calendarUiModel.selectedDate.date
                )
                vm.calendarUiModel = calendarUiModel
            },
            onTodayClickListener = {
                val finalStartDate = calendarUiModel.selectedDate.date
                calendarUiModel = vm.getData(
                    startDate = finalStartDate,
                    lastSelectedDate = calendarUiModel.selectedDate.date
                )
                vm.calendarUiModel = calendarUiModel
            }

        )
        HorizontalDivider(color = Color.White, thickness = 0.dp)
        VerticalDayEventScheduler(data = calendarUiModel, dragAndDropState = dragAndDropState, vm = vm)
        HorizontalDivider(color = Color.White, thickness = 0.dp)
        VerticalTasksToAssign(vm = vm, dragAndDropState = dragAndDropState)
    }
}

@Composable
fun VerticalHeader(
    vm: Calendar = viewModel(factory = Factory(LocalContext.current)),
    startDate: CalendarUiModel.Date, endDate: CalendarUiModel.Date,
    onPrevClickListener: (LocalDate) -> Unit,
    onNextClickListener: (LocalDate) -> Unit,
    onTodayClickListener: () -> Unit
) {
    val isCheched = remember { mutableStateOf(false) }
    val month = if (startDate.date.month != endDate.date.month) {
        startDate.date.month.toString().substring(0,3) + "/" + endDate.date.month.toString().substring(0,3)
    } else {
        startDate.date.month.toString().substring(0,3)
    }
    val year = if (startDate.date.year != endDate.date.year) {
        val year_len = endDate.date.year.toString().length
        startDate.date.year.toString() + "/" + endDate.date.year.toString().substring(year_len - 2)
    } else {
        startDate.date.year.toString()
    }
    Column(
    ) {
        Row {
            TextTrim(
                inputText = "Team #1 - Tasks",
                desiredLength = 20,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 10.dp),
            )
            Spacer(modifier = Modifier.weight(1f)) // Spazio flessibile per allineare la checkbox e il testo "My Tasks" alla fine
            Checkbox(
                checked = isCheched.value,
                onCheckedChange = { filterByMyTask ->
                    vm.filterScheduledTasks(filterByMyTask)
                    isCheched.value = filterByMyTask
                }
            )
            Text(
                text = "My Tasks",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 10.dp)
            )
        }
        Row {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.1f)
                    .fillMaxWidth(0.4f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = month + " " + startDate.date.dayOfMonth + " - " + endDate.date.dayOfMonth,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally),
                )
                Text(
                    text = year,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onPrevClickListener(startDate.date) }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Previous"
                )
            }
            IconButton(onClick = { onNextClickListener(endDate.date) }) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Next"
                )
            }
            Button(onClick = onTodayClickListener, modifier = Modifier.padding(end = 10.dp)) {
                Text("Today")
            }
        }
    }
}


//OGGETTO TASK
@Composable
fun EventItem(vm: Calendar = viewModel(factory = Factory(LocalContext.current)), task: Task, scheduleEntry: Map.Entry<Pair<Member, LocalDate>, Pair<Int, Int>>? = null, date: LocalDate? = null, isScheduled: Boolean) {
    var isOverSchedule = false
    // if scheduled assign memberTime otherwise not scheduled so no member/date provided
    var memberTime: Map.Entry<Pair<Member, LocalDate>, Pair<Int, Int>>? = null;
    var time: Pair<Int,Int> = Pair(0,0);
    if (isScheduled) {
        memberTime = scheduleEntry
        if (memberTime != null) {
            time = memberTime.value
        }
    } else {
        val totalMinutes1 = task.estimatedTime.first * 60 + task.estimatedTime.second
        val totalMinutes2 = task.schedules.values.sumOf { it.first } * 60 + task.schedules.values.sumOf { it.second }
        val differenceInMinutes = totalMinutes1 - totalMinutes2

        val hours = differenceInMinutes / 60
        val minutes = differenceInMinutes % 60
        if (differenceInMinutes < 0) {
            isOverSchedule = true
            time = Pair(-hours,-minutes)
        } else {
            time = Pair(hours,minutes)
        }
    }
    Column(
        modifier = Modifier
            .clickable {
                vm.assignTaskToSchedule(
                    Triple(
                        task,
                        null,
                        LocalDate.now()
                    )
                ); vm.openDialog(showDialog.task_detail)
            }
            .width(140.dp)
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(8.dp)),
        verticalArrangement = Arrangement.Center
    ) {
        val timeText = if(isOverSchedule)
            if (time.first >= 100)
                "+" + time.first.toString() + "h"
            else
                "+" + time.first.toString() + "h" + time.second.toString() + "m"
        else
            if (time.first >= 100)
                time.first.toString() + "h"
            else
                time.first.toString() + "h" + time.second.toString() + "m"

        Row(
            modifier = Modifier
                .padding(5.dp)
        ) {
            TextTrim(inputText = task.name, desiredLength = 5, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))  // Usa il peso per spingere il testo a destra
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    //fontWeight = FontWeight.Bold,  // Testo in grassetto
                    color = MaterialTheme.colorScheme.primary // Cambio colore per maggiore visibilità
                )
            )
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (memberTime!=null) {
                // get scheduled member
                MemberIcon(
                    modifierScale = Modifier.scale(0.6f),
                    modifierPadding = Modifier.padding(0.dp, 0.dp, 8.dp, 8.dp),
                    member = memberTime.key.first,
                )
            } else {
                // are not scheduled, so taskstoassign use the logged member
                MemberIcon(
                    modifierScale = Modifier.scale(0.6f),
                    modifierPadding = Modifier.padding(0.dp, 0.dp, 8.dp, 8.dp),
                    member = vm.memberProfile!!,

                )
            }
        }
    }
}


//OGGETTO GIORNO DELLA SETTIMANA
@Composable
fun DayItem(date: CalendarUiModel.Date) {
    Card(
        modifier = Modifier
            .padding(vertical = 7.dp, horizontal = 5.dp)
            //.clickable { onClickListener(date) }
            .size(width = 48.dp, height = 68.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (date.isSelected) {
                Color(0xff018FF3)
            } else {
                MaterialTheme.colorScheme.primary
            }
        ),
    ) {
        Text(
            text = date.day,// Lun Mar
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = date.date.dayOfMonth.toString(),// 15 16
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

//RIGA CON GIORNO E TASK
@Composable
fun VerticalDayEventScheduler(
    data: CalendarUiModel,
    dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
    vm: Calendar = viewModel(factory = Factory(LocalContext.current))
) {
    Box(modifier = Modifier.fillMaxHeight(0.8f)) {
        LazyColumn {
            items(items = data.visibleDates) { date ->
                val currentDate =
                    remember { mutableStateOf(date) } // Create a mutable state for the date
                currentDate.value = date // Update the date whenever it changes
                Row {
                    Column(modifier = Modifier.height(68.dp)) {
                        DayItem(date = date)
                    }
                    Column {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .padding(vertical = 2.dp)
                                .dropTarget(
                                    state = dragAndDropState,
                                    key = date.hashCode(), // Unique key for each drop target
                                    onDrop = { state -> // Data passed from the draggable item
                                        // take the date to schedule from the currentDate
                                        if (state.data.second != null) {
                                            // data passed from the DraggableItem, so move from 1 day to another
                                            vm.checkDialogs(
                                                state.data.first,
                                                state.data.second,
                                                currentDate.value.date,
                                                isNewSchedule = false
                                            )
                                            // trigger dialog
                                            vm.assignTaskToSchedule(
                                                Triple(
                                                    state.data.first,
                                                    state.data.second!!,
                                                    currentDate.value.date
                                                )
                                            )
                                        } else {
                                            // no data passed from the DraggableItem, so coming from the bottom
                                            vm.checkDialogs(
                                                state.data.first,
                                                null,
                                                currentDate.value.date,
                                                isNewSchedule = true
                                            )
                                            // trigger dialog
                                            vm.assignTaskToSchedule(
                                                Triple(
                                                    state.data.first,
                                                    null,
                                                    currentDate.value.date
                                                )
                                            )
                                        }

                                        if (vm.selectedShowDialog == showDialog.none) {
                                            // no new schedule, simply reschedule without dialogs
                                            // data passed from the DraggableItem, so move from 1 day to another
                                            val task = state.data.first
                                            val oldDate = state.data.second
                                            val hoursToSchedule = task.schedules.get(Pair(vm.memberProfile,oldDate))
                                            // remove the old day scheduled and add the new one
                                            vm.unScheduleTask(task, oldDate!!)
                                            if (hoursToSchedule != null) {
                                                vm.scheduleTask(
                                                    task,
                                                    currentDate.value.date,
                                                    hoursToSchedule
                                                )
                                            }
                                            // reset the task status
                                            vm.assignTaskToSchedule(null)
                                        }
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            item(1) {
                                vm.viewedScheduledTasks.filter { it.schedules.any { it.key.second == date.date } }.forEach { task ->
                                    task.schedules.filter { it.key.second == date.date }.forEach { it ->
                                        val memberId = it.key.first.id
                                        DraggableItem(
                                            state = dragAndDropState,
                                            key = task.id + memberId + date.hashCode(),// + task.schedules.keys.filter { it.second==date.date }[0].first.hashCode(), // Unique key for each draggable item
                                            data = Pair(
                                                task,
                                                date.date
                                            ), // Data to be passed to the drop target
                                            dragAfterLongPress = true
                                        ) {
                                            EventItem(
                                                task = task,
                                                scheduleEntry = it,
                                                date = date.date,
                                                isScheduled = true
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun VerticalTasksToAssign(
    dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
    vm: Calendar = viewModel(factory = Factory(LocalContext.current))
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Your Tasks to complete",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp, top = 5.dp)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .horizontalScroll(rememberScrollState())
                    .dropTarget(
                        state = dragAndDropState,
                        key = Int.MAX_VALUE, // Unique key for each drop target
                        onDrop = { state -> // Data passed from the draggable item
                            // Unschedule only if the data was passed, otherwise already unscheduled
                            if (state.data.second != null) {
                                vm.checkDialogs(state.data.first, state.data.second!!, LocalDate.now())
                                // Unschedule only if i have the permission to do it
                                if (vm.selectedShowDialog != showDialog.no_permission) {
                                    vm.unScheduleTask(state.data.first, state.data.second!!)
                                } else {
                                    // trigger the no permission alert
                                    vm.assignTaskToSchedule(
                                        Triple(
                                            state.data.first,
                                            null,
                                            state.data.second!!
                                        )
                                    )
                                }
                            }
                        }
                    )
                //.border(1.dp, Color.Gray),  // Aggiunge un bordo per visibilità
            ) {
                vm.tasksToAssign.forEach { task ->
                    DraggableItem(
                        state = dragAndDropState,
                        key = task.id, // Unique key for each draggable item
                        data = Pair(task, null), // Data to be passed to the drop target
                        dragAfterLongPress = true
                    ) {
                        EventItem(task = task, isScheduled = false)
                    }
                }
            }
        }
    }
}