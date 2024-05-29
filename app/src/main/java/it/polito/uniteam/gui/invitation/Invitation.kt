package it.polito.uniteam.gui.invitation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.DotShape
import com.lightspark.composeqr.QrCodeColors
import com.lightspark.composeqr.QrCodeView
import it.polito.uniteam.isVertical


@Composable
fun Invitation(teamId: String, teamName: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val invitationLink = "https://UniTeam/join/$teamId"
    val copied = remember { mutableStateOf(false) }

    if (isVertical()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            //Team name + QR code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invite new members to join the team:\n $teamName",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QrCodeView(
                    data = invitationLink,
                    modifier = Modifier.size(250.dp),
                    colors = QrCodeColors(
                        background = Color.Black,
                        foreground = Color.White
                    ),
                    dotShape = DotShape.Circle
                )
            }
            Spacer(modifier = Modifier.size(32.dp))
            //Buttons to share/copy the link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Or share the invitation link:",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(invitationLink)); copied.value = true },
                    enabled = !copied.value,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFFFF9248)
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    if (copied.value)
                        Icon(
                            Icons.Outlined.Done,
                            contentDescription = "Copy link",
                            modifier = Modifier.size(36.dp)
                        )
                    else
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Copy link",
                            modifier = Modifier.size(36.dp)
                        )
                }
                Spacer(modifier = Modifier.size(32.dp))
                FilledTonalIconButton(
                    onClick = { shareInvitationLink(context, teamName, invitationLink) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Share link",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    } else {
        //Team name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Invite new members to join the team: $teamName",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //QR code
            Column(
                modifier = Modifier.fillMaxHeight().padding(top = 32.dp).weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                QrCodeView(
                    data = invitationLink,
                    modifier = Modifier.size(200.dp),
                    colors = QrCodeColors(
                        background = Color.Black,
                        foreground = Color.White
                    ),
                    dotShape = DotShape.Circle
                )
            }
            Spacer(modifier = Modifier.size(32.dp))
            //Buttons to share/copy the link
            Column(
                modifier = Modifier.fillMaxHeight().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Or share the invitation link:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(invitationLink)); copied.value = true },
                        enabled = !copied.value,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFFFF9248)
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        if (copied.value)
                            Icon(
                                Icons.Outlined.Done,
                                contentDescription = "Copy link",
                                modifier = Modifier.size(36.dp)
                            )
                        else
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = "Copy link",
                                modifier = Modifier.size(36.dp)
                            )
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    FilledTonalIconButton(
                        onClick = { shareInvitationLink(context, teamName, invitationLink) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share link",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

fun shareInvitationLink(context: Context, teamName: String, invitationLink: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Join the team: $teamName using this link: $invitationLink")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}