package it.polito.uniteam.gui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
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
import it.polito.uniteam.R


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

    var imageValue by mutableStateOf(R.drawable.user_icon)
        private set
    var imageError by mutableStateOf("")
        private set

    fun setImage(n: Int) {
        imageValue = n
    }

    //TO DO
    private fun checkImage() {
        //check of integers
    }

    var nameValue by mutableStateOf("Gaetano  Roberto")
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
            descriptionError = "Location cannot be blank!"
        else
            descriptionError = ""
    }

    var KPIValue by mutableStateOf("100%")
        private set

    fun setKPI(t: String) {
        KPIValue = t
    }

    var cameraPressed by mutableStateOf(false)
        private set

    fun toggleCameraButtonPressed() {
        cameraPressed = !cameraPressed
    }


}

@Composable
fun EditRowItem(value: String, keyboardType: KeyboardType = KeyboardType.Text ,onChange: (String) -> Unit, label: String, errorText: String) {
    TextField(
        value = value,
        modifier = Modifier.fillMaxWidth(0.8f),
        onValueChange = onChange,
        label = { Text(label) },
        isError = errorText.isNotBlank(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        )
    )
    if (errorText.isNotBlank())
        Text(errorText, color = MaterialTheme.colorScheme.error)
}

@Preview
@Composable
fun EditProfile(vm: UserProfileScreen = viewModel()) {
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box() {
                    DefaultImage(vm)
                    if (!vm.cameraPressed) {
                        Box() {
                            Button(modifier = Modifier
                                .size(16.dp)
                                .offset(x = 55.dp, y = 55.dp),
                                onClick = { vm.toggleCameraButtonPressed() }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "camera")
                            }
                        }
                    } else {
                        Box() {
                            Column {
                                Row {
                                    FloatingActionButton(
                                        modifier = Modifier
                                            .offset(x = 55.dp, y = 55.dp)
                                            .width(100.dp),
                                        onClick = { vm.toggleCameraButtonPressed() },
                                    ) {

                                        Text(text = "take a photo")
                                    }
                                }
                                Row {
                                    FloatingActionButton(
                                        modifier = Modifier
                                            .offset(x = 55.dp, y = 55.dp)
                                            .width(100.dp),
                                        onClick = { vm.toggleCameraButtonPressed() },
                                    ) {
                                        Text(text = "choose from gallery")
                                    }
                                }
                            }
                        }
                    }
                }
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(vm.KPIValue)
                //Temporary button to validate
                Button(onClick = { vm.validate() }) {
                    Text("Validate")
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
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
}

@Preview
@Composable
fun DefaultImage(vm: UserProfileScreen = viewModel()) {
    val name = vm.nameValue
    val iconPainter: Painter = painterResource(id = R.drawable.camera)

    if (name.isNotBlank()) {
        val initialsValue =
            name.split(' ')
                .mapNotNull { it.firstOrNull()?.toString() }
                .first() +
                    name.split(' ')
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .last()

        Card(modifier = Modifier.background(Color.White)) {
            Row(
                modifier = Modifier
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                // Box per contenere l'icona della fotocamera
                Box(modifier = Modifier.size(200.dp)) {
                    Text(
                        modifier = Modifier
                            .padding(40.dp)
                            .size(80.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Color.Blue,
                                    radius = this.size.maxDimension
                                )
                            },
                        text = initialsValue,
                        style = TextStyle(color = Color.White, fontSize = 60.sp, textAlign = TextAlign.Center)
                    )
                    Button(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(0.5f)
                            .align(Alignment.Center),
                        onClick = { /* Azione per aprire la galleria */ }
                    ) {
                        // Mostra l'icona con l'immagine PNG
                        Icon(
                            painter = iconPainter,
                            contentDescription = "camera",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    } else {
        Image(
            painter = painterResource(id = vm.imageValue),
            contentDescription = "Image",
            modifier = Modifier
                .padding(40.dp, 0.dp, 40.dp, 0.dp)
                .size(160.dp)
        )
    }
}

@Composable
fun RowItem(icon: ImageVector, description: String, value: String) {
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
}

@Preview
@Composable
fun PresentationPane(vm: UserProfileScreen = viewModel()) {
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
                    DefaultImage(vm)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    RowItem(icon = Icons.Default.Person, description = "name", value = vm.nameValue)
                    RowItem(icon = Icons.Default.Face, description = "nickname", value = vm.nicknameValue)
                    RowItem(icon = Icons.Default.Email, description = "email", value = vm.emailValue)
                    RowItem(icon = Icons.Default.LocationOn, description = "location", value = vm.locationValue)
                    RowItem(icon = Icons.Default.Menu, description = "description", value = vm.descriptionValue)
                    RowItem(icon = Icons.Default.Star, description = "KPI", value = vm.KPIValue)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
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

}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(vm: UserProfileScreen = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                if (vm.isEditing)
                    Button(onClick = { vm.validate() }) {
                        Text("Done")
                    }
                else
                    IconButton(onClick = { vm.edit() }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
            }
        )
        //
        Spacer(modifier = Modifier.height(16.dp))
        //
        if (vm.isEditing)
            EditProfile(vm)
        else
            PresentationPane(vm)

    }
}