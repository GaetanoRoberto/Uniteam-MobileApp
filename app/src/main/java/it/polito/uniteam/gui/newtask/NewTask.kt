package it.polito.uniteam.gui.newtask

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    var nameError by mutableStateOf("")
        private set
    fun changeTaskName(s: String){
        taskName = s
    }
    private fun checkTaskName(){
        if(taskName.isBlank())
            nameError = "Task name cannot be blank!"
        else
            nameError = ""
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

    fun changeEstimatedHours(h: String){
        estimatedHours = h.toInt()
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

    fun changeSpentHours(h: String){
        spentHours = h.toInt()
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
        EditRowItem(label = "Name:", value = vm.taskName, errorText =vm.nameError, onChange = vm::changeTaskName )
        EditRowItem(label = "Description:", value =vm.description, errorText =vm.descriptionError, onChange =vm::changeDescription )
        EditRowItem(label = "Category:", value =vm.category, errorText =vm.categoryError, onChange = vm::changeCategory )
        EditRowItem(label = "Priority:", value =vm.priority, errorText =vm.priorityError, onChange =vm::changePriority )
        EditRowItem(label = "Deadline:", value =vm.deadline, errorText =vm.deadlineError, onChange = vm::changeDeadline )
        EditRowItem(label = "Estimated Hours:", value =vm.estimatedHours.toString(), errorText =vm.estimatedHoursError, onChange = vm::changeEstimatedHours)
        EditRowItem(label = "Spent Hours:", value =vm.spentHours.toString(), errorText =vm.spentHoursError, onChange = vm::changeSpentHours )
        DropdownMenuItem(
            text = { Text("Label text") },
            onClick = { /* Handle click */ },
            leadingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null
                )
            })

        //EditRowItem(label = "Repeatable:", value =vm.repeatable.toString(), errorText ="", onChange = vm::changeRepetition)
        //EditRowItem(label = "Members:", value =vm.members, errorText =vm.membersError, onChange = vm:: )
        //EditRowItem(label = "Status:", value =vm.state.toString(), errorText =vm.stateError, onChange = vm::changeState)
    }
}



@Composable
fun EditRowItem(value: String, keyboardType: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit, label: String, errorText: String) {
    OutlinedTextField(
        value = value,
        modifier = Modifier.fillMaxWidth(0.8f),
        onValueChange = onChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
            ) },
        isError = errorText.isNotBlank(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            autoCorrectEnabled = true,
            imeAction = ImeAction.Done
        ),

        )
    if (errorText.isNotBlank())
        Text(errorText, color = MaterialTheme.colorScheme.error)
}




