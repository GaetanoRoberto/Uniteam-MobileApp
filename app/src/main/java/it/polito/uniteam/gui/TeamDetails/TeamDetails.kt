package it.polito.uniteam.gui.TeamDetails

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import it.polito.uniteam.R
import it.polito.uniteam.UniTeamApplication
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.HourMinutesPicker
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.Team
import it.polito.uniteam.gui.showtaskdetails.CustomDatePickerPreview
import it.polito.uniteam.gui.showtaskdetails.Demo_ExposedDropdownMenuBox
import it.polito.uniteam.gui.showtaskdetails.EditRowItem
import it.polito.uniteam.gui.showtaskdetails.MembersDropdownMenuBox
import it.polito.uniteam.gui.showtaskdetails.RowItem
import it.polito.uniteam.gui.showtaskdetails.RowMemberItem
import it.polito.uniteam.gui.showtaskdetails.taskDetails
import it.polito.uniteam.gui.userprofile.AlertDialogExample
import it.polito.uniteam.gui.userprofile.UserProfileScreen
import it.polito.uniteam.gui.yourTasksCalendar.YourTasksCalendarViewModel
import it.polito.uniteam.isVertical
import it.polito.uniteam.ui.theme.Orange

class TeamDetailsViewModel(val model: UniTeamModel): ViewModel() {
    // from model
    var selectedTeam = mutableStateOf( model.selectedTeam)
    // internal
    var teamNameError by mutableStateOf("")
        private set
    fun changeTeamName(s: String) {
        selectedTeam.value = selectedTeam.value.copy(name = s)
        checkTeamName()
        Log.d("Deb", selectedTeam.value.name)
    }

    private fun checkTeamName() {
        if (selectedTeam.value.name.isBlank())
            teamNameError = "Task name cannot be blank!"
        else
            teamNameError = ""
    }

    var descriptionError by mutableStateOf("")
        private set

    fun changeDescription(s: String) {
        selectedTeam.value = selectedTeam.value.copy(description = s)
        checkDescription()    }

    private fun checkDescription() {
        if (selectedTeam.value.description.isBlank())
            descriptionError = "Task description cannot be blank!"
        else
            descriptionError = ""
    }

    fun validate() {
        checkTeamName()
        checkDescription()
        if (teamNameError.isEmpty() && descriptionError.isEmpty()) {
            model.changeSelectedTeamName(selectedTeam.value.name)
            model.changeSelectedTeamDescription(selectedTeam.value.description)
            val existingTeams = model.getAllTeams().map { it.id }
            // new team creation
            if(!existingTeams.contains(selectedTeam.value.id)){
                model.addTeam(selectedTeam.value)
            }

        }

    }

    var editing by mutableStateOf(false)
    var newTeam by mutableStateOf(false)
    fun changeEditing() {
        if(editing == true){
            selectedTeam = mutableStateOf(model.selectedTeam)
            teamMembersBeforeEditing = model.selectedTeam.members.toList()

        }
        selectedTeam = mutableStateOf(model.selectedTeam)
        Log.d("model", model.selectedTeam.name)
        editing = !editing
    }

    fun teamCreation(flag: Boolean){
        newTeam = flag
    }

    fun onCancel(){
        Log.d("view", selectedTeam.value.members.size.toString())

        model.changeSelectedTeamMembers(teamMembersBeforeEditing)
    }

    fun onNew(){
        model.newTeam()
        teamCreation(true)
    }

    var openAssignDialog = mutableStateOf(false)

    var possibleMembers = model.getAllMembers()

    var teamMembersBeforeEditing = model.selectedTeam.members.toList()




}

class Factory(context: Context): ViewModelProvider.Factory{
    val model: UniTeamModel = (context.applicationContext as? UniTeamApplication)?.model ?: throw IllegalArgumentException("Bad application config")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return if(modelClass.isAssignableFrom(TeamDetailsViewModel::class.java))
            TeamDetailsViewModel(model) as T
        else if(modelClass.isAssignableFrom(YourTasksCalendarViewModel::class.java))
            YourTasksCalendarViewModel(model) as T
        else throw IllegalArgumentException("Unknown ViewModel class")

    }
}

@Preview
@Composable
fun TeamViewScreen(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))){
    if(vm.editing){
        TeamDetailsEdit()
    }else{
        TeamDetailsView()
    }
}
@Preview
@Composable
fun TeamDetailsView(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))) {

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.padding(10.dp))
        Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                vm.onNew()
                vm.changeEditing()

            }) {
                Icon(Icons.Default.Add, contentDescription = "New team ")
            }
            IconButton(onClick = {
                vm.changeEditing()
                //vm.enterEditingMode()
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit ")
            }
        }

        RowItem(title = "Name:", value = vm.selectedTeam.value.name)
        RowItem(title = "Description:", value = vm.selectedTeam.value.description.toString())
        RowMemberItem(title = "Members:", value = vm.selectedTeam.value.members)
        RowItem(title = "Creation Date:", value = vm.selectedTeam.value.creationDate.toString())

    }
}


