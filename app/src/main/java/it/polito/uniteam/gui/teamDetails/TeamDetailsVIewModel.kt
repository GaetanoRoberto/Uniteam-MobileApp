package it.polito.uniteam.gui.teamDetails

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.handleInputString
import it.polito.uniteam.classes.permissionRole
import java.time.LocalDate
import java.time.LocalDateTime

class TeamDetailsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    // from model
    val loggedMember = model.loggedMemberFinal
    val teamId: String = checkNotNull(savedStateHandle["teamId"])
    val isAdmin = loggedMember.teamsInfo?.get(teamId)?.permissionrole == permissionRole.ADMIN
    var editing by mutableStateOf(false)
    var addTeam = teamId.length == 1 // pass 0 when add so length 1
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

    var history = mutableListOf<HistoryDBFinal>()
    fun hasChanged(oldValue: String, newValue: String): Boolean {
        return oldValue != newValue
    }

    fun handleTeamHistory(){
        val entryToAdd: MutableList<HistoryDBFinal> = mutableListOf()
        if(editing) {
            if(hasChanged(beforeTeamName,teamName.value) || hasChanged(beforeTeamDescription,teamDescription.value)) {
                // general editing
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = "Team Edited.",
                    date = LocalDateTime.now(),
                    user = loggedMember.id
                ))
            }
            // members
            val removedComment = "Members removed: "
            var removedMembers = ""
            val addedComment = "Members Added: "
            var addedMembers = ""
            for (oldMember in beforeTeamMembers) {
                if(!teamMembers.contains(oldMember)) {
                    // deleted member
                    removedMembers += oldMember.username + " "
                }
            }
            for (member in teamMembers) {
                if(!beforeTeamMembers.contains(member)) {
                    // added member
                    addedMembers += member.username + " "
                }
            }
            if (removedMembers.isNotEmpty() && addedMembers.isNotEmpty()) {
                val comment = "Team " + removedComment + removedMembers + "\n" +
                        "Team " + addedComment + addedMembers + "\n"
                entryToAdd.add(
                    HistoryDBFinal(
                        id = (temporaryId++).toString(),
                        comment = comment,
                        date = LocalDateTime.now(),
                        user = loggedMember.id
                    )
                )
            } else if (removedMembers.isNotEmpty()) {
                val comment = "Team " + removedComment + removedMembers
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = comment,
                    date = LocalDateTime.now(),
                    user = loggedMember.id
                ))
            } else if (addedMembers.isNotEmpty()) {
                val comment = "Team " + addedComment + addedMembers
                entryToAdd.add(HistoryDBFinal(
                    id = (temporaryId++).toString(),
                    comment = comment,
                    date = LocalDateTime.now(),
                    user = loggedMember.id
                ))
            }
            // add to history
            entryToAdd.forEach{ entry->
                history.add(entry)
            }
            // TODO Add db history directly in edit (I have firebase teamId)
            //model.addHistories(history,taskId)
        } else {
            // add team so call after the db
            entryToAdd.add(HistoryDBFinal(
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