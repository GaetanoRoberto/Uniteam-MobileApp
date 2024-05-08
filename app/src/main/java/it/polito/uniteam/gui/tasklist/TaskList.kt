package it.polito.uniteam.gui.tasklist

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import it.polito.uniteam.R
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.Task
import it.polito.uniteam.gui.showtaskdetails.CustomDatePicker
import it.polito.uniteam.isVertical
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale


class TaskList: ViewModel() {
    var membersList = mutableStateOf<List<Member>>(listOf(
        Member().apply {
            fullName = "John Doe"
            username = "johndoe"
            email = "johndoe@example.com"
            location = "New York"
            description = "Software Developer"
            kpi = "85"
        },
        Member().apply {
            fullName = "Jane Smith"
            username = "janesmith"
            email = "janesmith@example.com"
            location = "Los Angeles"
            description = "Product Manager"
            kpi = "90"
        },
        Member().apply {
            fullName = "Alice Johnson"
            username = "alicejohnson"
            email = "alicejohnson@example.com"
            location = "Chicago"
            description = "Data Analyst"
            kpi = "88"
        },
        Member().apply {
            fullName = "Bob Williams"
            username = "bobwilliams"
            email = "bobwilliams@example.com"
            location = "San Francisco"
            description = "UX Designer"
            kpi = "92"
        },
        Member().apply {
            fullName = "Charlie Brown"
            username = "charliebrown"
            email = "charliebrown@example.com"
            location = "Seattle"
            description = "Project Manager"
            kpi = "89"
        },
        Member().apply {
            fullName = "David Davis"
            username = "daviddavis"
            email = "daviddavis@example.com"
            location = "Austin"
            description = "QA Engineer"
            kpi = "91"
        },
        Member().apply {
            fullName = "Eve Evans"
            username = "eveevans"
            email = "eveevans@example.com"
            location = "Boston"
            description = "DevOps Engineer"
            kpi = "93"
        }
    ))
        private set

    var tasksList = mutableStateOf<List<Task>>(listOf(
        Task().apply {
            name = "API documentation"
            description = "This is task 1"
            category = Category.PROGRAMMING
            priority = Priority.LOW
            deadline = LocalDate.of(2024, 5, 6) // 1 week from now
            creationDate = LocalDate.of(2024, 4, 14)
            estimatedHours = 1
            status = Status.IN_PROGRESS
            repetition = Repetition.DAILY
            members = listOf(membersList.value[0], membersList.value[1])
        },
        Task().apply {
            name = "API implementation"
            description = "This is task 2"
            category = Category.PROGRAMMING
            priority = Priority.HIGH
            deadline = LocalDate.of(2024, 5, 6) // 1 week from now
            creationDate = LocalDate.of(2024, 5, 5)
            estimatedHours = 10
            status = Status.TODO
            repetition = Repetition.NONE
            members = listOf(membersList.value[2], membersList.value[3])
        },
        Task().apply {
            name = "Design Homepage"
            description = "This is task 3"
            category = Category.DESIGN
            priority = Priority.MEDIUM
            deadline = LocalDate.of(2024, 5, 6) // 1 week from now
            creationDate = LocalDate.of(2024, 4, 20)
            estimatedHours = 8
            status = Status.IN_PROGRESS
            repetition = Repetition.NONE
            members = listOf(membersList.value[4], membersList.value[5])
        },
        Task().apply {
            name = "Homepage Implementation"
            description = "This is task 4"
            category = Category.PROGRAMMING
            priority = Priority.HIGH
            deadline = LocalDate.of(2024, 5, 13) // 2 weeks from now
            creationDate = LocalDate.of(2024, 5, 6)
            estimatedHours = 20
            status = Status.TODO
            repetition = Repetition.NONE
            members = listOf(membersList.value[5], membersList.value[6])
        },
        Task().apply {
            name = "SCRUM Meeting"
            description = "This is task 5"
            category = Category.MEETING
            priority = Priority.MEDIUM
            deadline = LocalDate.of(2024, 5, 20) // 3 weeks from now
            creationDate = LocalDate.of(2024, 4, 13)
            estimatedHours = 1
            status = Status.COMPLETED
            repetition = Repetition.MONTHLY
            members = membersList.value
        }
    ))
        private set