@Preview
@Composable
fun TeamDetailsEdit(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))){
val selectedTeam = vm.selectedTeam.value
    Row(){
        Column(modifier = Modifier.fillMaxSize(),  verticalArrangement = Arrangement.Bottom) {
            Row(modifier = Modifier.fillMaxHeight(0.9f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp, 0.dp)
                ) {
                    Spacer(modifier = Modifier.padding(10.dp))
                    EditRowItem(
                        label = "Name:",
                        value = selectedTeam.name,
                        errorText = vm.teamNameError,
                        onChange = vm::changeTeamName
                    )
                    EditRowItem(
                        label = "Description:",
                        value = selectedTeam.description,
                        errorText = vm.descriptionError,
                        onChange = vm::changeDescription
                    )
                    TeamMembersDropdownMenuBox(
                        vm,
                        "AddMembers",
                        selectedTeam.members
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if(!isVertical()){

                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom){
                            Spacer(modifier = Modifier.width(15.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                    /*navController.navigate("Tasks"){
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }*/
                                    vm.onCancel()
                                    if(vm.newTeam){
                                        TODO("Navigate to team list")
                                    }
                                    vm.teamCreation(false)

                                    vm.changeEditing()
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.width(15.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                Button( colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                    vm.validate()
                                    if (vm.teamNameError == "" && vm.descriptionError == "") {

                                        vm.changeEditing()
                                        /*navController.navigate("Tasks"){
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }*/
                                    }
                                }, modifier = Modifier
                                    .fillMaxWidth()) {
                                    Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }

                        }

                    }
                }
            }
            if(isVertical()){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.fillMaxHeight()
                        .height(50.dp)
                    //.padding(0.dp, 8.dp, 0.dp, 5.dp)
                    ,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {


                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                vm.onCancel()
                                if(vm.newTeam){
                                    TODO("Navigate to team list")
                                }
                                vm.teamCreation(false)
                                vm.changeEditing()
                                /*navController.navigate("Tasks"){
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }*/

                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(15.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.width(15.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                vm.validate()
                                if (vm.teamNameError == "" && vm.descriptionError == "" ) {

                                    vm.changeEditing()
                                    /*navController.navigate("Tasks"){
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }*/

                                }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }

        }
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMembersDropdownMenuBox(
    vm: TeamDetailsViewModel,
    label: String,
    currentMembers: List<Member>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp)

    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
                vm.openAssignDialog.value = true
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
                trailingIcon = {
                    //IconButton(onClick = { vm.openAssignDialog.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add ")
                    //}
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                //isError = errorText.isNotBlank(),
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .horizontalScroll(rememberScrollState())
                            .padding(0.dp, 0.dp, 5.dp, 0.dp)
                    ) {
                        currentMembers.forEachIndexed { index, member ->
                            MemberIcon(member = member, modifierScale = Modifier.scale(0.65f), modifierPadding = Modifier.padding(start = if (index == 0) 12.dp else 0.dp))
                            Text(
                                text = member.username.toString() + if (index < currentMembers.size - 1) {
                                    ", "
                                } else {
                                    ""
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            if (index < currentMembers.size - 1) {
                                Spacer(modifier = Modifier.width(4.dp)) // Add spacing between images and texts
                            }
                        }
                    }
                }

            )
            if (vm.openAssignDialog.value) {
                TeamAssignMemberDialog(vm)
            }
        }
    }
}

@Composable
fun TeamAssignMemberDialog(vm: TeamDetailsViewModel) {
    val selectedTeam = vm.selectedTeam.value

    val selectedMembers = remember { mutableStateMapOf<Member, Boolean>() }
    vm.possibleMembers.forEach { member ->
        selectedMembers[member] = vm.selectedTeam.value.members.toMutableList().contains(member)
    }
    Dialog(onDismissRequest = { vm.openAssignDialog.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp), horizontalArrangement = Arrangement.Center
                ) {
                    if (isVertical())
                        Text(
                            text = selectedTeam.name,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    else
                        Text(
                            text = selectedTeam.name,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 0.dp, 0.dp, 5.dp), horizontalArrangement = Arrangement.Start
                ) {
                    Text(text = "Members assigned :", style = MaterialTheme.typography.bodyMedium)
                }

                LazyColumn(
                    modifier = if (isVertical()) Modifier.heightIn(
                        0.dp,
                        265.dp
                    ) else Modifier.heightIn(0.dp, 165.dp)
                ) {
                    item(1) {
                        vm.possibleMembers.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMembers[member] =
                                            !(selectedMembers[member] ?: false)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedMembers[member] ?: false,
                                    onCheckedChange = { selectedMembers[member] = it }
                                )
                                Text(text = member.username.toString(), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { vm.openAssignDialog.value = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    TextButton(onClick = {
                        vm.selectedTeam.value.members.clear()
                        vm.selectedTeam.value.members.addAll(selectedMembers.filterValues { it }.keys.toMutableStateList())
                        vm.openAssignDialog.value = false
                    }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }

    }

}

