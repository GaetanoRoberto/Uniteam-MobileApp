package it.polito.uniteam.gui.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.inputmethod.InputMethodManager
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.R
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.LoadingSpinner
import it.polito.uniteam.classes.MemberDB
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.TeamDB
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.TeamIcon
import it.polito.uniteam.isVertical
import kotlinx.coroutines.launch


class HomeViewModel (val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
//    var teamsList = model.getTeams()
//    var membersList = model.getAllTeamsMembersHome()
    //Stati di caricamento dati dal db
    val isLoading = model.isLoading
    val loaded = mutableStateOf(false)
    //Stati per la gestione dei filtri
    var lastAppliedFilters = mutableStateOf<Map<String,Any>>(mapOf("selectedMembers" to emptyMap<MemberDBFinal, Boolean>()))
    var expandedSearch by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var lastSearchQuery by mutableStateOf("")
    var descriptionSearched by mutableStateOf(false)
    val selectedMembers = mutableStateMapOf<MemberDBFinal, Boolean>()
    val radioOptions = listOf("Name", "Creation date")
    var selectedChip by mutableStateOf("First")
    var teamsList: List<TeamDBFinal> = listOf()
    var filteredTeamsList = mutableStateOf(teamsList.map { it.copy() })
//    val teams = model.getTeams()
//    val teams = model.getAllTeams2()
//    val members = model.getAllMembers2()
//    val tasks = model.getAllTasks2()
//    val histories = model.getAllHistories2()
//    val chats = model.getAllChats2()
//    val files = model.getAllFiles2()
//    val comments = model.getAllComments2()
//    val messages = model.getAllMessages2()
}

