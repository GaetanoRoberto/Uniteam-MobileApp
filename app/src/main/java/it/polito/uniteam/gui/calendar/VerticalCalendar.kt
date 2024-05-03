package it.polito.uniteam.gui.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import it.polito.uniteam.classes.Task
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
        ScheduleTaskDialog(taskScheduleDatePair = vm.taskToSchedule!!, vm = vm)
    }
    if (vm.haveNoPermission) {
        NoPermissionDialog(vm = vm)
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
    Column(
    ) {
        Row {
            Text(
                text = "Team #1 - Tasks ",
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
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
            Text(
                text = startDate.date.month.toString() + " " + startDate.date.dayOfMonth + " - " + endDate.date.dayOfMonth + ", " + startDate.date.year,// " MAY, 22 - 28  (2024)",
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
            )
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
            Text(text = task.name, style = MaterialTheme.typography.bodyMedium)
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
            task.members.forEach { member ->
                val painter = if (member.profileImage != null) {
                    rememberAsyncImagePainter(
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(data = member.profileImage)
                            .build()
                    )
                } else {
                    painterResource(id = R.drawable.user_icon)
                }
                Icon(
                    painter = painter,
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))  // Distanziamento tra le icone
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
    Box(modifier = Modifier.height(420.dp)) { // Imposta un'altezza fissa e abilita lo scrolling verticale
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
                                        // Check if the user is a member and can edit this task, otherwise block
                                        // take the date to schedule from the currentDate
                                        vm.checkPermission(state.data.first)
                                        if (!vm.haveNoPermission) {
                                            if (state.data.second != null) {
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
                                            } else {
                                                // no data passed from the DraggableItem, so coming from the bottom
                                                // trigger the alert as usual
                                                vm.assignTaskToSchedule(
                                                    Pair(
                                                        state.data.first,
                                                        currentDate.value.date
                                                    )
                                                )
                                            }
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
                            // Check if the user is a member and can edit this task, otherwise block
                            vm.checkPermission(state.data.first)
                            if (!vm.haveNoPermission) {
                                // Unschedule only if the data was passed, otherwise already unscheduled
                                if (state.data.second != null)
                                    vm.unScheduleTask(state.data.first, state.data.second!!)
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

// BOTH VERTICAL AND HORIZONTAL
@Composable
fun ScheduleTaskDialog(
    taskScheduleDatePair: Pair<Task, LocalDate?>,
    vm: Calendar = viewModel()
) {
    val scheduledHours = remember { mutableStateOf("") }
    val isError = remember { mutableStateOf("") }
    val onConfirmation = {
        val schedulableHours =
            taskScheduleDatePair.first.estimatedHours - taskScheduleDatePair.first.schedules.values.sumOf { it }
        if (scheduledHours.value.isNotEmpty() && scheduledHours.value.toInt() > schedulableHours) {
            isError.value = "The Hours Inserted exceed The Schedulable Ones."
        } else if (scheduledHours.value.isNotEmpty() && scheduledHours.value.toInt() == 0) {
            isError.value = "You Need To Schedule At Least One Hour."
        } else {
            isError.value = ""
            vm.scheduleTask(
                taskScheduleDatePair.first,
                taskScheduleDatePair.second!!,
                scheduledHours.value.toInt()
            )
            vm.assignTaskToSchedule(null)
        }
    }

    AlertDialog(
        modifier = Modifier.scale(0.8f),
        icon = {
            Icon(Icons.Default.DateRange, contentDescription = "Schedule Task")
        },
        title = {
            Text(text = "Insert The Hours to Schedule the Task:", color = Color.White)
        },
        text = {
            Column {
                TextField(value = scheduledHours.value,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = { value ->
                        scheduledHours.value = value
                    })
                if (isError.value.isNotEmpty())
                    Text(isError.value, color = MaterialTheme.colorScheme.error)
            }
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Schedule Task", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    vm.assignTaskToSchedule(null)
                }
            ) {
                Text("Undo", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

@Composable
fun NoPermissionDialog(vm: Calendar = viewModel()) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Close, contentDescription = "Permission Denied")
        },
        title = {
            Text(text = "Permission Denied")
        },
        text = {
            Text(text = "You cannot edit this task, since you are not a member of it.")
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = { vm.closePermissionDialog() }
            ) {
                Text("Ok", color = Color.White)
            }
        }
    )
}
