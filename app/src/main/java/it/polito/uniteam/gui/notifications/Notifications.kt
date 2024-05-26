package it.polito.uniteam.gui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager

@Composable
fun Notifications(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val icons = listOf(Icons.Filled.Comment, Icons.Filled.Info)
    val titles = notificationsSection.entries.map { it.toString() }
    Column {
        val pagerState = rememberPagerState {
            titles.size
        }
        LaunchedEffect(vm.tabState) {
            pagerState.animateScrollToPage(vm.tabState)
        }
        LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
            if(!pagerState.isScrollInProgress) {
                vm.switchTab(pagerState.currentPage)
                vm.changeSection((if(pagerState.currentPage == 0) notificationsSection.MESSAGES else notificationsSection.ACTIVITIES).toString())
            }
        }
        TabRow(selectedTabIndex = vm.tabState, indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.tabIndicatorOffset(tabPositions[vm.tabState])
            )
        }) {
            titles.forEachIndexed { index, title ->
                Tab(selected = vm.tabState == index,
                    onClick = { vm.switchTab(index); vm.changeSection(title); },
                    text = { Text(text = title, color = MaterialTheme.colorScheme.onPrimary) },
                    icon = { Icon(icons[index], title, tint = MaterialTheme.colorScheme.onPrimary) })
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier
            .fillMaxWidth()
            .weight(1f), verticalAlignment = Alignment.Top) { index ->
            if(index == 0) {
                MessagesSection(vm = vm)
            } else {
                ActivitiesSection(vm = vm)
            }
        }
    }
}

@Composable
fun MessagesSection(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    LazyColumn {
        item(vm.teamsMessages.size + vm.membersMessages.size) {
            vm.teamsMessages.forEach { (team,count) ->
                MessageItem(teamMemberName = team.name, teamMemberChatId = team.chat?.id!!, nOfMessages = count)
            }
            vm.membersMessages.forEach { (member,count)->
                MessageItem(teamMemberName = member.username, teamMemberChatId = vm.getUsersChat(member).id, nOfMessages = count)
            }
        }
    }
}

@Composable
fun MessageItem(teamMemberName: String, teamMemberChatId:Int, nOfMessages: Int) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .clickable { navController.navigate("Chat/$teamMemberChatId") }
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.onPrimary)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.8f)) {
            val text = if (nOfMessages > 1) teamMemberName + " -> New Messages"
            else
                teamMemberName + " -> New Message"
            Text(text = text)
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nOfMessages.toString(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun ActivitiesSection(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    LazyColumn {
        items(vm.teamsHistories) { (team,histories) ->
            histories.forEach { history ->
                ActivityItem(teamName = team.name, teamId = team.id, Activity = history.comment)
            }
        }
    }
}

@Composable
fun ActivityItem(teamName: String, teamId:Int, Activity: String) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .clickable { navController.navigate("Team/${teamId}") }
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.onPrimary)
            .padding(10.dp)
            .heightIn(min = 40.dp)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start
    ) {
        Text(text = teamName + " -> " + Activity)
    }
}