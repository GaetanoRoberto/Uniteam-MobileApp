package it.polito.uniteam.gui.calendar

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import it.polito.uniteam.R
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.TextTrim
import it.polito.uniteam.isVertical
import java.time.LocalDate

@Preview(showSystemUi = true)
@Composable
fun CalendarAppContainer(vm: Calendar = viewModel()) {
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
            showDialog.none -> {}
        }
    }
}

@Composable
fun VerticalCalendarApp(
    modifier: Modifier = Modifier,
    dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
    vm: Calendar = viewModel()
) {
    // get CalendarUiModel from CalendarDataSource, and the lastSelectedDate is Today.
    var calendarUiModel by remember { mutableStateOf(vm.getData(lastSelectedDate = vm.today)) }
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
            },
            onNextClickListener = { endDate ->
                // refresh the CalendarUiModel with new data
                // by get data with new Start Date (which is the endDate+2 from the visibleDates)
                val finalStartDate = endDate.plusDays(2)
                calendarUiModel = vm.getData(
                    startDate = finalStartDate,
                    lastSelectedDate = calendarUiModel.selectedDate.date
                )
            },
            onTodayClickListener = {
                val finalStartDate = calendarUiModel.selectedDate.date
                calendarUiModel = vm.getData(
                    startDate = finalStartDate,
                    lastSelectedDate = calendarUiModel.selectedDate.date
                )

            }

        )
        VerticalDayEventScheduler(data = calendarUiModel, dragAndDropState = dragAndDropState, vm = vm)
        VerticalTasksToAssign(vm = vm, dragAndDropState = dragAndDropState)
    }
}

@Composable
fun VerticalHeader(
    vm: Calendar = viewModel(),
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
                desiredLength = 16,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.weight(0.4f)) // Spazio flessibile per allineare la checkbox e il testo "My Tasks" alla fine
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
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                //modifier = Modifier.padding(start = 4.dp),
            )
        }
        Row {
            Column(
                modifier = Modifier.fillMaxHeight(0.1f).fillMaxWidth(0.4f),
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
            Button(onClick = onTodayClickListener) {
                Text("Today")
            }
        }
    }
}


//OGGETTO TASK
@Composable
fun EventItem(task: Task, date: LocalDate? = null, isScheduled: Boolean) {
    var hours = 0
    if (isScheduled) {
        hours = task.schedules.get(date)!!
    } else {
        hours = task.estimatedHours - task.schedules.values.sumOf { it }
    }
    Column(
        modifier = Modifier
            //.fillMaxWidth()
            .width(102.dp)
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp)),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
        ) {
            TextTrim(inputText = task.name, desiredLength = 5, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))  // Usa il peso per spingere il testo a destra
            Text(
                text = hours.toString() + "h",
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
            // handle if more than two members, display ...
            task.members.forEachIndexed { index, member ->
                if (index < 2) {
                    MemberIcon(
                        modifierScale = Modifier.scale(0.6f),
                        modifierPadding = Modifier.padding(0.dp, 0.dp, 10.dp, 10.dp),
                        member = member
                    )
                }
            }
            if (task.members.size > 2) {
                Text(text = "...", color = Color.White)
            }
        }
    }
}


//OGGETTO GIORNO DELLA SETTIMANA
@Composable
fun DayItem(date: CalendarUiModel.Date) {
    Card(
        modifier = Modifier
            .padding(vertical = 7.dp, horizontal = 1.dp)
            //.clickable { onClickListener(date) }
            .size(width = 48.dp, height = 68.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (date.isSelected) {
                Color.Magenta
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
    vm: Calendar = viewModel()
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
                                            val hoursToSchedule = task.schedules.get(oldDate)
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
                                vm.viewedScheduledTasks.filter { it.schedules.containsKey(date.date) }
                                    .forEach { task ->
                                        DraggableItem(
                                            state = dragAndDropState,
                                            key = task.id, // Unique key for each draggable item
                                            data = Pair(
                                                task,
                                                date.date
                                            ), // Data to be passed to the drop target
                                            dragAfterLongPress = true
                                        ) {
                                            EventItem(
                                                task = task,
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

@Composable
fun VerticalTasksToAssign(
    dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
    vm: Calendar = viewModel()
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
                modifier = Modifier.padding(start = 8.dp)
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
                                vm.checkDialogs(state.data.first, state.data.second!!)
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