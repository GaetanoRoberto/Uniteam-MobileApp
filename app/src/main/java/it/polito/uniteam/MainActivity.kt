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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ChecklistRtl
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.polito.uniteam.gui.TeamDetails.TeamViewScreen
import it.polito.uniteam.gui.calendar.Calendar
import it.polito.uniteam.gui.calendar.CalendarAppContainer
import it.polito.uniteam.gui.chat.ChatScreen
//import it.polito.uniteam.gui.chat.ChatScreen
import it.polito.uniteam.gui.notifications.Notifications
import it.polito.uniteam.gui.showtaskdetails.EditTaskView
import it.polito.uniteam.gui.showtaskdetails.TaskDetailsView
import it.polito.uniteam.gui.showtaskdetails.TaskScreen
import it.polito.uniteam.gui.statistics.Statistics
import it.polito.uniteam.gui.tasklist.TaskListView
import it.polito.uniteam.gui.userprofile.OtherProfileSettings
import it.polito.uniteam.gui.userprofile.ProfileSettings
import it.polito.uniteam.gui.yourTasksCalendar.YourTasksCalendarViewScreen

class MainActivity : ComponentActivity() {

    private lateinit var outputDirectory: File
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor();

    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        var isGranted = true

        for (permission in permissions) {
            if (!permission.value) {
                isGranted = false
            }
        }

