package it.polito.uniteam.gui.userprofile

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.JoinInner
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.TeamIcon
import it.polito.uniteam.ui.theme.Orange


class OtherUserProfileScreen (val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {

    val memberId = checkNotNull(savedStateHandle["memberId"]).toString()
    var member: MemberDBFinal = MemberDBFinal()
    var teamsInCommon: List<TeamDBFinal> = emptyList()
    var loggedMember = model.loggedMemberFinal.id
}


@Composable
fun computeKPI(memberId: String): String {
    // get all the teams of the member
    val userTeams = AppStateManager.getTeams().filter { it.members.contains(memberId) }
    // get all the taskId of all the teams of the member
    val userTeamsTasksIds = mutableListOf<String>()
    userTeams.forEach {
        userTeamsTasksIds.addAll(it.tasks)
    }
    // get all the task of all the teams of the member that are completed
    val overAllUserTeamsTasks = AppStateManager.getTasks().filter { userTeamsTasksIds.contains(it.id) && it.status == Status.COMPLETED }
    val overallCompletedTasks = overAllUserTeamsTasks.size 
    var memberCompletedTasks = 0
    overAllUserTeamsTasks.forEach { completedTask ->
        if(completedTask.members.contains(memberId))
            memberCompletedTasks+=1
    }
    val retValue = if(memberCompletedTasks == 0 || overallCompletedTasks == 0) 0 else memberCompletedTasks/overallCompletedTasks
    return "${retValue * 100} % on all Completed Tasks."
}

@Composable
fun SetupOtherUserProfileData(vm: OtherUserProfileScreen = viewModel(factory = Factory(LocalContext.current.applicationContext))){
    vm.member = AppStateManager.getMembers().find { it.id == vm.memberId }!!
    vm.teamsInCommon = AppStateManager.getTeams().filter { it.members.contains(vm.memberId) && it.members.contains(vm.loggedMember) }
}


@Composable
fun OtherUserProfile(vm: OtherUserProfileScreen = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
    SetupOtherUserProfileData(vm = vm)
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                val rowItems = listOf(
                    Triple(Icons.Default.Person, "name", vm.member.fullName ),
                    Triple(Icons.Default.Face, "nickname",vm.member.username ),
                    Triple(Icons.Default.Email, "email", vm.member.email),
                    Triple(Icons.Default.LocationOn, "location", vm.member.location),
                    Triple(Icons.Default.Star, "KPI", computeKPI(memberId = vm.member.id))
                ).filter { it.third.isNotBlank() }
                val line_modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.onSurface)
                rowItems.forEachIndexed { index, (icon, description, value) ->
                    if (index == rowItems.size) {
                        RowItem(icon = icon, description = description, value = value)
                    } else {
                        RowItem(modifier = line_modifier, icon = icon, description = description, value = value)
                    }

                }


                RowItem(icon = Icons.Default.PeopleAlt, description = "Teams", value = "Teams in common:" )
                    vm.teamsInCommon.forEach{
                        Row(modifier = Modifier.fillMaxWidth(0.8f)) {
                            RowTeamItem(team = it, role = vm.member.teamsInfo?.get(it.id)?.role.toString(), member= vm.member)                        }

                    }/*
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Teams in common: ", style = MaterialTheme.typography.headlineSmall)
                    vm.teamsInCommon.forEach{
                        Row(modifier = Modifier.fillMaxWidth()) {
                            RowTeamItem(team = it)

                        }
                    }
                }*/

            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val rowItems = listOf(
                    Triple(Icons.Default.Person, "name", vm.member.fullName ),
                    Triple(Icons.Default.Face, "nickname", vm.member.username ),
                    Triple(Icons.Default.Email, "email", vm.member.email),
                    Triple(Icons.Default.LocationOn, "location", vm.member.location),
                    Triple(Icons.Default.Star, "KPI", vm.member.kpi)
                ).filter { it.third.isNotBlank() }
                val line_modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.onSurface)
                rowItems.forEachIndexed { index, (icon, description, value) ->
                    if (index == rowItems.size) {
                        RowItem(icon = icon, description = description, value = value)
                    } else {
                        RowItem(modifier = line_modifier, icon = icon, description = description, value = value)
                    }

                }

                    RowItem(icon = Icons.Default.PeopleAlt, description = "Teams", value = "Teams in common:")
                    vm.teamsInCommon.forEach{
                        Row(modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(0.dp, 0.dp, 0.dp, 0.dp)) {RowTeamItem(team = it, role = vm.member.teamsInfo?.get(it.id)?.role.toString(), member= vm.member)}



                }




            }
        }
    }

}


@Composable
fun RowTeamItem(modifier: Modifier = Modifier, team: TeamDBFinal, role: String, member: MemberDBFinal) {
    val controller = NavControllerManager.getNavController()
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { controller.navigate("Team/${team.id}") }) {
        Row(
            modifier = Modifier.padding(6.dp, 0.dp, 0.dp, 0.dp)
        ) {
            TeamIcon(team = team, modifierPadding = Modifier.padding(top = 8.dp, end = 20.dp))
            Text(
                team.name,
                modifier = Modifier
                    .padding(6.dp, 6.dp),
                style = MaterialTheme.typography.titleMedium,
            )


        }
    }

    Spacer(modifier = Modifier.padding(15.dp))
    HorizontalDivider(color = Color.White)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileSettings( vm: OtherUserProfileScreen = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
    vm.loggedMember = AppStateManager.getLoggedMemberFinal(members = AppStateManager.getMembers(),vm.model.loggedMemberFinal.id).id

    BoxWithConstraints {

            Box(modifier = Modifier.fillMaxSize()) {

                //
                BoxWithConstraints {
                    if (this.maxHeight > this.maxWidth) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                DefaultImageForTeam( vm)
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                            OtherUserProfile()

                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.33f)
                                    .fillMaxHeight()
                                    .padding(10.dp, 0.dp, 10.dp, 0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                DefaultImageForTeam( vm)
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                            OtherUserProfile()

                        }
                    }
                }
            }
        }
    }



@Composable
fun DefaultImageForTeam(vm: OtherUserProfileScreen = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
    val member = vm.member
    val name = member.fullName
    println(name)
    if (name.isNotBlank() || member.profileImage != Uri.EMPTY) {

        Card(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    if (member.profileImage != Uri.EMPTY) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(member.profileImage)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape), // Clip the image into a circular shape
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = name.trim().split(' ');
                        var initialsValue = initials
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .first();

                        if (initials.size >=2) {
                            initialsValue += initials
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .last()
                        }
                        Text(
                            modifier = Modifier
                                .padding(40.dp)
                                .size(80.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Orange,
                                        radius = this.size.maxDimension
                                    )
                                },
                            text = initialsValue,
                            style = TextStyle(color = Color.White, fontSize = 60.sp, textAlign = TextAlign.Center)
                        )
                    }

                }
            }
        }
    }
}






