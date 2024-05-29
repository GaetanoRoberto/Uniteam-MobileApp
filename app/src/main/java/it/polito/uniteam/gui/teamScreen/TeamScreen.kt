package it.polito.uniteam.gui.teamScreen

import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.R
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.TeamIcon
import it.polito.uniteam.gui.showtaskdetails.CustomDatePicker
import it.polito.uniteam.isVertical
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale


class TeamScreenViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    val teamId: String = checkNotNull(savedStateHandle["teamId"])
    val currentTeam = model.getTeam(teamId.toInt())
    val membersList = currentTeam.members
    val tasksList = currentTeam.tasks
    //Stati vari
    var filteredTasksList = mutableStateOf<List<Task>>(tasksList.sortedByDescending { it.creationDate })
    var expandedSearch by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var expandedDropdown by mutableStateOf(false)
    var openAssignDialog by mutableStateOf(false)
    var openLeaveTeamDialog by mutableStateOf(false)
    var taskToAssign by mutableStateOf<Task?>(null)
    var lastAppliedFilters = mutableStateOf<Map<String, Any>>(mapOf())
    var lastSearchQuery = mutableStateOf<String>("")
    var membersError by mutableStateOf("")
    //Stati per la gestione degli ExpandableRow
    val assigneeExpanded = mutableStateOf(false)
    val categoryExpanded = mutableStateOf(false)
    val priorityExpanded = mutableStateOf(false)
    val statusExpanded = mutableStateOf(false)
    val repetitionExpanded = mutableStateOf(false)
    val deadlineExpanded = mutableStateOf(false)
    val sortByExpanded = mutableStateOf(false)
    //Stati per la gestione dei filtri
    val selectedMembers = mutableStateMapOf<Member, Boolean>()
    val selectedCategory = mutableStateMapOf<Category, Boolean>()
    val selectedPriority = mutableStateMapOf<Priority, Boolean>()
    val selectedStatus = mutableStateMapOf<Status, Boolean>()
    val selectedRepetition = mutableStateMapOf<Repetition, Boolean>()
    val selectedDeadline = mutableStateOf<LocalDate?>(null)
    var selectedChip by mutableStateOf("First")
}


