package it.polito.uniteam.gui.teamDetails

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.CategoryRole
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.handleInputString
import it.polito.uniteam.classes.permissionRole
import java.time.LocalDate
import java.time.LocalDateTime

class TeamDetailsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    // from model
    var loggedMember = MemberDBFinal()
    val teamId: String = checkNotNull(savedStateHandle["teamId"])
    var isAdmin:Boolean? = null//loggedMember.teamsInfo?.get(teamId)?.permissionrole == permissionRole.ADMIN
    var addTeam = teamId.length == 1 // pass 0 when add so length 1
    var editing by mutableStateOf(teamId.length == 1)
    var isTeamDeleted = false
    var temporaryId: Int = 1

    var teamName = mutableStateOf("")
    var teamDescription = mutableStateOf("")
    var teamProfileImage = mutableStateOf(Uri.EMPTY)
    var teamMembers = mutableStateListOf<MemberDBFinal>()
    var teamCreationDate = LocalDate.now()

    var beforeTeamMembers = teamMembers
    var beforeTeamName = teamName.value
    var beforeTeamDescription = teamDescription.value
    var beforeTeamProfileImage = teamProfileImage.value

    // add team memberinfo
    var memberRole = mutableStateOf(CategoryRole.NONE)
    var times = mutableStateOf("0")
    var hours = mutableStateOf("0")
    var minutes = mutableStateOf("0")

    fun setRole(role: CategoryRole) {
        memberRole.value = role
        Log.i("change",memberRole.value.toString())
    }
    fun setTimes(timess: String) {
        times.value = timess
    }
    fun setHours(hourss: String) {
        hours.value = hourss
    }
    fun setMinutes(minutess:String) {
        minutes.value = minutess
    }
    fun checkTimes() {
        try {
            val timesInt = times.value.toUInt().toInt()
            if (timesInt == 0) {
                model.timesError.value = "You Need To Schedule A Positive Number."
            } else {
                model.timesError.value = ""
                times.value = timesInt.toString()
            }
        } catch (e: RuntimeException) {
            model.timesError.value = "Valid Positive Number Must Be Provided."
        }
    }

    fun checkTime() {
        try {
            val hoursInt = hours.value.toUInt().toInt()
            val minutesInt = minutes.value.toUInt().toInt()
            if (hoursInt == 0 && minutesInt == 0) {
                model.timeError.value = "You Need To Schedule A Positive Time Interval."
            } else if (minutesInt >= 60) {
                model.timeError.value = "Invalid Minute Value."
            } else {
                model.timeError.value = ""
                hours.value = hoursInt.toString()
                minutes.value = minutesInt.toString()
            }
        } catch (e: RuntimeException) {
            model.timeError.value = "Valid Positive Numbers Must Be Provided."
        }
    }

    var history = mutableListOf<HistoryDBFinal>()
    fun hasChanged(oldValue: String, newValue: String): Boolean {
        return oldValue != newValue
    }

    fun handleTeamHistory(){
        val entryToAdd: MutableList<HistoryDBFinal> = mutableListOf()
        if(!addTeam) {
            if(hasChanged(beforeTeamName,teamName.value) || hasChanged(beforeTeamDescription,teamDescription.value)
                || teamProfileImage != beforeTeamProfileImage) {
                // general editing
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = "Team Edited.",
                    date = LocalDateTime.now(),
                    user = loggedMember.id
                ))
            }
            // members
            for (oldMember in beforeTeamMembers) {
                if(!teamMembers.contains(oldMember)) {
                    // deleted member
                    entryToAdd.add(HistoryDBFinal(
                        id = (temporaryId++).toString(),
                        comment = "${oldMember.username} removed from the Team ${teamName.value}.",
                        date = LocalDateTime.now(),
                        user = loggedMember.id
                    ))
                }
            }
            for (member in teamMembers) {
                if(!beforeTeamMembers.contains(member)) {
                    // added member
                    entryToAdd.add(HistoryDBFinal(
                        id = (temporaryId++).toString(),
                        comment = "${member.username} added in the Team ${teamName.value}.",
                        date = LocalDateTime.now(),
                        user = loggedMember.id
                    ))
                }
            }
            // add to history
            entryToAdd.forEach{ entry->
                history.add(entry)
            }
        } else {
            // add team so call after the db
            history.add(HistoryDBFinal(
                id = (temporaryId++).toString(),
                comment = "Team Created.",
                date = LocalDateTime.now(),
                user = loggedMember.id
            ))
        }
    }

    var teamNameError by mutableStateOf("")
        private set
    fun changeTeamName(s: String) {
        teamName.value = handleInputString(s)
    }

    private fun checkTeamName() {
        if (teamName.value.isBlank())
            teamNameError = "Team name cannot be blank!"
        else
            teamNameError = ""
    }

    var descriptionError by mutableStateOf("")
        private set

    fun changeDescription(s: String) {
        teamDescription.value = handleInputString(s)
    }

    private fun checkDescription() {
        if (teamDescription.value.isBlank())
            descriptionError = "Team description cannot be blank!"
        else
            descriptionError = ""
    }

    fun validate() {
        checkTeamName()
        checkDescription()
        checkTime()
        checkTimes()
    }

    fun cancel(){
        teamName.value = beforeTeamName
        teamDescription.value = beforeTeamDescription
        teamProfileImage.value = beforeTeamProfileImage
        teamMembers = beforeTeamMembers
    }

    fun changeEditing() {
        editing = !editing
    }

    /*fun teamCreation(flag: Boolean){
        newTeam = flag
    }*/

    fun setUri(uri: Uri) {
        teamProfileImage.value = uri
    }

    var openAssignDialog = mutableStateOf(false)

    var cameraPressed by mutableStateOf(false)
        private set

    fun toggleCameraButtonPressed() {
        cameraPressed = !cameraPressed
    }

    var showCamera by mutableStateOf(false)
        private set
    fun showCamera(boolean: Boolean) {
        showCamera = boolean
    }

    var temporaryUri = Uri.EMPTY
        private set

    fun setTemporaryUri(uri: Uri) {
        temporaryUri = uri
    }

    var showPhoto by mutableStateOf(false)
        private set

    fun showPhoto(boolean: Boolean) {
        showPhoto = boolean
    }
    var isFrontCamera by mutableStateOf(true)
        private set

    fun setIsFrontCamera(boolean: Boolean) {
        isFrontCamera = boolean
    }
    var openGallery by mutableStateOf(false)
        private set

    fun openGallery(boolean: Boolean) {
        openGallery = boolean
    }

    fun handleImageCapture(uri: Uri) {
        showCamera = false
        temporaryUri = uri
        showPhoto = true
    }

    var showConfirmationDialog by mutableStateOf(false)
        private set

    fun toggleDialog() {
        showConfirmationDialog = !showConfirmationDialog
    }

    var openDeleteTeamDialog by mutableStateOf(false)

}