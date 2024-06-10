package it.polito.uniteam.gui.chat
import android.net.Uri
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.Factory
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
fun ChatScreen( vm : ChatViewModel = viewModel(factory = Factory(LocalContext.current)),teams: List<TeamDBFinal>,members: List<MemberDBFinal>,messages: List<MessageDB>,chats: List<ChatDBFinal>    ) {
    val chatId = vm.chatId

    val team = teams.find { it.chat == chatId }
    val chat = chats.find { it.id == chatId }!!
    val chatMessages = messages.filter { chat.messages.contains(it.id) }.sortedBy { it.creationDate }
    val membersChat = if (team != null) {
        // If teamId is defined, get all members of the team
        members.filter { team.members.contains(it.id) }
    } else {
        // If it is a direct chat, get both the receiver and sender of the chat
        val sender = chat.sender
        val receiver = chat.receiver

        members.filter { it.id == sender || it.id == receiver }

    }
    Column(modifier = Modifier.fillMaxSize()) {
            ChatHeader(vm,team,chat,membersChat)
            ChatBody(vm = vm,chat,chatMessages,membersChat)
            SendMessage(vm = vm,team)
        }
}

@Composable
fun ChatBody(
    vm: ChatViewModel,
    chat: ChatDBFinal,
    messages: List<MessageDB>,
    membersChat: List<MemberDBFinal>
){
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isVertical()) (screenHeightDp * 0.60).dp else (screenHeightDp * 0.45).dp)
            .background(
                MaterialTheme.colorScheme.secondary, RoundedCornerShape(
                    topStart = 30.dp, topEnd = 30.dp
                )
            )
        //.padding(top = 5.dp)

    ) {
        key(messages.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
                    //.border(BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary))
            ) {
                if (messages.isEmpty())
                    EmptyChat()
                else{
                    messages.forEach { message ->
                        if (chat.teamId != null)
                            ChatRowTeam(message = message, vm, membersChat)
                        else
                            ChatRowDirect(message = message, vm)
                    }
                }

            }
        }
    }
}
@Composable
fun EmptyChat(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Center
    ) {
        Text(
            text = "No messages yet",
            style = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp
            )
        )
    }
}
@Composable
fun ChatRowDirect(
    message: MessageDB,
    vm: ChatViewModel
) {
    val loggedMember = vm.loggedMember
    val isSender = message.senderId== loggedMember.id
    val alignment = if (isSender) Alignment.End else Alignment.Start
    if (message.senderId != loggedMember.id)
        vm.markUserMessageAsRead( message)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(modifier = Modifier.padding(all = 8.dp)) {
                Box(
                    modifier = Modifier
                        .background(
                            if (message.senderId == loggedMember.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Center
                ) {
                    Text(
                        text = message.message, style = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 25.dp),//cambia grandezza card messaggio
                        textAlign = TextAlign.Start
                    )
                }
            }
            Text(
                text = formatMessageDate(message.creationDate),//.format(DateTimeFormatter.ofPattern("dd MMM Y HH:mm")),
                style = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 15.dp),
            )

        }
}

@Composable
fun ChatRowTeam(
    message: MessageDB,
    vm: ChatViewModel,
    membersChat: List<MemberDBFinal>
) {

    val member = membersChat.find { it.id == message.senderId }
    Log.d("ChatScreen", "membersChat: $membersChat")

    //val member = vm.getMemberById(message.senderId)
    val loggedMember = vm.loggedMember
    val isSender = message.senderId == loggedMember.id

    val alignment = if (isSender) Alignment.End else Alignment.Start
    vm.markTeamMessageAsRead(loggedMember.id, message)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(modifier = Modifier.padding(all = 8.dp)) {
            if (!isSender){
                if (member != null) {
                    Log.d("ChatScreen", "Member: $member")
                    MemberIcon(modifierScale= Modifier.scale(0.9f), modifierPadding = Modifier.padding(4.dp, 12.dp, 15.dp, 0.dp),member = member)
                }
            }
            Column {
                if (!isSender){
                    Text(text = member?.username ?: "Unknown", style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ))
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (message.senderId == loggedMember.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Center
                ) {
                    Text(
                        text = message.message, style = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 25.dp),//cambia grandezza card messaggio
                        textAlign = TextAlign.Start
                    )
                }
            }
        }//Fine row
        Text(
            text = formatMessageDate(message.creationDate),//.format(DateTimeFormatter.ofPattern("dd MMM Y HH:mm")),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 15.dp),
        )
    }


}
@Composable
fun ChatHeader(
    vm : ChatViewModel,
    team: TeamDBFinal?,
    chat: ChatDBFinal,
    membersChat: List<MemberDBFinal>
) {
    val teamName = team?.name
    val receiver = membersChat.find { (it.id == chat.receiver || it.id == chat.sender)  && it.id != vm.loggedMember.id}
    Log.d("ChatScreen",vm.loggedMember.id)
    Log.d("ChatScreen", "membersChat: ${membersChat.find { it.id == chat.receiver || it.id == chat.sender  && it.id != vm.loggedMember.id}}")
    val text = if (chat.teamId != null) teamName else receiver?.username

    Row(
        modifier = Modifier
            .fillMaxWidth()
            //.height((LocalConfiguration.current.screenHeightDp * 0.08).dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (team == null) {
                if (receiver != null) {
                    MemberIcon(member = receiver)
                }

        } else {
                TeamIcon(
                    team = team,
                    modifierPadding = if(team.image != Uri.EMPTY) Modifier.padding(6.dp, 4.dp, 12.dp, 7.dp) else Modifier.padding(4.dp, 0.dp, 12.dp, 0.dp),
                    modifierScale = if(team.image != Uri.EMPTY) Modifier.scale(2f) else Modifier.scale(1f))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text =  text ?: "Unknown",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
fun SendMessage(
    vm: ChatViewModel,
    team: TeamDBFinal?
) {
    val focusManager = LocalFocusManager.current
    var messageText by remember { mutableStateOf("") }
    val loggedMember = vm.loggedMember
    Row(
        modifier = Modifier
            .fillMaxWidth()
            //.align(Alignment.BottomCenter)
            //.padding(bottom = 2.dp)
            .background(MaterialTheme.colorScheme.secondary),
        verticalAlignment = Alignment.CenterVertically
            ) {
        OutlinedTextField(
            label = { Text(text = "Type a message") },
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier
                .height(if (isVertical()) (LocalConfiguration.current.screenHeightDp * 0.15).dp else (LocalConfiguration.current.screenHeightDp * 0.3).dp)
                .weight(1f),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            if(team != null)
                                vm.addTeamMessage(loggedMember.id, messageText,team.members)
                            else
                                vm.addMessage(loggedMember.id, messageText)


                            messageText = ""
                            focusManager.clearFocus()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send Icon"
                    )
                }
            }
        )
    }
}
fun formatMessageDate(dateTime: LocalDateTime): String {
    val today = LocalDate.now()
    val messageDate = dateTime.toLocalDate()

    return when {
        // Se il messaggio è stato inviato oggi, mostra solo l'orario
        ChronoUnit.DAYS.between(messageDate, today) == 0L -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        // Se il messaggio è stato inviato ieri, mostra "Yesterday"
        ChronoUnit.DAYS.between(messageDate, today) == 1L -> "Yesterday"
        // Altrimenti, mostra la data completa
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
    }
}




