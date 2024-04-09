package it.polito.uniteam.gui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.R


class UserProfileScreen :ViewModel(){
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

        if (nameError.isBlank() && emailError.isBlank() && nicknameError.isBlank() && locationError.isBlank() && descriptionError.isBlank() )
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
        if (emailValue.isBlank())
            emailError = "Email cannot be blank!"
        else if (!emailValue.contains('@'))
            emailError = "Invalid email address!"
        else if (!emailValue.contains('.'))
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

    var descriptionValue by mutableStateOf("Ciao Sono Gaetano")
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





}

@Preview
@Composable
fun EditProfile(vm: UserProfileScreen = viewModel()) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //
        DefaultImage(vm)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = vm.nameValue,
            onValueChange = vm::setName,
            label = { Text("Name") },
            isError = vm.nameError.isNotBlank()
        )
        //
        if (vm.nameError.isNotBlank())
            Text(vm.nameError, color = MaterialTheme.colorScheme.error)
        //
        Spacer(modifier = Modifier.height(16.dp))
        //
        TextField(
            value = vm.nicknameValue,
            onValueChange = vm::setNickname,
            label = { Text("Nickname") },
            isError = vm.nicknameError.isNotBlank()
        )
        //
        if (vm.nicknameError.isNotBlank())
            Text(vm.nicknameError, color = MaterialTheme.colorScheme.error)
        //
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = vm.emailValue,
            onValueChange = vm::setEmail,
            label = { Text("Email") },
            isError = vm.emailError.isNotBlank()
        )

        if (vm.emailError.isNotBlank())
            Text(vm.emailError, color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = vm.locationValue,
            onValueChange = vm::setLocation,
            label = { Text("Location") },
            isError = vm.locationError.isNotBlank()
        )

        if (vm.locationError.isNotBlank())
            Text(vm.locationError, color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = vm.descriptionValue,
            modifier = Modifier
                .height(100.dp)
                .padding(10.dp),
            onValueChange = vm::setDescription,
            label = { Text("Description") },
            isError = vm.descriptionError.isNotBlank()
        )

        if (vm.descriptionError.isNotBlank())
            Text(vm.descriptionError, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))

        Text(vm.KPIValue)


        //Temporary button to validate
        Button(onClick = {vm.validate()}) {
            Text("Validate")
        }
    }
}
@Preview
@Composable
fun DefaultImage(vm: UserProfileScreen = viewModel()) {
    val name = vm.nameValue

    if (name.isNotBlank()) {
        val initialsValue =
            name.split(' ')
                .mapNotNull { it.firstOrNull()?.toString() }
                .reduce { acc, s -> acc + s }
                .take(2)
        //Double Name
        Box() {
            // First element

            // Second element stacked on top of the first one
            Box(){
                Card(modifier = Modifier.background(Color.White)) {
                    Row(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            modifier = Modifier
                                .padding(16.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color.Blue,
                                        radius = this.size.maxDimension
                                    )
                                },
                            text = initialsValue,
                            style = TextStyle(color = Color.White, fontSize = 20.sp)
                        )

                    }
                }
            }
            Box() {
                Button(modifier = Modifier.size(10.dp), onClick = { vm.setEmail("httrfht") }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "camera")
                }
            }

        }
    } else {
        Image(painter = painterResource(id = vm.imageValue), contentDescription = "Image")
    }
}

@Preview
@Composable
fun PresentationPane(vm: UserProfileScreen = viewModel()) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultImage(vm)
        }
        //
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "name", modifier = Modifier.size(48.dp))
            Text(
                vm.nameValue,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp, 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        //
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Face, contentDescription = "nickname", modifier = Modifier.size(48.dp))
            Text(
                vm.nicknameValue,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp, 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        //
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Email, contentDescription = "email", modifier = Modifier.size(48.dp))
            Text(
                vm.emailValue,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp, 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        //
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "location", modifier = Modifier.size(48.dp))
            Text(
                vm.locationValue,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp, 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        //
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = "description", modifier = Modifier.size(48.dp))
            Text(
                vm.descriptionValue,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp, 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        //
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = "KPI", modifier = Modifier.size(48.dp))
            Text(
                vm.KPIValue,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp, 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
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
            title = { Text("Contact") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                if(vm.isEditing)
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