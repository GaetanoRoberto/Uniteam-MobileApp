package it.polito.uniteam.gui.statistics

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.model.LegendLabel
import co.yml.charts.common.model.LegendsConfig
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import it.polito.uniteam.Factory
import it.polito.uniteam.R
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.gui.showtaskdetails.CustomDatePicker
import it.polito.uniteam.gui.showtaskdetails.Demo_ExposedDropdownMenuBox
import it.polito.uniteam.gui.tasklist.AssignDialog
import it.polito.uniteam.gui.tasklist.ExpandableRow
import it.polito.uniteam.gui.tasklist.HorizontalTaskListView
import it.polito.uniteam.gui.tasklist.VerticalTaskListView
import it.polito.uniteam.gui.tasklist.applyFilters
import it.polito.uniteam.gui.tasklist.sortTasks
import it.polito.uniteam.isVertical
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun Statistics(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
                                        .fillMaxHeight(0.6f)
                                        .verticalScroll(scrollState)
                            ) {
                                ExpandableRow(
                                    expanded = vm.assigneeExpanded,
                                    filter = {
                                        Text("Members", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            vm.initialTeamMembers.forEach { member ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (vm.selectedMembers[member] == true) {
                                                                    vm.selectedMembers.remove(member)
                                                                } else {
                                                                    vm.selectedMembers[member] =
                                                                        true
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
                                                                    vm.selectedMembers[member] =
                                                                        true
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
                                    expanded = vm.tasksExpanded,
                                    filter = {
                                        Text("Tasks", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            vm.initialTeamTasks.forEach { task ->
                                                Row(
                                                    modifier = if (isVertical())
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (vm.selectedTasks[task] == true) {
                                                                    vm.selectedTasks.remove(task)
                                                                } else {
                                                                    vm.selectedTasks[task] = true
                                                                }
                                                            }
                                                    else
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .height(40.dp)
                                                            .clickable {
                                                                if (vm.selectedTasks[task] == true) {
                                                                    vm.selectedTasks.remove(task)
                                                                } else {
                                                                    vm.selectedTasks[task] = true
                                                                }
                                                            },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = vm.selectedTasks[task] ?: false,
                                                        onCheckedChange = {
                                                            if (it) {
                                                                vm.selectedTasks[task] = true
                                                            } else {
                                                                vm.selectedTasks.remove(task)
                                                            }
                                                        }
                                                    )
                                                    Text(task.name, textAlign = TextAlign.Center)
                                                }
                                            }
                                            if (vm.tasksExpanded.value)
                                                HorizontalDivider(color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                )
                                ExpandableRow(
                                    expanded = vm.categoryExpanded,
                                    filter = {
                                        Text("Task category", style = MaterialTheme.typography.titleMedium)
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
                                                                    vm.selectedCategory.remove(
                                                                        category
                                                                    )
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
                                                                    vm.selectedCategory.remove(
                                                                        category
                                                                    )
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
                                                        category.toString().lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                                                            Locale.getDefault()) else it.toString() },
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
                                        Text("Task priority", style = MaterialTheme.typography.titleMedium)
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
                                                                    vm.selectedPriority.remove(
                                                                        priority
                                                                    )
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
                                                                    vm.selectedPriority.remove(
                                                                        priority
                                                                    )
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
                                                        priority.toString().lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                                                            Locale.getDefault()) else it.toString() },
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
                                        Text("Task status", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Column {
                                            Status.entries.filter { status -> status != Status.TODO }.forEach { status ->
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
                                    expanded = vm.datesExpanded,
                                    filter = {
                                        Text("Period", style = MaterialTheme.typography.titleMedium)
                                    },
                                    filterOptions = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, end = 16.dp)
                                        ) {
                                            CustomDatePickerStatistics(
                                                label = if (vm.selectedStart.value != null) {
                                                    ""
                                                } else {
                                                    "Start"
                                                },
                                                value = vm.selectedStart.value,
                                                onValueChange = { vm.selectedStart.value = it },
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            CustomDatePickerStatistics(
                                                label = if (vm.selectedEnd.value != null) {
                                                    ""
                                                } else {
                                                    "End"
                                                },
                                                value = vm.selectedEnd.value,
                                                onValueChange = { vm.selectedEnd.value = it },
                                                modifier = Modifier.weight(1f)
                                            )
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
                                        vm.selectedStart.value = null
                                        vm.selectedEnd.value = null
                                        vm.selectedMembers.clear()
                                        vm.selectedTasks.clear()
                                        vm.selectedCategory.clear()
                                        vm.selectedPriority.clear()
                                        vm.selectedStatus.clear()
                                        vm.assigneeExpanded.value = false
                                        vm.tasksExpanded.value = false
                                        vm.categoryExpanded.value = false
                                        vm.priorityExpanded.value = false
                                        vm.statusExpanded.value = false
                                        vm.datesExpanded.value = false
                                        scope.launch { scrollState.animateScrollTo(0) }
                                        vm.teamTasks.value = vm.initialTeamTasks
                                        vm.teamMembers.value = vm.initialTeamMembers
                                        vm.selectedChartValue = ""
                                        Log.i("TeamTasksReset", vm.teamTasks.value.toString())
                                        Log.i("InitialReset", vm.initialTeamTasks.toString())
                                    }) {
                                    Text("Reset")
                                }
                                Spacer(modifier = Modifier.padding(10.dp))
                                FilledTonalButton(colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                    vm.lastAppliedFilters.value = mapOf(
                                        "selectedTasks" to vm.selectedTasks,
                                        "selectedMembers" to vm.selectedMembers,
                                        "selectedCategory" to vm.selectedCategory,
                                        "selectedPriority" to vm.selectedPriority,
                                        "selectedStatus" to vm.selectedStatus
                                    )
                                    Log.i("initial", vm.initialTeamTasks.toString())
                                    Log.i("TeamTasks", vm.teamTasks.value.toString())
                                    vm.teamTasks.value = vm.initialTeamTasks.filter { task ->
                                        vm.applyTasksFilters(task, vm.lastAppliedFilters.value)
                                    }
                                    Log.i("TeamTasks", vm.teamTasks.value.toString())
                                    Log.i("TeamTasksBefore", vm.initialTeamTasks.toString())

                                    vm.teamTasks.value = vm.teamTasks.value.map { t->
                                        val task = t.copy()
                                        val newSchedules = task.schedules.filter { (key,value) ->
                                            val (member,date) = key
                                            if (vm.selectedStart.value != null && vm.selectedEnd.value != null)
                                                (date.isAfter(vm.selectedStart.value) || date.isEqual(vm.selectedStart.value)) && (date.isBefore(vm.selectedEnd.value) || date.isEqual(vm.selectedEnd.value))
                                            else if (vm.selectedStart.value != null && vm.selectedEnd.value == null)
                                                date.isAfter(vm.selectedStart.value) || date.isEqual(vm.selectedStart.value)
                                            else if (vm.selectedStart.value == null && vm.selectedEnd.value != null)
                                                date.isBefore(vm.selectedEnd.value) || date.isEqual(vm.selectedEnd.value)
                                            else
                                                true
                                        }
                                        task.schedules = newSchedules.toMap(HashMap())
                                        task
                                    }.filter { task -> task.schedules.isNotEmpty() }
                                    Log.i("TeamTasks", vm.teamTasks.value.toString())
                                    Log.i("TeamTasksAfter", vm.initialTeamTasks.toString())

                                    vm.teamMembers.value = vm.initialTeamMembers.filter { member ->
                                        vm.applyMembersFilters(member, vm.lastAppliedFilters.value)
                                    }
                                    vm.selectedChartValue = ""
                                    scope.launch { drawerState.close() }
                                }) {
                                    Text("Apply")
                                }
                            }
                        }
                    }
                }
            }
            },
            content = { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                //Resto della UI
                if(isVertical()) {
                    VerticalStatistics(vm = vm, scope = scope, drawerState = drawerState)
                } else {
                    HorizontalStatistics(vm = vm, scope = scope, drawerState = drawerState)
                }
            } }
        )
    }
}

