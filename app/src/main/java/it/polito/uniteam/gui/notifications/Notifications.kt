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
import androidx.compose.ui.graphics.Color
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
    val loggedMember = vm.model.loggedMemberFinal.id
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
    val histories = AppStateManager.getHistories()
    vm.unreadMessages.clear()
    // teamsMessages
    val userTeamsMessages = teams.filter { it.members.contains(vm.loggedMember.id)}
        .map { userTeam -> Pair(userTeam,chats.find {chat -> chat.id == userTeam.chat }) }
        .map { userTeamChat -> Pair(userTeamChat.first,userTeamChat.second?.messages) }
        .map { userTeamMessagesIds ->
            val teamMessages = messages.filter { userTeamMessagesIds.second?.contains(it.id)!! }
            Pair(userTeamMessagesIds.first,teamMessages)
        }.map {
            val count = vm.getUnreadMessagesTeamDB(it.second,vm.loggedMember.id)
            val newestCreationDate = it.second.maxOfOrNull { it.creationDate }
            MessagesInfos(teamMemberName = it.first.name, date = newestCreationDate, chatId = it.first.chat!!, unreadMessages = count)
        }.filter { it.unreadMessages > 0 }
    vm.unreadMessages.addAll(userTeamsMessages)
    // membersChatMessages
    val directChats = chats.filter { chat -> chat.receiver == vm.loggedMember.id || chat.sender == vm.loggedMember.id }
        .map { chat ->
            // take the other member to chat with and messagesIds
            if(chat.receiver == vm.loggedMember.id) {
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
        }.map {
            val count = vm.getUnreadMessagesUserDB(it.third,vm.loggedMember.id)
            val newestCreationDate = it.third.maxOfOrNull { it.creationDate }
            MessagesInfos(teamMemberName = it.first.username, date = newestCreationDate, chatId = it.second, unreadMessages = count)
        }.filter { it.unreadMessages > 0 }
    vm.unreadMessages.addAll(directChats)

    vm.teamsHistories.clear()
    // teams histories
    val userTeams = teams.filter { it.members.contains(vm.loggedMember.id)}
    vm.teamsHistories.addAll(userTeams.map { userTeam ->
        val teamHistories = histories.filter { userTeam.teamHistory.contains(it.id) }
        Pair(userTeam,teamHistories)
    }.flatMap { it.second.map { history->
        Pair(it.first,history)
    } })
    // histories no more from the teams (removed from the team, team deleted)
    val noTeamHistories = histories.filter {
        it.comment.contains("${vm.loggedMember.username} removed from the Team.")
                || (it.comment.contains(Regex("""Team (.+) deleted\.""")) && it.oldMembers.contains(vm.loggedMember.id))
    }
    vm.teamsHistories.addAll(noTeamHistories.map {
        Pair(null,it)
    })
}

@Composable
fun Notifications(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    vm.loggedMember = AppStateManager.getLoggedMemberFinal(members = AppStateManager.getMembers(),vm.model.loggedMemberFinal.id)
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
        item(vm.unreadMessages.size) {
            vm.unreadMessages.sortedByDescending { it.date }.forEach { (name,_,chatId,count) ->
                MessageItem(teamMemberName = name, teamMemberChatId = chatId, nOfMessages = count)
            }
        }
    }
}

@Composable
fun MessageItem(teamMemberName: String, teamMemberChatId:String, nOfMessages: Int) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .clickable {
                Log.i(
                    "prova",
                    teamMemberChatId
                ); navController.navigate("Chat/$teamMemberChatId")
            }
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
        items(1) {
            vm.teamsHistories.sortedByDescending { it.second.date }.forEach { (team,history) ->
                val comment = if(history.comment == "Team Joined.") {
                    val member = AppStateManager.getMembers().find { it.id == history.user }!! //TODO
                    if(member.id != vm.loggedMember.id)
                        "${member.username} Joined The Team."
                    else
                        "You Joined The Team."
                } else if(history.comment == "Team Left.") {
                    val member = AppStateManager.getMembers().find { it.id == history.user }!!
                    if(member.id != vm.loggedMember.id)
                        "${member.username} Left The Team."
                    else
                        "You Left The Team."
                } else if(history.comment == "${vm.loggedMember.username} removed from the Team.") {
                    "You were removed from the Team."
                } else {
                    history.comment
                }
                ActivityItem(teamName = team?.name, teamId = team?.id, Activity = comment)
            }
        }
    }
}

@Composable
fun ActivityItem(teamName: String?, teamId:String?, Activity: String) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = if(teamId!=null)
            Modifier
                .clickable { navController.navigate("Team/${teamId}") }
                .fillMaxWidth()
                .border(0.5.dp, MaterialTheme.colorScheme.onPrimary)
                .padding(10.dp)
                .heightIn(min = 40.dp)
                .wrapContentHeight()
        else
            Modifier
                .fillMaxWidth()
                .background(Color.Red.copy(alpha = 0.3f))
                .border(0.5.dp, MaterialTheme.colorScheme.onPrimary)
                .padding(10.dp)
                .heightIn(min = 40.dp)
                .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start
    ) {
        Text(text = if(teamName!=null) "$teamName -> $Activity" else Activity)
    }
}