@Composable
fun TeamScreen(vm: TeamScreenViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val view = LocalView.current
    val radioOptions = listOf("Creation date", "Deadline", "Priority", "Estimated hours", "Spent hours")
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
                                        .fillMaxHeight(0.75f)
                                        .verticalScroll(scrollState)
                            ) {
                                ExpandableRow(
                                    expanded = vm.assigneeExpanded,
                                    filter = {
                                        Text("Assignee", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            vm.membersList.forEach { member ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (vm.selectedMembers[member] == true) {
                                                                    vm.selectedMembers.remove(member)
                                                                } else {
                                                                    vm.selectedMembers[member] = true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (vm.selectedMembers[member] == true) {
                                                                    vm.selectedMembers.remove(member)
                                                                } else {
                                                                    vm.selectedMembers[member] = true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = vm.selectedMembers[member] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                vm.selectedMembers[member] = true
                                                            } else {
                                                                vm.selectedMembers.remove(member)
                                                            }
                                                        }
                                                    )
                                                    Text(member.username, textAlign = TextAlign.Center)
                                                }
                                            }
                                            if (vm.assigneeExpanded.value)
                                                HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = vm.categoryExpanded,
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
                                                                if (vm.selectedCategory[category] == true) {
                                                                    vm.selectedCategory.remove(category)
                                                                } else {
                                                                    vm.selectedCategory[category] =
                                                                        true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (vm.selectedCategory[category] == true) {
                                                                    vm.selectedCategory.remove(category)
                                                                } else {
                                                                    vm.selectedCategory[category] =
                                                                        true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = vm.selectedCategory[category] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                vm.selectedCategory[category] = true
                                                            } else {
                                                                vm.selectedCategory.remove(category)
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        category.toString().lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                            if (vm.categoryExpanded.value)
                                                HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = vm.priorityExpanded,
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
                                                                if (vm.selectedPriority[priority] == true) {
                                                                    vm.selectedPriority.remove(priority)
                                                                } else {
                                                                    vm.selectedPriority[priority] =
                                                                        true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (vm.selectedPriority[priority] == true) {
                                                                    vm.selectedPriority.remove(priority)
                                                                } else {
                                                                    vm.selectedPriority[priority] =
                                                                        true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = vm.selectedPriority[priority] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                vm.selectedPriority[priority] = true
                                                            } else {
                                                                vm.selectedPriority.remove(priority)
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        priority.toString().lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                            if (vm.priorityExpanded.value)
                                                HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = vm.statusExpanded,
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
                                                                if (vm.selectedStatus[status] == true) {
                                                                    vm.selectedStatus.remove(status)
                                                                } else {
                                                                    vm.selectedStatus[status] = true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (vm.selectedStatus[status] == true) {
                                                                    vm.selectedStatus.remove(status)
                                                                } else {
                                                                    vm.selectedStatus[status] = true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = vm.selectedStatus[status] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                vm.selectedStatus[status] = true
                                                            } else {
                                                                vm.selectedStatus.remove(status)
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
                                            if (vm.statusExpanded.value)
                                                HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = vm.repetitionExpanded,
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
                                                                if (vm.selectedRepetition[repetition] == true) {
                                                                    vm.selectedRepetition.remove(
                                                                        repetition
                                                                    )
                                                                } else {
                                                                    vm.selectedRepetition[repetition] =
                                                                        true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (vm.selectedRepetition[repetition] == true) {
                                                                    vm.selectedRepetition.remove(
                                                                        repetition
                                                                    )
                                                                } else {
                                                                    vm.selectedRepetition[repetition] =
                                                                        true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = vm.selectedRepetition[repetition]
                                                            ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                vm.selectedRepetition[repetition] = true
                                                            } else {
                                                                vm.selectedRepetition.remove(repetition)
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
                                            if (vm.repetitionExpanded.value)
                                                HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = vm.deadlineExpanded,
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
                                                    label = if (vm.selectedDeadline.value != null) {
                                                        ""
                                                    } else {
                                                        "Select a date"
                                                    },
                                                    value = vm.selectedDeadline.value,
                                                    onValueChange = { vm.selectedDeadline.value = it }
                                                )
                                            }
                                        }
                                    }
                                )
                                HorizontalDivider(color = Color.White, thickness = 2.dp, modifier = Modifier.padding(horizontal = 16.dp))
                                ExpandableRow(
                                    expanded = vm.sortByExpanded,
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
                                                    selected = (vm.selectedChip == "First"),
                                                    onClick = { vm.selectedChip = "First" },
                                                    label = {
                                                        when (selectedOption) {
                                                            "Creation date" -> Text("Newer")
                                                            "Deadline" -> Text("Earlier")
                                                            "Priority" -> Text("Higher")
                                                            else -> Text("Descending")
                                                        }
                                                    },
                                                    leadingIcon = if (vm.selectedChip == "First") {
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
                                                    selected = vm.selectedChip == "Second",
                                                    onClick = { vm.selectedChip = "Second" },
                                                    label = {
                                                        when (selectedOption) {
                                                            "Creation date" -> Text("Older")
                                                            "Deadline" -> Text("Later")
                                                            "Priority" -> Text("Lower")
                                                            else -> Text("Ascending")
                                                        }
                                                    },
                                                    leadingIcon = if (vm.selectedChip == "Second") {
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
                                            if (vm.sortByExpanded.value)
                                                HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                )
                                // Execute scrollState.animateScrollTo() when SortBy row expands
                                LaunchedEffect(vm.sortByExpanded.value) {
                                    if (vm.sortByExpanded.value) {
                                        scrollState.animateScrollTo(scrollState.value + 1000)
                                    }
                                }
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
                                        vm.selectedDeadline.value = null
                                        vm.lastAppliedFilters.value = mapOf(
                                            "selectedDeadline" to LocalDate.MAX
                                    )
                                        vm.selectedMembers.clear()
                                        vm.selectedCategory.clear()
                                        vm.selectedPriority.clear()
                                        vm.selectedStatus.clear()
                                        vm.selectedRepetition.clear()
                                        vm.assigneeExpanded.value = false
                                        vm.categoryExpanded.value = false
                                        vm.priorityExpanded.value = false
                                        vm.statusExpanded.value = false
                                        vm.repetitionExpanded.value = false
                                        vm.deadlineExpanded.value = false
                                        vm.sortByExpanded.value = false
                                        scope.launch { scrollState.animateScrollTo(0) }
                                        vm.filteredTasksList.value = vm.tasksList.filter { task ->
                                            applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                                        }
                                        onOptionSelected(radioOptions[0])
                                        vm.selectedChip = "First"
                                        sortTasks(radioOptions[0], "First", vm)
                                    }) {
                                        Text("Reset")
                                    }
                                Spacer(modifier = Modifier.padding(10.dp))
                                FilledTonalButton(colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary),onClick = {
                                    vm.lastAppliedFilters.value = mapOf(
                                        "selectedDeadline" to (vm.selectedDeadline.value ?: LocalDate.MAX),
                                        "selectedMembers" to vm.selectedMembers,
                                        "selectedCategory" to vm.selectedCategory,
                                        "selectedPriority" to vm.selectedPriority,
                                        "selectedStatus" to vm.selectedStatus,
                                        "selectedRepetition" to vm.selectedRepetition
                                    )
                                    vm.filteredTasksList.value = vm.tasksList.filter { task ->
                                        applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                                    }
                                    sortTasks(selectedOption, vm.selectedChip, vm)
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
                Box(modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 0.dp)) {
                    Scaffold(
                        floatingActionButton = { FAB(vm) },
                        content = { paddingValue ->
                            if (isVertical())
                                VerticalTaskListView(vm, drawerState, scope, context, view, selectedOption, vm.selectedChip, paddingValue)
                            else
                                HorizontalTaskListView(vm, drawerState, scope, context, view, selectedOption, vm.selectedChip, paddingValue)
                        }
                    )
                }
                //Dialog per l'assegnazione di un task
                when {
                    vm.openAssignDialog -> {
                        AssignDialog(vm)
                    }
                }
                //Dialog per l'uscita dal team
                when {
                    vm.openLeaveTeamDialog -> {
                        LeaveTeamDialog(vm)
                    }
                }
            } }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalTaskListView(vm: TeamScreenViewModel, drawerState: DrawerState, scope: CoroutineScope, context: Context, view: View, selectedOption: String, selectedChip: String, paddingValue: PaddingValues) {
    val navController = NavControllerManager.getNavController()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    var textExpanded by remember { mutableStateOf(false) }
    var isTextOverflowing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValue)
    ) {
        //Row del TeamName + image + dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamIcon(team = vm.currentTeam, modifierScale = Modifier.scale(1f), modifierPadding = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp))
            Spacer(modifier = Modifier.padding(5.dp))
            Text(
                text = vm.currentTeam.name,
                style = MaterialTheme.typography.displaySmall,
                maxLines = if (textExpanded) Int.MAX_VALUE else 1,
                overflow = if (textExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult: TextLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) isTextOverflowing = true
                },
                modifier =
                if (isTextOverflowing)
                    Modifier
                        .weight(1f)
                        .clickable { textExpanded = !textExpanded }
                        .animateContentSize()
                else
                    Modifier.weight(1f)
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
                                "More team info",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = { vm.expandedDropdown = false; navController.navigate("TeamDetails") { launchSingleTop = true } },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "See more team info",
                                modifier = Modifier.size(36.dp)
                            )
                        })
                    HorizontalDivider(color = Color.White)
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Change availability",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = { vm.expandedDropdown = false; navController.navigate("ChangeAvailability/${vm.teamId}") { launchSingleTop = true } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.availability),
                                contentDescription = "Set availability",
                                modifier = Modifier.size(36.dp)
                            )
                        })
                    HorizontalDivider(color = Color.White)
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add member/s",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = { vm.expandedDropdown = false; navController.navigate("Invitation/${vm.teamId}/${vm.currentTeam.name}") { launchSingleTop = true } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.addmember),
                                contentDescription = "Add member/s",
                                modifier = Modifier.size(36.dp)
                            )
                        })
                    HorizontalDivider(color = Color.White)
                    DropdownMenuItem(
                        text = {
                            Text(
                                "See statistics",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = { vm.expandedDropdown = false; navController.navigate("Statistics/${vm.teamId}") {launchSingleTop = true} },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.statistics),
                                contentDescription = "See statistics",
                                modifier = Modifier.size(36.dp)
                            )
                        })
                    HorizontalDivider(color = Color.White)
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Leave team",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Red
                            )
                        },
                        onClick = { vm.expandedDropdown = false; vm.openLeaveTeamDialog = true },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.exitteam),
                                contentDescription = "Leave team",
                                modifier = Modifier.size(36.dp),
                                tint = Color.Red
                            )
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        //Row dei membri del team
        Row(
            modifier = Modifier
                .fillMaxHeight(0.08f)
                .fillMaxWidth()
                .clickable { navController.navigate("ChatList/${vm.teamId}") { launchSingleTop = true } },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Members:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.padding(5.dp))
            //ProfileImages dei membri
            val membersToShow = if (vm.membersList.size > 5)
                vm.membersList.take(4)
            else
                vm.membersList
            for ((index, member) in membersToShow.withIndex()) {
                val endPadding = if (index == membersToShow.lastIndex) 16.dp else 20.dp
                MemberIcon(
                    modifierPadding = Modifier.padding(0.dp, 0.dp, endPadding, 0.dp),
                    member = member
                )
            }
            if (vm.membersList.size > 5) {
                Text(
                    text = "+ " + (vm.membersList.size - 4).toString() + " more",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = ">", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 16.dp))
        }
        Spacer(modifier = Modifier.padding(5.dp))
        HorizontalDivider(color = Color.White)
        Spacer(modifier = Modifier.padding(1.dp))
        //Row creazione di un nuovo task
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tasks:",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(
                onClick = {navController.navigate("Task/0"){
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
        //Row per searchbar, filtri e accesso al calendario
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SearchBar(
                colors= SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = vm.searchQuery,
                        onQueryChange = {
                            vm.searchQuery = it
                            vm.lastSearchQuery.value = vm.searchQuery.trim()
                            vm.filteredTasksList.value = vm.tasksList.filter { task ->
                                applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                            }
                            sortTasks(selectedOption, selectedChip, vm)
                        },
                        onSearch = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, 0)
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
                                        vm.filteredTasksList.value = vm.tasksList.filter { task ->
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
                modifier = Modifier.width((screenWidthDp * 0.6).dp)
            ) {}
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { navController.navigate("Calendar/${vm.teamId}"){
                    /*popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }*/
                    launchSingleTop = true
                    //restoreState = true
                } },
                modifier = Modifier
                    .scale(1.5f)
                    .padding(0.dp, 5.dp, 0.dp, 0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = "Access calendar",
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.padding(5.dp))
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .scale(1.5f)
                    .padding(0.dp, 5.dp, 0.dp, 0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.filters),
                    contentDescription = "Access filters",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.padding(3.dp))
        HorizontalDivider(color = Color.White)
        //Lista dei task
        if (vm.tasksList.isEmpty() || vm.filteredTasksList.value.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text =
                        if (vm.tasksList.isEmpty())
                            "${vm.currentTeam.name} team\nhas no tasks yet.\nCreate the first!"
                        else
                            "No tasks found!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            LazyColumn {
                itemsIndexed(vm.filteredTasksList.value) {index, task ->
                    ListItem(
                        modifier = Modifier.clickable { navController.navigate("Task/${task.id}") },
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
                                val membersToShow = if (task.members.size > 4)
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
                                if (task.members.size > 4) {
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
                    if (index < vm.filteredTasksList.value.lastIndex)
                        HorizontalDivider(color = Color.White)
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalTaskListView(vm: TeamScreenViewModel, drawerState: DrawerState, scope: CoroutineScope, context: Context, view: View, selectedOption: String, selectedChip: String, paddingValue: PaddingValues) {
    val navController = NavControllerManager.getNavController()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    var textExpanded by remember { mutableStateOf(false) }
    var isTextOverflowing by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f)
        ) {
            //Row del TeamName + image + dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
            ) {
                TeamIcon(team = vm.currentTeam, modifierScale = Modifier.scale(1f), modifierPadding = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp))
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    text = vm.currentTeam.name,
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = if (textExpanded) Int.MAX_VALUE else 1,
                    overflow = if (textExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult: TextLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) isTextOverflowing = true
                    },
                    modifier =
                    if (isTextOverflowing)
                        Modifier
                            .weight(1f)
                            .clickable { textExpanded = !textExpanded }
                            .animateContentSize()
                    else
                        Modifier.weight(1f)
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
                                    "More team info",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = { vm.expandedDropdown = false; navController.navigate("TeamDetails") { launchSingleTop = true } },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "See more team info",
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        HorizontalDivider(color = Color.White)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Change availability",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = { vm.expandedDropdown = false; navController.navigate("ChangeAvailability/${vm.teamId}") { launchSingleTop = true } },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.availability),
                                    contentDescription = "Set availability",
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        HorizontalDivider(color = Color.White)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Add member/s",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = { vm.expandedDropdown = false; navController.navigate("Invitation/${vm.teamId}/${vm.currentTeam.name}") { launchSingleTop = true } },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.addmember),
                                    contentDescription = "Add member/s",
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        HorizontalDivider(color = Color.White)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "See statistics",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = { vm.expandedDropdown = false; navController.navigate("Statistics/${vm.teamId}") {launchSingleTop = true} },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.statistics),
                                    contentDescription = "See statistics",
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        HorizontalDivider(color = Color.White)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Leave team",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Red

                                )
                            },
                            onClick = { vm.expandedDropdown = false; vm.openLeaveTeamDialog = true },
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
                modifier = Modifier
                    .fillMaxHeight(0.2f)
                    .fillMaxWidth()
                    .clickable { navController.navigate("ChatList/${vm.teamId}") { launchSingleTop = true } },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Members:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.padding(5.dp))
                //ProfileImages dei membri
                val membersToShow = if (vm.membersList.size > 5)
                    vm.membersList.take(4)
                else
                    vm.membersList
                for ((index, member) in membersToShow.withIndex()) {
                    val endPadding = if (index == membersToShow.lastIndex) 16.dp else 20.dp
                    MemberIcon(
                        modifierPadding = Modifier.padding(0.dp, 0.dp, endPadding, 0.dp),
                        member = member
                    )
                }
                if (vm.membersList.size > 5) {
                    Text(
                        text = "+ " + (vm.membersList.size - 4).toString() + " more",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = ">", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 16.dp))
            }
            Spacer(modifier = Modifier.padding(5.dp))
            HorizontalDivider(color = Color.White)
            Spacer(modifier = Modifier.padding(3.dp))
            //Row per searchbar, filtri e accesso al calendario
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SearchBar(
                    colors= SearchBarDefaults.colors(
                       containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = vm.searchQuery,
                            onQueryChange = {
                                vm.searchQuery = it
                                vm.lastSearchQuery.value = vm.searchQuery.trim()
                                vm.filteredTasksList.value = vm.tasksList.filter { task ->
                                    applyFilters(task, vm.lastAppliedFilters.value, vm.lastSearchQuery.value)
                                }
                                sortTasks(selectedOption, selectedChip, vm)
                            },
                            onSearch = {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(view.windowToken, 0)
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
                                            vm.filteredTasksList.value = vm.tasksList.filter { task ->
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
                    modifier = Modifier.width((screenWidthDp * 0.3).dp)
                ) {}
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { navController.navigate("Calendar/${vm.teamId}"){
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
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(5.dp))
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .scale(1.5f)
                        .padding(0.dp, 5.dp, 0.dp, 0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filters),
                        contentDescription = "Access filters",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        VerticalDivider(color = Color.White)
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = {navController.navigate("Task/0"){
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
            HorizontalDivider(color = Color.White)
            //Lista dei task
            if (vm.tasksList.isEmpty() || vm.filteredTasksList.value.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text =
                        if (vm.tasksList.isEmpty())
                            "${vm.currentTeam.name} team\nhas no tasks yet.\nCreate the first!"
                        else
                            "No tasks found!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                LazyColumn {
                    itemsIndexed(vm.filteredTasksList.value) { index, task ->
                        ListItem(
                            modifier = Modifier.clickable { navController.navigate("Task/${task.id}") },
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
                                    val membersToShow = if (task.members.size > 4)
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
                                    if (task.members.size > 4) {
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
                        if (index < vm.filteredTasksList.value.lastIndex)
                            HorizontalDivider(color = Color.White)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FAB(vm: TeamScreenViewModel){
    val navController = NavControllerManager.getNavController()

    FloatingActionButton(onClick ={
        navController.navigate("ChatList/${vm.teamId}"){
            launchSingleTop = true
        }},
        containerColor = MaterialTheme.colorScheme.primary,
    ){
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Chat",
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun AssignDialog(vm: TeamScreenViewModel) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    if (vm.taskToAssign != null) {
        val selectedMembers = remember { mutableStateMapOf<Member, Boolean>() }
        vm.membersList.forEach { member ->
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
                    LazyColumn(
                        modifier = Modifier.heightIn(0.dp, (screenHeightDp * 0.4).dp)
                    ) {
                        items(vm.membersList) { member ->
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
                                Text(text = member.username, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    if (vm.membersError.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp, 10.dp, 0.dp, 0.dp), horizontalArrangement = Arrangement.Start
                        ) {
                            Text(text = vm.membersError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(onClick = { vm.openAssignDialog = false; vm.membersError = "" }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        TextButton(onClick = {
                            if(selectedMembers.all { !it.value }) {
                                vm.membersError = "You Must Select at Least One Member"
                            } else {
                                vm.taskToAssign!!.members = selectedMembers.filterValues { it }.keys.toList()
                                vm.openAssignDialog = false
                                vm.membersError = ""
                            }
                        } ) {
                            Text("Confirm")
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun LeaveTeamDialog(vm: TeamScreenViewModel) {
    val navController = NavControllerManager.getNavController()

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = { vm.openLeaveTeamDialog = false },
        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Are you sure you want to leave the team: ${vm.currentTeam.name} ?") },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.openLeaveTeamDialog = false
                    //vm.deleteTeamMember
                    navController.navigate("Teams") { launchSingleTop = true }
                }
            ) {
                Text("Confirm", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { vm.openLeaveTeamDialog = false }
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun ExpandableRow(
    expanded: MutableState<Boolean>,
    filter: @Composable () -> Unit,
    filterOptions: @Composable () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (expanded.value) 180f else 0f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing), label = ""
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
    if (!expanded.value)
        HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))

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


fun sortTasks(selectedOption: String, selectedChip: String, vm: TeamScreenViewModel) {
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
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedBy { it.estimatedTime.first * 60 + it.estimatedTime.second }
            } else {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedByDescending { it.estimatedTime.first * 60 + it.estimatedTime.second }
            }
        }
        "Spent hours" -> {
            if (selectedChip == "Second") {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedBy { if(it.spentTime.isNotEmpty()) it.spentTime.values.sumOf { it.first } * 60 + it.spentTime.values.sumOf { it.second } else 0}
            } else {
                vm.filteredTasksList.value = vm.filteredTasksList.value.sortedByDescending { if(it.spentTime.isNotEmpty()) it.spentTime.values.sumOf { it.first } * 60 + it.spentTime.values.sumOf { it.second } else 0}
            }
        }
    }
}