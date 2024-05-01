package it.polito.uniteam.gui.showtaskdetails


import android.graphics.Paint.Align
import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import it.polito.uniteam.R
import it.polito.uniteam.classes.Comment
import it.polito.uniteam.classes.MemberPreview
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.isRepetition
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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


    var members = mutableStateOf(mutableListOf(MemberPreview("Nick"),MemberPreview("Nick2", R.drawable.user_icon_blue)))
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
        //checkMembers()

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
    }

    var comments by mutableStateOf(mutableListOf(Comment("Marco", "Ciao", "")))

}

@Preview
@Composable
fun TaskScreen(vm: taskDetails = viewModel()) {
    if(vm.editing){
        EditTaskView()
    }
    else{
        TaskDetailsView()
    }

}


@Preview
@Composable
fun TaskDetailsView(vm: taskDetails = viewModel() ){

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())){
        Spacer(modifier = Modifier.padding(10.dp))
        Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                vm.changeEditing()
                vm.enterEditingMode()
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Add ")
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
        CustomDatePickerPreview("Deadline", vm.deadline, vm::changeDeadline)
        EditRowItem(label = "Estimated Hours:", value =vm.estimatedHours, errorText =vm.estimatedHoursError, onChange = vm::changeEstimatedHours)
        EditRowItem(label = "Spent Hours:", value =vm.spentHours, errorText =vm.spentHoursError, onChange = vm::changeSpentHours )
        Demo_ExposedDropdownMenuBox("Repeatable", vm.repeatable, vm.repeatableValues, vm::changeRepetition)
        Demo_ExposedDropdownMenuBox("Status",vm.state, vm.possibleStates, vm::changeState)
        MembersDropdownMenuBox("AddMembers",vm.members, vm.possilbleMembersPreview, vm::addMembers, vm::removeMembers, vm.membersError)
        CommentsView("",vm.members, vm.possilbleMembersPreview, vm::addMembers, vm::removeMembers, vm.membersError)
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
                    }
                                     }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Save", style = MaterialTheme.typography.bodyLarge)
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
    Divider(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp))

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
            Image(painter = painterResource(id = member.profileImage), contentDescription = "Image", modifier = Modifier
                .padding(12.dp, 0.dp, 0.dp, 0.dp)
                .size(30.dp, 30.dp))
            Text(
                member.username.toString() + if(i< value.size -1){","} else{""},
                modifier = Modifier
                    .padding(6.dp, 0.dp),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
    Divider(modifier = Modifier.padding(10.dp, 2.dp, 10.dp, 0.dp))
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
    Spacer(modifier = Modifier.padding(5.dp))

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersDropdownMenuBox(label: String, currentValue: MutableState<MutableList<MemberPreview>>, possibleValues: List<MemberPreview>, addMember: (MemberPreview) -> Unit, removeMember: (MemberPreview) -> Unit, errorText: String) {
    val context = LocalContext.current
    val values = possibleValues
    var expanded by remember { mutableStateOf(false) }
    var selectedText = currentValue.value.toMutableList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp)

    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = Modifier.fillMaxWidth()

        ) {

            OutlinedTextField(
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                    )
                },
                value = " ",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.Add, contentDescription = "Add ") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    ,
                //isError = errorText.isNotBlank(),
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .horizontalScroll(rememberScrollState())
                            .padding(0.dp, 0.dp, 5.dp, 0.dp)
                    ) {
                        selectedText.forEachIndexed { index, pair ->
                            Image(
                                painter = painterResource(id = pair.profileImage),
                                contentDescription = "Image",
                                modifier = Modifier
                                    .padding(start = if (index == 0) 12.dp else 0.dp) // Add padding only for the first image
                                    .size(25.dp, 25.dp)
                            )
                            Text(
                                text = pair.username.toString() + if(index < selectedText.size -1){" ,"}else{""},
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            if (index < selectedText.size - 1) {
                                Spacer(modifier = Modifier.width(4.dp)) // Add spacing between images and texts
                            }
                        }
                    }
                }
                
            )


            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(color = Color.White) // Set background color to avoid transparency
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .heightIn(0.dp, 200.dp) // Set max height to limit the dropdown size
                        .verticalScroll(rememberScrollState())
                ) {
                    values.forEachIndexed {i, item ->
                        val isSelected = selectedText.contains(item)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DropdownMenuItem(

                                text = { Text(text = item.username.toString()) },
                                onClick = {
                                    if (isSelected) {
                                        removeMember(item)
                                    } else {
                                        addMember(item)
                                    }
                                    //expanded = false if you want to close the dropdown any time you click on a member item
                                    Toast.makeText(context, item.username.toString(), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {if(item in currentValue.value){
                                    Icon(Icons.Default.Check, contentDescription= "Check icon", tint = Color.Green)
                                }}


                            )



                        }

                        if(i != values.size -1){
                            Divider(modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp))
                        }



                    }
                }
            }

        }


    }
    /*
    if (errorText.isNotBlank())
        Text(errorText, color = MaterialTheme.colorScheme.error)*/

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Demo_ExposedDropdownMenuBox(label: String, currentValue: String, values: List<String>, onChange: (String) -> Unit) {
    val context = LocalContext.current
    val values = values
    var expanded by remember { mutableStateOf(false) }
    var selectedText = currentValue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp)

    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = Modifier.fillMaxWidth()

        ) {
            OutlinedTextField(
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
                    ) },
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(color = Color.White) // Set background color to avoid transparency
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .heightIn(0.dp, 200.dp) // Set max height to limit the dropdown size
                ) {
                    values.forEachIndexed {i, item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                onChange(item)
                                expanded = false
                                Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if(i != values.size -1){
                            Divider(modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp))
                        }

                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    label: String,
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit
) {

    val open = remember { mutableStateOf(false)}

    if (open.value) {
        CalendarDialog(
            state = rememberUseCaseState(visible = true, true, onCloseRequest = { open.value = false } ),
            config = CalendarConfig(
                yearSelection = true,
                style = CalendarStyle.MONTH,
            ),
            selection = CalendarSelection.Date(
                selectedDate = value
            ) { newDate ->
                onValueChange(newDate)
            },
        )
    }

    OutlinedTextField(
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
            ) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { //Click event
                open.value = true
            },
        enabled = false,// <- Add this to make click event work
        value = value.format(DateTimeFormatter.ISO_DATE) ,
        onValueChange = {},
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = Color.Black,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)
    )
}

