package it.polito.uniteam.gui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.classes.Task
import java.time.LocalDate

@Composable
fun ScheduleTaskDialog(
    vm: Calendar = viewModel()
) {
    val taskScheduleDatePair = vm.taskToSchedule!!
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
                taskScheduleDatePair.third,
                scheduledHours.value.toInt()
            )
            // reset the task status and close the dialog
            vm.assignTaskToSchedule(null)
            vm.closeDialog()
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
                    // reset the task status and close the dialog
                    vm.assignTaskToSchedule(null)
                    vm.closeDialog()
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
                onClick = {
                    // reset the task status and close the dialog
                    vm.assignTaskToSchedule(null)
                    vm.closeDialog()
                }
            ) {
                Text("Ok", color = Color.White)
            }
        }
    )
}

@Composable
fun ScheduleBackInTimeDialog(vm: Calendar = viewModel()) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Warning, contentDescription = "Back In Time")
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
                        val hoursToSchedule = task.schedules.get(oldDate)
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
                Text("Schedule Task", color = Color.White)
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
                Text("Undo", color = Color.White)
            }
        }
    )
}