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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.R
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.CategoryRole
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.DummyDataProvider
import it.polito.uniteam.classes.History
import it.polito.uniteam.classes.HourMinutesPicker
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.MemberTeamInfo
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.TeamIcon
import it.polito.uniteam.classes.permissionRole
import it.polito.uniteam.gui.showtaskdetails.CommentsView
import it.polito.uniteam.gui.showtaskdetails.CustomDatePickerPreview
import it.polito.uniteam.gui.showtaskdetails.Demo_ExposedDropdownMenuBox
import it.polito.uniteam.gui.showtaskdetails.EditRowItem
import it.polito.uniteam.gui.showtaskdetails.FilesView
import it.polito.uniteam.gui.showtaskdetails.HistoryView
import it.polito.uniteam.gui.showtaskdetails.MembersDropdownMenuBox
import it.polito.uniteam.gui.showtaskdetails.RowItem
import it.polito.uniteam.gui.showtaskdetails.RowMemberItem
import it.polito.uniteam.gui.teamScreen.LeaveTeamDialog
import it.polito.uniteam.gui.teamScreen.TeamScreenViewModel
import it.polito.uniteam.gui.userprofile.AlertDialogExample
import it.polito.uniteam.gui.userprofile.DefaultImage
import it.polito.uniteam.gui.userprofile.getCameraProvider
import it.polito.uniteam.gui.userprofile.takePhoto
import it.polito.uniteam.isVertical
import it.polito.uniteam.ui.theme.Orange
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

class TeamDetailsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    // from model
    val member = model.loggedMember
    val teamId = checkNotNull(savedStateHandle["teamId"]).toString().toInt()
    val isAdmin = member.value.teamsInfo?.get(teamId)?.permissionrole == permissionRole.ADMIN
    var editing by mutableStateOf(false)
    var newTeam by mutableStateOf(false)
    var selectedTeam = mutableStateOf(
        if (teamId == 0){
            changeEditing()
            onNew()
        } else {
            model.selectTeam(teamId)
            model.getTeam(teamId)
        }
    )
    var beforeSelectedTeam = selectedTeam.value
    var history = if (teamId == 0) mutableListOf() else model.getTeam(teamId).teamHistory
    fun addTeamHistory(teamId: Int, history: History){
        model.addTeamHistory(teamId, history)
    }
    fun addTeam(team: Team) = model.addTeam(team)
    // internal
    var teamNameError by mutableStateOf("")
        private set
    fun changeTeamName(s: String) {
        selectedTeam.value = selectedTeam.value.copy(name = s)
    }

    private fun checkTeamName() {
        if (selectedTeam.value.name.isBlank())
            teamNameError = "Task name cannot be blank!"
        else
            teamNameError = ""
    }

    var descriptionError by mutableStateOf("")
        private set

    fun changeDescription(s: String) {
        selectedTeam.value = selectedTeam.value.copy(description = s)
    }
    fun setUri(uri: Uri) {
        selectedTeam.value.image = uri
    }

    private fun checkDescription() {
        if (selectedTeam.value.description.isBlank())
            descriptionError = "Task description cannot be blank!"
        else
            descriptionError = ""
    }

    fun validate() {
        checkTeamName()
        checkDescription()
        if (teamNameError.isEmpty() && descriptionError.isEmpty()) {
            model.changeSelectedTeamName(selectedTeam.value.name)
            model.changeSelectedTeamDescription(selectedTeam.value.description)
            val existingTeams = model.getAllTeams().map { it.id }
            // new team creation
            if(!existingTeams.contains(selectedTeam.value.id)){
                selectedTeam.value.members.addAll(DummyDataProvider.getMembers())
                selectedTeam.value.chat = Chat(
                    id = ++DummyDataProvider.chatId,
                    sender = member.value,
                    teamId = selectedTeam.value.id,
                    messages = mutableStateListOf()
                )
                model.addTeam(selectedTeam.value)
                model.addTeamInfo(selectedTeam.value.id, MemberTeamInfo().apply {
                    role = CategoryRole.PROGRAMMER
                    weeklyAvailabilityTimes = 5
                    weeklyAvailabilityHours = Pair(3, 0)
                    permissionrole = permissionRole.ADMIN
                })
            }

        }

    }
    fun changeEditing() {
        if(editing == true){
            selectedTeam.value = beforeSelectedTeam
            teamMembersBeforeEditing = selectedTeam.value.members.toList()
            teamImageBeforeEditing = selectedTeam.value.image
        }
        //selectedTeam.value = model.selectedTeam
        //Log.d("model", model.selectedTeam.toString())
        editing = !editing
    }

    fun teamCreation(flag: Boolean){
        newTeam = flag
    }

    fun onCancel(){
        Log.d("oncancel", teamMembersBeforeEditing.toString())

        selectedTeam.value.members = teamMembersBeforeEditing.toMutableList()
        selectedTeam.value.image = teamImageBeforeEditing
        /*model.changeSelectedTeamMembers(teamMembersBeforeEditing)
        model.changeSelectedTeamImage(teamImageBeforeEditing)*/
    }

    fun onNew(): Team {
        val x = model.newTeam()
        teamCreation(true)
        return x
    }

    var openAssignDialog = mutableStateOf(false)

    var possibleMembers = DummyDataProvider.getMembers()

    var teamMembersBeforeEditing = selectedTeam.value.members.toList()
    var teamImageBeforeEditing = selectedTeam.value.image



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
        Log.i("kilo", "Image captured: $uri")
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

