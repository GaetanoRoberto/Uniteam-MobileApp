package it.polito.uniteam.gui.teamDetails

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import it.polito.uniteam.AppStateManager
import coil.request.ImageRequest
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.R
import it.polito.uniteam.classes.CompressImage
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.handleInputString
import it.polito.uniteam.classes.permissionRole
import it.polito.uniteam.gui.availability.Availability
import it.polito.uniteam.gui.availability.Join
import it.polito.uniteam.gui.showtaskdetails.EditRowItem
import it.polito.uniteam.gui.showtaskdetails.HistoryView
import it.polito.uniteam.gui.showtaskdetails.RowItem
import it.polito.uniteam.gui.userprofile.AlertDialogExample
import it.polito.uniteam.gui.userprofile.getCameraProvider
import it.polito.uniteam.gui.userprofile.takePhoto
import it.polito.uniteam.isVertical
import it.polito.uniteam.ui.theme.Orange
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

@Composable
fun SetupTeamData(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
    if(!vm.addTeam && !vm.isTeamDeleted) {
        val team = AppStateManager.getTeams().find { it.id == vm.teamId }!!
        val teamMembers = AppStateManager.getMembers().filter { team.members.contains(it.id) }
        val teamHistory = AppStateManager.getHistories().filter { team.teamHistory.contains(it.id) }
        vm.teamName.value = team.name
        vm.beforeTeamName = team.name
        vm.teamDescription.value = team.description
        vm.beforeTeamDescription = team.description
        vm.teamProfileImage.value = team.image
        vm.beforeTeamProfileImage = team.image
        vm.teamCreationDate = team.creationDate
        vm.teamMembers = teamMembers.toMutableStateList()
        vm.beforeTeamMembers = teamMembers.toMutableStateList()
        vm.history = teamHistory.toMutableList()
    }
}

@Composable
fun isTeamChanges(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))): Boolean {
    return vm.editing
}

