package it.polito.uniteam.gui.showtaskdetails


import android.net.Uri
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import it.polito.uniteam.isVertical
import androidx.compose.runtime.key
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.ImeAction
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon


@Preview
@Composable
fun TaskScreen(vm: taskDetails = viewModel()) {
    if (vm.editing) {
        EditTaskView()
    } else {
        TaskDetailsView()
    }

}


@Preview
@Composable
fun TaskDetailsView(vm: taskDetails = viewModel()) {
    var scrollState = rememberScrollState()
    key(vm.commentHistoryFileSelection) {
        scrollState = if (isVertical())
            rememberScrollState(vm.scrollTaskDetails)
        else
            scrollState
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    scrollState
                )
        ) {
            Spacer(modifier = Modifier.padding(10.dp))
            Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = {
                    vm.changeEditing()
                    vm.enterEditingMode()
                    vm.newTask()
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add ")
                }
                IconButton(onClick = {
                    vm.changeEditing()
                    vm.enterEditingMode()
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ")
                }
            }

            RowItem(title = "Name:", value = vm.taskName)
            RowItem(title = "Description:", value = vm.description)
            RowItem(title = "Category:", value = vm.category)
            RowItem(title = "Priority:", value = vm.priority)
            RowItem(title = "Deadline:", value = vm.deadline)
            RowItem(title = "Estimated Hours:", value = vm.estimatedHours)
            RowItem(title = "Spent Hours:", value = vm.spentHours)
            RowItem(title = "Repeatable:", value = vm.repeatable)
            RowMemberItem(title = "Members:", value = vm.members)
            RowItem(title = "Status:", value = vm.status)
            if (vm.commentHistoryFileSelection == "comments") {
                CommentsView(vm = vm, label = "Comments")
            } else if (vm.commentHistoryFileSelection == "files") {
                FilesView(vm = vm)
            } else if (vm.commentHistoryFileSelection == "history") {
                HistoryView(
                    "History",
                    vm.history,
                    vm::changeCommentHistoryFileSelection,
                    vm.commentHistoryFileSelection
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}


@Preview
@Composable
fun EditTaskView(vm: taskDetails = viewModel()) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
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
                EditRowItem(
                    label = "Estimated Hours:",
                    keyboardType = KeyboardType.Number,
                    value = vm.estimatedHours,
                    errorText = vm.estimatedHoursError,
                    onChange = vm::changeEstimatedHours
                )
                EditRowItem(
                    label = "Spent Hours:",
                    keyboardType = KeyboardType.Number,
                    value = vm.spentHours,
                    errorText = vm.spentHoursError,
                    onChange = vm::changeSpentHours
                )
                Demo_ExposedDropdownMenuBox(
                    "Repeatable",
                    vm.repeatable,
                    vm.repeatableValues,
                    vm::changeRepetition
                )
                Demo_ExposedDropdownMenuBox("Status", vm.status, vm.possibleStates, vm::changeState)
                MembersDropdownMenuBox(
                    vm,
                    "AddMembers",
                    vm.members,
                    vm.possibleMembers,
                    vm::addMembers,
                    vm::removeMembers,
                    vm.membersError
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
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
                    TextButton(onClick = {
                        vm.validate()
                        if (vm.taskError == "" && vm.descriptionError == "" && vm.categoryError == "" && vm.deadlineError == "" && vm.estimatedHoursError == "" && vm.spentHoursError == "" && vm.priorityError == "") {
                            vm.handleHistory()
                            vm.changeEditing()
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Save", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            //Spacer(modifier = Modifier.width(15.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
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
fun RowMemberItem(modifier: Modifier = Modifier, title: String, value: List<Member>) {
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
            MemberIcon(member = member, modifierScale = Modifier.scale(0.65f), modifierPadding = Modifier.padding(start = if (i == 0) 12.dp else 0.dp))
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
        ),
    )
    if (errorText.isNotBlank())
        Text(errorText, color = MaterialTheme.colorScheme.error)

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersDropdownMenuBox(
    vm: taskDetails,
    label: String,
    currentMembers: List<Member>,
    possibleMembers: List<Member>,
    addMember: (Member) -> Unit,
    removeMember: (Member) -> Unit,
    errorText: String
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
                AssignMemberDialog(vm)
            }
        }
    }
}

@Composable
fun AssignMemberDialog(vm: taskDetails) {

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
                verticalArrangement = Arrangement.Center
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
                        vm.members.clear()
                        vm.members.addAll(selectedMembers.filterValues { it }.keys.toMutableStateList())
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
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)//testo
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
                modifier = Modifier.background(color = Color.White) // Set background color to avoid transparency
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .heightIn(0.dp, 200.dp) // Set max height to limit the dropdown size
                        .verticalScroll(rememberScrollState())
                ) {
                    values.forEachIndexed { i, item ->
                        DropdownMenuItem(
                            text = { Text(text = item, color = Color.Blue) },
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
        /*colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = Color.Black,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)*/
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
    vm: taskDetails = viewModel(),
    label: String
) {
    var date = ""
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = { vm.changeCommentHistoryFileSelection("comments") },
            modifier = Modifier.weight(1f),
            colors = if (vm.commentHistoryFileSelection == "comments") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "Comments",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        TextButton(
            onClick = { vm.changeCommentHistoryFileSelection("history") },
            modifier = Modifier.weight(1f),
            colors = if (vm.commentHistoryFileSelection == "history") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "History",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black // Aligning text to the center
            )
        }
        TextButton(
            onClick = { vm.changeCommentHistoryFileSelection("files") },
            modifier = Modifier.weight(1f),
            colors = if (vm.commentHistoryFileSelection == "files") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "Files",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            label = {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            },
            value = "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        key(vm.comments.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(199.dp)
                    .padding(0.dp, 10.dp, 0.dp, 0.dp)
                    .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
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
                            enabled = false,// <- Add this to make click event work
                            value = comment.commentValue,
                            onValueChange = {},
                            trailingIcon = {
                                Text(text = comment.hour, textAlign = TextAlign.End)
                            },
                            /*colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.primary,
                                focusedBorderColor = Color.Black,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)*/
                        )
                        if (comment.user == vm.member) {
                            IconButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterVertically),
                                onClick = { vm.deleteComment(comment) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Delete Comment"
                                )
                            }
                        }
                    }
                }
            }
        }


    }
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(label = { Text(text = "Add a comment") },
            value = vm.addComment.commentValue,
            onValueChange = { it -> vm.changeAddComment(it) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { vm.addNewComment() }) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send Icon"
                    )
                }
            })

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryView(
    label: String,
    history: MutableList<History>,
    changeSelection: (String) -> Unit,
    commentHistoryFileSelection: String
) {
    val context = LocalContext.current
    val values = history
    var date = ""
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = { changeSelection("comments") },
            modifier = Modifier.weight(1f),
            colors = if (commentHistoryFileSelection == "comments") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "Comments",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        TextButton(
            onClick = { changeSelection("history") },
            modifier = Modifier.weight(1f),
            colors = if (commentHistoryFileSelection == "history") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "History",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black // Aligning text to the center
            )
        }
        TextButton(
            onClick = { changeSelection("files") },
            modifier = Modifier.weight(1f),
            colors = if (commentHistoryFileSelection == "files") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "Files",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            label = {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            },
            value = "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE)),
        )

        key(values.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(199.dp)
                    .padding(0.dp, 10.dp, 0.dp, 0.dp)
                    .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
            ) {
                values.forEachIndexed { index, history ->

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
                            enabled = false,// <- Add this to make click event work
                            value = history.comment,
                            onValueChange = {},
                            trailingIcon = {
                            },
                            /*colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.primary,
                                focusedBorderColor = Color.Black,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)*/
                        )
                    }
                }
            }

        }


    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesView(
    vm: taskDetails = viewModel(),
) {
    var date = ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = { vm.changeCommentHistoryFileSelection("comments") },
            modifier = Modifier.weight(1f),
            colors = if (vm.commentHistoryFileSelection == "comments") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "Comments",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        TextButton(
            onClick = { vm.changeCommentHistoryFileSelection("history") },
            modifier = Modifier.weight(1f),
            colors = if (vm.commentHistoryFileSelection == "history") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "History",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black // Aligning text to the center
            )
        }
        TextButton(
            onClick = { vm.changeCommentHistoryFileSelection("files") },
            modifier = Modifier.weight(1f),
            colors = if (vm.commentHistoryFileSelection == "files") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                containerColor = Color.Unspecified
            )
        ) {
            Text(
                text = "Files",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            label = {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            },
            value = "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)

        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(199.dp)
                .padding(0.dp, 10.dp, 0.dp, 0.dp)
                .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
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
                        },
                        /*colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.primary,
                            focusedBorderColor = Color.Black,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)*/
                    )
                    if (file.user == vm.member) {
                        IconButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterVertically),
                            onClick = { vm.removeFile(file) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Delete File"
                            )
                        }
                    }
                }
            }
        }
    }
    FileUpload(vm = vm)
}


@Composable
fun FileUpload(vm: taskDetails = viewModel()) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val chooseFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        if (uri != null) {
            vm.addFile(
                File(
                    user = vm.member,
                    filename = uri.path.toString(),
                    date = LocalDate.now().toString(),
                    uri = uri
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { chooseFileLauncher.launch("*/*") }) {
            Text("Upload File")
        }
        Spacer(modifier = Modifier.height(16.dp))

    }
}