    var filteredTasksList = mutableStateOf<List<Task>>(tasksList.value.sortedByDescending { it.creationDate })

    var expandedSearch by mutableStateOf(false)

    var searchQuery by mutableStateOf("")

    var expandedDropdown by mutableStateOf(false)

    var openAssignDialog by mutableStateOf(false)

    var taskToAssign by mutableStateOf<Task?>(null)

    var lastAppliedFilters = mutableStateOf<Map<String, Any>>(mapOf())

    var lastSearchQuery = mutableStateOf<String>("")

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListView(vm: TaskList = viewModel(), navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    //Stati per la gestione degli ExpandableRow
    val assigneeExpanded = remember { mutableStateOf(false) }
    val categoryExpanded = remember { mutableStateOf(false) }
    val priorityExpanded = remember { mutableStateOf(false) }
    val statusExpanded = remember { mutableStateOf(false) }
    val repetitionExpanded = remember { mutableStateOf(false) }
    val deadlineExpanded = remember { mutableStateOf(false) }
    val sortByExpanded = remember { mutableStateOf(false) }
    //Stati per la gestione dei filtri
    val scrollState = rememberScrollState()
    val selectedMembers = remember { mutableStateMapOf<Member, Boolean>() }
    val selectedCategory = remember { mutableStateMapOf<Category, Boolean>() }
    val selectedPriority = remember { mutableStateMapOf<Priority, Boolean>() }
    val selectedStatus = remember { mutableStateMapOf<Status, Boolean>() }
    val selectedRepetition = remember { mutableStateMapOf<Repetition, Boolean>() }
    val selectedDeadline = remember { mutableStateOf<LocalDate?>(null) }
    val radioOptions = listOf("Creation date", "Deadline", "Priority", "Estimated hours", "Spent hours")
    val selectedChip = remember { mutableStateOf("First") }
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }


