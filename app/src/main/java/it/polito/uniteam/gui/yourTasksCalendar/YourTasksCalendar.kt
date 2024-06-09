package it.polito.uniteam.gui.yourTasksCalendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.TaskForCalendar
import it.polito.uniteam.classes.TextTrim
import java.time.LocalDate

class YourTasksCalendarViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    var memberId = "d67br0MqJf6Qs1tzKHhm" // TODO hardcoded
        private set
}

@Composable
fun YourTasksCalendarView(vm: YourTasksCalendarViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))){
    var initialDate = ""
    val userTasks = AppStateManager.getTasks().filter { it.members.contains(vm.memberId) }
    val taskOrdered:MutableList<TaskForCalendar> = mutableListOf()
    for(task in userTasks) {
        // get team name for that task
        val teamName = AppStateManager.getTeams().find { it.tasks.contains(task.id) }?.name
        for((k,v) in task.schedules){
            // filter schedules of that member and >= today
            if(k.first == vm.memberId && k.second.isAfter(LocalDate.now().minusDays(1)))
                taskOrdered.add(TaskForCalendar(id = task.id, team = teamName!!, name=task.name, date = k.second, scheduledTime = v, deadline = task.deadline))
        }
    }
    taskOrdered.sortBy { it.date }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (taskOrdered.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(100.dp))
                Text(text = "No Task scheduled from now on.", textAlign = TextAlign.Center)
            }
        }

        key(taskOrdered.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(0.dp, 10.dp, 0.dp, 0.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
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
                    TaskItem(task = task)
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }

        }
    }
}

@Composable
fun TaskItem(task: TaskForCalendar) {
    val controller = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .clickable { controller.navigate("Task/${task.id}") }
            .fillMaxWidth()
            .heightIn(min = 60.dp, max = 100.dp)
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(8.dp)),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(modifier = Modifier.weight(3f), horizontalAlignment = Alignment.Start) {
            Row(
                modifier = Modifier
                    .padding(5.dp)
            ) {
                Text(text = task.team, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(
                modifier = Modifier
                    .padding(5.dp)
            ) {
                Text(text = task.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(modifier = Modifier
            .weight(1f)
            .heightIn(min = 60.dp, max = 100.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = task.scheduledTime.first.toString() + "h" + task.scheduledTime.second.toString() + "m",
                style = MaterialTheme.typography.bodyLarge.copy(
                    //fontWeight = FontWeight.Bold,  // Testo in grassetto
                    color = MaterialTheme.colorScheme.primary // Cambio colore per maggiore visibilità
                )
            )

            if(task.deadline.isEqual(LocalDate.now())) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Deadline",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        //fontWeight = FontWeight.Bold,  // Testo in grassetto
                        color = MaterialTheme.colorScheme.error // Cambio colore per maggiore visibilità
                    )
                )
            }
        }
    }
}
