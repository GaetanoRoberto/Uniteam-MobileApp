package it.polito.uniteam.gui

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import it.polito.uniteam.gui.CameraView
import it.polito.uniteam.R
import it.polito.uniteam.ui.theme.Orange
import java.io.File
import java.util.concurrent.ExecutorService


class UserProfileScreen : ViewModel() {
    var isEditing by mutableStateOf(false)
        private set  //By adding "private set" only this class can change 'isEditing'

    fun edit() {
        isEditing = true
    }

    fun validate() {
        checkName()
        checkEmail()
        checkNickname()
        checkLocation()
        checkDescription()

        if (nameError.isBlank() && emailError.isBlank() && nicknameError.isBlank() && locationError.isBlank() && descriptionError.isBlank())
            isEditing = false
    }

    var nameValue by mutableStateOf("Gaetano Roberto")
        private set
    var nameError by mutableStateOf("")
        private set

    fun setName(n: String) {
        nameValue = n
    }

    private fun checkName() {
        if (nameValue.isBlank())
            nameError = "Name cannot be blank!"
        else
            nameError = ""
    }

    var nicknameValue by mutableStateOf("Tanucc")
        private set
    var nicknameError by mutableStateOf("")
        private set

    fun setNickname(n: String) {
        nicknameValue = n
    }

    private fun checkNickname() {
        if (nicknameValue.isBlank())
            nicknameError = "Nickname cannot be blank!"
        else
            nicknameError = ""
    }

    var emailValue by mutableStateOf("franco@gmail.com")
        private set
    var emailError by mutableStateOf("")
        private set

    fun setEmail(e: String) {
        emailValue = e
    }

    private fun checkEmail() {
        val emailRegex = Regex("^\\w+@[a-zA-Z_]+?\\.[a-zA-Z]{2,3}\$")
        if (emailValue.isBlank())
            emailError = "Email cannot be blank!"
        else if (!emailRegex.matches(emailValue))
            emailError = "Invalid email address!"
        else
            emailError = ""
    }

    var locationValue by mutableStateOf("Corso Duca Degli Abruzzi")
        private set
    var locationError by mutableStateOf("")
        private set

    fun setLocation(t: String) {
        locationValue = t
    }

    private fun checkLocation() {
        if (locationValue.isBlank())
            locationError = "Location cannot be blank!"
        else
            locationError = ""
    }

    var descriptionValue by mutableStateOf("Ciao Sono Gaetano vvmkogrtnijtrnjtrnjhnjrnhkjtrnhktrnhjkrhnrjknhktrjnhkrnthkjtnjhkrjnhrknhtkhnrkt")
        private set
    var descriptionError by mutableStateOf("")
        private set

    fun setDescription(t: String) {
        descriptionValue = t
    }

    private fun checkDescription() {
        if (descriptionValue.isBlank())
            descriptionError = "Description cannot be blank!"
        else
            descriptionError = ""
    }

    var KPIValue by mutableStateOf("100% on all Tasks")
        private set

    fun setKPI(t: String) {
        KPIValue = t
    }

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
    var photoUri = Uri.EMPTY
        private set