@Composable
fun VerticalStatistics(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current)), drawerState: DrawerState, scope: CoroutineScope) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Statistics", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(10.dp,5.dp,0.dp,0.dp))
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .scale(1.5f)
                    .padding(0.dp, 5.dp, 10.dp, 0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.filters),
                    contentDescription = "Access filters",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(15.dp))
            Demo_ExposedDropdownMenuBox(
                label = "Chart Type:",
                currentValue = vm.selectedChart.toString(),
                values = chartType.entries.map { it.toString() },
                onChange = vm::changeChart
            )
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                when(vm.selectedChart){
                    chartType.PLANNED_SPENT_HOURS_RATIO -> {
                        Text(text = "Hold on a Chart Bar to see details")
                        BarChart(vm = vm)
                    }
                    chartType.OVERALL_SPENT_HOURS -> {
                        if(vm.getOverallSpentHours()!=null)
                            Text(text = "Click on the Chart to see/hide details")
                        OverallSpentHoursChart(vm = vm)
                    }
                    chartType.OVERALL_TEAM_KPI -> {
                        if(vm.getOverallTeamKPI()!=null)
                            Text(text = "Click on the Chart to see/hide details")
                        OverallTeamKPIChart(vm = vm)
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalStatistics(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current)), drawerState: DrawerState, scope: CoroutineScope) {
    Row {
        Column(modifier = Modifier
            .fillMaxWidth(0.6f)
            .fillMaxHeight(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                when(vm.selectedChart){
                    chartType.PLANNED_SPENT_HOURS_RATIO -> {
                        Text(text = "Hold on a Chart Bar to see details")
                        BarChart(vm = vm)
                    }
                    chartType.OVERALL_SPENT_HOURS -> {
                        if(vm.getOverallSpentHours()!=null)
                            Text(text = "Click on the Chart to see/hide details")
                        OverallSpentHoursChart(vm = vm)
                    }
                    chartType.OVERALL_TEAM_KPI -> {
                        if(vm.getOverallTeamKPI()!=null)
                            Text(text = "Click on the Chart to see/hide details")
                        OverallTeamKPIChart(vm = vm)
                    }
                }
            }
        }
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Top) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Statistics", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(10.dp,5.dp,0.dp,0.dp))
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .scale(1.5f)
                        .padding(0.dp, 5.dp, 10.dp, 0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filters),
                        contentDescription = "Access filters",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxHeight(0.8f), verticalAlignment = Alignment.Bottom) {
                Demo_ExposedDropdownMenuBox(
                    label = "Chart Type:",
                    currentValue = vm.selectedChart.toString(),
                    values = chartType.entries.map { it.toString() },
                    onChange = vm::changeChart
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerStatistics(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
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
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
            )
        },
        modifier = modifier
            .clickable { open.value = true },
        enabled = false,
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
fun SingleLegend(config: LegendsConfig, legendLabel: LegendLabel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp,10.dp,0.dp,0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val boxModifier = Modifier.size(config.colorBoxSize)
        if (legendLabel.brush != null) {
            Box(modifier = boxModifier.background(legendLabel.brush!!))
        } else {
            Box(modifier = boxModifier.background(legendLabel.color))
        }

        Spacer(modifier = Modifier.padding(config.spaceBWLabelAndColorBox))
        Text(
            text = legendLabel.name, style = config.textStyle, overflow = TextOverflow.Ellipsis
        )
    }
}