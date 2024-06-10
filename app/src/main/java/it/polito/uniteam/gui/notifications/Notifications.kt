package it.polito.uniteam.gui.notifications

import android.util.Log
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
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues.Empty.model
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.messageStatus

@Composable
fun messageUnreadCountForBottomBar(vm : NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))): Int{
    val loggedMember = vm.loggedMember
    /*vm.teamsMessages.forEach { (_,c) -> count += c }
    vm.membersMessages.forEach { (_,c) -> count += c }*/
    val messages = AppStateManager.getMessages()
    val chats = AppStateManager.getChats()
    val teams = AppStateManager.getTeams()

    val directChats = chats.filter { chat -> chat.sender == loggedMember || chat.receiver == loggedMember }
    val teamChats = chats.filter { chat -> teams.any { team -> team.id == chat.teamId && team.members.contains(loggedMember) } }
    val directMessages = messages.filter { message ->
        directChats.any { chat -> chat.messages.contains(message.id) }
    }

    val teamMessages = messages.filter { message ->
        teamChats.any { chat -> chat.messages.contains(message.id) }
    }

    val unreadDirectMessages = directMessages.filter { message ->
        message.status == messageStatus.UNREAD && message.senderId != loggedMember
    }.size

    val unreadTeamMessages = teamMessages.filter { message ->
        message.membersUnread.contains(loggedMember)
    }.size

    return unreadTeamMessages + unreadDirectMessages
    }

@Composable
fun SetupNotificationsData(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val chats = AppStateManager.getChats()
    val teams = AppStateManager.getTeams()
    val messages = AppStateManager.getMessages()
    val members = AppStateManager.getMembers()
    // teamsMessages
    val userTeamsMessages = teams.filter { it.members.contains(vm.loggedMember)}
        .map { userTeam -> Pair(userTeam,chats.find {chat -> chat.id == userTeam.chat }) }
        .map { userTeamChat -> Pair(userTeamChat.first,userTeamChat.second?.messages) }
        .map { userTeamMessagesIds ->
            val teamMessages = messages.filter { userTeamMessagesIds.second?.contains(it.id)!! }
            Pair(userTeamMessagesIds.first,teamMessages)
        }
    vm.teamsMessages = userTeamsMessages.map {
        Pair(it.first,vm.getUnreadMessagesTeamDB(it.second,vm.loggedMember))
    }.filter { it.second > 0 }
    // membersChatMessages
    val directChats = chats.filter { chat -> chat.receiver == vm.loggedMember || chat.sender == vm.loggedMember }
        .map { chat ->
            // take the other member to chat with and messagesIds
            if(chat.receiver == vm.loggedMember) {
                val member = members.find {it.id == chat.sender}!!
                Triple(member,chat.id,chat.messages)
            } else {
                val member = members.find {it.id == chat.receiver}!!
                Triple(member,chat.id,chat.messages)
            }
        }
        .map { memberChatIdMessagesId ->
            val userMessages = messages.filter { memberChatIdMessagesId.third.contains(it.id) }
            Triple(memberChatIdMessagesId.first,memberChatIdMessagesId.second,userMessages)
        }
    vm.membersChatMessages = directChats.map { memberChatIdMessages ->
        Triple(memberChatIdMessages.first,memberChatIdMessages.second,vm.getUnreadMessagesUserDB(memberChatIdMessages.third,vm.loggedMember))
    }.filter { it.third > 0 }

    val userTeams = teams.filter { it.members.contains(vm.loggedMember)}
    vm.teamsHistories = userTeams.map { userTeam ->
        val teamHistories = AppStateManager.getHistories().filter { userTeam.teamHistory.contains(it.id) }
        Pair(userTeam,teamHistories)
    }
}

@Composable
fun Notifications(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    SetupNotificationsData(vm = vm)
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
        item(vm.teamsMessages.size + vm.membersChatMessages.size) {
            vm.teamsMessages.forEach { (team,count) ->
                MessageItem(teamMemberName = team.name, teamMemberChatId = team.chat!!, nOfMessages = count)
            }
            vm.membersChatMessages.forEach { (member,chatId,count)->
                MessageItem(teamMemberName = member.username, teamMemberChatId = chatId, nOfMessages = count)
            }
        }
    }
}

@Composable
fun MessageItem(teamMemberName: String, teamMemberChatId:String, nOfMessages: Int) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .clickable { Log.i("prova",teamMemberChatId); navController.navigate("Chat/$teamMemberChatId") }
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
                val comment = if(history.comment == "Team Joined.") {
                    val member = AppStateManager.getMembers().find { it.id == history.user }!!
                    "${member.username} Joined The Team."
                } else if(history.comment == "Team Left.") {
                    val member = AppStateManager.getMembers().find { it.id == history.user }!!
                    "${member.username} Left The Team."
                } else {
                    history.comment
                }
                ActivityItem(teamName = team.name, teamId = team.id, Activity = comment)
            }
        }
    }
}

@Composable
fun ActivityItem(teamName: String, teamId:String, Activity: String) {
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