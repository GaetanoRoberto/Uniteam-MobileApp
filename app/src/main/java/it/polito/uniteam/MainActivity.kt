package it.polito.uniteam

//import it.polito.uniteam.gui.chat.ChatScreen
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import it.polito.uniteam.classes.CommentDBFinal
import it.polito.uniteam.classes.FileDBFinal
import it.polito.uniteam.classes.HistoryDBFinal
import it.polito.uniteam.classes.LoadingSpinner
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.TaskDBFinal
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.gui.showtaskdetails.SetupTaskData
import it.polito.uniteam.gui.showtaskdetails.isTaskChanges
import it.polito.uniteam.gui.userprofile.isProfileChanges
import it.polito.uniteam.gui.userprofile.SetupProfileData
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ChecklistRtl
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import it.polito.uniteam.gui.teamDetails.TeamViewScreen
import it.polito.uniteam.gui.availability.Availability
import it.polito.uniteam.gui.availability.Join
import it.polito.uniteam.gui.calendar.CalendarAppContainer
import it.polito.uniteam.gui.chat.ChatScreen
import it.polito.uniteam.gui.chatlist.ChatListScreen
import it.polito.uniteam.gui.invitation.Invitation
import it.polito.uniteam.gui.notifications.Notifications
import it.polito.uniteam.gui.showtaskdetails.TaskScreen
import it.polito.uniteam.gui.statistics.Statistics
import it.polito.uniteam.gui.teamScreen.TeamScreen
import it.polito.uniteam.gui.home.Home
import it.polito.uniteam.gui.notifications.messageUnreadCountForBottomBar
import it.polito.uniteam.gui.userprofile.OtherProfileSettings
import it.polito.uniteam.gui.userprofile.ProfileSettings
import it.polito.uniteam.gui.userprofile.UserProfileScreen
import it.polito.uniteam.ui.theme.UniTeamTheme
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.lifecycle.ViewModel
import it.polito.uniteam.classes.ChatDBFinal
import it.polito.uniteam.classes.MemberDB
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.gui.teamDetails.TeamDetailsView
import it.polito.uniteam.gui.yourTasksCalendar.YourTasksCalendarView
import okhttp3.internal.wait

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


            UniTeamTheme(
                darkTheme = theme,
                vm = viewModel(factory = Factory(LocalContext.current))
            ) {
                // Stati da passare per le queries che non vanno rieseguite ad ogni recomposition
                /*val teamsList = it.getTeams().collectAsState(initial = emptyList())
                val membersList = it.getAllTeamsMembersHome().collectAsState(initial = emptyList())*/

                val teams = it.teams.collectAsState(initial = emptyList())
                val tasks = it.tasks.collectAsState(initial = emptyList())
                val messages = it.messages.collectAsState(initial = emptyList())
                val members = it.members.collectAsState(initial = emptyList())
                val histories = it.histories.collectAsState(initial = emptyList())
                val files = it.files.collectAsState(initial = emptyList())
                val comments = it.comments.collectAsState(initial = emptyList())
                val chats = it.chats.collectAsState(initial = emptyList())

                Log.d("UniTeam Solo", "Members: ${members.value}")

                AppStateManager.ProvideLocalAppState(it) {
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
                            badgeCount = messageUnreadCountForBottomBar()
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

                                    val drawerState =
                                        rememberDrawerState(initialValue = DrawerValue.Closed)
                                    val coroutineScope = rememberCoroutineScope()
                                    if(!isVertical()){
                                        ModalNavigationDrawer(
                                            drawerState = drawerState,
                                            gesturesEnabled = !isVertical(),
                                            drawerContent = {
                                                ModalDrawerSheet(
                                                    drawerContainerColor = MaterialTheme.colorScheme.secondary,
                                                    drawerContentColor = MaterialTheme.colorScheme.primary,
                                                ) {
                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    items.forEachIndexed { index, item ->
                                                        NavigationDrawerItem(
                                                            colors = NavigationDrawerItemDefaults.colors(
                                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                                selectedIconColor = MaterialTheme.colorScheme.onPrimary
                                                            ),
                                                            label = {
                                                                Text(text = item.title)
                                                            },
                                                            selected = item.title == navBackStackEntry?.destination?.route,
                                                            onClick = {

                                                                navController.navigate(item.title) {
                                                                    launchSingleTop = true
                                                                }


                                                                selectedItemIndex = index
                                                                coroutineScope.launch {
                                                                    drawerState.close()
                                                                }
                                                            },
                                                            icon = {
                                                                Icon(
                                                                    imageVector = if (index == selectedItemIndex) {
                                                                        item.selectedIcon
                                                                    } else item.unselectedIcon,
                                                                    contentDescription = item.title
                                                                )
                                                            },
                                                            badge = {
                                                                if(item.badgeCount != null &&  item.badgeCount > 0){
                                                                    Badge(
                                                                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                                    ) {
                                                                        Text(text = item.badgeCount.toString())
                                                                    }
                                                                }
                                                                /* item.badgeCount?.let {
                                                                     Text(text = item.badgeCount.toString())
                                                                 }*/
                                                            },
                                                            modifier = Modifier
                                                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                                        )
                                                    }
                                                    NavigationDrawerItem(
                                                        colors = NavigationDrawerItemDefaults.colors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary
                                                        ),
                                                        label = { Text(text = "Profile") },
                                                        selected = "Profile" == navBackStackEntry?.destination?.route,
                                                        onClick = {
                                                            navController.navigate("Profile") {
                                                                launchSingleTop = true
                                                            }
                                                            selectedItemIndex = 0
                                                            coroutineScope.launch {
                                                                drawerState.close()
                                                            }
                                                        },
                                                        icon = {
                                                            Icon(
                                                                imageVector = if ("Profile" == navBackStackEntry?.destination?.route) {
                                                                    Icons.Default.ManageAccounts
                                                                } else Icons.Outlined.ManageAccounts,
                                                                contentDescription = "Profile"
                                                            )
                                                        },
                                                        modifier = Modifier
                                                            .padding(NavigationDrawerItemDefaults.ItemPadding)

                                                    )
                                                }
                                            },
                                            content = {
                                                Scaffold(
                                                    topBar = {
                                                        MyTopAppBar(
                                                            vm = viewModel(
                                                                factory = Factory(
                                                                    LocalContext.current
                                                                )
                                                            ),
                                                            navController,
                                                            coroutineScope,
                                                            drawerState
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
                                                                composable("Teams") {
                                                                    Home(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable(
                                                                    "Team/{teamId}",
                                                                    arguments = listOf(navArgument("teamId") {
                                                                        type = NavType.StringType
                                                                    })
                                                                ) {
                                                                    TeamScreen(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable("Task/{taskId}") {
                                                                    TaskScreen(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable("Calendar/{teamId}") {
                                                                    CalendarAppContainer(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable("Notifications") {
                                                                    Notifications(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable("Chat/{chatId}") {

                                                                    ChatScreen(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        ),
                                                                        teams = teams.value,
                                                                        members = members.value,
                                                                        messages = messages.value,
                                                                        chats = chats.value
                                                                    )
                                                                }
                                                                composable("ChatList/{teamId}") {
                                                                    ChatListScreen(
                                                                        vm = viewModel(
                                                                            factory = Factory(LocalContext.current)
                                                                        ),
                                                                        chatList = chats.value,
                                                                        members = members.value,
                                                                        teams = teams.value,
                                                                        messages = messages.value
                                                                    )
                                                                }
                                                                composable("Profile") {
                                                                    ProfileSettings(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        ),
                                                                        outputDirectory = getOutputDirectory(),
                                                                        cameraExecutor = cameraExecutor
                                                                    )
                                                                }
                                                                composable(
                                                                    "Invitation/{teamId}/{teamName}",
                                                                    arguments = listOf(
                                                                        navArgument("teamId") {
                                                                            type = NavType.StringType
                                                                        },
                                                                        navArgument("teamName") {
                                                                            type = NavType.StringType
                                                                        })
                                                                ) { backStackEntry ->
                                                                    Invitation(
                                                                        teamId = backStackEntry.arguments?.getString(
                                                                            "teamId"
                                                                        )!!,
                                                                        teamName = backStackEntry.arguments?.getString(
                                                                            "teamName"
                                                                        )!!
                                                                    )
                                                                }
                                                                composable(
                                                                    "ChangeAvailability/{teamId}/{teamName}",
                                                                    arguments = listOf(navArgument("teamId") {
                                                                        type = NavType.StringType
                                                                    })
                                                                ) {
                                                                    Availability(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable(
                                                                    "JoinTeam/{teamId}",
                                                                    deepLinks = listOf(navDeepLink {
                                                                        uriPattern =
                                                                            "https://UniTeam/join/{teamId}"
                                                                    })
                                                                ) {
                                                                    Join(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable(
                                                                    "Statistics/{teamId}",
                                                                    arguments = listOf(navArgument("teamId") {
                                                                        type = NavType.StringType
                                                                    })
                                                                ) {
                                                                    Statistics(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable(
                                                                    "TeamDetails/{teamId}",
                                                                    arguments = listOf(navArgument("teamId") {
                                                                        type = NavType.StringType
                                                                    })
                                                                ) {
                                                                    TeamViewScreen(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        ),
                                                                        outputDirectory = getOutputDirectory(),
                                                                        cameraExecutor = cameraExecutor
                                                                    )
                                                                }
                                                                composable("Tasks") {
                                                                    YourTasksCalendarView(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                composable(
                                                                    "OtherUserProfile/{memberId}",
                                                                    arguments = listOf(navArgument("memberId") {
                                                                        type = NavType.StringType
                                                                    })
                                                                ) {
                                                                    OtherProfileSettings(
                                                                        vm = viewModel(
                                                                            factory = Factory(
                                                                                LocalContext.current
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    },
                                                    bottomBar = {
                                                        if (!isVertical()) {
                                                            Row {}
                                                        } else {
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
                                                                                    if (item.badgeCount != null && item.badgeCount > 0) {
                                                                                        Badge(
                                                                                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                                                        ) {
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
                                                    }
                                                )
                                            })
                                    }else{
                                        LaunchedEffect(key1 = "close") {
                                            coroutineScope.launch {
                                                drawerState.close()
                                            }
                                        }

                                        Scaffold(
                                            topBar = {
                                                MyTopAppBar(
                                                    vm = viewModel(
                                                        factory = Factory(
                                                            LocalContext.current
                                                        )
                                                    ),
                                                    navController,
                                                    coroutineScope,
                                                    drawerState
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
                                                },*/
                                                content = { paddingValue ->
                                                    Column(Modifier.padding(paddingValue)) {
                                                        // In your main activity or main screen composable
                                                        NavHost(
                                                            navController = navController,
                                                            startDestination = "Teams"
                                                        ) {
                                                            composable("Teams") {
                                                                Home(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable(
                                                                "Team/{teamId}",
                                                                arguments = listOf(navArgument("teamId") {
                                                                    type = NavType.StringType
                                                                })
                                                            ) {
                                                                TeamScreen(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable("Task/{taskId}/{teamId}") {
                                                                TaskScreen(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable("Calendar/{teamId}") {
                                                                CalendarAppContainer(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable("Notifications") {
                                                                Notifications(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable("Task/{taskId}") {
                                                                SetupTaskData()
                                                                TaskScreen(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable("Calendar/{teamId}") {
                                                                CalendarAppContainer(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable("Notifications") {
                                                                Notifications(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable("Chat/{chatId}") {
                                                                ChatScreen(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    ),
                                                                    teams = teams.value,
                                                                    members = members.value,
                                                                    messages = messages.value,
                                                                    chats = chats.value
                                                                )
                                                            }
                                                            composable("ChatList/{teamId}") {
                                                                ChatListScreen(
                                                                    vm = viewModel(
                                                                        factory = Factory(LocalContext.current)
                                                                    ),
                                                                    chatList = chats.value,
                                                                    members = members.value,
                                                                    teams = teams.value,
                                                                    messages = messages.value
                                                                )
                                                            }
                                                            composable("Profile") {
                                                                if (!isProfileChanges())
                                                                    SetupProfileData()
                                                                ProfileSettings(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    ),
                                                                    outputDirectory = getOutputDirectory(),
                                                                    cameraExecutor = cameraExecutor
                                                                )
                                                            }
                                                            composable(
                                                                "Invitation/{teamId}/{teamName}",
                                                                arguments = listOf(
                                                                    navArgument("teamId") {
                                                                        type = NavType.StringType
                                                                    },
                                                                    navArgument("teamName") {
                                                                        type = NavType.StringType
                                                                    })
                                                            ) { backStackEntry ->
                                                                Invitation(
                                                                    teamId = backStackEntry.arguments?.getString(
                                                                        "teamId"
                                                                    )!!,
                                                                    teamName = backStackEntry.arguments?.getString(
                                                                        "teamName"
                                                                    )!!
                                                                )
                                                            }
                                                            composable(
                                                                "ChangeAvailability/{teamId}/{teamName}",
                                                                arguments = listOf(navArgument("teamId") {
                                                                    type = NavType.StringType
                                                                })
                                                            ) {
                                                                Availability(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable(
                                                                "JoinTeam/{teamId}",
                                                                deepLinks = listOf(navDeepLink {
                                                                    uriPattern =
                                                                        "https://UniTeam/join/{teamId}"
                                                                })
                                                            ) {
                                                                Join(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable(
                                                                "Statistics/{teamId}",
                                                                arguments = listOf(navArgument("teamId") {
                                                                    type = NavType.StringType
                                                                })
                                                            ) {
                                                                Statistics(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable(
                                                                "TeamDetails/{teamId}",
                                                                arguments = listOf(navArgument("teamId") {
                                                                    type = NavType.StringType
                                                                })
                                                            ) {
                                                                TeamViewScreen(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    ),
                                                                    outputDirectory = getOutputDirectory(),
                                                                    cameraExecutor = cameraExecutor
                                                                )
                                                            }
                                                            composable("Tasks") {
                                                                YourTasksCalendarView(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            composable(
                                                                "OtherUserProfile/{memberId}",
                                                                arguments = listOf(navArgument("memberId") {
                                                                    type = NavType.StringType
                                                                })
                                                            ) {
                                                                OtherProfileSettings(
                                                                    vm = viewModel(
                                                                        factory = Factory(
                                                                            LocalContext.current
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                        }
                                                }
                                            },
                                            bottomBar = {
                                                if (!isVertical()) {
                                                    Row {}
                                                } else {
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
                                                                            if (item.badgeCount != null && item.badgeCount > 0) {
                                                                                Badge(
                                                                                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                                                ) {
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
                                            }
                                        )
                                    }

                                }
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
        val permissions = arrayOf(Manifest.permission.CAMERA,Manifest.permission.POST_NOTIFICATIONS)

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
    navController: NavHostController,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState
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
            if (navController.currentBackStackEntry?.destination?.route != "Teams" && navController.currentBackStackEntry?.destination?.route != "JoinTeam/{teamId}") {
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        actions = {
            if (isVertical()){
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
            }

            //TODO DRAWER
            if (!isVertical()) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
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

class AppStateManager {
    companion object {
        private val LocalLoggedMember = staticCompositionLocalOf<MemberDBFinal> { MemberDBFinal() }
        private val LocalTeams = staticCompositionLocalOf<List<TeamDBFinal>> { emptyList() }
        private val LocalTasks = staticCompositionLocalOf<List<TaskDBFinal>> { emptyList() }
        private val LocalMembers = staticCompositionLocalOf<List<MemberDBFinal>> { emptyList() }
        private val LocalHistories = staticCompositionLocalOf<List<HistoryDBFinal>> { emptyList() }
        private val LocalFiles = staticCompositionLocalOf<List<FileDBFinal>> { emptyList() }
        private val LocalComments = staticCompositionLocalOf<List<CommentDBFinal>> { emptyList() }
        private val LocalChats = staticCompositionLocalOf<List<ChatDBFinal>> { emptyList() }
        private val LocalMessages = staticCompositionLocalOf<List<MessageDB>> { emptyList() }

        @Composable
        fun ProvideLocalAppState(
            viewModel: GeneralViewModel = viewModel(
                factory = Factory(
                    LocalContext.current
                )
            ), content: @Composable () -> Unit
        ) {
            val loggedMember = viewModel.loggedMember.collectAsState(initial = MemberDBFinal())
            val teams = viewModel.teams.collectAsState(initial = emptyList())
            val tasks = viewModel.tasks.collectAsState(initial = emptyList())
            val members = viewModel.members.collectAsState(initial = emptyList())
            val histories = viewModel.histories.collectAsState(initial = emptyList())
            val files = viewModel.files.collectAsState(initial = emptyList())
            val comments = viewModel.comments.collectAsState(initial = emptyList())
            val chats = viewModel.chats.collectAsState(initial = emptyList())
            val messages = viewModel.messages.collectAsState(initial = emptyList())

            CompositionLocalProvider(
                LocalLoggedMember provides loggedMember.value,
                LocalTeams provides teams.value,
                LocalTasks provides tasks.value,
                LocalMembers provides members.value,
                LocalHistories provides histories.value,
                LocalFiles provides files.value,
                LocalComments provides comments.value,
                LocalChats provides chats.value,
                LocalMessages provides messages.value
            ) {
                Log.d("UniTeam", "Members: ${members.value}")
                if (teams.value.isNotEmpty() && tasks.value.isNotEmpty() && members.value.isNotEmpty()
                    && histories.value.isNotEmpty() && files.value.isNotEmpty() && comments.value.isNotEmpty()
                    && chats.value.isNotEmpty() && messages.value.isNotEmpty()) {
                    content()
                }/* else {
                    LoadingSpinner()
                }*/
            }
        }

        @Composable
        fun getTeams(): List<TeamDBFinal> {
            return LocalTeams.current
        }

        @Composable
        fun getTasks(): List<TaskDBFinal> {
            return LocalTasks.current
        }

        @Composable
        fun getMembers(): List<MemberDBFinal> {
            return LocalMembers.current
        }

        @Composable
        fun getHistories(): List<HistoryDBFinal> {
            return LocalHistories.current
        }

        @Composable
        fun getFiles(): List<FileDBFinal> {
            return LocalFiles.current
        }

        @Composable
        fun getComments(): List<CommentDBFinal> {
            return LocalComments.current
        }

        @Composable
        fun getChats(): List<ChatDBFinal> {
            return LocalChats.current
        }

        @Composable
        fun getMessages(): List<MessageDB> {
            return LocalMessages.current
        }

        @Composable
        fun getLoggedMember(): MemberDBFinal {
            return LocalLoggedMember.current
        }
    }
}