    //Drawer dei filtri
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            //gesturesEnabled = false,
            drawerContent = { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                ModalDrawerSheet(drawerState, drawerShape = RoundedCornerShape(topStart =  16.dp, bottomStart = 16.dp, topEnd = 0.dp, bottomEnd = 0.dp)) {
                    Box() {
                        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
                            Row(
                                modifier = if (isVertical())
                                    Modifier.padding(16.dp, 16.dp, 0.dp, 0.dp)
                                else
                                    Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "FILTER BY :",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { scope.launch { drawerState.close() } }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close filters"
                                    )
                                }
                            }

                            Column(
                                modifier = if (isVertical())
                                    Modifier
                                        .fillMaxHeight(0.9f)
                                        .verticalScroll(scrollState)
                                else
                                    Modifier
                                        .fillMaxHeight(0.7f)
                                        .verticalScroll(scrollState)
                                    ) {
                                ExpandableRow(
                                    expanded = assigneeExpanded,
                                    filter = {
                                        Text("Assignee", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            vm.membersList.value.forEach { member ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (selectedMembers[member] == true) {
                                                                    selectedMembers.remove(member)
                                                                } else {
                                                                    selectedMembers[member] = true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (selectedMembers[member] == true) {
                                                                    selectedMembers.remove(member)
                                                                } else {
                                                                    selectedMembers[member] = true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = selectedMembers[member] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                            selectedMembers[member] = true
                                                            } else {
                                                                selectedMembers.remove(member)
                                                            }
                                                        }
                                                    )
                                                    Text(member.fullName, textAlign = TextAlign.Center)
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = categoryExpanded,
                                    filter = {
                                        Text("Category", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            Category.entries.forEach { category ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (selectedCategory[category] == true) {
                                                                    selectedCategory.remove(category)
                                                                } else {
                                                                    selectedCategory[category] =
                                                                        true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (selectedCategory[category] == true) {
                                                                    selectedCategory.remove(category)
                                                                } else {
                                                                    selectedCategory[category] =
                                                                        true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = selectedCategory[category] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                selectedCategory[category] = true
                                                            } else {
                                                                selectedCategory.remove(category)
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        category.toString().lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = priorityExpanded,
                                    filter = {
                                        Text("Priority", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            Priority.entries.forEach { priority ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (selectedPriority[priority] == true) {
                                                                    selectedPriority.remove(priority)
                                                                } else {
                                                                    selectedPriority[priority] =
                                                                        true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (selectedPriority[priority] == true) {
                                                                    selectedPriority.remove(priority)
                                                                } else {
                                                                    selectedPriority[priority] =
                                                                        true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = selectedPriority[priority] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                selectedPriority[priority] = true
                                                            } else {
                                                                selectedPriority.remove(priority)
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        priority.toString().lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = statusExpanded,
                                    filter = {
                                        Text("Status", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            Status.entries.forEach { status ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (selectedStatus[status] == true) {
                                                                    selectedStatus.remove(status)
                                                                } else {
                                                                    selectedStatus[status] = true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (selectedStatus[status] == true) {
                                                                    selectedStatus.remove(status)
                                                                } else {
                                                                    selectedStatus[status] = true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = selectedStatus[status] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                selectedStatus[status] = true
                                                            } else {
                                                                selectedStatus.remove(status)
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        when (status) {
                                                            Status.IN_PROGRESS -> "In Progress"
                                                            Status.TODO -> "To Do"
                                                            Status.COMPLETED -> "Completed"
                                                        },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = repetitionExpanded,
                                    filter = {
                                        Text("Repetition", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            Repetition.entries.forEach { repetition ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (selectedRepetition[repetition] == true) {
                                                                    selectedRepetition.remove(
                                                                        repetition
                                                                    )
                                                                } else {
                                                                    selectedRepetition[repetition] =
                                                                        true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (selectedRepetition[repetition] == true) {
                                                                    selectedRepetition.remove(
                                                                        repetition
                                                                    )
                                                                } else {
                                                                    selectedRepetition[repetition] =
                                                                        true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = selectedRepetition[repetition]
                                                            ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                selectedRepetition[repetition] = true
                                                            } else {
                                                                selectedRepetition.remove(repetition)
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        repetition.toString().lowercase()
                                                            .replaceFirstChar {
                                                                if (it.isLowerCase()) it.titlecase(
                                                                    Locale.getDefault()
                                                                ) else it.toString()
                                                            },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = deadlineExpanded,
                                    filter = {
                                        Text("Deadline", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp)
                                                ) {
                                                    CustomDatePicker(
                                                        label = if (selectedDeadline.value != null) {
                                                            ""
                                                        } else {
                                                            "Select a date"
                                                        },
                                                        value = selectedDeadline.value,
                                                        onValueChange = { selectedDeadline.value = it }
                                                    )
                                                }
                                            }
                                    }
                                )
                                HorizontalDivider(thickness = 2.dp)
                                ExpandableRow(
                                    expanded = sortByExpanded,
                                    filter = {
                                        Text("Sort by",
                                            style = if (isVertical())
                                                MaterialTheme.typography.titleLarge
                                            else
                                                MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            Row(modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp)) {
                                                FilterChip(
                                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary, selectedLeadingIconColor =  MaterialTheme.colorScheme.onPrimary),
                                                    selected = (selectedChip.value == "First"),
                                                    onClick = { selectedChip.value = "First" },
                                                    label = {
                                                        when (selectedOption) {
                                                            "Creation date" -> Text("Newer")
                                                            "Deadline" -> Text("Earlier")
                                                            "Priority" -> Text("Higher")
                                                            else -> Text("More")
                                                        }
                                                    },
                                                    leadingIcon = if (selectedChip.value == "First") {
                                                        {
                                                            Icon(
                                                                imageVector = Icons.Filled.Done,
                                                                contentDescription = "Done icon",
                                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                            )
                                                        }
                                                    } else {
                                                        null
                                                    }
                                                )
                                                Spacer(modifier = Modifier.padding(10.dp))
                                                FilterChip(
                                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary, selectedLeadingIconColor =  MaterialTheme.colorScheme.onPrimary),
                                                    selected = selectedChip.value == "Second",
                                                    onClick = { selectedChip.value = "Second" },
                                                    label = {
                                                        when (selectedOption) {
                                                            "Creation date" -> Text("Older")
                                                            "Deadline" -> Text("Later")
                                                            "Priority" -> Text("Lower")
                                                            else -> Text("Less")
                                                        }
                                                    },
                                                    leadingIcon = if (selectedChip.value == "Second") {
                                                        {
                                                            Icon(
                                                                imageVector = Icons.Filled.Done,
                                                                contentDescription = "Done icon",
                                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                            )
                                                        }
                                                    } else {
                                                        null
                                                    }
                                                )
                                            }
                                            Column(Modifier.selectableGroup()) {
                                                radioOptions.forEach { text ->
                                                    Row(
                                                        modifier = if (isVertical())
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .selectable(
                                                                    selected = (text == selectedOption),
                                                                    onClick = {
                                                                        onOptionSelected(
                                                                            text
                                                                        )
                                                                    },
                                                                    role = Role.RadioButton
                                                                )
                                                        else
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .height(40.dp)
                                                                .selectable(
                                                                    selected = (text == selectedOption),
                                                                    onClick = {
                                                                        onOptionSelected(
                                                                            text
                                                                        )
                                                                    },
                                                                    role = Role.RadioButton
                                                                ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        RadioButton(
                                                            selected = (text == selectedOption),
                                                            onClick = { onOptionSelected(text) }
                                                        )
                                                        Text(
                                                            text = text
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.padding(5.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                FilledTonalButton(colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary),
                                    onClick = {
                                    selectedDeadline.value = null
                                    vm.lastAppliedFilters.value = mapOf(
                                        "selectedDeadline" to LocalDate.MAX
                                    )
                                    selectedMembers.clear()
                                    selectedCategory.clear()
                                    selectedPriority.clear()
                                    selectedStatus.clear()
                                    selectedRepetition.clear()
                                    assigneeExpanded.value = false
                                    categoryExpanded.value = false
                                    priorityExpanded.value = false
                                    statusExpanded.value = false
                                    repetitionExpanded.value = false
                                    sortByExpanded.value = false
                                    scope.launch { scrollState.animateScrollTo(0) }
                                    vm.filteredTasksList.value = vm.tasksList.value.filter { task ->
                                        applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                                    }
                                    onOptionSelected(radioOptions[0])
                                    selectedChip.value = "First"
                                    sortTasks(radioOptions[0], "First", vm)
                                }) {
                                    Text("Reset")
                                }
                                Spacer(modifier = Modifier.padding(10.dp))
                                FilledTonalButton(colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary),onClick = {
                                    vm.lastAppliedFilters.value = mapOf(
                                        "selectedDeadline" to (selectedDeadline.value ?: LocalDate.MAX),
                                        "selectedMembers" to selectedMembers,
                                        "selectedCategory" to selectedCategory,
                                        "selectedPriority" to selectedPriority,
                                        "selectedStatus" to selectedStatus,
                                        "selectedRepetition" to selectedRepetition
                                    )
                                    vm.filteredTasksList.value = vm.tasksList.value.filter { task ->
                                        applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                                    }
                                    sortTasks(selectedOption, selectedChip.value, vm)
                                    scope.launch { drawerState.close() }
                                }) {
                                    Text("Apply")
                                }
                            }
                        }
                    }
                }
            }},
            content = { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                //Resto della UI
                Box(modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 0.dp)) {
                    if (isVertical())
                        VerticalTaskListView(vm, drawerState, scope, context, view, selectedOption, selectedChip.value,navController)
                    else
                        HorizontalTaskListView(vm, drawerState, scope, context, view, selectedOption, selectedChip.value,navController)
                }
                //Dialog per l'assegnazione di un task
                when {
                    vm.openAssignDialog -> {
                        AssignDialog(vm)
                    }
                }
            } }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalTaskListView(vm: TaskList, drawerState: DrawerState, scope: CoroutineScope, context: Context, view: View, selectedOption: String, selectedChip: String,navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        //Row del TeamName + icone
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Team #1",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(5.dp))
            //Dropdown icone
            Box() {
                IconButton(
                    onClick = { vm.expandedDropdown = true },
                    modifier = Modifier.scale(1.2f)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.scale(1.5f)
                    )
                }
                DropdownMenu(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    expanded = vm.expandedDropdown,
                    onDismissRequest = { vm.expandedDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Change availability",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = { /* Handle change! */ },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.availability),
                                contentDescription = "Set availability",
                                modifier = Modifier.size(36.dp)
                            )
                        })
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add member/s",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = { /* Handle add! */ },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.addmember),
                                contentDescription = "Add member/s",
                                modifier = Modifier.size(36.dp)
                            )
                        })
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "See statistics",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = { /* Handle statistics */ },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.statistics),
                                contentDescription = "See statistics",
                                modifier = Modifier.size(36.dp)
                            )
                        })
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Leave team",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Red
                            )
                        },
                        onClick = { /* Handle exit */ },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.exitteam),
                                contentDescription = "Leave team",
                                modifier = Modifier.size(36.dp),
                                tint = Color.Red
                            )
                        })
                }
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        //Row dei membri del team
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Members: ",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.padding(5.dp))
            //ProfileImages dei membri
            val membersToShow = if (vm.membersList.value.size > 4)
                vm.membersList.value.take(3)
            else
                vm.membersList.value
            for (member in membersToShow) {
                MemberIcon(Modifier.scale(1f), Modifier.padding(0.dp, 5.dp, 30.dp, 0.dp), member)
            }
            if (vm.membersList.value.size > 4) {
                TextButton(
                    onClick = { /* Do something! */ },
                    contentPadding = PaddingValues(0.dp, 0.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append("+ " + (vm.membersList.value.size - 3).toString() + " more")
                            }
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        if (vm.membersList.value.size > 4)
            Spacer(modifier = Modifier.padding(5.dp))
        else
            Spacer(modifier = Modifier.padding(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.padding(3.dp))
        //Row creazione di un nuovo task
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tasks:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(
                onClick = {navController.navigate("Tasks"){
                    /*popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }*/
                    launchSingleTop = true
                    //restoreState = true
                } },
                contentPadding = PaddingValues(5.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new task")
                Text(
                    "NEW TASK",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        //Spacer(modifier = Modifier.padding(5.dp))
        //Row per searchbar, filtri e accesso al calendario
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                colors= SearchBarDefaults.colors(
                    containerColor =MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = vm.searchQuery,
                        onQueryChange = {
                            vm.searchQuery = it
                            if (it == "") {
                                vm.lastSearchQuery.value = vm.searchQuery
                                vm.filteredTasksList.value = vm.tasksList.value.filter { task ->
                                    applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                                }
                                sortTasks(selectedOption, selectedChip, vm)
                            }
                        },
                        onSearch = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, 0)

                            vm.lastSearchQuery.value = vm.searchQuery
                            vm.filteredTasksList.value = vm.tasksList.value.filter { task ->
                                applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                            }
                        },
                        expanded = false,
                        onExpandedChange = { vm.expandedSearch = it },
                        placeholder = { Text("Search task") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (vm.searchQuery != "") {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        vm.searchQuery = ""
                                        vm.lastSearchQuery.value = vm.searchQuery
                                        vm.filteredTasksList.value = vm.tasksList.value.filter { task ->
                                            applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                                        }
                                        sortTasks(selectedOption, selectedChip, vm)
                                    }
                                )
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = { vm.expandedSearch = it },
                modifier = Modifier.width(205.dp)
            ) {}
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { navController.navigate("Calendar"){
                    /*popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }*/
                    launchSingleTop = true
                    //restoreState = true
                }
                          }, modifier = Modifier
                    .scale(1.5f)
                    .padding(0.dp, 5.dp, 0.dp, 0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = "Access calendar",
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .scale(1.5f)
                    .padding(0.dp, 5.dp, 0.dp, 0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.filters),
                    contentDescription = "Access filters",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        HorizontalDivider()
        //Lista dei task
        if (vm.tasksList.value.isEmpty() || vm.filteredTasksList.value.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text =
                    if (vm.tasksList.value.isEmpty())
                        "Team#1 team\nhas no tasks yet.\nCreate the first!"
                    else
                        "No tasks found!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            LazyColumn {
                items(vm.filteredTasksList.value) { task ->
                    ListItem(
                        modifier = Modifier.clickable { /*TODO*/ },
                        headlineContent = {
                            Text(
                                task.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp)
                            ) {
                                Text("Assignee: ")
                                //
                                val membersToShow = if (task.members.size > 5)
                                    task.members.take(3)
                                else
                                    task.members
                                if (membersToShow.isEmpty()) {
                                    Text(" nobody assigned", style = MaterialTheme.typography.labelMedium)
                                } else {
                                    for (member in membersToShow) {
                                        MemberIcon(Modifier.scale(0.7f), Modifier.padding(5.dp, 0.dp, 10.dp, 0.dp), member)
                                    }
                                }
                                if (task.members.size > 5) {
                                    Text(
                                        text = "+ " + (task.members.size - 3).toString() + " more",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 0.dp)
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                when (task.status) {
                                    Status.IN_PROGRESS -> {
                                        Text(
                                            "IN PROGRESS",
                                            modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
                                        )
                                    }
                                    Status.TODO -> {
                                        Text(
                                            "TO DO",
                                            modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
                                        )
                                    }
                                    Status.COMPLETED -> {
                                        Text(
                                            "COMPLETED",
                                            modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
                                        )
                                    }
                                }
                                TextButton(onClick = {
                                    vm.taskToAssign = task; vm.openAssignDialog = true
                                }, contentPadding = PaddingValues(0.dp, 0.dp)) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                                append("+ Assign")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalTaskListView(vm: TaskList, drawerState: DrawerState, scope: CoroutineScope, context: Context, view: View, selectedOption: String, selectedChip: String,navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f)
        ) {
            //Row del TeamName + icone
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Team #1",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(5.dp))
                //Dropdown icone
                Box() {
                    IconButton(
                        onClick = { vm.expandedDropdown = true },
                        modifier = Modifier.scale(1.2f)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.scale(1.5f)
                        )
                    }
                    DropdownMenu(
                        expanded = vm.expandedDropdown,
                        onDismissRequest = { vm.expandedDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Change availability",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = { /* Handle change! */ },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.availability),
                                    contentDescription = "Set availability",
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Add member/s",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = { /* Handle add! */ },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.addmember),
                                    contentDescription = "Add member/s",
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "See statistics",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = { /* Handle statistics */ },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.statistics),
                                    contentDescription = "See statistics",
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Leave team",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Red

                                )
                            },
                            onClick = { /* Handle exit */ },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.exitteam),
                                    contentDescription = "Leave team",
                                    modifier = Modifier.size(36.dp),
                                    tint = Color.Red
                                )
                            })
                    }
                }
            }
            Spacer(modifier = Modifier.padding(5.dp))
            //Row dei membri del team
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Members: ",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.padding(5.dp))
                //ProfileImages dei membri
                val membersToShow = if (vm.membersList.value.size > 5)
                    vm.membersList.value.take(4)
                else
                    vm.membersList.value
                for (member in membersToShow) {
                    MemberIcon(Modifier.scale(1f), Modifier.padding(0.dp, 5.dp, 30.dp, 0.dp), member)
                }
                if (vm.membersList.value.size > 5) {
                    TextButton(
                        onClick = { /* TODO */ },
                        contentPadding = PaddingValues(0.dp, 0.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                    append("+ " + (vm.membersList.value.size - 4).toString() + " more")
                                }
                            },
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            if (vm.membersList.value.size > 5)
                Spacer(modifier = Modifier.padding(5.dp))
            else
                Spacer(modifier = Modifier.padding(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(3.dp))
            //Row per searchbar, filtri e accesso al calendario
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    colors= SearchBarDefaults.colors(
                       containerColor =MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = vm.searchQuery,
                            onQueryChange = {
                                vm.searchQuery = it
                                if (it == "") {
                                    vm.lastSearchQuery.value = vm.searchQuery
                                    vm.filteredTasksList.value = vm.tasksList.value.filter { task ->
                                        applyFilters(
                                            task,
                                            vm.lastAppliedFilters.value,
                                            vm.lastSearchQuery.value
                                        )
                                    }
                                    sortTasks(selectedOption, selectedChip, vm)
                                }
                            },
                            onSearch = {
                                val imm =
                                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(view.windowToken, 0)

                                vm.lastSearchQuery.value = vm.searchQuery
                                vm.filteredTasksList.value = vm.tasksList.value.filter { task ->
                                    applyFilters(
                                        task,
                                        vm.lastAppliedFilters.value,
                                        vm.lastSearchQuery.value
                                    )
                                }
                            },
                            expanded = false,
                            onExpandedChange = { vm.expandedSearch = it },
                            placeholder = { Text("Search task") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (vm.searchQuery != "") {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.clickable {
                                            vm.searchQuery = ""
                                            vm.lastSearchQuery.value = vm.searchQuery
                                            vm.filteredTasksList.value =
                                                vm.tasksList.value.filter { task ->
                                                    applyFilters(
                                                        task,
                                                        vm.lastAppliedFilters.value,
                                                        vm.lastSearchQuery.value
                                                    )
                                                }
                                            sortTasks(selectedOption, selectedChip, vm)
                                        }
                                    )
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = { vm.expandedSearch = it },
                    modifier = Modifier.width(205.dp)
                ) {}
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { navController.navigate("Calendar"){
                        /*popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }*/
                        launchSingleTop = true
                        //restoreState = true
                    }
                    }, modifier = Modifier
                        .scale(1.5f)
                        .padding(0.dp, 5.dp, 0.dp, 0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.calendar),
                        contentDescription = "Access calendar",
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(10.dp))
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .scale(1.5f)
                        .padding(0.dp, 5.dp, 0.dp, 0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filters),
                        contentDescription = "Access filters",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        VerticalDivider()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f)
        ) {
            //Row creazione di un nuovo task
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tasks:",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = {navController.navigate("Tasks"){
                        /*popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }*/
                        launchSingleTop = true
                        //restoreState = true
                    } },
                    contentPadding = PaddingValues(5.dp, 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add new task")
                    Text(
                        "NEW TASK",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(modifier = Modifier.padding(5.dp))
            HorizontalDivider()
            //Lista dei task
            if (vm.tasksList.value.isEmpty() || vm.filteredTasksList.value.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text =
                        if (vm.tasksList.value.isEmpty())
                            "Team#1 team\nhas no tasks yet.\nCreate the first!"
                        else
                            "No tasks found!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                LazyColumn {
                    items(vm.filteredTasksList.value) { task ->
                        ListItem(
                            modifier = Modifier.clickable { /*TODO*/ },
                            headlineContent = {
                                Text(
                                    task.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            supportingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp)
                                ) {
                                    Text("Assignee: ")
                                    //
                                    val membersToShow = if (task.members.size > 5)
                                        task.members.take(3)
                                    else
                                        task.members
                                    if (membersToShow.isEmpty()) {
                                        Text(" nobody assigned", style = MaterialTheme.typography.labelMedium)
                                    } else {
                                        for (member in membersToShow) {
                                            MemberIcon(Modifier.scale(0.7f), Modifier.padding(5.dp, 0.dp, 10.dp, 0.dp), member)
                                        }
                                    }
                                    if (task.members.size > 5) {
                                        Text(
                                            text = "+ " + (task.members.size - 3).toString() + " more",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 0.dp)
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                Column(horizontalAlignment = Alignment.End) {
                                    when (task.status) {
                                        Status.IN_PROGRESS -> {
                                            Text(
                                                "IN PROGRESS",
                                                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
                                            )
                                        }

                                        Status.TODO -> {
                                            Text(
                                                "TO DO",
                                                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
                                            )
                                        }

                                        Status.COMPLETED -> {
                                            Text(
                                                "COMPLETED",
                                                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
                                            )
                                        }
                                    }
                                    TextButton(onClick = {
                                        vm.taskToAssign = task; vm.openAssignDialog = true
                                    }, contentPadding = PaddingValues(0.dp, 0.dp)) {
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                                    append("+ Assign")
                                                }
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}


@Composable
fun AssignDialog(vm: TaskList) {
    if (vm.taskToAssign != null) {
        val selectedMembers = remember { mutableStateMapOf<Member, Boolean>() }
        vm.membersList.value.forEach { member ->
            selectedMembers[member] = vm.taskToAssign!!.members.contains(member)
        }
        Dialog(onDismissRequest = { vm.openAssignDialog = false }) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondary),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp), horizontalArrangement = Arrangement.Center) {
                        if (isVertical())
                            Text(text = vm.taskToAssign!!.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        else
                            Text(text = vm.taskToAssign!!.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 0.dp, 0.dp, 5.dp), horizontalArrangement = Arrangement.Start) {
                        Text(text = "Members assigned :", style = MaterialTheme.typography.bodyMedium)
                    }

                    LazyColumn(modifier = if (isVertical()) Modifier.heightIn(0.dp, 265.dp) else Modifier.heightIn(0.dp, 165.dp)) {
                        items(vm.membersList.value) { member ->
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
                                Text(text = member.fullName, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(onClick = { vm.openAssignDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        TextButton(onClick = {
                            vm.taskToAssign!!.members = selectedMembers.filterValues{ it }.keys.toList()
                            vm.openAssignDialog = false
                        }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun ExpandableRow(
    expanded: MutableState<Boolean>,
    filter: @Composable () -> Unit,
    filterOptions: @Composable () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (expanded.value) 180f else 0f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

    ListItem(
        modifier = if (isVertical())
            Modifier
                .animateContentSize()
                .clickable { expanded.value = !expanded.value }
        else
            Modifier
                .animateContentSize()
                .height(40.dp)
                .clickable { expanded.value = !expanded.value },
        headlineContent = { filter() },
        trailingContent = {
            if (expanded.value)
                Icon(
                    painter = painterResource(id = R.drawable.minus),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                        .rotate(rotationState)
                )
            else
                Icon(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                        .rotate(rotationState)
                )
        }
    )
    HorizontalDivider()

    AnimatedVisibility(
        visible = expanded.value,
        enter = expandVertically(animationSpec = tween(200), expandFrom = Alignment.Top),
        exit = shrinkVertically(animationSpec = tween(200))
    ) { filterOptions() }
}


fun applyFilters(task: Task, lastAppliedFilters: Map<String, Any>, lastSearchQuery: String): Boolean {
    var keep = true

    val selectedDeadline = lastAppliedFilters["selectedDeadline"] as LocalDate?
    val selectedMembers = lastAppliedFilters["selectedMembers"] as? Map<*, *> ?: emptyMap<Member, Boolean>()
    val selectedCategory = lastAppliedFilters["selectedCategory"] as? Map<*, *> ?: emptyMap<Category, Boolean>()
    val selectedPriority = lastAppliedFilters["selectedPriority"] as? Map<*, *> ?: emptyMap<Priority, Boolean>()
    val selectedStatus = lastAppliedFilters["selectedStatus"] as? Map<*, *> ?: emptyMap<Status, Boolean>()
    val selectedRepetition = lastAppliedFilters["selectedRepetition"] as? Map<*, *> ?: emptyMap<Repetition, Boolean>()

    if (selectedDeadline != null) {
        keep = keep && task.deadline!! <= selectedDeadline
        //TODO: controllare task schedulate
    }

    if (selectedMembers.isNotEmpty())
        keep = keep && task.members.any { selectedMembers.containsKey(it) }
    if (selectedCategory.isNotEmpty())
        keep = keep && selectedCategory.filterValues{ it as Boolean }.keys.contains(task.category)
    if (selectedPriority.isNotEmpty())
        keep = keep && selectedPriority.filterValues{ it as Boolean }.keys.contains(task.priority)
    if (selectedStatus.isNotEmpty())
        keep = keep && selectedStatus.filterValues{ it as Boolean }.keys.contains(task.status)
    if (selectedRepetition.isNotEmpty())
        keep = keep && selectedRepetition.filterValues{ it as Boolean }.keys.contains(task.repetition)

    keep = keep && task.name.contains(lastSearchQuery, ignoreCase = true)

    return keep
}


fun sortTasks(selectedOption: String, selectedChip: String, vm: TaskList) {
    when (selectedOption) {
        "Creation date" -> {
            if (selectedChip == "Second") {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedBy { it.creationDate }
            } else {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedByDescending { it.creationDate }
            }
        }
        "Deadline" -> {
            if (selectedChip == "First") {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedBy { it.deadline }
            } else {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedByDescending { it.deadline }
            }
        }
        "Priority" -> {
            if (selectedChip == "Second") {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedBy { it.priority }
            } else {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedByDescending { it.priority }
            }
        }
        "Estimated hours" -> {
            if (selectedChip == "Second") {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedBy { it.estimatedHours }
            } else {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedByDescending { it.estimatedHours }
            }
        }
        "Spent hours" -> {
            if (selectedChip == "Second") {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedBy { it.spentHours }
            } else {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedByDescending { it.spentHours }
            }
        }
    }
}