        if (isGranted) {
            Log.i("UniTeam", "all Permissions granted.")
        } else {
            Log.i("UniTeam", "permissions denied.")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val interactionSource = remember { MutableInteractionSource() }
            val focusManager = LocalFocusManager.current
            val theme = isSystemInDarkTheme()


            UniTeamTheme(darkTheme = theme) {
                val items = listOf(
                    BottomNavigationItem(
                        title = "Teams",
                        selectedIcon = Icons.Filled.Diversity3,
                        unselectedIcon = Icons.Outlined.Diversity3,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        title = "Tasks",
                        selectedIcon = Icons.Filled.ChecklistRtl,
                        unselectedIcon = Icons.Outlined.ChecklistRtl,
                        hasNews = false,//mette un pallino nuovo
                    ),
                    BottomNavigationItem(
                        title = "Notifications",
                        selectedIcon = Icons.Filled.Notifications,
                        unselectedIcon = Icons.Outlined.Notifications,
                        hasNews = false,
                        badgeCount = 5
                    ),

                    )
                var selectedItemIndex by rememberSaveable {
                    mutableIntStateOf(0)
                }
                NavControllerManager.ProvideNavController {
                    val navController = NavControllerManager.getNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination?.route
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            // remove the focus and the opened photo/gallery menu
                            .pointerInput(Unit, interactionSource) {
                                detectTapGestures(
                                    onPress = {
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
                                Scaffold(
                                    topBar = {
                                        MyTopAppBar(
                                            vm = viewModel(factory = Factory(LocalContext.current)),
                                            navController
                                        )
                                    },
                                    /*floatingActionButton = {
                                        //if (vm.isEditing) {
                                            if (currentDestination == "Profile") {
                                                FloatingActionButton(onClick ={
                                                    navController.navigate("EditProfile"){
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }},
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ){
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = MaterialTheme.colorScheme.onSecondary
                                                    )
                                                }
                                        }else if(currentDestination == "Tasks"){
                                            FloatingActionButton(onClick ={
                                                navController.navigate("EditTask"){
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }},
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ){
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.onSecondary
                                                )
                                            }
                                        }
                                        },*/
                                    content = { paddingValue ->
                                        Column(Modifier.padding(paddingValue)) {
                                            // In your main activity or main screen composable
                                            NavHost(
                                                navController = navController,
                                                startDestination = "Teams"
                                            ) {
                                                // Define destinations for your screens
                                                composable("Teams") {
                                                    TaskListView(
                                                        vm = viewModel(
                                                            factory = Factory(LocalContext.current)
                                                        )
                                                    )
                                                }
                                                composable("Tasks") {
                                                    TaskScreen(
                                                        vm = viewModel(
                                                            factory = Factory(LocalContext.current)
                                                        )
                                                    )
                                                }
                                                composable("Calendar") {
                                                    CalendarAppContainer(
                                                        vm = viewModel(
                                                            factory = Factory(LocalContext.current)
                                                        )
                                                    )
                                                }
                                                composable("Notifications") {
                                                    Notifications(
                                                        vm = viewModel(
                                                            factory = Factory(LocalContext.current)
                                                        )
                                                    )
                                                }
                                                composable("Chat") {
                                                    ChatScreen(
                                                        vm = viewModel(
                                                            factory = Factory(LocalContext.current)
                                                        )
                                                    )
                                                }
                                                composable("Profile") {
                                                    ProfileSettings(
                                                        vm = viewModel(
                                                            factory = Factory(LocalContext.current)
                                                        ),
                                                        outputDirectory = getOutputDirectory(),
                                                        cameraExecutor = cameraExecutor
                                                    )
                                                }

                                                composable("Statistics") {
                                                    Statistics(
                                                        vm = viewModel(
                                                            factory = Factory(LocalContext.current)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    bottomBar = {
                                        /*if (!isVertical()) {
                                            Row {}
                                        } else {*/
                                        NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                                            items.forEachIndexed { index, item ->
                                                NavigationBarItem(
                                                    colors = NavigationBarItemColors(
                                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                        disabledIconColor = MaterialTheme.colorScheme.onPrimary,
                                                        disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                                                        selectedIndicatorColor = MaterialTheme.colorScheme.secondary,
                                                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    selected = item.title == navBackStackEntry?.destination?.route,//selectedItemIndex == index , PRIMA DELLA NAVIGATION
                                                    onClick = {
                                                        selectedItemIndex = index
                                                        navController.navigate(item.title) {
                                                            //println("Destination: ${navController.previousBackStackEntry?.destination?.route}")
                                                            /* popUpTo(navController.graph.findStartDestination().id){
                                                                 //saveState = true
                                                             }*/
                                                            launchSingleTop = true
                                                            //restoreState = true
                                                        }
                                                    },
                                                    label = {
                                                        Text(text = item.title)
                                                    },
                                                    alwaysShowLabel = true,
                                                    icon = {
                                                        BadgedBox(
                                                            badge = {
                                                                if (item.badgeCount != null) {
                                                                    Badge(containerColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                                                                        Text(text = item.badgeCount.toString())
                                                                    }
                                                                } else if (item.hasNews) {
                                                                    Badge()
                                                                }
                                                            }
                                                        ) {
                                                            Icon(
                                                                imageVector = if (index == selectedItemIndex) {
                                                                    item.selectedIcon
                                                                } else item.unselectedIcon,
                                                                contentDescription = item.title

                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
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
            if (!hasPermission(permission)) {
                launch = true
                break
            }
        }
        if (launch) {
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
fun MyTopAppBar(
    vm: UserProfileScreen = viewModel(factory = Factory(LocalContext.current)),
    navController: NavHostController
) {
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
            if (navController.currentBackStackEntry?.destination?.route != "Teams") {
                IconButton(
                    onClick = {//navController.previousBackStackEntry?.savedStateHandle?.set("back", true)
                        if (!navController.popBackStack()) {//inutile fare if diverso da teams perchè fa la pop lo stessoghb
                            navController.navigate("Teams") {
                                //println("Destination: ${navController.previousBackStackEntry?.destination?.route}")
                                /* popUpTo(navController.graph.findStartDestination().id){
                             //saveState = true
                         }*/
                                launchSingleTop = true
                                //restoreState = true
                            }
                        }
                        println("Destination Back: ${navController.previousBackStackEntry?.destination?.route}")

                    },
                    colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                navController.navigate("Profile") {

                    /*popUpTo(navController.graph.findStartDestination().id) {
                        // Pop everything up to the "destination_a" destination off the back stack before
                        // navigating to the "destination_b" destination
                        //saveState = true
                    }*/
                    launchSingleTop = true //avoiding multiple copies on the top of the back stack
                    //restoreState = true
                }
            }, colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.secondary)) {
                Icon(
                    imageVector = Icons.Default.ManageAccounts,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
            //SCOMMENTA PER PENNINA IN TOPBAR
            /*
            if (vm.isEditing) {
                Button(onClick = { vm.validate() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Done", color = MaterialTheme.colorScheme.onSecondary)
                }
            } else {
                IconButton(onClick = { vm.edit() }, colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.secondary)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSecondary)
                }
            }*/
        }
    )
}

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)


@Composable
fun isVertical(): Boolean {
    val context = LocalContext.current
    return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}

class NavControllerManager {
    companion object {
        private val LocalNavController = staticCompositionLocalOf<NavHostController> {
            error("NavController not provided")
        }

        @Composable
        fun ProvideNavController(content: @Composable () -> Unit) {
            val navController = rememberNavController()
            CompositionLocalProvider(LocalNavController provides navController) {
                content()
            }
        }

        @Composable
        fun getNavController(): NavHostController {
            return LocalNavController.current
        }
    }
}