    fun setPhotoUri(uri: Uri) {
        photoUri = uri
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRowItem(value: String, keyboardType: KeyboardType = KeyboardType.Text ,onChange: (String) -> Unit, label: String, errorText: String) {
    OutlinedTextField(
        value = value,
        modifier = Modifier.fillMaxWidth(0.8f),
        onValueChange = onChange,
        label = {
            Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary)//testo dentro
        ) },
        isError = errorText.isNotBlank(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        /*colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.onTertiary, // Cambia il colore del bordo quando il campo è in focus
            unfocusedBorderColor = MaterialTheme.colorScheme.onTertiary ,// Cambia il colore del bordo quando il campo non è in focus
            cursorColor = MaterialTheme.colorScheme.secondary, // Cambia il colore del cursore

        )*/
    )
    if (errorText.isNotBlank())
        Text(errorText, color = MaterialTheme.colorScheme.error)
}

@Preview
@Composable
fun EditProfile(vm: UserProfileScreen = viewModel()) {
    // Handle Back Button
    BackHandler(onBack = {
        vm.validate()
    })
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.nameValue, onChange = vm::setName, label = "Full Name", errorText = vm.nameError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.nicknameValue, onChange = vm::setNickname, label = "Nickname", errorText = vm.nicknameError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.emailValue, keyboardType = KeyboardType.Email ,onChange = vm::setEmail, label = "Email", errorText = vm.emailError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.locationValue, onChange = vm::setLocation, label = "Location", errorText = vm.locationError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.descriptionValue, onChange = vm::setDescription, label = "Description", errorText = vm.descriptionError)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EditRowItem(value = vm.nameValue, onChange = vm::setName, label = "Full Name", errorText = vm.nameError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.nicknameValue, onChange = vm::setNickname, label = "Nickname", errorText = vm.nicknameError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.emailValue, onChange = vm::setEmail, label = "Email", errorText = vm.emailError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.locationValue, onChange = vm::setLocation, label = "Location", errorText = vm.locationError)
                Spacer(modifier = Modifier.height(16.dp))
                EditRowItem(value = vm.descriptionValue, onChange = vm::setDescription, label = "Description", errorText = vm.descriptionError)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview
@Composable
fun DefaultImage(vm: UserProfileScreen = viewModel()) {
    val name = vm.nameValue

    if (name.isNotBlank()) {
        val initials = name.trim().split(' ');
        var initialsValue = initials
            .mapNotNull { it.firstOrNull()?.toString() }
            .first();

        if (initials.size >=2) {
            initialsValue += initials
                .mapNotNull { it.firstOrNull()?.toString() }
                .last()
        }

        Card(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    if (vm.photoUri != Uri.EMPTY) {
                        Image(
                            painter = rememberAsyncImagePainter(vm.photoUri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape), // Clip the image into a circular shape
                            contentScale = ContentScale.Crop
                        )
                    } else {
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
                    if (vm.isEditing) {
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
                                        ) {
                                            Icon(modifier = Modifier.scale(0.9f), painter = painterResource(id = R.drawable.camera), contentDescription = "take photo")
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(3.dp))
                                    Row {
                                        FloatingActionButton(
                                            modifier = Modifier
                                                .offset(x = 75.dp, y = 14.dp)
                                                .size(40.dp),
                                            onClick = { vm.openGallery(true); vm.toggleCameraButtonPressed() },
                                        ) {
                                            Icon(modifier = Modifier.scale(0.9f), painter = painterResource(id = R.drawable.gallery), contentDescription = "choose from gallery")
                                        }
                                    }
                                    if (vm.photoUri != Uri.EMPTY) {
                                        Spacer(modifier = Modifier.padding(3.dp))
                                        Row {
                                            FloatingActionButton(
                                                modifier = Modifier
                                                    .offset(x = 75.dp, y = 14.dp)
                                                    .size(40.dp),
                                                onClick = { vm.toggleDialog() }
                                            ) {
                                                Icon(modifier = Modifier.scale(1.5f), imageVector = Icons.Default.Delete, contentDescription = "remove photo")
                                            }
                                        }
                                    }
                                    if (vm.showConfirmationDialog) {
                                        AlertDialogExample(
                                            onDismissRequest = { vm.toggleDialog() },
                                            onConfirmation = { vm.toggleDialog(); vm.setPhotoUri(Uri.EMPTY); vm.toggleCameraButtonPressed() })
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
                painter = painterResource(id = R.drawable.user_icon),
                contentDescription = "Image",
                modifier = Modifier
                    //.padding(40.dp, 0.dp, 40.dp, 0.dp)
                    .size(160.dp)
            )
            Button(
                modifier = Modifier
                    .size(100.dp)
                    .scale(0.5f)
                    .align(Alignment.BottomEnd),
                onClick = { vm.toggleCameraButtonPressed() }
            ) {
                // Mostra l'icona con l'immagine PNG
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Info, contentDescription = "Example Icon")
        },
        text = {
            Text(text = "Are you Sure to Remove the Profile Image ?")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Undo")
            }
        }
    )
}

@Composable
fun RowItem(modifier: Modifier = Modifier, icon: ImageVector, description: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(48.dp)
        )
        Text(
            value,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp, 0.dp),
            style = MaterialTheme.typography.headlineSmall
        )
    }
    Row(
        modifier = modifier
    ) {
    }
    Spacer(modifier = Modifier.padding(5.dp))
}

