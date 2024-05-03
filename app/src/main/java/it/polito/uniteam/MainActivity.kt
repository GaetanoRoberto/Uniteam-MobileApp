package it.polito.uniteam

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.gui.userprofile.UserProfileScreen
import it.polito.uniteam.ui.theme.UniTeamTheme
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import it.polito.uniteam.gui.calendar.Calendar
import it.polito.uniteam.gui.showtaskdetails.TaskScreen

class MainActivity : ComponentActivity() {

    private lateinit var outputDirectory: File
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor();
    private val vm: UserProfileScreen by viewModels()
    private val vm2: Calendar by viewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activity: ActivityResult? ->
        // Handle the selected image URI here
        val uri = activity?.data?.data
        if (uri != null) {
            // Image picked successfully, do something with the URI
            vm.setUri(uri)
        }
        vm.openGallery(false)
    }

    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        var isGranted = true

        for(permission in permissions) {
            if(!permission.value) {
                isGranted = false
            }
        }

        if(isGranted) {
            Log.i("UniTeam","all Permissions granted.")
        } else {
            Log.i("UniTeam","permissions denied.")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val interactionSource = remember { MutableInteractionSource() }
            val focusManager = LocalFocusManager.current
            val theme = isSystemInDarkTheme()
            UniTeamTheme(darkTheme = theme){
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    // remove the focus and the opened photo/gallery menu
                    .pointerInput(Unit, interactionSource) {
                        detectTapGestures(
                            onPress = {
                                if (vm.cameraPressed) {
                                    vm.toggleCameraButtonPressed()
                                }
                                focusManager.clearFocus()
                            }
                        )
                    },
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        MyTopAppBar()
                        /*ProfileSettings(
                            vm = viewModel(),
                            outputDirectory = getOutputDirectory(),
                            cameraExecutor = cameraExecutor,
                            pickImageLauncher = pickImageLauncher
                        )*/
                        //CalendarAppContainer(vm = viewModel())
                        TaskScreen(vm = viewModel())
                        //TaskListView(vm = viewModel())
                    }
                    BottomBar()
                }}
            }
        }

        requestPermissions()
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        /*
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
        <uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
        <uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
        val permissions = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }*/
        val permissions = arrayOf(Manifest.permission.CAMERA)

        var launch = false
        for (permission in permissions) {
            if(!hasPermission(permission)) {
                launch = true
                break
            }
        }
        if(launch) {
            permissionLauncher.launch(permissions)
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(vm: UserProfileScreen = viewModel()) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Allinea orizzontalmente il contenuto al centro
                verticalAlignment = Alignment.CenterVertically // Allinea verticalmente il contenuto al centro
            ) {
                Text("UNITEAM")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        navigationIcon = {
            IconButton(onClick = { /* Azione per tornare indietro */ }, colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.secondary)) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSecondary)
            }
        },
        actions = {
            if (vm.isEditing) {
                Button(onClick = { vm.validate() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Done", color = MaterialTheme.colorScheme.onSecondary)
                }
            } else {
                IconButton(onClick = { vm.edit() }, colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.secondary)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }
    )
}
@Composable
fun BottomBar(vm: UserProfileScreen = viewModel()){
    BottomAppBar(
        modifier = Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text(text = "TEAMS")
                }
                Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text(text = "TASKS")
                }
                IconButton(onClick = { /*TODO*/ }, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }


            }
        }
    )
}

@Composable
fun isVertical(): Boolean {
    val context = LocalContext.current
    return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}
