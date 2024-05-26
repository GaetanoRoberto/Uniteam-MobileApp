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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.CategoryRole
import it.polito.uniteam.classes.HourMinutesPicker
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.isVertical
import kotlin.enums.EnumEntries


class AvailabilityViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    val teamId: String = checkNotNull(savedStateHandle["teamId"])
    val teamName = model.getTeam(teamId.toInt()).name
    var role by mutableStateOf(model.loggedMember.value.teamsInfo?.get(teamId.toInt())?.role)
        private set

    val roleValues = CategoryRole.entries
    fun changeRole(categoryRole: CategoryRole) {
        role = categoryRole
    }
    var times = mutableStateOf(model.loggedMember.value.teamsInfo?.get(teamId.toInt())?.weeklyAvailabilityTimes.toString())
        private set
    var timesError = mutableStateOf("")
        private set
    private fun checkTimes() {
        try {
            val timesInt = times.value.toUInt().toInt()
            if (timesInt == 0) {
                timesError.value = "You Need To Schedule A Positive Number."
            } else {
                timesError.value = ""
                times.value = timesInt.toString()
            }
        } catch (e: RuntimeException) {
            timesError.value = "Valid Positive Number Must Be Provided."
        }
    }
    var hours = mutableStateOf(model.loggedMember.value.teamsInfo?.get(teamId.toInt())?.weeklyAvailabilityHours?.first.toString())
        private set
    var minutes = mutableStateOf(model.loggedMember.value.teamsInfo?.get(teamId.toInt())?.weeklyAvailabilityHours?.second.toString())
        private set
    var timeError = mutableStateOf("")
        private set
    private fun checkTime() {
        try {
            val hoursInt = hours.value.toUInt().toInt()
            val minutesInt = minutes.value.toUInt().toInt()
            if (hoursInt == 0 && minutesInt == 0) {
                timeError.value = "You Need To Schedule A Positive Time Interval."
            } else if (minutesInt >= 60) {
                timeError.value = "Invalid Minute Value."
            } else {
                timeError.value = ""
                hours.value = hoursInt.toString()
                minutes.value = minutesInt.toString()
            }
        } catch (e: RuntimeException) {
            timeError.value = "Valid Positive Numbers Must Be Provided."
        }
    }
    fun save(): Boolean {
        checkTimes()
        checkTime()
        if (timesError.value.isEmpty() && timeError.value.isEmpty()) {
            val newTeamInfo = MemberTeamInfo(
                    role = role!!,
                    weeklyAvailabilityTimes = times.value.toInt(),
                    weeklyAvailabilityHours = Pair(hours.value.toInt(), minutes.value.toInt())
            )
            model.updateTeamInfo(teamId = teamId.toInt(), newTeamInfo = newTeamInfo)
            return true
        } else return false
    }
}


@Composable
fun Availability(vm: AvailabilityViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val navController = NavControllerManager.getNavController()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = if (isVertical()) Modifier.fillMaxHeight(0.9f) else Modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                //Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Change your role and availability in the team: ${vm.teamName}",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                //Dropdown roles
                RolesDropdown(vm.role!!, vm.roleValues, vm::changeRole)
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
                            value = vm.times.value,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                autoCorrectEnabled = true,
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            onValueChange = { value ->
                                vm.times.value = value
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
                        if (vm.timesError.value.isNotEmpty())
                            Text(vm.timesError.value, color = MaterialTheme.colorScheme.error)
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
                HourMinutesPicker(hourState = vm.hours, minuteState = vm.minutes, errorMsg = vm.timeError)
                if (!isVertical()) {
                    Spacer(modifier = Modifier.padding(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Save button
                        FilledTonalButton(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            onClick = { if (vm.save()) navController.navigate("Team/${vm.teamId}") {launchSingleTop = true} }
                        ) {
                            Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
        if (isVertical()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Save button
                FilledTonalButton(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    onClick = { if (vm.save()) navController.navigate("Team/${vm.teamId}") {launchSingleTop = true} }
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