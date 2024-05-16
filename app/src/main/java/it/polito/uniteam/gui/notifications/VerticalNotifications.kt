package it.polito.uniteam.gui.notifications

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory
import it.polito.uniteam.classes.TextTrim
import it.polito.uniteam.isVertical

@Composable
fun Notifications(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    if (isVertical()) {
        VerticalNotificationsContainer(vm = vm)
    }
}

@Composable
fun VerticalNotificationsContainer(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val icons = listOf(Icons.Filled.Comment, Icons.Filled.Info)
    val titles = notificationsSection.entries.map { it.toString() }
    Column {
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
        if(vm.selectedSection == notificationsSection.MESSAGES) {
            VerticalMessagesSection(vm = vm)
        } else {
            VerticalActivitiesSection(vm = vm)
        }
    }
}

@Composable
fun VerticalMessagesSection(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    LazyColumn {
        item(vm.teamsMessages) {
            vm.teamsMessages.forEachIndexed { index, message ->
                if (index % 2 == 0)
                    MessageItem(teamMemberName = vm.teamsHistories[index].first.name, nOfMessages = 35)
                else
                    MessageItem(teamMemberName = vm.teamsHistories[index].first.name + " - " + vm.teamsHistories[index].first.members.get(0).fullName, nOfMessages = 1)
            }
        }
    }
}

@Composable
fun MessageItem(teamMemberName: String, nOfMessages: Int) {
    Row(
        modifier = Modifier
            .clickable { Log.i("diooo","reg") }
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
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
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
fun VerticalActivitiesSection(vm: NotificationsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    LazyColumn {
        items(vm.teamsHistories) { TeamHistory ->
            TeamHistory.second.forEach { history ->
                ActivityItem(teamName = TeamHistory.first.name, Activity = history.comment)
            }
        }
    }
}

@Composable
fun ActivityItem(teamName: String, Activity: String) {
    Row(
        modifier = Modifier
            .clickable { Log.i("diooo","reg") }
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