@Composable
fun CustomDatePickerPreview(label: String, value: String, onChange: (String) -> Unit){
    val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    val dt = LocalDate.parse(value, dtf)
     CustomDatePicker(
         label,
         value = dt,
         onValueChange = {onChange(it.toString())}
        )
    Spacer(modifier = Modifier.padding(5.dp))


}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsView(label: String, currentValue: MutableState<MutableList<MemberPreview>>, possibleValues: List<MemberPreview>, addMember: (MemberPreview) -> Unit, removeMember: (MemberPreview) -> Unit, errorText: String) {
    val context = LocalContext.current
    val values = possibleValues
    var expanded by remember { mutableStateOf(false) }
    var selectedText = currentValue.value.toMutableList()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
        TextButton(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
            Text(text = "Comments", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center )
        }

        TextButton(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
            Text(text = "History", modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center // Aligning text to the center
            )
        }
        TextButton(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
            Text(text = "Files", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }


        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                    )
                },
                value = "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .verticalScroll(rememberScrollState()),
            )

            Column(modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(199.dp)
                .padding(0.dp, 10.dp, 0.dp, 0.dp)
                .verticalScroll(rememberScrollState())) {
                values.forEachIndexed { index, memberPreview ->
                    Row {
                        OutlinedTextField(
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
                                ) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp, 0.dp, 0.dp, 3.dp)
                                .widthIn(10.dp, 100.dp)

                                ,
                            enabled = false,// <- Add this to make click event work
                            value = "additional",
                            onValueChange = {},
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.primary,
                                focusedBorderColor = Color.Black,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        )                    }
                }
            }


        }

    }











