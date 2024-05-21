package it.polito.uniteam.gui.userprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.JoinInner
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toIcon
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import it.polito.uniteam.Factory
import it.polito.uniteam.R
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.TeamIcon
import it.polito.uniteam.gui.TeamDetails.TeamDetailsViewModel
import it.polito.uniteam.gui.showtaskdetails.RowMemberItem
import it.polito.uniteam.ui.theme.Orange
import java.io.File
import java.util.concurrent.ExecutorService


class OtherUserProfileScreen (val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    val member = model.selectedUser
    val teamsInCommon = model.teams.filter{
        it.members.contains(member) && it.members.contains(model.loggedMember)
    }
}





@Preview
@Composable
fun OtherUserProfile(vm: OtherUserProfileScreen = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
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
                    Triple(Icons.Default.Star, "KPI", vm.member.kpi)
                )
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


                RowItem(icon = Icons.Default.JoinInner, description = "Teams", value = "Teams in common:" )
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
                    Triple(Icons.Default.Face, "nickname",vm.member.username ),
                    Triple(Icons.Default.Email, "email", vm.member.email),
                    Triple(Icons.Default.LocationOn, "location", vm.member.location),
                    Triple(Icons.Default.Star, "KPI", vm.member.kpi)
                )
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

                    RowItem(icon = Icons.Default.JoinInner, description = "Teams", value = "Teams in common:")
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
fun RowTeamItem(modifier: Modifier = Modifier, team: Team, role: String, member: Member) {
    Column {
        Row(
            modifier = Modifier.padding(6.dp, 0.dp, 0.dp, 0.dp)
        ) {

            TeamIcon(team = team, modifierScale = Modifier.scale(0.65f),)
            Text(
                team.name.toString() ,
                modifier = Modifier
                    .padding(6.dp, 0.dp),
                style = MaterialTheme.typography.headlineSmall,
            )


        }
        // not good to visualize
        if(role != "null"){
            Row {
                Text(
                    " Role: " + role ,
                    modifier = Modifier
                        .padding(8.dp, 0.dp, 0.dp,5.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                )
            }
        }
        if(member.teamsInfo?.get(team.id)?.weeklyAvailabilityHours?.first != null){
            Row {
                Text(
                    " Availability hours: " + member.teamsInfo?.get(team.id)?.weeklyAvailabilityHours?.first + "," + member.teamsInfo?.get(team.id)?.weeklyAvailabilityHours?.second + "h" ,
                    modifier = Modifier
                        .padding(8.dp, 0.dp, 0.dp,5.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                )
            }
        }

    }

    Spacer(modifier = Modifier.padding(5.dp))
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileSettings(vm: OtherUserProfileScreen = viewModel(factory = Factory(LocalContext.current.applicationContext))) {


    BoxWithConstraints {

            Box(modifier = Modifier.fillMaxSize()) {
                // Image at the top
                Image(
                    painter = rememberAsyncImagePainter(vm.member.profileImage),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .align(Alignment.TopCenter),
                )

                // Buttons at the bottom

                Spacer(modifier = Modifier.height(16.dp))

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
                                DefaultImageForTeam(vm)
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
                                DefaultImageForTeam(vm)
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                            OtherUserProfile()

                        }
                    }
                }
            }
        }
    }



@Preview
@Composable
fun DefaultImageForTeam(vm: OtherUserProfileScreen = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
    val name = vm.member.fullName
    println(name)
    if (name.isNotBlank() || vm.member.profileImage != Uri.EMPTY) {

        Card(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    if (vm.member.profileImage != Uri.EMPTY) {
                        Image(
                            painter = rememberAsyncImagePainter(vm.member.profileImage),
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






