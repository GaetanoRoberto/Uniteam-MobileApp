package it.polito.uniteam.gui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory
import it.polito.uniteam.classes.HourMinutesPicker
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.Task
import it.polito.uniteam.gui.showtaskdetails.RowItem
import it.polito.uniteam.gui.showtaskdetails.RowMemberItem
import it.polito.uniteam.isVertical
import java.time.LocalDate

@Composable
fun ScheduleTaskDialog(
    vm: Calendar = viewModel(factory = Factory(LocalContext.current))
) {
    val taskScheduleDatePair = vm.taskToSchedule!!
    val scheduledHours = remember { mutableStateOf("0") }
    val scheduledMinutes = remember { mutableStateOf("0") }
    val isError = remember { mutableStateOf("") }
    val onConfirmation = {
        try {
            val hours = scheduledHours.value.toUInt().toInt()
            val minutes = scheduledMinutes.value.toUInt().toInt()
            /* NO MORE NEEDED, CAN SCHEDULE MORE THAN ESTIMATED
            val schedulableHours = taskScheduleDatePair.first.estimatedHours - taskScheduleDatePair.first.schedules.values.sumOf { it }
            if (scheduledHours.value.isNotEmpty() && scheduledHours.value.toInt() > schedulableHours) {
                isError.value = "The Hours Inserted exceed The Schedulable Ones."
            } */
            if (hours == 0 && minutes == 0) {
                isError.value = "You Need To Schedule A Positive Time Interval."
            } else if (minutes >= 60) {
                isError.value = "Invalid Minute Value."
            } else {
                isError.value = ""
                vm.scheduleTask(
                    taskScheduleDatePair.first,
                    taskScheduleDatePair.third,
                    Pair(hours,minutes)
                )
                // reset the task status and close the dialog
                vm.assignTaskToSchedule(null)
                vm.closeDialog()
            }
        } catch (e: RuntimeException) {
            isError.value = "Valid Positive Numbers Must Be Provided."
        }
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = if(!isVertical()) Modifier.scale(0.8f) else Modifier,
        icon = {
            Icon(Icons.Default.DateRange, contentDescription = "Schedule Task", tint = MaterialTheme.colorScheme.onPrimary)
        },
        title = {
            Text(text = "Insert The Hours to Schedule the Task:", color = MaterialTheme.colorScheme.onPrimary)
        },
        text = {
            HourMinutesPicker(hourState = scheduledHours, minuteState = scheduledMinutes, errorMsg = isError)
            /*Column {
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
            }*/
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Schedule Task", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // reset the task status and close the dialog
                    vm.assignTaskToSchedule(null)
                    vm.closeDialog()
                }
            ) {
                Text("Undo", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun NoPermissionDialog(vm: Calendar = viewModel(factory = Factory(LocalContext.current))) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        icon = {
            Icon(Icons.Default.Close, contentDescription = "Permission Denied", tint = MaterialTheme.colorScheme.onPrimary)
        },
        title = {
            Text(text = "Permission Denied")
        },
        text = {
            Text(text = "You cannot edit this task, since you did not schedule it.")
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    // reset the task status and close the dialog
                    vm.assignTaskToSchedule(null)
                    vm.closeDialog()
                }
            ) {
                Text("Ok", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun ScheduleBackInTimeDialog(vm: Calendar = viewModel(factory = Factory(LocalContext.current))) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = "Back In Time", tint = MaterialTheme.colorScheme.onPrimary)
        },
        title = {
            Text(text = "Schedule Back In Time")
        },
        text = {
            Text(text = "You are trying to schedule a task back in time. Do You still want to proceed ?")
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    val (task,oldDate,newDate) = vm.taskToSchedule!!
                    if (oldDate != null) {
                        // old data passed from the state, so move from 1 day to another
                        val hoursToSchedule = task.schedules.get(Pair(vm.memberProfile,oldDate))
                        // remove the old day scheduled and add the new one
                        vm.unScheduleTask(task, oldDate)
                        if (hoursToSchedule != null) {
                            vm.scheduleTask(
                                task,
                                newDate,
                                hoursToSchedule
                            )
                        }
                        // reset the task status and close the dialog
                        vm.assignTaskToSchedule(null)
                        vm.closeDialog()
                    } else {
                        // no data passed from the state, so coming from the bottom
                        // trigger ScheduleTaskDialog
                        vm.openDialog(showDialog.schedule_task)
                    }
                }
            ) {
                Text("Schedule Task", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // reset the task status and close the dialog
                    vm.assignTaskToSchedule(null)
                    vm.closeDialog()
                }
            ) {
                Text("Undo", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun ScheduleAfterDeadlineDialog(vm: Calendar = viewModel(factory = Factory(LocalContext.current))) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = "After Deadline", tint = MaterialTheme.colorScheme.onPrimary)
        },
        title = {
            Text(text = "Schedule After task Deadline")
        },
        text = {
            Text(text = "You are trying to schedule a task after his deadline. Do You still want to proceed ?")
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    val (task,oldDate,newDate) = vm.taskToSchedule!!
                    if (oldDate != null) {
                        // old data passed from the state, so move from 1 day to another
                        val hoursToSchedule = task.schedules.get(Pair(vm.memberProfile,oldDate))
                        // remove the old day scheduled and add the new one
                        vm.unScheduleTask(task, oldDate)
                        if (hoursToSchedule != null) {
                            vm.scheduleTask(
                                task,
                                newDate,
                                hoursToSchedule
                            )
                        }
                        // reset the task status and close the dialog
                        vm.assignTaskToSchedule(null)
                        vm.closeDialog()
                    } else {
                        // no data passed from the state, so coming from the bottom
                        // trigger ScheduleTaskDialog
                        vm.openDialog(showDialog.schedule_task)
                    }
                }
            ) {
                Text("Schedule Task", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // reset the task status and close the dialog
                    vm.assignTaskToSchedule(null)
                    vm.closeDialog()
                }
            ) {
                Text("Undo", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun TaskDetailDialog(vm: Calendar = viewModel(factory = Factory(LocalContext.current))) {
    val task = vm.taskToSchedule!!.first
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        icon = {
            Icon(Icons.Default.Info, contentDescription = "Task Detail", tint = MaterialTheme.colorScheme.onPrimary)
        },
        title = {
            Text(text = "Task Info")
        },
        text = {
            Column(modifier = Modifier.fillMaxHeight(0.6f).verticalScroll(rememberScrollState())) {
                RowItem(title = "Name:", value = task.name)
                RowItem(title = "Description:", value = task.description ?: "")
                RowItem(title = "Category:", value = task.category ?: "")
                RowItem(title = "Priority:", value = task.priority)
                RowItem(title = "Deadline:", value = task.deadline.toString())
                RowItem(title = "Repeatable:", value = task.repetition)
                RowMemberItem(title = "Members:", value = task.members, selectUser = vm.selectUser)
                RowItem(title = "Status:", value = if(task.status==Status.IN_PROGRESS) "IN PROGRESS" else task.status)
            }
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    // reset the task status and close the dialog
                    vm.assignTaskToSchedule(null)
                    vm.closeDialog()
                }
            ) {
                Text("Ok", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}