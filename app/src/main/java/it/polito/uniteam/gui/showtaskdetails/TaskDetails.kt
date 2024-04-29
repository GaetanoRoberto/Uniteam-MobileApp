package it.polito.uniteam.gui.showtaskdetails


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.R
import it.polito.uniteam.classes.MemberPreview
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.gui.newtask.taskCreation
import java.text.SimpleDateFormat
import java.util.Locale


class taskDetails : ViewModel(){

    var taskName by mutableStateOf("Task Name value")
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


    var description by mutableStateOf("Description value")
        private set
    var descriptionError by mutableStateOf("")
        private set
    fun changeDescription(s: String){
        description = s
    }
    private fun checkDescription(){
        if(description.isBlank())
            descriptionError = "Task description cannot be blank!"
        else
            descriptionError = ""
    }


    var category by mutableStateOf("Category value")
        private set
    var categoryError by mutableStateOf("")
        private set
    fun changeCategory(s: String){
        category = s
    }
    private fun checkCategory(){
        if(category.isBlank())
            categoryError = "Task category cannot be blank!"
        else
            categoryError = ""
    }


    var priority by mutableStateOf("Priority value")
        private set
    var priorityError by mutableStateOf("")
        private set
    fun changePriority(s: String){
        priority = s
    }
    private fun checkPriority(){
        if(priority.isBlank())
            priorityError = "Task priority cannot be blank!"
        else
            priorityError = ""
    }



    var deadline by mutableStateOf("Deadline value")
        private set
    var deadlineError by mutableStateOf("")
        private set
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
        private set
    fun changeState(s: Status){
        state = s
    }



    var estimatedHours by mutableStateOf("")
        private set
    var estimatedHoursError by mutableStateOf("")
        private set
    fun changeEstimatedHours(h: String){
        var check = h.toIntOrNull()
        if(h == ""){
            estimatedHours = ""
        }
        else if (check != null){
            estimatedHours = h
            estimatedHoursError = ""
        }
        else{
            estimatedHoursError = "The value must be an Integer"
        }
    }
    private fun checkEstimatedHours(){
        if(estimatedHours.toInt() < 0)
            estimatedHoursError = "Task estimated hours must be greater than 0"
        else
            estimatedHoursError = ""
    }



    var spentHours by mutableStateOf("")
        private set
    var spentHoursError by mutableStateOf("")
        private set
    fun changeSpentHours(h: String){
        var check = h.toIntOrNull()
        if(h == ""){
            spentHours = ""
        }
        else if (check != null){
            spentHours = h
            spentHoursError = ""
        }
        else{
            spentHoursError = "The value must be an Integer"
        }
    }
    private fun checkSpentHours(){
        if(spentHours.toInt() < 0)
            spentHoursError = "Task spent hours must be greater than 0"
        else
            spentHoursError = ""
    }


    var members = mutableStateOf<List<MemberPreview>>(listOf(MemberPreview("Nick"), MemberPreview("Nick2", R.drawable.user_icon_blue)))
        private set
    var membersError by mutableStateOf("")
        private set
    fun addMembers(m: MemberPreview){
        members.value.toMutableList().add(m)
    }
    fun removeMembers(m: MemberPreview){
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
fun TaskDetailsView(vm: taskDetails = viewModel() ){

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())){
        Spacer(modifier = Modifier.padding(10.dp))
        RowItem(title = "Name:", value =vm.taskName)
        RowItem(title = "Description:", value =vm.description)
        RowItem(title = "Category:", value =vm.category)
        RowItem(title = "Priority:", value =vm.priority)
        RowItem(title = "Deadline:", value =vm.deadline)
        
        RowItem(title = "Estimated Hours:", value =vm.estimatedHours)
        RowItem(title = "Spent Hours:", value =vm.spentHours)
        RowItem(title = "Repeatable:", value =vm.repeatable)
        RowMemberItem(title = "Members:", value =vm.members.value)
        RowItem(title = "Status:", value =vm.state)

    }
}




@Preview
@Composable
fun EditTaskView(vm: taskDetails = viewModel() ){

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(10.dp, 0.dp)){
        Spacer(modifier = Modifier.padding(10.dp))
        EditRowItem(label = "Name:", value = vm.taskName, errorText =vm.taskError, onChange = vm::changeTaskName )
        EditRowItem(label = "Description:", value =vm.description, errorText =vm.descriptionError, onChange =vm::changeDescription )
        EditRowItem(label = "Category:", value =vm.category, errorText =vm.categoryError, onChange = vm::changeCategory )
        EditRowItem(label = "Priority:", value =vm.priority, errorText =vm.priorityError, onChange =vm::changePriority )
        EditRowItem(label = "Deadline:", value =vm.deadline, errorText =vm.deadlineError, onChange = vm::changeDeadline )
        EditRowItem(label = "Estimated Hours:", value =vm.estimatedHours, errorText =vm.estimatedHoursError, onChange = vm::changeEstimatedHours)
        EditRowItem(label = "Spent Hours:", value =vm.spentHours, errorText =vm.spentHoursError, onChange = vm::changeSpentHours )
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
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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



@Composable
fun RowMemberItem(modifier: Modifier = Modifier, title: String, value:List<MemberPreview> ) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp, 0.dp),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
    }
    Row(
        modifier = modifier,
    ) {
        for((i, member) in value.withIndex()){
            Image(painter = painterResource(id = member.profileImage), contentDescription = "Image", modifier = Modifier.padding(12.dp,0.dp,0.dp,0.dp).size(30.dp, 30.dp))
            Text(
                member.username.toString() + if(i< value.size -1){","} else{""},
                modifier = Modifier
                    .padding(6.dp, 0.dp),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
    Spacer(modifier = Modifier.padding(5.dp))
}



@Composable
fun EditRowItem(value: String, keyboardType: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit, label: String, errorText: String) {
    OutlinedTextField(
        value = value,
        modifier = Modifier.fillMaxWidth(1f),
        onValueChange = onChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
            ) },
        isError = errorText.isNotBlank(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),

        )
    if (errorText.isNotBlank())
        Text(errorText, color = MaterialTheme.colorScheme.error)
}



