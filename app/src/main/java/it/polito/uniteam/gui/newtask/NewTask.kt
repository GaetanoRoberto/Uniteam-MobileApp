package it.polito.uniteam.gui.newtask

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import java.text.SimpleDateFormat
import java.util.Locale


class taskCreation : ViewModel(){

    var taskName by mutableStateOf("")
        private set
    var taskError by mutableStateOf("")
        private set
    fun changeTaskName(s: String){
        taskName = s
    }
    private fun checkTaskName(){
        if(taskName.isBlank())
            taskError = "Task name cannot be blank!"
        else
            taskError = ""
    }



    var description by mutableStateOf("")
        private set
    var descriptionError by mutableStateOf("")

    fun changeDescription(s: String){
        description = s
    }
    private fun checkDescription(){
        if(description.isBlank())
            descriptionError = "Task description cannot be blank!"
        else
            descriptionError = ""
    }


    var category by mutableStateOf("")
        private set
    var categoryError by mutableStateOf("")

    fun changeCategory(s: String){
        category = s
    }
    private fun checkCategory(){
        if(category.isBlank())
            categoryError = "Task category cannot be blank!"
        else
            categoryError = ""
    }


    var priority by mutableStateOf("")
        private set
    var priorityError by mutableStateOf("")

    fun changePriority(s: String){
        priority = s
    }
    private fun checkPriority(){
        if(priority.isBlank())
            priorityError = "Task priority cannot be blank!"
        else
            priorityError = ""
    }



    var deadline by mutableStateOf("")
        private set
    var deadlineError by mutableStateOf("")

    fun changeDeadline(s: String){
        deadline = s
    }
    private fun checkDeadline(dateStr: String, format: String){
        val dateFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.isLenient = false
        try {
            sdf.parse(dateStr)
            deadlineError = ""
        } catch (e: Exception) {
            deadlineError = "Invalid date"
        }
    }


    var state by mutableStateOf(Status.TODO)
        private set
    var stateError by mutableStateOf("")

    fun changeState(s: Status){
        state = s
    }



    var estimatedHours by mutableIntStateOf(0)
        private set
    var estimatedHoursError by mutableStateOf("")

    fun changeEstimatedHours(h: Int){
        estimatedHours = h
    }
    private fun checkEstimatedHours(){
        if(estimatedHours < 0)
            estimatedHoursError = "Task estimated hours must be greater than 0"
        else
            estimatedHoursError = ""
    }



    var spentHours by mutableIntStateOf(0)
        private set
    var spentHoursError by mutableStateOf("")

    fun changeSpentHours(h: Int){
        spentHours = h
    }
    private fun checkSpentHours(){
        if(spentHours < 0)
            spentHoursError = "Task spent hours must be greater than 0"
        else
            spentHoursError = ""
    }


    var members = mutableStateOf<List<String>>(emptyList())
        private set
    var membersError by mutableStateOf("")

    fun addMembers(m: String){
        members.value.toMutableList().add(m)
    }
    fun removeMembers(m: String){
        members.value.toMutableList().remove(m)
    }
    private fun checkMembers(){
        if(members.value.count() <0)
            spentHoursError = "Almost a member should be assigned"
        else
            spentHoursError = ""
    }

    var repeatable by mutableStateOf(Repetition.NONE)
        private set
    fun changeRepetition(r: Repetition){
        repeatable = r
    }





}

@Preview
@Composable
fun TaskDetailView(vm: taskCreation = viewModel() ){

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())){
        RowItem(title = "Name:", value =vm.taskName)
        RowItem(title = "Description:", value =vm.description)
        RowItem(title = "Category:", value =vm.category)
        RowItem(title = "Priority:", value =vm.priority)
        RowItem(title = "Deadline:", value =vm.deadline)
        RowItem(title = "Estimated Hours:", value =vm.estimatedHours)
        RowItem(title = "Spent Hours:", value =vm.spentHours)
        RowItem(title = "Repeatable:", value =vm.repeatable)
        RowItem(title = "Members:", value =vm.members)
        RowItem(title = "Status:", value =vm.state)

    }
}
/*
@Composable
fun RowItem(title: String, value: Any){
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start){
        Text(title)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start){
            Text(text= value.toString())

        }
    }


}

 */

@Composable
fun RowItem(modifier: Modifier = Modifier, title: String, value: Any) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            title,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp, 0.dp),
            style = MaterialTheme.typography.headlineSmall,
        )

    }
    Row(
        modifier = modifier,
    ) {
        Text(
            value.toString(),
            modifier = Modifier
                .weight(1f)
                .padding(16.dp, 0.dp),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
    Spacer(modifier = Modifier.padding(5.dp))
}




