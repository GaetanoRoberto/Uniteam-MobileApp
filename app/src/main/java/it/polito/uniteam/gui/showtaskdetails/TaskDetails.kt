package it.polito.uniteam.gui.showtaskdetails


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import it.polito.uniteam.classes.File
import it.polito.uniteam.classes.History
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import it.polito.uniteam.isVertical
import androidx.compose.runtime.key
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.classes.HourMinutesPicker
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Status


//@Preview
@Composable
fun TaskScreen(vm: taskDetails = viewModel(factory = Factory(LocalContext.current)) ) {
    if (vm.editing) {
        /*vm.changeEditing()
        vm.enterEditingMode()*/
        EditTaskView()
   } else {
        /*vm.changeEditing()
        vm.enterEditingMode()
        vm.newTask()*/
        TaskDetailsView()
    }
    //Dialog per la delete del task
    when {
        vm.openDeleteTaskDialog -> {
            DeleteTaskDialog(vm)
        }
    }
}


@Preview
@Composable
fun TaskDetailsView(vm: taskDetails = viewModel(factory = Factory(LocalContext.current))) {
    /*vm.changeEditing()
    vm.enterEditingMode()
    //vm.newTask()*/
    val isVertical = isVertical()
    var scrollState = rememberScrollState()
    val isFirstRender = remember { mutableStateOf(true) }
    LaunchedEffect(vm.commentHistoryFileSelection) {
        if (isVertical && !isFirstRender.value) {
            scrollState.scrollTo(Int.MAX_VALUE)
        }
        isFirstRender.value = false
    }
    scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                scrollState
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.End) {
            FloatingActionButton(
                onClick = { vm.changeEditing(); vm.enterEditingMode(); },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 5.dp, top = 10.dp, end = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(30.dp)
                )
            }
            FloatingActionButton(
                onClick = { vm.openDeleteTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 5.dp, top = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        RowItem(title = "Name:", value = vm.taskName)
        RowItem(title = "Description:", value = vm.description)
        RowItem(title = "Category:", value = vm.category)
        RowItem(title = "Priority:", value = vm.priority)
        RowItem(title = "Deadline:", value = vm.deadline)
        RowItem(
            title = "Estimated Time:",
            value = vm.estimatedHours.value + "h " + vm.estimatedMinutes.value + "m"
        )
        RowItem(
            title = "Spent Time:",
            value = vm.spentTime.values.sumOf { it.first }.toString() + "h " + vm.spentTime.values.sumOf { it.second }.toString() + "m"
        )
        RowItem(title = "Repeatable:", value = vm.repeatable)
        //RowMemberItem( title = "Members:", value = vm.members)
        RowItem(
            title = "Status:",
            value = if (vm.status == Status.IN_PROGRESS.toString()) "IN PROGRESS" else vm.status
        )

        val icons = listOf(Icons.Filled.Comment, Icons.Filled.History, Icons.Filled.InsertDriveFile)
        val titles = listOf("Comments", "History", "Files")
        val pagerState = rememberPagerState {
            titles.size
        }
        Column {
            LaunchedEffect(vm.tabState) {
                pagerState.animateScrollToPage(vm.tabState)
            }
            LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                if (!pagerState.isScrollInProgress) {
                    vm.switchTab(pagerState.currentPage)
                    vm.changeCommentHistoryFileSelection(if (pagerState.currentPage == 0) "comments" else if (pagerState.currentPage == 1) "history" else "files")
                }
            }
            TabRow(selectedTabIndex = vm.tabState, indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.tabIndicatorOffset(tabPositions[vm.tabState])
                )
            }) {
                titles.forEachIndexed { index, title ->
                    Tab(selected = vm.tabState == index,
                        onClick = { vm.switchTab(index); vm.changeCommentHistoryFileSelection(title.lowercase()); },
                        text = { Text(text = title, color = MaterialTheme.colorScheme.onPrimary) },
                        icon = {
                            Icon(
                                icons[index],
                                title,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        })
                }
            }
        }
        HorizontalPager(
            state = pagerState, modifier = Modifier
                .fillMaxWidth(), verticalAlignment = Alignment.Top
        ) { index ->
            if (index == 0) {
                CommentsView(vm = vm)
            } else if (index == 1) {
                HistoryView(vm.history)
            } else {
                FilesView(vm = vm)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}


@SuppressLint("RestrictedApi")
@Composable
fun EditTaskView(vm: taskDetails = viewModel(factory = Factory(LocalContext.current))) {
    /*vm.changeEditing()
    vm.enterEditingMode()*/
    val controller = NavControllerManager.getNavController()
    BackHandler(onBack = {
        vm.validate()
        if (vm.taskError == "" && vm.descriptionError == "" && vm.categoryError == "" && vm.deadlineError == "" && vm.estimatedTimeError.value == "" && vm.spentTimeError.value == "" && vm.priorityError == "") {
            if(vm.newTask) {
                vm.handleHistory()
                // TODO save now navigate
                /*vm.model.addTeamTask(vm.model.selectedTeam.value.id, Task(
                    id = ++DummyDataProvider.taskId,
                    name = vm.taskName,
                    description = vm.description,
                    category = Category.valueOf(vm.category),
                    priority = Priority.valueOf(vm.priority),
                    creationDate = LocalDate.now(),
                    deadline = LocalDate.parse(vm.deadline),
                    estimatedTime = Pair(vm.estimatedHours.value.toInt(),vm.estimatedMinutes.value.toInt()),
                    spentTime = HashMap(vm.spentTime),
                    status = Status.valueOf(vm.status),
                    repetition = Repetition.valueOf(vm.repeatable),
                    members = vm.members,
                    schedules = hashMapOf(),
                    taskFiles = vm.files,
                    taskComments = vm.comments,
                    taskHistory = vm.history
                ))*/
                controller.popBackStack()
            } else {
                vm.handleHistory()
                vm.changeEditing()
            }
            /*navController.navigate("Tasks"){
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }*/
        }
    })
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
                        value = vm.taskName,
                        errorText = vm.taskError,
                        onChange = vm::changeTaskName
                    )
                    EditRowItem(
                        label = "Description:",
                        value = vm.description,
                        errorText = vm.descriptionError,
                        onChange = vm::changeDescription
                    )
                    Demo_ExposedDropdownMenuBox(
                        "Category",
                        vm.category,
                        vm.categoryValues,
                        vm::changeCategory,
                        vm.categoryError
                    )
                    Demo_ExposedDropdownMenuBox(
                        "Priority",
                        vm.priority,
                        vm.priorityValues,
                        vm::changePriority,
                        vm.priorityError
                    )
                    CustomDatePickerPreview("Deadline", vm.deadline, vm::changeDeadline)
                    Text(text = "Estimated Time:")
                    HourMinutesPicker(hourState = vm.estimatedHours, minuteState = vm.estimatedMinutes, errorMsg = vm.estimatedTimeError)
                    Text(text = "Your Spent Time To Add:")
                    HourMinutesPicker(hourState = vm.spentHours, minuteState = vm.spentMinutes, errorMsg = vm.spentTimeError)
                    Demo_ExposedDropdownMenuBox(
                        "Repeatable",
                        vm.repeatable,
                        vm.repeatableValues,
                        vm::changeRepetition
                    )
                    Demo_ExposedDropdownMenuBox("Status", if(vm.status== Status.IN_PROGRESS.toString()) "IN PROGRESS" else vm.status, vm.possibleStates, vm::changeState)
                    MembersDropdownMenuBox(
                        vm,
                        "AddMembers",
                        vm.members
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
                                    if(vm.newTask){
                                        controller.popBackStack()
                                    } else {
                                        vm.cancelEdit()
                                        vm.changeEditing()
                                    }
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.width(15.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                Button( colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                    vm.validate()
                                    if (vm.taskError == "" && vm.descriptionError == "" && vm.categoryError == "" && vm.deadlineError == "" && vm.estimatedTimeError.value == "" && vm.spentTimeError.value == "" && vm.priorityError == "") {
                                        if(vm.newTask) {
                                            vm.handleHistory()
                                            // TODO save now navigate
                                            /*vm.model.addTeamTask(vm.model.selectedTeam.value.id, Task(
                                                id = ++DummyDataProvider.taskId,
                                                name = vm.taskName,
                                                description = vm.description,
                                                category = Category.valueOf(vm.category),
                                                priority = Priority.valueOf(vm.priority),
                                                creationDate = LocalDate.now(),
                                                deadline = LocalDate.parse(vm.deadline),
                                                estimatedTime = Pair(vm.estimatedHours.value.toInt(),vm.estimatedMinutes.value.toInt()),
                                                spentTime = HashMap(vm.spentTime),
                                                status = Status.valueOf(vm.status),
                                                repetition = Repetition.valueOf(vm.repeatable),
                                                members = vm.members,
                                                schedules = hashMapOf(),
                                                taskFiles = vm.files,
                                                taskComments = vm.comments,
                                                taskHistory = vm.history
                                            ))*/
                                            controller.popBackStack()
                                        } else {
                                            vm.handleHistory()
                                            vm.changeEditing()
                                        }
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
                                if(vm.newTask){
                                    controller.popBackStack()
                                } else {
                                    vm.cancelEdit()
                                    vm.changeEditing()
                                }
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
                                if (vm.taskError == "" && vm.descriptionError == "" && vm.categoryError == "" && vm.deadlineError == "" && vm.estimatedTimeError.value == "" && vm.spentTimeError.value == "" && vm.priorityError == "") {
                                    if(vm.newTask) {
                                        vm.handleHistory()
                                        // TODO save now navigate
                                        /*vm.model.addTeamTask(vm.model.selectedTeam.value.id, Task(
                                            id = ++DummyDataProvider.taskId,
                                            name = vm.taskName,
                                            description = vm.description,
                                            category = Category.valueOf(vm.category),
                                            priority = Priority.valueOf(vm.priority),
                                            creationDate = LocalDate.now(),
                                            deadline = LocalDate.parse(vm.deadline),
                                            estimatedTime = Pair(vm.estimatedHours.value.toInt(),vm.estimatedMinutes.value.toInt()),
                                            spentTime = HashMap(vm.spentTime),
                                            status = Status.valueOf(vm.status),
                                            repetition = Repetition.valueOf(vm.repeatable),
                                            members = vm.members,
                                            schedules = hashMapOf(),
                                            taskFiles = vm.files,
                                            taskComments = vm.comments,
                                            taskHistory = vm.history
                                        ))*/
                                        controller.popBackStack()
                                    } else {
                                        vm.handleHistory()
                                        vm.changeEditing()
                                    }
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
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
            style = MaterialTheme.typography.bodyLarge,
        )

    }
    Divider(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp))

    Spacer(modifier = Modifier.padding(5.dp))
}


@Composable
fun RowMemberItem(loggedMember: String,dialogAction: () -> Unit = {}, loggedMemberAction: () -> Unit = {}, modifier: Modifier = Modifier, title: String, value: List<MemberDBFinal>) {
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
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        for ((i, member) in value.withIndex()) {
            MemberIcon(member = member, modifierScale = Modifier.scale(0.65f), modifierPadding = Modifier.padding(start = if (i == 0) 16.dp else 0.dp, top = 8.dp), dialogAction = dialogAction, loggedMemberAction = if(member.id == loggedMember) loggedMemberAction else null)
            Text(
                member.username.toString() + if (i < value.size - 1) {
                    ", "
                } else {
                    ""
                },
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
fun EditRowItem(
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
    label: String,
    errorText: String
) {
    OutlinedTextField(
        value = value,
        modifier = Modifier.fillMaxWidth(1f),
        onValueChange = onChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
            )
        },
        isError = errorText.isNotBlank(),
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrectEnabled = true,
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        )
    )
    if (errorText.isNotBlank())
        Text(errorText, color = MaterialTheme.colorScheme.error)

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersDropdownMenuBox(
    vm: taskDetails,
    label: String,
    currentMembers: List<Member>
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = vm.openAssignDialog.value,
            onExpandedChange = {
                //expanded = !expanded
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
//                            MemberIcon(member = member, modifierScale = Modifier.scale(0.65f), modifierPadding = Modifier.padding(start = if (index == 0) 12.dp else 0.dp), enableNavigation = false)
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
                AssignMemberDialog(vm)
            }
        }
    }
    if(vm.membersError.isNotEmpty()) {
        Text(text = vm.membersError, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun AssignMemberDialog(vm: taskDetails) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val selectedMembers = remember { mutableStateMapOf<Member, Boolean>() }
    vm.possibleMembers.forEach { member ->
        selectedMembers[member] = vm.members.toMutableList().contains(member)
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
                            text = vm.taskName,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    else
                        Text(
                            text = vm.taskName,
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
                    modifier = Modifier.heightIn(0.dp, (screenHeightDp * 0.4).dp)
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
                if (vm.membersDialogError.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp, 10.dp, 0.dp, 0.dp), horizontalArrangement = Arrangement.Start
                    ) {
                        Text(text = vm.membersDialogError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { vm.openAssignDialog.value = false; vm.membersDialogError = "" }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    TextButton(onClick = {
                        Log.i("diooo",selectedMembers.toString())
                        if(selectedMembers.all { !it.value }) {
                            vm.membersDialogError = "You Must Select at Least One Member"
                        } else {
                            vm.members.clear()
                            vm.members.addAll(selectedMembers.filterValues { it }.keys.toMutableStateList())
                            vm.openAssignDialog.value = false
                            vm.membersDialogError = ""
                        }
                    }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Demo_ExposedDropdownMenuBox(
    label: String,
    currentValue: String,
    values: List<String>,
    onChange: (String) -> Unit,
    errorMsg: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary)//testo
                    )
                },
                isError = errorMsg.isNotBlank(),
                value = currentValue,
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
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary)
                    .heightIn(0.dp, 200.dp) // Set max height to limit the dropdown size // Set background color to avoid transparency
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary)
                        .heightIn(0.dp, 200.dp) // Set max height to limit the dropdown size
                        .verticalScroll(rememberScrollState())
                ) {
                    values.forEachIndexed { i, item ->
                        DropdownMenuItem(
                            text = { Text(text = item, color = MaterialTheme.colorScheme.onPrimary) },
                            onClick = {
                                onChange(item)
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (i != values.size - 1) {
                            Divider(modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp))
                        }

                    }
                }
            }
        }
    }
    Row {
        if (errorMsg.isNotBlank()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.End)
        }
        Spacer(modifier = Modifier.padding(0.dp,0.dp,0.dp,8.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit
) {

    val open = remember { mutableStateOf(false) }

    if (open.value) {
        CalendarDialog(
            state = rememberUseCaseState(
                visible = true,
                true,
                onCloseRequest = { open.value = false }),
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
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { //Click event
                open.value = true
            },
        enabled = false,// <- Add this to make click event work
        value = if (value == null) "" else value.format(DateTimeFormatter.ISO_DATE),
        onValueChange = {},
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Date Range",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
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
fun CustomDatePickerPreview(label: String, value: String, onChange: (String) -> Unit) {
    val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    val dt = LocalDate.parse(value, dtf)
    CustomDatePicker(
        label,
        value = dt,
        onValueChange = { onChange(it.toString()) }
    )
    Spacer(modifier = Modifier.padding(5.dp))
}


@Composable
fun CommentsView(
    vm: taskDetails = viewModel(factory = Factory(LocalContext.current))
) {
    var date = ""
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeightDp * 0.7).dp)
    ) {
        key(vm.comments.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeightDp * 0.5).dp)
                    .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary))
            ) {
                vm.comments.forEachIndexed { index, comment ->
                    if (comment.date != date) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = comment.date,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            date = comment.date
                        }
                    }
                    Row {
                        OutlinedTextField(
                            label = {
                                Text(
                                    text = comment.user.username,
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(8.dp, 0.dp, 0.dp, 3.dp)
                                .width(IntrinsicSize.Max),
                            enabled = (comment.user == vm.member),// <- Add this to make click event work
                            value = comment.commentValue,
                            onValueChange = {value ->
                                vm.comments.replaceAll { c->
                                    if(c.id == comment.id)
                                        c.copy(commentValue = value.replace(Regex("\\n+"), "\n"))
                                    else
                                        c
                                }
                            },
                            trailingIcon = {
                                Text(text = comment.hour, textAlign = TextAlign.End)
                            }
                        )
                        if (comment.user == vm.member) {
                            IconButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterVertically),
                                onClick = { vm.deleteComment(comment) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Comment",
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                }
            }
        }
        val focusManager = LocalFocusManager.current
        Row(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)) {
            OutlinedTextField(
                label = { Text(text = "Add a comment") },
                value = vm.addComment.commentValue,
                onValueChange = { vm.changeAddComment(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeightDp * 0.2).dp),
                trailingIcon = {
                    IconButton(
                        onClick = { vm.addNewComment(); focusManager.clearFocus() }
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send Icon"
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun HistoryView(
    history: MutableList<History>,
    customHeight: Float = 0.7f
) {
    var date = ""
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        //key(history.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height((screenHeightDp * customHeight).dp)
                    .padding(0.dp, 10.dp, 0.dp, 0.dp)
                    .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
            ) {
                history.forEachIndexed { index, history ->

                    if (history.date != date) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = history.date,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            date = history.date
                        }

                    }
                    Row {

                        OutlinedTextField(
                            label = {
                                Text(
                                    text = history.user.username,
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp, 0.dp, 0.dp, 3.dp)
                                .widthIn(10.dp, 100.dp),
                            enabled = true,// <- Add this to make click event work
                            value = history.comment,
                            readOnly = true,
                            onValueChange = {},
                            trailingIcon = {
                            }
                        )
                    }
                }
            }

        //}
    }
}


@Composable
fun FilesView(
    vm: taskDetails = viewModel(factory = Factory(LocalContext.current)),
) {
    var date = ""
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeightDp * 0.7).dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height((screenHeightDp * 0.6).dp)
                .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary))
        ) {
             vm.files.forEachIndexed { index, file ->

                if (file.date != date) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = file.date,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        date = file.date
                    }

                }
                Row {

                    OutlinedTextField(
                        label = {
                            Text(
                                text = file.user.username,
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(8.dp, 0.dp, 0.dp, 3.dp)
                            .widthIn(10.dp, 100.dp),
                        enabled = false,// <- Add this to make click event work
                        value = file.filename,
                        onValueChange = {},
                        trailingIcon = {
                            IconButton(onClick = { /*TODO download web*/ }) {
                                Icon(Icons.Default.Download, contentDescription = null)
                            }
                        }
                    )
                    if (file.user == vm.member) {
                        IconButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterVertically),
                            onClick = { vm.removeFile(file) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete File",
                                tint = MaterialTheme.colorScheme.onError

                            )
                        }
                    }
                }
            }
        }
        FileUpload(vm = vm, modifier = Modifier
            .height((screenHeightDp * 0.1).dp)
            .align(Alignment.BottomCenter))
    }
}


@Composable
fun FileUpload(vm: taskDetails = viewModel(factory = Factory(LocalContext.current)), modifier: Modifier) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    val contentResolver = LocalContext.current.contentResolver

    val chooseFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        if (uri != null) {
            // Extract filename from URI
            selectedFileName = getFileName(uri, contentResolver)

            vm.addFile(
                File(
                    user = vm.member,
                    filename = selectedFileName ?: uri.path.toString(),
                    date = LocalDate.now().toString(),
                    uri = uri
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { chooseFileLauncher.launch("*/*") }) {
            Text("Upload File")
        }
    }
}

// Function to extract filename from URI
fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use { c ->
        val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        c.moveToFirst()
        c.getString(nameIndex)
    }
}

@Composable
fun DeleteTaskDialog(vm: taskDetails) {
    val navController = NavControllerManager.getNavController()

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = { vm.openDeleteTaskDialog = false },
        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Are you sure you want to delete the task: ${vm.taskName} ?") },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.openDeleteTaskDialog = false
                    //vm.deleteTask
                    navController.navigate("Team/${vm.model.selectedTeam.value.id}") { launchSingleTop = true }
                }
            ) {
                Text("Confirm", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { vm.openDeleteTaskDialog = false }
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