@Preview
@Composable
fun PresentationPane(vm: UserProfileScreen = viewModel()) {
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                val rowItems = listOf(
                    Triple(Icons.Default.Person, "name", vm.nameValue),
                    Triple(Icons.Default.Face, "nickname", vm.nicknameValue),
                    Triple(Icons.Default.Email, "email", vm.emailValue),
                    Triple(Icons.Default.LocationOn, "location", vm.locationValue),
                    Triple(Icons.Default.Info, "description", vm.descriptionValue),
                    Triple(Icons.Default.Star, "KPI", vm.KPIValue)
                )
                val line_modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(1.dp)
                    .background(color = Color.Black)
                rowItems.forEachIndexed { index, (icon, description, value) ->
                    if (index == rowItems.size-1) {
                        RowItem(icon = icon, description = description, value = value)
                    } else {
                        RowItem(modifier = line_modifier, icon = icon, description = description, value = value)
                    }
                }
                /*RowItem(icon = Icons.Default.Person, description = "name", value = vm.nameValue)
                RowItem(icon = Icons.Default.Face, description = "nickname", value = vm.nicknameValue)
                RowItem(icon = Icons.Default.Email, description = "email", value = vm.emailValue)
                RowItem(icon = Icons.Default.LocationOn, description = "location", value = vm.locationValue)
                RowItem(icon = Icons.Default.Menu, description = "description", value = vm.descriptionValue)
                RowItem(icon = Icons.Default.Star, description = "KPI", value = vm.KPIValue)*/
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RowItem(icon = Icons.Default.Person, description = "name", value = vm.nameValue)
                RowItem(icon = Icons.Default.Face, description = "nickname", value = vm.nicknameValue)
                RowItem(icon = Icons.Default.Email, description = "email", value = vm.emailValue)
                RowItem(icon = Icons.Default.LocationOn, description = "location", value = vm.locationValue)
                RowItem(icon = Icons.Default.Menu, description = "description", value = vm.descriptionValue)
                RowItem(icon = Icons.Default.Star, description = "KPI", value = vm.KPIValue)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    vm: UserProfileScreen = viewModel(),
    outputDirectory: File,
    cameraExecutor: ExecutorService,
    pickImageLauncher: ActivityResultLauncher<Intent>
) {
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
        CameraView(
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
                    Text(modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp), text = "Photo Preview", style = TextStyle(fontSize = 30.sp, textAlign = TextAlign.Center))
                    // Image at the top
                    Image(
                        painter = rememberAsyncImagePainter(vm.temporaryUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopCenter),
                    )

                    // Buttons at the bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .fillMaxHeight(0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    vm.showPhoto(false)
                                    vm.setTemporaryUri(Uri.EMPTY)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Undo")
                            }

                            Spacer(modifier = Modifier.padding(16.dp))

                            Button(
                                onClick = {
                                    vm.showPhoto(false)
                                    vm.setPhotoUri(vm.temporaryUri)
                                    vm.setTemporaryUri(Uri.EMPTY)
                                    Toast.makeText(context, "Profile Image Updated", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Confirm")
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Image at the top
                    Image(
                        painter = rememberAsyncImagePainter(vm.temporaryUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .align(Alignment.Center)
                    )

                    // Buttons at the bottom
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxHeight(0.1f)
                    ) {
                        Button(
                            onClick = {
                                vm.showPhoto(false)
                                vm.setTemporaryUri(Uri.EMPTY)
                            },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text(text = "Undo")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                vm.showPhoto(false)
                                vm.setPhotoUri(vm.temporaryUri)
                                vm.setTemporaryUri(Uri.EMPTY)
                                Toast.makeText(context, "Profile Image Updated", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text(text = "Confirm")
                        }
                    }

                    // Text at the top
                    Text(
                        modifier = Modifier
                            .fillMaxHeight(0.1f)
                            .fillMaxWidth(),
                        text = "Photo Preview",
                        style = TextStyle(fontSize = 30.sp, textAlign = TextAlign.Center)
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor =  MaterialTheme.colorScheme.primary),
                actions = {
                    if (vm.isEditing)
                        Button(onClick = { vm.validate() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) // Imposta il colore di sfondo del bottone a rosso
                        ) {
                            Text("Done",color = MaterialTheme.colorScheme.onSecondary)

                        }
                    else
                        IconButton(onClick = { vm.edit() }, colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.secondary),
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSecondary)
                        }
                }
            )
            //
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
                            DefaultImage(vm)
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        if (vm.isEditing)
                            EditProfile(vm)
                        else
                            PresentationPane(vm)
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
                            DefaultImage(vm)
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        if (vm.isEditing)
                            EditProfile(vm)
                        else
                            PresentationPane(vm)
                    }
                }
            }
            /*Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    DefaultImage(vm)
                }
                Spacer(modifier = Modifier.height(30.dp))
                if (vm.isEditing)
                    EditProfile(vm)
                else
                    PresentationPane(vm)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EditRowItem(value = vm.nameValue, onChange = vm::setName, label = "Full Name", errorText = vm.nameError)
                    Spacer(modifier = Modifier.height(16.dp))
                    EditRowItem(value = vm.nicknameValue, onChange = vm::setNickname, label = "Nickname", errorText = vm.nicknameError)
                    Spacer(modifier = Modifier.height(16.dp))
                    EditRowItem(value = vm.emailValue, keyboardType = KeyboardType.Email ,onChange = vm::setEmail, label = "Email", errorText = vm.emailError)
                    Spacer(modifier = Modifier.height(16.dp))
                    EditRowItem(value = vm.locationValue, onChange = vm::setLocation, label = "Location", errorText = vm.locationError)
                    Spacer(modifier = Modifier.height(16.dp))
                    EditRowItem(value = vm.descriptionValue, onChange = vm::setDescription, label = "Description", errorText = vm.descriptionError)
                }
            }
            if (vm.isEditing)
                EditProfile(vm)
            else
                PresentationPane(vm)*/

        }
    }
}