//@Preview
//@Composable
//fun Db(vm: HomeViewModel = viewModel(factory = Factory(LocalContext.current))) {
//    val comments by vm.messages.collectAsState(initial = listOf())
//    LazyColumn(modifier = Modifier.fillMaxSize()) {
//        items(comments) {
//            Text(text = it.toString())
//            Spacer(modifier = Modifier.height(8.dp))
//}    }
//}


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(vm: HomeViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val navController = NavControllerManager.getNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val screenWeightDp = LocalConfiguration.current.screenWidthDp
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(vm.radioOptions[0]) }
    // Stati per team e membri
    val loggedMember = AppStateManager.getLoggedMember()
    val teams = AppStateManager.getTeams()
    val membersList = AppStateManager.getMembers()
    vm.teamsList = teams.filter { team -> team.members.contains(loggedMember.id)}
    vm.filteredTeamsList = mutableStateOf(vm.teamsList.sortedBy { it.name })
    val filteredMembersList = membersList.filter { member -> vm.teamsList.flatMap { team -> team.members.filter { it != loggedMember.id } }.toSet().contains(member.id) }

    LaunchedEffect(vm.teamsList) {
        vm.loaded.value = true
        vm.isLoading.value = false
        vm.filteredTeamsList.value = vm.teamsList.sortedBy { it.name }
    }

    //Drawer dei filtri
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            //gesturesEnabled = false,
            drawerContent = { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                ModalDrawerSheet(drawerState, drawerShape = RoundedCornerShape(topStart =  16.dp, bottomStart = 16.dp, topEnd = 0.dp, bottomEnd = 0.dp)) {
                    Box() {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Row(
                                modifier = if (isVertical())
                                    Modifier.padding(16.dp, 16.dp, 0.dp, 0.dp)
                                else
                                    Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Filter by team members :",
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
                                filteredMembersList.forEach { member ->
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
                                HorizontalDivider(
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                                ) {
                                    Text(
                                        "Sort by :",
                                        style = if (isVertical())
                                            MaterialTheme.typography.titleLarge
                                        else
                                            MaterialTheme.typography.titleMedium
                                    )
                                }
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp)
                                    ) {
                                        FilterChip(
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            selected = (vm.selectedChip == "First"),
                                            onClick = { vm.selectedChip = "First" },
                                            label = {
                                                when (selectedOption) {
                                                    "Name" -> Text("Ascending")
                                                    "Creation date" -> Text("Newer")
                                                }
                                            },
                                            leadingIcon = if (vm.selectedChip == "First") {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Done,
                                                        contentDescription = "Done icon",
                                                        modifier = Modifier.size(
                                                            FilterChipDefaults.IconSize
                                                        )
                                                    )
                                                }
                                            } else {
                                                null
                                            }
                                        )
                                        Spacer(modifier = Modifier.padding(10.dp))
                                        FilterChip(
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            selected = vm.selectedChip == "Second",
                                            onClick = { vm.selectedChip = "Second" },
                                            label = {
                                                when (selectedOption) {
                                                    "Name" -> Text("Descending")
                                                    "Creation date" -> Text("Older")
                                                }
                                            },
                                            leadingIcon = if (vm.selectedChip == "Second") {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Done,
                                                        contentDescription = "Done icon",
                                                        modifier = Modifier.size(
                                                            FilterChipDefaults.IconSize
                                                        )
                                                    )
                                                }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                    Column(Modifier.selectableGroup()) {
                                        vm.radioOptions.forEach { text ->
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
                            Spacer(modifier = Modifier.padding(5.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                FilledTonalButton(colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                    onClick = {
                                        vm.selectedMembers.clear()
                                        scope.launch { scrollState.animateScrollTo(0) }
                                        vm.filteredTeamsList.value =
                                            vm.teamsList.filter { team ->
                                                applyFilters(
                                                    team,
                                                    vm.lastAppliedFilters.value,
                                                    vm.lastSearchQuery,
                                                    vm
                                                )
                                            }
                                        onOptionSelected(vm.radioOptions[0])
                                        vm.selectedChip = "First"
                                        sortTeams(vm.radioOptions[0], "First", vm.filteredTeamsList)
                                    }) {
                                    Text("Reset")
                                }
                                Spacer(modifier = Modifier.padding(10.dp))
                                FilledTonalButton(colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ), onClick = {
                                    vm.lastAppliedFilters.value = mapOf(
                                        "selectedMembers" to vm.selectedMembers
                                    )
                                    vm.filteredTeamsList.value = vm.teamsList.filter { team ->
                                        applyFilters(
                                            team,
                                            vm.lastAppliedFilters.value,
                                            vm.lastSearchQuery,
                                            vm
                                        )
                                    }
                                    sortTeams(selectedOption, vm.selectedChip, vm.filteredTeamsList)
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
                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { navController.navigate("TeamDetails/0") { launchSingleTop = true } },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create new team",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        },
                        content = { paddingValue ->
                            Column(modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp)
                                .padding(paddingValue)) {
                                //Row per searchbar e filtri
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SearchBar(
                                        colors = SearchBarDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        ),
                                        inputField = {
                                            SearchBarDefaults.InputField(
                                                query = vm.searchQuery,
                                                onQueryChange = {
                                                    vm.loaded.value = false
                                                    vm.searchQuery = it
                                                    vm.lastSearchQuery = vm.searchQuery.trim()
                                                    vm.filteredTeamsList.value =
                                                        vm.teamsList.filter { team ->
                                                            applyFilters(
                                                                team,
                                                                vm.lastAppliedFilters.value,
                                                                vm.lastSearchQuery,
                                                                vm
                                                            )
                                                        }
                                                    sortTeams(selectedOption, vm.selectedChip, vm.filteredTeamsList)
                                                },
                                                onSearch = {
                                                    val imm =
                                                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                                                },
                                                expanded = false,
                                                onExpandedChange = { vm.expandedSearch = it },
                                                placeholder = { Text("Search team") },
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
                                                                vm.lastSearchQuery = vm.searchQuery
                                                                vm.filteredTeamsList.value =
                                                                    vm.teamsList.filter { team ->
                                                                        applyFilters(
                                                                            team,
                                                                            vm.lastAppliedFilters.value,
                                                                            vm.lastSearchQuery,
                                                                            vm
                                                                        )
                                                                    }
                                                                sortTeams(
                                                                    selectedOption,
                                                                    vm.selectedChip,
                                                                    vm.filteredTeamsList
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                            )
                                        },
                                        expanded = false,
                                        onExpandedChange = { vm.expandedSearch = it },
                                        modifier = Modifier.width((screenWeightDp * 0.8).dp)
                                    ) {}
                                    Spacer(modifier = Modifier.weight(1f))
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
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    HorizontalDivider(color = Color.White)
                                    //Lista dei team
                                    if (vm.isLoading.value)
                                        LoadingSpinner()
                                    else if (vm.teamsList.isEmpty() || vm.filteredTeamsList.value.isEmpty()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text =
                                                if (vm.teamsList.isEmpty())
                                                    "You are not a member of any team yet.\nCreate or join one!"
                                                else if (vm.loaded.value)
                                                    ""
                                                else
                                                    "No teams found!",
                                                style = MaterialTheme.typography.headlineSmall,
                                                modifier = Modifier.padding(top = 16.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn {
                                            items(vm.filteredTeamsList.value) { team ->
                                                ListItem(
                                                    modifier = Modifier.clickable { /*vm.model.selectTeam(team.id);*/ navController.navigate("Team/${team.id}") { launchSingleTop = true } },
                                                    headlineContent = {
                                                        Text(
                                                            team.name,
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    },
                                                    leadingContent = {
                                                        TeamIcon(team = team, modifierPadding = if(team.image != Uri.EMPTY) Modifier.padding(5.dp, 0.dp, 15.dp, 0.dp) else Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp))
                                                    },
                                                    supportingContent = {
                                                        if (vm.descriptionSearched) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(
                                                                    0.dp,
                                                                    10.dp,
                                                                    0.dp,
                                                                    0.dp
                                                                )
                                                            ) {
                                                                Text(text = team.description)
                                                            }
                                                        }
                                                    }
                                                )
                                                HorizontalDivider(color = Color.White)
                                            }
                                        }
                                    }
                                }
                        }
                    )
                }
            } }
        )
    }
}