@Composable
fun TeamViewScreen(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext)),
                   outputDirectory: File,
                   cameraExecutor: ExecutorService){
    if(vm.editing){
        TeamEditViewScreen(vm, outputDirectory, cameraExecutor)

    }else{
        if (isVertical() && vm.isAdmin) {
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.fillMaxSize()
            ) {
                Column {
                    FloatingActionButton(
                        onClick = { vm.changeEditing() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .padding(0.dp, 20.dp, 0.dp, 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {

                                DefaultImageForTeamScreen(vm)
                            }
                            //Spacer(modifier = Modifier.height(0.dp))

                            TeamDetailsView(customHeightForHistory = 0.41f)

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
                                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                        DefaultImageForTeamScreen(vm)
                                    }
                                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (vm.isAdmin) {
                                            FloatingActionButton(
                                                onClick = { vm.changeEditing() },
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
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
                            TeamDetailsView()

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
}

@Composable
fun TeamEditViewScreen(vm: TeamDetailsViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext)), outputDirectory: File,
                       cameraExecutor: ExecutorService
){
    val controller = NavControllerManager.getNavController()
    // Handle Back Button
    BackHandler(onBack = {
        vm.validate()
        if (vm.teamNameError == "" && vm.descriptionError == "") {
            // TODO finish save team logic with db
            if(vm.newTeam){
                controller.navigate("Teams")
                vm.addTeamHistory(vm.selectedTeam.value.id, History(comment = "Team created successfully", date = LocalDate.now().toString(), user = vm.member.value))
                //vm.teamCreation(false)
            }else{
                vm.addTeamHistory(vm.teamId, History(comment = "Team details updated", date = LocalDate.now().toString(), user = vm.member.value))
                vm.changeEditing()
            }
            /*vm.changeEditing()
            vm.teamMembersBeforeEditing = selectedTeam.members
            vm.teamImageBeforeEditing = selectedTeam.image
            navController.navigate("Tasks"){
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }*/
        }
    })
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
                vm.setUri(uri)
            }
        }
        // Optionally, you can still call vm.openGallery() here if needed
        vm.openGallery(false)
    }

    val context = LocalContext.current
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
                    Image(
                        painter = rememberAsyncImagePainter(vm.temporaryUri),
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
                        Image(
                            painter = rememberAsyncImagePainter(vm.temporaryUri),
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
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            //
            BoxWithConstraints {
                if(this.maxHeight > this.maxWidth) {
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
                            DefaultImageForEditingTeam(vm)
                        }

                        TeamDetailsEdit(vm)


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
                            DefaultImageForEditingTeam(vm)
                        }
                        TeamDetailsEdit(vm)



                    }
                }
            }


        }
    }

}


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

        RowItem(title = "Name:", value = vm.selectedTeam.value.name)
        RowItem(title = "Description:", value = vm.selectedTeam.value.description.toString())
        RowMemberItem( title = "Members:", value = vm.selectedTeam.value.members)
        RowItem(title = "Creation Date:", value = vm.selectedTeam.value.creationDate.toString())
        val icon = Icons.Filled.History
        val title = " Team History"
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
    val selectedTeam = vm.selectedTeam.value
    Row(){
        Column(modifier = Modifier.fillMaxSize(),  verticalArrangement = Arrangement.Bottom) {
            Row(modifier = Modifier.fillMaxHeight(0.8f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp, 0.dp)
                ) {
                    Box{
                        Image(
                            painter = rememberAsyncImagePainter(vm.selectedTeam.value.image),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f)
                                .align(Alignment.TopCenter),
                        )
                    }

                    Spacer(modifier = Modifier.padding(10.dp))
                    EditRowItem(
                        label = "Name:",
                        value = selectedTeam.name,
                        errorText = vm.teamNameError,
                        onChange = vm::changeTeamName
                    )
                    EditRowItem(
                        label = "Description:",
                        value = selectedTeam.description,
                        errorText = vm.descriptionError,
                        onChange = vm::changeDescription
                    )
                    if (!vm.newTeam) {
                        TeamMembersDropdownMenuBox(
                            vm,
                            "Manage Members",
                            selectedTeam.members
                        )
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

                                    if(vm.newTeam){
                                        controller.navigate("Teams")
                                    } else {
                                        vm.onCancel()
                                        vm.teamCreation(false)
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
                                        if(vm.newTeam){
                                            controller.navigate("Teams")
                                            vm.addTeamHistory(selectedTeam.id, History(comment = "Team created successfully", date = LocalDate.now().toString(), user = vm.member.value))
                                            //vm.teamCreation(false)
                                        }else{
                                            vm.addTeamHistory(vm.teamId, History(comment = "Team details updated", date = LocalDate.now().toString(), user = vm.member.value))
                                            vm.changeEditing()
                                        }
                                        /*vm.changeEditing()
                                        vm.teamMembersBeforeEditing = selectedTeam.members
                                        vm.teamImageBeforeEditing = selectedTeam.image
                                        navController.navigate("Tasks"){
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }*/
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
                            if(vm.newTeam){
                                controller.navigate("Teams")
                            } else {
                                vm.onCancel()
                                vm.teamCreation(false)
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
                            if (vm.teamNameError == "" && vm.descriptionError == "" ) {
                                // TODO finish save team logic with db
                                if(vm.newTeam){
                                    controller.navigate("Teams")
                                    vm.addTeamHistory(selectedTeam.id, History(comment = "Team created successfully", date = LocalDate.now().toString(), user = vm.member.value))
                                    //vm.teamCreation(false)
                                }else{
                                    vm.addTeamHistory(vm.teamId, History(comment = "Team details updated", date = LocalDate.now().toString(), user = vm.member.value))
                                    vm.changeEditing()
                                }
                                /*vm.changeEditing()
                                vm.teamMembersBeforeEditing = selectedTeam.members
                                vm.teamImageBeforeEditing = selectedTeam.image
                                navController.navigate("Tasks"){
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }*/

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
    currentMembers: List<Member>
) {

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
                    //IconButton(onClick = { vm.openAssignDialog.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add ")
                    //}
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
                            MemberIcon(member = member, modifierScale = Modifier.scale(0.65f), modifierPadding = Modifier.padding(start = if (index == 0) 12.dp else 0.dp), enableNavigation = false)
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
    val selectedTeam = vm.selectedTeam.value

    val selectedMembers = remember { mutableStateMapOf<Member, Boolean>() }
    vm.possibleMembers.forEach { member ->
        selectedMembers[member] = vm.selectedTeam.value.members.toMutableList().contains(member)
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
                            text = selectedTeam.name,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    else
                        Text(
                            text = selectedTeam.name,
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
                        vm.possibleMembers.forEach { member ->
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
                        vm.selectedTeam.value.members.clear()
                        vm.selectedTeam.value.members.addAll(selectedMembers.filterValues { it }.keys.toMutableStateList())
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
    val name = vm.selectedTeam.value.name
    println(name)
    if (name.isNotBlank() || vm.selectedTeam.value.image != Uri.EMPTY) {

        Card(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    if (vm.selectedTeam.value.image != Uri.EMPTY) {
                        Image(
                            painter = rememberAsyncImagePainter(vm.selectedTeam.value.image),
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
    val name = vm.selectedTeam.value.name
    if (name.isNotBlank() || vm.selectedTeam.value.image != Uri.EMPTY) {

        Card(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    if (vm.selectedTeam.value.image != Uri.EMPTY) {
                        Image(
                            painter = rememberAsyncImagePainter(vm.selectedTeam.value.image),
                            contentDescription = null,
                            modifier = if (!isVertical()) {
                                Modifier
                                    .padding(
                                        start = 0.dp,
                                        top = 0.dp,
                                        end = 35.dp,
                                        bottom = 15.dp)
                                    .size(160.dp)
                                    .clip(CircleShape) // Clip the image into a circular shape
                            } else {
                                Modifier
                                    .padding(
                                        start = 0.dp,
                                        top = 8.dp,
                                        end = 0.dp,
                                        bottom = 0.dp)
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
                                        radius = this.size.minDimension/2f
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
                                    if (vm.selectedTeam.value.image != Uri.EMPTY) {
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
                            radius = this.size.minDimension/2f
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
                        if (vm.selectedTeam.value.image != Uri.EMPTY) {
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

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = { vm.openDeleteTeamDialog = false },
        icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Are you sure you want to delete the team: ${vm.selectedTeam.value.name} ?") },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.openDeleteTeamDialog = false
                    //vm.deleteTeam
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
