package it.polito.uniteam.gui.calendar

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import it.polito.uniteam.Factory
import it.polito.uniteam.classes.Task
import java.time.LocalDate

@Composable
fun HorizontalCalendarApp(
    modifier: Modifier = Modifier,
    dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
    vm: Calendar = viewModel(factory = Factory(LocalContext.current))
) {
    // get CalendarUiModel from CalendarDataSource, and the lastSelectedDate is Today.
    var calendarUiModel by remember { mutableStateOf(vm.getData(lastSelectedDate = vm.today)) }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        HorizontalHeader(vm = vm, startDate = calendarUiModel.startDate, endDate = calendarUiModel.endDate,
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
        Row {
            Column(modifier = Modifier.weight(0.6f)) {
                HorizontalDivider(color = Color.White, thickness = 0.dp, modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp))
                HorizontalDayEventScheduler(data = calendarUiModel, dragAndDropState = dragAndDropState, vm = vm)
            }
            VerticalDivider(color = MaterialTheme.colorScheme.onPrimary, thickness = 0.dp, modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 8.dp))
            Column(modifier = Modifier.weight(0.4f)) {
                HorizontalDivider(color = Color.White, thickness = 0.dp, modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp))
                HorizontalTasksToAssign(vm = vm, dragAndDropState = dragAndDropState)
            }
        }
    }
}

@Composable
fun HorizontalHeader(
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
    Row(modifier = Modifier.padding(5.dp), horizontalArrangement = Arrangement.Center) {
        Text(
            text = month + "  " + startDate.date.dayOfMonth + " - " + endDate.date.dayOfMonth,
            modifier = Modifier
                .align(Alignment.CenterVertically),
        )
        Text(
            text = " - $year",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
        )
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
                .padding(end = 120.dp)
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

@Composable
fun HorizontalDayEventScheduler(data: CalendarUiModel,
                                dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
                                vm: Calendar = viewModel(factory = Factory(LocalContext.current))) {
    Box(modifier = Modifier.fillMaxHeight()) {
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
                                            key = task.id + date.hashCode(), // Unique key for each draggable item
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
fun HorizontalTasksToAssign(
    dragAndDropState: DragAndDropState<Pair<Task, LocalDate?>>,
    vm: Calendar = viewModel(factory = Factory(LocalContext.current))
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "Your Tasks to complete",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp, top = 5.dp)
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
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