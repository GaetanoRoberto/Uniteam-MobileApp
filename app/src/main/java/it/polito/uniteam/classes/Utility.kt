package it.polito.uniteam.classes

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import it.polito.uniteam.ui.theme.Orange
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow


enum class Repetition{
    DAILY, WEEKLY, MONTHLY, NONE

}

enum class Status{
    TODO, IN_PROGRESS, COMPLETED
}

enum class Priority{
    LOW, MEDIUM, HIGH
}

enum class Category{
    NONE,RESEARCH, WRITING, PRESENTATION, STUDY, MEETING, PROGRAMMING, DESIGN, EXPERIMENTATION, WORK, ADMINISTRATION
}

fun String.isRepetition(): Boolean{
    if(this.equals("DAILY") || this.equals("WEEKLY") || this.equals("MONTHLY") || this.equals("NONE")){
        return true
    }
    else{
        return false
    }
}


@Composable
fun MemberIcon(modifierScale: Modifier = Modifier.scale(0.8f), modifierPadding: Modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp), member: Member) {
    Box(modifier = modifierScale) {
        if (member.profileImage != Uri.EMPTY) {
            Image(
                painter = rememberAsyncImagePainter(member.profileImage),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = member.fullName.trim().split(' ');
            var initialsValue = initials
                .mapNotNull { it.firstOrNull()?.toString() }
                .first();
            if (initials.size >= 2) {
                initialsValue += initials
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .last()
            }
            Box(
                modifier = Modifier
                    .then(modifierPadding)
                    .size(22.dp)
                    .drawBehind {
                        drawCircle(
                            color = Orange,
                            radius = this.size.maxDimension
                        )
                    }
            ) {
                Text(
                    text = initialsValue,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
fun TextTrim(inputText: String, desiredLength: Int, modifier: Modifier = Modifier, style: TextStyle = LocalTextStyle.current) {
    val text = if (inputText.length > desiredLength) {
         inputText.substring(0, desiredLength) + "..."
    } else {
        inputText
    }
    Text(text = text, modifier = modifier, style = style)
}


@Composable
fun HourMinutesPicker(
    hourState: MutableState<String>,
    minuteState: MutableState<String>,
    errorMsg: MutableState<String>
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                label = { Text(text = "Hours")},
                value = hourState.value,
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                onValueChange = { value ->
                    hourState.value = value
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                label = { Text(text = "Minutes")},
                value = minuteState.value,
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                onValueChange = { value ->
                    minuteState.value = value
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            if (errorMsg.value.isNotEmpty())
                Text(errorMsg.value, color = MaterialTheme.colorScheme.error)
        }
    }
}