package it.polito.uniteam.gui.yourTasksCalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.TaskForCalendar
import it.polito.uniteam.gui.TeamDetails.Factory
import it.polito.uniteam.gui.TeamDetails.TeamDetailsViewModel
import java.time.LocalDate

class YourTasksCalendarViewModel(val model: UniTeamModel): ViewModel() {
    val tasks = model.getAllTasks().filter { it.members.contains(model.loggedMember) }
    val loggedMember = model.loggedMember
    fun getAllTeams() = model.getAllTeams()
}
@Preview
@Composable
fun YourTasksCalendarViewScreen(vm: YourTasksCalendarViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))){
    YourTasksCalendarView()
}
@Composable
fun YourTasksCalendarView(vm: YourTasksCalendarViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))){
    var initialDate = ""
    val taskOrdered:MutableList<TaskForCalendar> = mutableListOf()
    for(team in vm.getAllTeams()){
        if(team.members.contains(vm.loggedMember)){
            //member is part of the team
            for (task in team.tasks){
                if(task.members.contains(vm.loggedMember)){
                    //task is assigned to the member
                    for((k,v) in task.schedules){
                        taskOrdered.add(TaskForCalendar(team = team.name, name=task.name, date = k, estimatedTime = v))
                    }
                }else{
                    //task is not assigned to the member
                    continue
                }
            }
        }else{
            //member is not part of the team
            continue
        }
    }
    taskOrdered.sortBy { it.date }
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary))
    ) {
        key(taskOrdered.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height((screenHeightDp * 0.7).dp)
                    .padding(0.dp, 10.dp, 0.dp, 0.dp)
                    .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(10.dp, 0.dp, 0.dp, 10.dp)) {
                    Text("Your Tasks", style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
                    )}
                taskOrdered.forEachIndexed { index, task ->

                    if (task.date.toString() != initialDate) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(

                                text = if(task.date.isEqual(LocalDate.now())){ "Today"} else if(task.date.isEqual(LocalDate.now().plusDays(1)))"Tomorrow" else task.date.toString(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            initialDate = task.date.toString()
                        }

                    }
                    Row {
                        OutlinedTextField(
                            label = {Text(
                                text = task.team,
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
                            )},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp, 0.dp, 0.dp, 5.dp)
                                .widthIn(10.dp, 100.dp),
                            enabled = false,// <- Add this to make click event work
                            value = task.name,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                            onValueChange = {},
                            trailingIcon = {
                                Box(
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text(
                                        text = task.estimatedTime.first.toString() + "," + task.estimatedTime.second.toString() + "h",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                }

                            }
                        )
                    }
                }
            }

        }
    }
}
