package it.polito.uniteam.gui.availability

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.CategoryRole
import it.polito.uniteam.classes.HourMinutesPicker
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.isVertical
import kotlin.enums.EnumEntries


class AvailabilityViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    val teamId: String? = savedStateHandle["teamId"]
    val teamName: String? = savedStateHandle["teamName"]
    fun updateLoggedMemberTeamInfo(memberId: String, teamId: String, newRole: String, newHours: Number, newMinutes: Number, newTimes: Number) = model.updateLoggedMemberTeamInfo(memberId,teamId,newRole,newHours,newMinutes,newTimes)

    val roleValues = CategoryRole.entries
    var timesError = mutableStateOf("")
        private set
    var timeError = mutableStateOf("")
        private set

    var role = mutableStateOf(CategoryRole.NONE)
}


@Composable
fun Availability(vm: AvailabilityViewModel = viewModel(factory = Factory(LocalContext.current)), roleCallback: (role:CategoryRole) -> Unit = {}, timesCallback: (times:String) -> Unit = {}, hoursCallback: (hours:String) -> Unit = {}, minutesCallback: (minutes:String) -> Unit = {}) {
    val navController = NavControllerManager.getNavController()
    val loggedMember = AppStateManager.getLoggedMemberFinal(members = AppStateManager.getMembers(),vm.model.loggedMemberFinal.id)
    val initialRole = if(vm.teamId != "0") loggedMember.teamsInfo?.get(vm.teamId)?.role!! else CategoryRole.NONE
    val currentRoleState = rememberSaveable { mutableStateOf(initialRole) }
    val times = remember { mutableStateOf(if(vm.teamId != "0") loggedMember.teamsInfo?.get(vm.teamId)?.weeklyAvailabilityTimes.toString() else "0") }
    val hours = remember { mutableStateOf(if(vm.teamId != "0") loggedMember.teamsInfo?.get(vm.teamId)?.weeklyAvailabilityHours?.first.toString() else "0")}
    val minutes = remember { mutableStateOf(if(vm.teamId != "0") loggedMember.teamsInfo?.get(vm.teamId)?.weeklyAvailabilityHours?.second.toString() else "0")}

    LaunchedEffect(Unit) {
        vm.role.value = currentRoleState.value
        vm.model.resetAvailabilitiesErrors()
    }

    fun changeRole(categoryRole: CategoryRole) {
        currentRoleState.value = categoryRole
        vm.role.value = categoryRole
        roleCallback(categoryRole)
    }

    fun checkTimes() {
        try {
            val timesInt = times.value.toUInt().toInt()
            if (timesInt == 0) {
                vm.model.timesError.value = "You Need To Schedule A Positive Number."
            } else {
                vm.model.timesError.value = ""
                times.value = timesInt.toString()
            }
        } catch (e: RuntimeException) {
            vm.model.timesError.value = "Valid Positive Number Must Be Provided."
        }
    }

    fun checkTime() {
        try {
            val hoursInt = hours.value.toUInt().toInt()
            val minutesInt = minutes.value.toUInt().toInt()
            if (hoursInt == 0 && minutesInt == 0) {
                vm.model.timeError.value = "You Need To Schedule A Positive Time Interval."
            } else if (minutesInt >= 60) {
                vm.model.timeError.value = "Invalid Minute Value."
            } else {
                vm.model.timeError.value = ""
                hours.value = hoursInt.toString()
                minutes.value = minutesInt.toString()
            }
        } catch (e: RuntimeException) {
            vm.model.timeError.value = "Valid Positive Numbers Must Be Provided."
        }
    }

    fun save(): Boolean {
        checkTimes()
        checkTime()
        if (vm.model.timesError.value.isEmpty() && vm.model.timeError.value.isEmpty()) {
            val newTeamInfo = MemberTeamInfo(
                role = vm.role.value,
                weeklyAvailabilityTimes = times.value.toInt(),
                weeklyAvailabilityHours = Pair(hours.value.toInt(), minutes.value.toInt())
            )
            vm.updateLoggedMemberTeamInfo(loggedMember.id, vm.teamId!!, newTeamInfo.role.toString(), newTeamInfo.weeklyAvailabilityHours.first, newTeamInfo.weeklyAvailabilityHours.second, newTeamInfo.weeklyAvailabilityTimes)
            return true
        } else return false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = if (isVertical()) Modifier.fillMaxHeight(0.9f) else Modifier) {
            Column(
                modifier = if(vm.teamId != "0") Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                else
                    Modifier.fillMaxSize()
            ) {
                //Title
                if (vm.teamId != "0") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Change your role and availability in the team:\n ${vm.teamName}",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
                //Dropdown roles
                RolesDropdown(vm.role.value, vm.roleValues, ::changeRole)
                Spacer(modifier = Modifier.padding(16.dp))
                //Availability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly availability:",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Start
                    )
                }
                Spacer(modifier = Modifier.padding(5.dp))
                //Times per week
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "How many times per week?",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start
                    )
                }
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = times.value,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                autoCorrectEnabled = true,
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            onValueChange = { value ->
                                times.value = value
                                if(vm.teamId == "0") {
                                    checkTimes()
                                    timesCallback(value)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedContainerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        if (vm.model.timesError.value.isNotEmpty())
                            Text(vm.model.timesError.value, color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                //Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "How much time per day?",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start
                    )
                }
                HourMinutesPicker(hourState = hours, minuteState = minutes, errorMsg = vm.model.timeError, hoursCallback = {hoursCallback(it); checkTime();}, minutesCallback = {minutesCallback(it); checkTime(); })
                if (!isVertical() && vm.teamId != "0") {
                    Spacer(modifier = Modifier.padding(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Save button
                        FilledTonalButton(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            onClick = { if (save()) navController.navigate("Team/${vm.teamId}") {launchSingleTop = true} }
                        ) {
                            Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
        if (isVertical() && vm.teamId != "0") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Save button
                FilledTonalButton(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    onClick = { if (save()) navController.navigate("Team/${vm.teamId}") {launchSingleTop = true} }
                ) {
                    Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolesDropdown(currentValue: CategoryRole, values: EnumEntries<CategoryRole>, onChange: (CategoryRole) -> Unit ) {
    var expanded by remember { mutableStateOf(false) }

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
                    text = "Role:",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary)
                )
            },
            value = currentValue.toString(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .heightIn(0.dp, 200.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary)
                    .heightIn(0.dp, 200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                values.forEachIndexed { i, item ->
                    DropdownMenuItem(
                        text = { Text(text = item.toString(), color = MaterialTheme.colorScheme.onPrimary) },
                        onClick = {
                            onChange(item)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (i != values.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp), color = Color.White)
                    }
                }
            }
        }
    }
}