@Composable
fun TeamViewScreen(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext)),
                   outputDirectory: File,
                   cameraExecutor: ExecutorService){
    val controller = NavControllerManager.getNavController()
    LaunchedEffect(key1 = 1) {
        vm.model.resetAvailabilitiesErrors()
    }
    vm.loggedMember = AppStateManager.getLoggedMemberFinal(members = AppStateManager.getMembers(),vm.model.loggedMemberFinal.id)
    vm.isAdmin = vm.loggedMember.teamsInfo?.get(vm.teamId)?.permissionrole == permissionRole.ADMIN
    // Handle Back Button
    BackHandler(onBack = {
        if(vm.addTeam)
            controller.navigate("Teams")
        if(vm.editing)
            vm.changeEditing()
        else
            controller.navigate("Team/${vm.teamId}")
    })
    val context = LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activity: ActivityResult? ->
        if (activity == null || activity.resultCode != Activity.RESULT_OK) {
            // User canceled the action, handle it here
            // For example, you can show a toast or log a message
            Log.d("Uniteam", "User canceled image selection")
            vm.openGallery(false)
        } else {
            val uri = activity.data?.data
            if (uri != null) {
                // Image picked successfully, do something with the URI
                vm.setUri(CompressImage(context = context, sourceUri = uri))
            }
        }
        // Optionally, you can still call vm.openGallery() here if needed
        vm.openGallery(false)
    }

    if (vm.openGallery) {
        // Launch gallery intent
        val galleryIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(galleryIntent)
    }
    if (vm.showCamera) {
        // Handle Back Button
        BackHandler(onBack = {
            vm.showCamera(false)
        })
        CameraViewForTeam(
            vm = vm,
            outputDirectory = outputDirectory,
            executor = cameraExecutor,
            onError = { Log.e("kilo", "View error:", it) }
        )
    } else if (vm.showPhoto) {
        // Handle Back Button
        BackHandler(onBack = {
            vm.showPhoto(false)
        })

        BoxWithConstraints {
            if (this.maxHeight > this.maxWidth) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Image at the top
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(vm.temporaryUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                            .align(Alignment.TopCenter),
                    )

                    // Buttons at the bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxHeight(0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    vm.showPhoto(false)
                                    vm.setTemporaryUri(Uri.EMPTY)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),// Imposta il colore di sfondo del bottone a rosso,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Undo", color = MaterialTheme.colorScheme.onSecondary)
                            }

                            Spacer(modifier = Modifier.padding(16.dp))

                            Button(
                                onClick = {
                                    vm.showPhoto(false)
                                    vm.setUri(vm.temporaryUri)
                                    vm.setTemporaryUri(Uri.EMPTY)
                                    Toast.makeText(
                                        context,
                                        "Profile Image Updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),// Imposta il colore di sfondo del bottone a rosso,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Confirm",
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Image on the left
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1.2f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(vm.temporaryUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Buttons on the right
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                vm.showPhoto(false)
                                vm.setTemporaryUri(Uri.EMPTY)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),// Imposta il colore di sfondo del bottone a rosso,

                            modifier = Modifier.width(300.dp)
                        ) {
                            Text(text = "Undo", color = MaterialTheme.colorScheme.onSecondary)
                        }

                        Spacer(modifier = Modifier.padding(10.dp))

                        Button(
                            onClick = {
                                vm.showPhoto(false)
                                vm.setUri(vm.temporaryUri)
                                vm.setTemporaryUri(Uri.EMPTY)
                                Toast.makeText(context, "Profile Image Updated", Toast.LENGTH_SHORT)
                                    .show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),// Imposta il colore di sfondo del bottone a rosso,
                            modifier = Modifier.width(300.dp)
                        ) {
                            Text(text = "Confirm", color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                }
            }
        }
    }else{




        BoxWithConstraints {

            Box(modifier = Modifier.fillMaxSize()) {
                // Image at the top
                /*
                Image(
                    painter = rememberAsyncImagePainter(vm.selectedTeam.value.image),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .align(Alignment.TopCenter),
                )*/


                Spacer(modifier = Modifier.height(16.dp))

                //
                BoxWithConstraints {
                    if (this.maxHeight > this.maxWidth) {
                        if (!vm.editing && vm.isAdmin!!) {
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column {
                                FloatingActionButton(
                                    onClick = { vm.changeEditing() },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 16.dp,
                                        end = 16.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                FloatingActionButton(
                                    onClick = { vm.openDeleteTeamDialog = true },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }}
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .padding(0.dp, 20.dp, 0.dp, 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {

                                key(vm.teamProfileImage.value){
                                    DefaultImageForEditingTeam(vm)
                                }



                            }
                            if(vm.editing){
                                TeamDetailsEdit()
                            }else{
                                TeamDetailsView(customHeightForHistory = 0.41f)

                            }
                            //Spacer(modifier = Modifier.height(0.dp))


                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .fillMaxHeight()
                                    .padding(10.dp, 0.dp, 10.dp, 0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row {
                                    Column(
                                        modifier = Modifier.fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        key(vm.teamId){
                                            DefaultImageForEditingTeam(vm)
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.fillMaxHeight(),
                                        verticalArrangement = Arrangement.Top,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (!vm.editing && vm.isAdmin!!) {
                                            FloatingActionButton(
                                                onClick = { vm.changeEditing() },
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(
                                                    start = 16.dp,
                                                    top = 16.dp,
                                                    end = 16.dp
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.onSecondary,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                            FloatingActionButton(
                                                onClick = { vm.openDeleteTeamDialog = true },
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.onSecondary,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                            if(vm.editing){
                                TeamDetailsEdit()
                            }else{
                                TeamDetailsView(customHeightForHistory = 0.41f)

                            }

                        }
                    }
                }
            }
        }


    //Dialog per la delete del team
    when {
        vm.openDeleteTeamDialog -> {
            DeleteTeamDialog(vm)
        }
    }
}}

@Composable
fun CameraViewForTeam(
    vm: TeamDetailsViewModel= viewModel(factory = Factory(LocalContext.current.applicationContext)),
    outputDirectory: File,
    executor: Executor,
    onError: (ImageCaptureException) -> Unit
) {
    // 1
    val lensFacing = if(vm.isFrontCamera) { CameraSelector.LENS_FACING_FRONT } else { CameraSelector.LENS_FACING_BACK}
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = androidx.camera.core.Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    // 2
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // 3
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        val configuration = LocalConfiguration.current
        IconButton(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .size(60.dp),
            onClick = {
                takePhoto(
                    filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                    imageCapture = imageCapture,
                    outputDirectory = outputDirectory,
                    executor = executor,
                    onImageCaptured = vm::handleImageCapture,
                    onError = onError,
                    flip = vm.isFrontCamera,
                    configuration = configuration
                )
            }) {
            Icon(
                painter = painterResource(R.drawable.camera),
                contentDescription = "Take picture",
                tint = Color.White,
                modifier = Modifier
                    .padding(1.dp)
                    .scale(0.8f)
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
                .size(60.dp),
            onClick = {
                vm.setIsFrontCamera(!vm.isFrontCamera)
            }) {
            Icon(
                painter = painterResource(R.drawable.change_camera),
                contentDescription = "Change Camera",
                tint = Color.White,
                modifier = Modifier
                    .padding(1.dp)
                    .size(50.dp)
            )
        }
    }
}
@Preview
@Composable
fun TeamDetailsView(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext)), customHeightForHistory: Float = 0.7f) {

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.padding(16.dp))
        Log.i("Prova",vm.teamName.value)
        RowItem(title = "Name:", value = vm.teamName.value)
        RowItem(title = "Description:", value = vm.teamDescription.value)
        //RowMemberItem( title = "Members:", value = vm.selectedTeam.value.members)
        RowItem(title = "Creation Date:", value = vm.teamCreationDate)
        val icon = Icons.Filled.History
        val title = "Team History"
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            Tab(selected = true,
                enabled = false,
                onClick = {},
                text = { Text(text = title, color = MaterialTheme.colorScheme.onPrimary) },
                icon = {
                    Icon(
                        icon,
                        title,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                })
        }
        if(customHeightForHistory != 0.7f){
            HistoryView(history = vm.history.toMutableList(), customHeightForHistory)
        }else{
            HistoryView(history = vm.history.toMutableList())
        }

    }
}


@Preview
@Composable
fun TeamDetailsEdit(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))){
    val controller = NavControllerManager.getNavController()
    Row(){
        Column(modifier = Modifier.fillMaxSize(),  verticalArrangement = Arrangement.Bottom) {
            Row(modifier = Modifier.fillMaxHeight(0.8f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp, 0.dp)
                ) {

                    Spacer(modifier = Modifier.padding(10.dp))
                    EditRowItem(
                        label = "Name:",
                        value = vm.teamName.value,
                        errorText = vm.teamNameError,
                        onChange = vm::changeTeamName
                    )
                    EditRowItem(
                        label = "Description:",
                        value = vm.teamDescription.value,
                        errorText = vm.descriptionError,
                        onChange = vm::changeDescription
                    )
                    if (!vm.addTeam ) {
                        if(vm.teamMembers.size>1){
                            TeamMembersDropdownMenuBox(
                                vm,
                                "Manage Members",
                                vm.teamMembers.filter{ it.id != vm.loggedMember.id }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.White)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Choose Your Role/Availability for The New Team.",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Availability(roleCallback = vm::setRole,
                        timesCallback = vm::setTimes,
                        hoursCallback = vm::setHours,
                        minutesCallback = vm::setMinutes)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if(!isVertical()){

                        Row(modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp), verticalAlignment = Alignment.Bottom){
                            Spacer(modifier = Modifier.width(15.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                    /*navController.navigate("Tasks"){
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }*/

                                    if(vm.addTeam){
                                        controller.navigate("Teams")
                                    } else {
                                        vm.cancel()
                                        //vm.teamCreation(false)
                                        vm.changeEditing()
                                    }
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.width(15.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                Button( colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                                    vm.validate()
                                    if (vm.teamNameError == "" && vm.descriptionError == "") {
                                        // TODO finish save team logic with db
                                        vm.handleTeamHistory()
                                        if(vm.addTeam){
                                            if (vm.model.timeError.value == "" && vm.model.timesError.value == "") {
                                                vm.model.createTeam(TeamDBFinal(
                                                    name = handleInputString(vm.teamName.value),
                                                    description = handleInputString(vm.teamDescription.value),
                                                    image = vm.teamProfileImage.value,
                                                    creationDate = LocalDate.now(),
                                                    members = mutableListOf(vm.loggedMember.id)
                                                ), MemberTeamInfo(
                                                    role = vm.memberRole.value,
                                                    weeklyAvailabilityTimes = vm.times.value.toInt(),
                                                    weeklyAvailabilityHours = Pair(vm.hours.value.toInt(),vm.minutes.value.toInt()),
                                                    permissionrole = permissionRole.ADMIN
                                                ),vm.history.first())
                                                controller.navigate("Teams")
                                            }
                                        }else{
                                            vm.changeEditing()
                                            vm.model.updateTeam(
                                                vm.teamId,
                                                handleInputString(vm.teamName.value),
                                                handleInputString(vm.teamDescription.value),
                                                vm.teamProfileImage.value,
                                                vm.teamMembers.map { it.id },
                                                vm.history
                                            )
                                        }
                                    }
                                }, modifier = Modifier
                                    .fillMaxWidth()) {
                                    Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }

                        }

                    }
                }
            }
            if(isVertical()){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        //.fillMaxHeight()
                        .padding(0.dp, 0.dp, 0.dp, 5.dp)
                    ,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                            if(vm.addTeam){
                                controller.navigate("Teams")
                            } else {
                                vm.cancel()
                                //vm.teamCreation(false)
                                vm.changeEditing()
                            }
                            /*navController.navigate("Tasks"){
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }*/

                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                            vm.validate()
                            if (vm.teamNameError == "" && vm.descriptionError == "") {
                                // TODO finish save team logic with db
                                vm.handleTeamHistory()
                                if(vm.addTeam){
                                    if (vm.model.timeError.value == "" && vm.model.timesError.value == "") {
                                        vm.model.createTeam(TeamDBFinal(
                                            name = handleInputString(vm.teamName.value),
                                            description = handleInputString(vm.teamDescription.value),
                                            image = vm.teamProfileImage.value,
                                            creationDate = LocalDate.now(),
                                            members = mutableListOf(vm.loggedMember.id)
                                        ), MemberTeamInfo(
                                            role = vm.memberRole.value,
                                            weeklyAvailabilityTimes = vm.times.value.toInt(),
                                            weeklyAvailabilityHours = Pair(vm.hours.value.toInt(),vm.minutes.value.toInt()),
                                            permissionrole = permissionRole.ADMIN
                                        ),vm.history.first())
                                        controller.navigate("Teams")
                                    }
                                }else{
                                    vm.changeEditing()
                                    vm.model.updateTeam(
                                        vm.teamId,
                                        handleInputString(vm.teamName.value),
                                        handleInputString(vm.teamDescription.value),
                                        vm.teamProfileImage.value,
                                        vm.teamMembers.map { it.id },
                                        vm.history
                                    )
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

        }
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMembersDropdownMenuBox(
    vm: TeamDetailsViewModel,
    label: String,
    currentMembers: List<MemberDBFinal>
) {
    val controller = NavControllerManager.getNavController()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp)

    ) {
        ExposedDropdownMenuBox(
            expanded = vm.openAssignDialog.value,
            onExpandedChange = {
                vm.openAssignDialog.value = true
            },
            modifier = Modifier.fillMaxWidth()

        ) {

            OutlinedTextField(
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                    )
                },
                value = " ",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        modifier = Modifier.width(15.dp),
                        painter = painterResource(id = R.drawable.minus),
                        contentDescription = "Remove"
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                //isError = errorText.isNotBlank(),
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .horizontalScroll(rememberScrollState())
                            .padding(0.dp, 0.dp, 5.dp, 0.dp)
                    ) {
                        currentMembers.forEachIndexed { index, member ->
                            MemberIcon(
                                member = member,
                                modifierScale = Modifier.scale(0.65f),
                                modifierPadding = Modifier.padding(start = if (index == 0) 12.dp else 0.dp),
                                isLoggedMember = (member.id == vm.loggedMember.id)
                            )
                            Text(
                                text = member.username.toString() + if (index < currentMembers.size - 1) {
                                    ", "
                                } else {
                                    ""
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            if (index < currentMembers.size - 1) {
                                Spacer(modifier = Modifier.width(4.dp)) // Add spacing between images and texts
                            }
                        }
                    }
                }

            )
            if (vm.openAssignDialog.value) {
                TeamAssignMemberDialog(vm)
            }
        }
    }
}

@Composable
fun TeamAssignMemberDialog(vm: TeamDetailsViewModel) {
    val selectedMembers = remember { mutableStateMapOf<MemberDBFinal, Boolean>() }
    vm.teamMembers.forEach { member ->
        selectedMembers[member] = vm.teamMembers.toMutableList().contains(member)
    }
    Dialog(onDismissRequest = { vm.openAssignDialog.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp), horizontalArrangement = Arrangement.Center
                ) {
                    if (isVertical())
                        Text(
                            text = vm.teamName.value,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    else
                        Text(
                            text = vm.teamName.value,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 0.dp, 0.dp, 5.dp), horizontalArrangement = Arrangement.Start
                ) {
                    Text(text = "Members assigned :", style = MaterialTheme.typography.bodyMedium)
                }

                LazyColumn(
                    modifier = if (isVertical()) Modifier.heightIn(
                        0.dp,
                        265.dp
                    ) else Modifier.heightIn(0.dp, 165.dp)
                ) {
                    item(1) {
                        vm.teamMembers.filter{it.id != vm.loggedMember.id}.forEach { member ->
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
                                Text(text = member.username.toString(), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { vm.openAssignDialog.value = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    TextButton(onClick = {
                        vm.teamMembers.clear()
                        vm.teamMembers.addAll(selectedMembers.filterValues { it }.keys.toMutableStateList())
                        vm.openAssignDialog.value = false
                    }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }

    }

}



@Preview
@Composable
fun DefaultImageForTeamScreen(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
    val name = vm.teamName.value
    if (name.isNotBlank() || vm.teamProfileImage.value != Uri.EMPTY) {

        Card(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    if (vm.teamProfileImage.value != Uri.EMPTY) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(vm.teamProfileImage.value)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape), // Clip the image into a circular shape
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(40.dp)
                                .size(80.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Orange,
                                        radius = this.size.maxDimension
                                    )
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.people),
                                contentDescription = "Image",
                                modifier = Modifier
                                    //.padding(40.dp, 0.dp, 40.dp, 0.dp)
                                    .scale(1.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DefaultImageForEditingTeam(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext))) {
    val name = vm.teamName.value
    if (name.isNotBlank() || vm.teamProfileImage.value != Uri.EMPTY) {

        Card(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    if (vm.teamProfileImage.value != Uri.EMPTY) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(vm.teamProfileImage.value)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = if (!isVertical()) {
                                Modifier
                                    .padding(
                                        start = 0.dp,
                                        top = 0.dp,
                                        end = 35.dp,
                                        bottom = 15.dp
                                    )
                                    .size(160.dp)
                                    .clip(CircleShape) // Clip the image into a circular shape
                            } else {
                                Modifier
                                    .padding(
                                        start = 0.dp,
                                        top = 8.dp,
                                        end = 0.dp,
                                        bottom = 0.dp
                                    )
                                    .size(160.dp)
                                    .clip(CircleShape) // Clip the image into a circular shape
                            },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = if (!isVertical()) {
                                Modifier
                                    .padding(
                                        start = 40.dp,
                                        top = 40.dp,
                                        end = 75.dp,
                                        bottom = 55.dp
                                    )
                                    .size(80.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = Orange,
                                            radius = this.size.maxDimension
                                        )
                                    }
                            } else {
                                Modifier
                                    .padding(
                                        start = 40.dp,
                                        top = 48.dp,
                                        end = 40.dp,
                                        bottom = 40.dp
                                    )
                                    .size(80.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = Orange,
                                            radius = this.size.maxDimension
                                        )
                                    }
                            }
                        ) {
                            Image(
                            painter = painterResource(id = R.drawable.people),
                            contentDescription = "Image",
                            modifier = Modifier
                                //.padding(40.dp, 0.dp, 40.dp, 0.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Orange,
                                        radius = this.size.minDimension / 2f
                                    )
                                }
                                .scale(1.5f)
                        )
                        }
                    }
                    if (vm.editing) {
                        if(!vm.cameraPressed) {
                            Button(
                                modifier = Modifier
                                    .size(100.dp)
                                    .scale(0.5f)
                                    .align(Alignment.BottomEnd),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) // Imposta il colore di sfondo del bottone a rosso

                                ,
                                onClick = { vm.toggleCameraButtonPressed() }
                            ) {
                                // Mostra l'icona con l'immagine PNG
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    contentDescription = "Edit Profile",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Box() {
                                Column {
                                    Row {
                                        FloatingActionButton(

                                            modifier = Modifier
                                                .offset(x = 75.dp, y = 14.dp)
                                                .size(40.dp),
                                            onClick = { vm.setIsFrontCamera(true); vm.showCamera(true); vm.toggleCameraButtonPressed() },
                                            containerColor = MaterialTheme.colorScheme.tertiary
                                        ) {
                                            Icon(modifier = Modifier.scale(0.8f), painter = painterResource(id = R.drawable.camera), contentDescription = "take photo", tint = MaterialTheme.colorScheme.onSecondary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(3.dp))
                                    Row {
                                        FloatingActionButton(
                                            modifier = Modifier
                                                .offset(x = 75.dp, y = 14.dp)
                                                .size(40.dp),
                                            onClick = { vm.openGallery(true); vm.toggleCameraButtonPressed() },
                                            containerColor = MaterialTheme.colorScheme.tertiary
                                        ) {
                                            Icon(modifier = Modifier.scale(0.8f), painter = painterResource(id = R.drawable.gallery), contentDescription = "choose from gallery",tint = MaterialTheme.colorScheme.onSecondary)
                                        }
                                    }
                                    if (vm.teamProfileImage.value != Uri.EMPTY) {
                                        Spacer(modifier = Modifier.padding(3.dp))
                                        Row {
                                            FloatingActionButton(
                                                modifier = Modifier
                                                    .offset(x = 75.dp, y = 14.dp)
                                                    .size(40.dp),
                                                onClick = { vm.toggleDialog() },
                                                containerColor = MaterialTheme.colorScheme.tertiary
                                            ) {
                                                Icon(modifier = Modifier.scale(1.5f), imageVector = Icons.Default.Delete, contentDescription = "remove photo",tint = MaterialTheme.colorScheme.onSecondary)
                                            }
                                        }
                                    }
                                    if (vm.showConfirmationDialog) {
                                        AlertDialogExample(
                                            onDismissRequest = { vm.toggleDialog() },
                                            onConfirmation = { vm.toggleDialog(); vm.setUri(Uri.EMPTY); vm.toggleCameraButtonPressed() })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.people),
                contentDescription = "Image",
                modifier = Modifier
                    //.padding(40.dp, 0.dp, 40.dp, 0.dp)
                    .drawBehind {
                        drawCircle(
                            color = Orange,
                            radius = this.size.minDimension / 2f
                        )
                    }
                    .scale(0.7f)
            )
            if(!vm.cameraPressed) {
                Button(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(0.5f)
                        .align(Alignment.BottomEnd),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) // Imposta il colore di sfondo del bottone a rosso

                    ,
                    onClick = { vm.toggleCameraButtonPressed() }
                ) {
                    // Mostra l'icona con l'immagine PNG
                    Icon(
                        imageVector = Icons.Default.Edit,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box() {
                    Column {
                        Row {
                            FloatingActionButton(

                                modifier = Modifier
                                    .offset(x = 75.dp, y = 14.dp)
                                    .size(40.dp),
                                onClick = { vm.setIsFrontCamera(true); vm.showCamera(true); vm.toggleCameraButtonPressed() },
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ) {
                                Icon(modifier = Modifier.scale(0.8f), painter = painterResource(id = R.drawable.camera), contentDescription = "take photo", tint = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.padding(3.dp))
                        Row {
                            FloatingActionButton(
                                modifier = Modifier
                                    .offset(x = 75.dp, y = 14.dp)
                                    .size(40.dp),
                                onClick = { vm.openGallery(true); vm.toggleCameraButtonPressed() },
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ) {
                                Icon(modifier = Modifier.scale(0.8f), painter = painterResource(id = R.drawable.gallery), contentDescription = "choose from gallery",tint = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                        if (vm.teamProfileImage.value != Uri.EMPTY) {
                            Spacer(modifier = Modifier.padding(3.dp))
                            Row {
                                FloatingActionButton(
                                    modifier = Modifier
                                        .offset(x = 75.dp, y = 14.dp)
                                        .size(40.dp),
                                    onClick = { vm.toggleDialog() },
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ) {
                                    Icon(modifier = Modifier.scale(1.5f), imageVector = Icons.Default.Delete, contentDescription = "remove photo",tint = MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }
                        if (vm.showConfirmationDialog) {
                            AlertDialogExample(
                                onDismissRequest = { vm.toggleDialog() },
                                onConfirmation = { vm.toggleDialog(); vm.setUri(Uri.EMPTY); vm.toggleCameraButtonPressed() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteTeamDialog(vm: TeamDetailsViewModel) {
    val navController = NavControllerManager.getNavController()
    val files = AppStateManager.getFiles()
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = { vm.openDeleteTeamDialog = false },
        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Are you sure you want to delete the team: ${vm.teamName.value} ?") },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.isTeamDeleted = true
                    vm.openDeleteTeamDialog = false
                    vm.model.deleteTeam(vm.teamId,files,vm.loggedMember.id)
                    navController.navigate("Teams") { launchSingleTop = true }
                }
            ) {
                Text("Confirm", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { vm.openDeleteTeamDialog = false }
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
