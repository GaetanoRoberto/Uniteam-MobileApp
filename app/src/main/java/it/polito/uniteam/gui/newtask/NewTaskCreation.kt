package it.polito.uniteam.gui.newtask
/*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.R
import it.polito.uniteam.classes.MemberPreview
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.isRepetition
import it.polito.uniteam.gui.showtaskdetails.CustomDatePickerPreview
import it.polito.uniteam.gui.showtaskdetails.Demo_ExposedDropdownMenuBox
import it.polito.uniteam.gui.showtaskdetails.MembersDropdownMenuBox
import it.polito.uniteam.gui.showtaskdetails.RowItem
import it.polito.uniteam.gui.showtaskdetails.RowMemberItem
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale


class emptyTaskDetails : ViewModel(){

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


    var category by mutableStateOf("")
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


    var priority by mutableStateOf("")
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



    var deadline by mutableStateOf(LocalDate.now().toString())
        private set
    var deadlineError by mutableStateOf("")
        private set
    fun changeDeadline(s: String){
        deadline = s
    }
    private fun checkDeadline(){
        val dateFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        sdf.isLenient = false
        try {
            sdf.parse(deadline)
            deadlineError = ""
        } catch (e: Exception) {
            deadlineError = "Invalid date"
        }
    }


    var state by mutableStateOf(Status.TODO.toString())
        private set
    var stateError by mutableStateOf("")
        private set
    fun changeState(s: String){
        state = s
    }
    val possibleStates = listOf(Status.TODO.toString(),  Status.IN_PROGRESS.toString(), Status.COMPLETED.toString())



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
        if(estimatedHours == ""){
            estimatedHoursError = "Task estimated hours cannot be blank!"

        }
        else if(estimatedHours.toInt() < 0)
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
        if(spentHours == ""){
            spentHoursError = "Task estimated hours cannot be blank!"
        }
        else if(spentHours.toInt() < 0)
            spentHoursError = "Task spent hours must be greater than 0"
        else
            spentHoursError = ""
    }


    var members: MutableState<MutableList<MemberPreview>> = mutableStateOf(mutableListOf())
        private set
    val possilbleMembersPreview = listOf(MemberPreview("Nick"),MemberPreview("Nick2", R.drawable.user_icon_blue),MemberPreview("Nick3", R.drawable.user_icon_blue),MemberPreview("Nick4", R.drawable.user_icon_blue),MemberPreview("Nick5", R.drawable.user_icon_blue),MemberPreview("Nick6", R.drawable.user_icon_blue),MemberPreview("Nick7", R.drawable.user_icon_blue) )
    var membersError by mutableStateOf("")
        private set
    fun addMembers(m: MemberPreview){
        members.value = members.value.toMutableList().apply { add(m) }
    }
    fun removeMembers(m: MemberPreview){
        members.value = members.value.toMutableList().apply { remove(m) }
    }
    private fun checkMembers(){
        if(members.value.count() <=0)
            membersError = "Almost a member should be assigned"
        else
            membersError = ""
    }


    var repeatable by mutableStateOf(Repetition.NONE.toString())
        private set
    val repeatableValues = listOf<String>(Repetition.NONE.toString(), Repetition.DAILY.toString(), Repetition.WEEKLY.toString(), Repetition.MONTHLY.toString())
    fun changeRepetition(r: String){
        if(r.isRepetition()){
            repeatable = r
        }
    }

    fun validate(){
        checkTaskName()
        checkDescription()
        checkCategory()
        checkPriority()
        checkDeadline()
        checkEstimatedHours()
        checkSpentHours()
        checkMembers()

    }

    var editing by mutableStateOf(false)
    fun changeEditing(){
        editing = !editing
    }


    // before states to cancel an edit
    var taskNameBefore = ""
    var descriptionBefore= ""
    var categoryBefore = ""
    var priorityBefore = ""
    var deadlineBefore = ""
    var estimateHoursBefore = ""
    var spentHoursBefore = ""
    var repeatableBefore = ""
    var statusBefore = ""
    var membersBefore = mutableListOf<MemberPreview>()

    fun enterEditingMode(){
        taskNameBefore = taskName
        descriptionBefore = description
        categoryBefore = category
        priorityBefore = priority
        deadlineBefore = deadline
        estimateHoursBefore = estimatedHours
        spentHoursBefore = spentHours
        repeatableBefore = repeatable
        statusBefore = state
        membersBefore = members.value
    }

    fun cancelEdit(){
        taskName = taskNameBefore
        description =descriptionBefore
        category = categoryBefore
        priority = priorityBefore
        deadline = deadlineBefore
        estimatedHours = estimateHoursBefore
        spentHours = spentHoursBefore
        repeatable = repeatableBefore
        state = statusBefore
        members.value = membersBefore

        taskError = ""
        descriptionError = ""
        categoryError= ""
        priorityError = ""
        deadlineError = ""
        estimatedHoursError = ""
        spentHoursError = ""
        membersError = ""
        noTask = true
    }
    var noTask by mutableStateOf(true)


}


@Preview
@Composable
fun TaskScreen(vm: emptyTaskDetails = viewModel()) {

    if (vm.noTask){
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                vm.changeEditing()
                vm.enterEditingMode()
                vm.noTask = false
            }) {
                Text(text = "Create a new task",style = MaterialTheme.typography.headlineSmall)
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
    }
    else if(vm.editing){
        EditTaskView()
    }else{
        TaskDetailsView()
    }

}


@Preview
@Composable
fun TaskDetailsView(vm: emptyTaskDetails = viewModel() ){

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())){
        Spacer(modifier = Modifier.padding(10.dp))
        Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                vm.changeEditing()
                vm.enterEditingMode()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
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
fun EditTaskView(vm: emptyTaskDetails = viewModel() ){

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(10.dp, 0.dp)){
        Spacer(modifier = Modifier.padding(10.dp))
        it.polito.uniteam.gui.showtaskdetails.EditRowItem(label = "Name:", value = vm.taskName, errorText =vm.taskError, onChange = vm::changeTaskName )
        it.polito.uniteam.gui.showtaskdetails.EditRowItem(label = "Description:", value =vm.description, errorText =vm.descriptionError, onChange =vm::changeDescription )
        it.polito.uniteam.gui.showtaskdetails.EditRowItem(label = "Category:", value =vm.category, errorText =vm.categoryError, onChange = vm::changeCategory )
        it.polito.uniteam.gui.showtaskdetails.EditRowItem(label = "Priority:", value =vm.priority, errorText =vm.priorityError, onChange =vm::changePriority )
        CustomDatePickerPreview("Deadline", vm.deadline, vm::changeDeadline)
        it.polito.uniteam.gui.showtaskdetails.EditRowItem(label = "Estimated Hours:", value =vm.estimatedHours, errorText =vm.estimatedHoursError, onChange = vm::changeEstimatedHours)
        it.polito.uniteam.gui.showtaskdetails.EditRowItem(label = "Spent Hours:", value =vm.spentHours, errorText =vm.spentHoursError, onChange = vm::changeSpentHours )
        Demo_ExposedDropdownMenuBox("Repeatable", vm.repeatable, vm.repeatableValues, vm::changeRepetition)
        Demo_ExposedDropdownMenuBox("Status",vm.state, vm.possibleStates, vm::changeState)
        MembersDropdownMenuBox(
            vm,
            "AddMembers",
            vm.members,
            vm.possilbleMembersPreview,
            vm::addMembers,
            vm::removeMembers,
            vm.membersError
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 8.dp, 0.dp, 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TextButton(onClick = {
                    vm.validate()

                    if(vm.taskError == "" && vm.descriptionError == "" && vm.categoryError == "" && vm.deadlineError == "" && vm.estimatedHoursError == "" && vm.spentHoursError == "" && vm.priorityError == "" ){
                        vm.changeEditing()
                    } }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Create", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(modifier = Modifier.weight(1f)) {
                TextButton(onClick = {
                    vm.cancelEdit()
                    vm.changeEditing()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

    }
}


*/
