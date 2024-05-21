package it.polito.uniteam.gui.chat
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory
import it.polito.uniteam.R
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Chat
import it.polito.uniteam.classes.MemberIcon
import it.polito.uniteam.classes.Message
import it.polito.uniteam.classes.TeamIcon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService

@Composable
fun ChatScreen( vm : ChatViewModel = viewModel(factory = Factory(LocalContext.current))) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            ChatHeader(vm = vm)
            ChatBody(vm = vm)
            SendMessage(vm = vm)
        }
}

@Composable
fun ChatBody(vm: ChatViewModel){
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeightDp * 0.57).dp)
            .background(
                MaterialTheme.colorScheme.secondary, RoundedCornerShape(
                    topStart = 30.dp, topEnd = 30.dp
                )
            )
        //.padding(top = 5.dp)

    ) {
        key(vm.messages.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE))
                    //.border(BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary))
            ) {
                vm.messages.forEach { message ->
                    if (vm.chat.teamId != null)
                        ChatRowTeam(message = message, vm)
                    else
                        //ChatRowTeam(message = message, vm)
                        ChatRowDirect(message = message, vm)
                }
            }
        }
    }
}
@Composable
fun ChatRowDirect(
    message: Message,
    vm: ChatViewModel
) {
    val loggedMember = vm.getLoggedMember()
    val isSender = message.senderId == loggedMember?.id
    val alignment = if (isSender) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(modifier = Modifier.padding(all = 8.dp)) {
                Box(
                    modifier = Modifier
                        .background(
                            if (message.senderId == loggedMember?.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
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
                text = message.creationDate.format(DateTimeFormatter.ofPattern("dd MMM Y HH:mm")), style = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 15.dp),
            )

        }
}

@Composable
fun ChatRowTeam(
    message: Message,
    vm: ChatViewModel
) {
    val member = vm.getMemberById(message.senderId)
    val loggedMember = vm.getLoggedMember()
    val isSender = message.senderId == loggedMember?.id

    val alignment = if (isSender) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(modifier = Modifier.padding(all = 8.dp)) {
            if (!isSender){
                if (member != null) {
                    MemberIcon(modifierScale= Modifier.scale(0.9f), modifierPadding = Modifier.padding(4.dp, 12.dp, 15.dp, 0.dp),member = member )
                }
            }
            Column {
                if (!isSender){
                    Text(text = member?.fullName ?: "Unknown", style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ))
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (message.senderId == loggedMember?.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
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
            text = message.creationDate.format(DateTimeFormatter.ofPattern("dd MMM Y HH:mm")),
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
    vm: ChatViewModel
) {

    val text = if (vm.chat.teamId != null) vm.teamName else vm.chat.receiver?.fullName
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height((LocalConfiguration.current.screenHeightDp * 0.08).dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (vm.chat.teamId == null) {
            vm.chat.receiver?.let { MemberIcon(member = it) }
        } else {
            TeamIcon(team = vm.getTeam(vm.chat.teamId))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text =  text ?: "Unknown",
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
    vm: ChatViewModel
) {
    val focusManager = LocalFocusManager.current
    var messageText by remember { mutableStateOf("") }
    val loggedMember = vm.getLoggedMember()
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
                .height((LocalConfiguration.current.screenHeightDp * 0.15).dp)
                .weight(1f),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            vm.addMessage(
                                Message(
                                    id = vm.messages.size + 1,
                                    senderId = loggedMember!!.id,
                                    message = messageText,
                                    creationDate = LocalDateTime.now()
                                )
                            )
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





