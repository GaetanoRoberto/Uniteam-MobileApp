package it.polito.uniteam.gui.chatlist

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.classes.DummyDataProvider

import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.Team
import it.polito.uniteam.classes.TeamIcon
import it.polito.uniteam.isVertical
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ChatListScreen( vm : ChatListViewModel = viewModel(factory = Factory(LocalContext.current))) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListHeader(vm = vm)
        UserList(vm = vm)
    }
}

@Composable
fun ListHeader(vm : ChatListViewModel ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "${vm.team.name} Members",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
    }
}
@Composable
fun UserList(vm : ChatListViewModel) {
    //TODO: implement sorting

    //order by last message
    val members = vm.getMembers().sortedByDescending { member ->
        val messages = vm.getChat(vm.getUsersChat(member).id).messages

        //member.messages.maxOfOrNull { it.creationDate } ?: LocalDateTime.MIN
        messages.maxOfOrNull { it.creationDate } ?: LocalDateTime.MIN
    }

   /*Text( // PER CONTROLLARE ORDINE
        text = "Members: ${vm.getMembers().joinToString(", ") { it.id.toString() }}",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White
    )*/
        TeamRow(team = vm.team, vm = vm)
        LazyColumn {
        items(members.filter { member -> member != vm.loggedMember.value  }) { user ->
            UserItem(member = user,vm = vm)
        }
    }
}

@Composable
fun TeamRow(team: Team,vm: ChatListViewModel) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)// LEVA SE VUOI UNIRE LE RIGHE
            .background(MaterialTheme.colorScheme.onSecondaryContainer)
            .clickable { /*TODO*/ }
            .padding(16.dp)
        //.height((LocalConfiguration.current.screenHeightDp * 0.2).dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamIcon(modifierScale= Modifier.scale(1f), modifierPadding = Modifier.padding(4.dp, 0.dp, 15.dp, 0.dp),team = team )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(0.40f)
                .padding(end = 5.dp)
        ) {
            Text(text = team.name, style = MaterialTheme.typography.bodyLarge)
        }
        Column (modifier = Modifier.weight(0.20f)){
            val recentMessageDate = vm.messages
                //.filter { it.senderId != team.chat?.sender?.id }
                .maxOfOrNull { it.creationDate }

            recentMessageDate?.let {
                Text(
                    textAlign = TextAlign.Center,
                    text = formatMessageDate(it),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(CenterHorizontally), // Center text horizontally
                    color = MaterialTheme.colorScheme.onPrimaryContainer// Color.Gray
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(if (isVertical()) 0.3f else 0.2f)
        ) {
            Icon(
                imageVector = Icons.Filled.Chat,
                contentDescription = "Chat",
                modifier = Modifier
                    //.fillMaxSize()
                    .align(Alignment.Center)
                    .size(50.dp)
                    .clickable { navController.navigate("Chat/${vm.team.chat?.id}"){
                        launchSingleTop = true
                    }
                    },
            )
            val unreadCount =
                vm.chat.teamId?.let { vm.getUnreadMessagesTeam(it) }//vm.chat.messages.filter { it.status == messageStatus.UNREAD && it.senderId == member.id}.size
            if (unreadCount != null && unreadCount > 0 ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .defaultMinSize(18.dp,18.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }


    }
}
@Composable
fun UserItem(member: Member,vm : ChatListViewModel) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)// LEVA SE VUOI UNIRE LE RIGHE
            .background(MaterialTheme.colorScheme.secondary)
            .clickable { /*TODO*/ }
            .padding(16.dp)
            //.height((LocalConfiguration.current.screenHeightDp * 0.2).dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberIcon(modifierScale= Modifier.scale(1f), modifierPadding = Modifier.padding(4.dp, 0.dp, 15.dp, 0.dp),member = member, selectUser = vm.selectUser )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(0.40f)
                .padding(end = 5.dp)
        ) {
            Text(text = member.fullName, style = MaterialTheme.typography.bodyLarge)
            //ROLE IN DUMMY DATA DA CAMBIARE TODO
            Text(text = member.teamsInfo?.get(1)?.role.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Column (modifier = Modifier.weight(0.20f)){
            val messages = vm.getChat(vm.getUsersChat(member).id).messages
            val recentMessageDate =  messages
                //.filter { it.senderId == member.id }
                .maxOfOrNull { it.creationDate }

            recentMessageDate?.let {
                Text(
                    textAlign = TextAlign.Center,
                    text = formatMessageDate(it),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(CenterHorizontally), // Center text horizontally
                    color = MaterialTheme.colorScheme.onPrimaryContainer// Color.Gray
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(if (isVertical()) 0.3f else 0.2f)
        ) {
            Icon(
                imageVector = Icons.Filled.Chat,
                contentDescription = "Chat",
                modifier = Modifier
                    //.fillMaxSize()
                    .align(Alignment.Center)
                    .size(50.dp)
                    .clickable {
                        val chat = vm.getUsersChat(member)
                        if (chat != null) {
                            navController.navigate("Chat/${chat.id}"){
                                launchSingleTop = true
                            }
                        }
                },
            )
            val unreadCount = vm.getUnreadMessagesCount(member.id)//vm.chat.messages.filter { it.status == messageStatus.UNREAD && it.senderId == member.id}.size
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }


    }
}
fun formatMessageDate(dateTime: LocalDateTime): String {
    val today = LocalDate.now()
    val messageDate = dateTime.toLocalDate()

    return when {
        ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now()) <= 5 -> "Now"
        ChronoUnit.DAYS.between(messageDate, today) == 0L -> "Today"
        ChronoUnit.DAYS.between(messageDate, today) == 1L -> "Yesterday"
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}