fun applyFilters(team: TeamDBFinal, lastAppliedFilters: Map<String, Any>, lastSearchQuery: String, vm: HomeViewModel): Boolean {
    var keep = true

    val selectedMembers = lastAppliedFilters["selectedMembers"] as? Map<*, *> ?: emptyMap<MemberDBFinal, Boolean>()

    if (selectedMembers.isNotEmpty()) {
        val selectedMembers2 = selectedMembers as Map<MemberDBFinal, Boolean>
        keep = keep && team.members.any { memberId -> selectedMembers2.any { it.key.id == memberId } }
    }

    keep = keep && (team.name.contains(lastSearchQuery, ignoreCase = true) || team.description.contains(lastSearchQuery, ignoreCase = true))

    vm.descriptionSearched = team.description.contains(lastSearchQuery, ignoreCase = true)

    if (lastSearchQuery == "")
        vm.descriptionSearched = false

    return keep
}

fun sortTeams(selectedOption: String, selectedChip: String, filteredTeamsList: MutableState<List<TeamDBFinal>>) {
    when (selectedOption) {
        "Name" -> {
            if (selectedChip == "First") {
                filteredTeamsList.value = filteredTeamsList.value.sortedBy { it.name }
            } else {
                filteredTeamsList.value = filteredTeamsList.value.sortedByDescending { it.name }
            }
        }
        "Creation date" -> {
            if (selectedChip == "Second") {
                filteredTeamsList.value = filteredTeamsList.value.sortedBy { it.creationDate }
            } else {
                filteredTeamsList.value = filteredTeamsList.value.sortedByDescending { it.creationDate }
            }
        }
    }
}