package it.polito.uniteam.gui.chatlist

import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.classes.ChatDBFinal

import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.MessageDB
import it.polito.uniteam.classes.TeamDBFinal
import it.polito.uniteam.classes.TeamIcon
import it.polito.uniteam.isVertical
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ChatListScreen(
        vm : ChatListViewModel = viewModel(factory = Factory(LocalContext.current)),
        chatList : List<ChatDBFinal>,
        members : List<MemberDBFinal>,
        teams : List<TeamDBFinal>,
        messages : List<MessageDB>
) {
    val t = teams.find { it.id == vm.teamId }!!
    val m = members.filter { t.members.contains(it.id) }
    val c = chatList.filter { it.teamId == t.id || it.sender == vm.loggedMember.id || it.receiver == vm.loggedMember.id }
    // Filtra i messaggi per includere solo quelli che appartengono alle chat in c
    val filteredMessages = messages.filter { message ->
        c.any { chat -> chat.messages.contains(message.id) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ListHeader(t)
        UserList(vm = vm, m, c, filteredMessages, t)
    }
}

@Composable
fun ListHeader(team : TeamDBFinal?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        if (team != null) {
            Text(
                text = "${team.name}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun UserList(
        vm : ChatListViewModel,
        members : List<MemberDBFinal>,
        chatList : List<ChatDBFinal>,
        messages : List<MessageDB>,
        team : TeamDBFinal
) {
//TODO: implement sorting

//order by last message
    val membersSorted = members.sortedByDescending { member ->
        // Trova la chat pertinente con il membro corrente
        val relevantChat = chatList.find {
            (it.sender == member.id || it.receiver == member.id) &&
                    (it.sender == vm.loggedMember.id || it.receiver == vm.loggedMember.id)
        }

        // Ottieni gli ID dei messaggi per la chat pertinente
        val messagesId = relevantChat?.messages ?: mutableListOf()

        // Filtra i messaggi pertinenti a quella chat
        val mess = messages.filter { messagesId.contains(it.id) }

        // Trova la data di creazione più recente o MIN se non ci sono messaggi
        mess.maxOfOrNull { it.creationDate } ?: LocalDateTime.MIN
////val messages = vm.getChat(vm.getUsersChat(member).id).messages
//        val messagesId = chatList.find { it.sender == member.id || it.receiver == member.id }?.messages ?: mutableListOf()
//        val mess = messages.filter { messagesId.contains(it.id) }
////member.messages.maxOfOrNull { it.creationDate } ?: LocalDateTime.MIN
//        mess.maxOfOrNull { it.creationDate } ?: LocalDateTime.MIN
   }

    TeamRow(vm = vm,team = team, messages.filter { chatList.find { it.teamId == team.id }?.messages?.contains(it.id) == true })
    LazyColumn {
        items(membersSorted.filter { member -> member.id != vm.loggedMember.id }) { user ->
            chatList.find {
                (it.sender == user.id || it.receiver == user.id) &&
                        (it.sender == vm.loggedMember.id || it.receiver == vm.loggedMember.id)
            }?.let { chat ->
                val relevantMessages = messages.filter { chat.messages.contains(it.id) }
                UserItem(member = user, vm = vm, chat = chat, messages = relevantMessages)
            }
        }
    }
}

@Composable
fun TeamRow(vm: ChatListViewModel , team : TeamDBFinal, messages : List<MessageDB>) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("TeamDetails/${team.id}") }
            .padding(2.dp)// LEVA SE VUOI UNIRE LE RIGHE
            .background(MaterialTheme.colorScheme.onSecondaryContainer)
            .padding(16.dp)
//.height((LocalConfiguration.current.screenHeightDp * 0.2).dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamIcon(
            team = team,
            modifierPadding = if (team.image != Uri.EMPTY) Modifier.padding(6.dp,8.dp,12.dp,7.dp) else Modifier.padding(4.dp, 0.dp, 12.dp, 0.dp),
            modifierScale = if (team.image != Uri.EMPTY) Modifier.scale(2f) else Modifier.scale(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(0.40f)
                .padding(end = 5.dp)
        ) {
            Text(text = team.name, style = MaterialTheme.typography.bodyLarge)
        }
        Column(modifier = Modifier.weight(0.20f)) {
            val recentMessageDate = messages
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
                    .align(Alignment.Center)
                    .size(50.dp)
                    .clickable {
                        if (team != null) {
                            navController.navigate("Chat/${team.chat}") {
                                launchSingleTop = true
                            }
                        }
                    },
            )
            Log.d("ChatList", "Messages: ${messages}")
            val unreadCount = vm.getUnreadMessagesTeamDB(messages)// t team.ideam.let { vm.getUnreadMessagesTeam(it) }
//  vm.chat.teamId?.let { vm.getUnreadMessagesTeam(it) }//vm.chat.messages.filter { it.status == messageStatus.UNREAD && it.senderId == member.id}.size
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .defaultMinSize(18.dp, 18.dp)
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
fun UserItem(member : MemberDBFinal, vm : ChatListViewModel, chat : ChatDBFinal, messages : List<MessageDB>) {
    val navController = NavControllerManager.getNavController()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)// LEVA SE VUOI UNIRE LE RIGHE
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(onClick = { navController.navigate("OtherUserProfile/${member.id}") })
            .padding(16.dp)
//.height((LocalConfiguration.current.screenHeightDp * 0.2).dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberIcon(
            modifierScale = Modifier.scale(1f),
            modifierPadding = Modifier.padding(4.dp, 0.dp, 15.dp, 0.dp),
            member = member
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(0.40f)
                .padding(end = 5.dp)
        ) {
            Text(text = member.username, style = MaterialTheme.typography.bodyLarge) //ROLE IN DUMMY DATA DA CAMBIARE TODO
            val role = member.teamsInfo?.get(vm.teamId)?.role?.toString() ?: "Unknown"
            if(member.id == "2nm8PdGbk5CaROcyWjq7"){
                Log.d("ChatList", "Role: $role")
                Log.d("ChatList", "Member: ${member.teamsInfo}")
            }

            if(member.teamsInfo != null){
                Text(text =role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Column(modifier = Modifier.weight(0.20f)) {
            //FIND LAST MESSAGE DATE
            val recentMessageDate = messages.filter { it.senderId == member.id || it.senderId == vm.loggedMember.id}
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
                    .align(Alignment.Center)
                    .size(50.dp)
                    .clickable {
                        navController.navigate("Chat/${chat.id}") {
                            launchSingleTop = true
                        }
                    },
            )
            val unreadCount =
                vm.getUnreadMessagesUserDB(messages)//vm.chat.messages.filter { it.status == messageStatus.UNREAD && it.senderId == member.id}.size
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

fun formatMessageDate(dateTime : LocalDateTime) : String {
    val today = LocalDate.now()
    val messageDate = dateTime.toLocalDate()

    return when {
        ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now()) <= 5 -> "Now"
        ChronoUnit.HOURS.between(dateTime, LocalDateTime.now()) <= 1 -> "Last hour"
        ChronoUnit.DAYS.between(messageDate, today) == 0L -> "Today"
        ChronoUnit.DAYS.between(messageDate, today) == 1L -> "Yesterday"
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}