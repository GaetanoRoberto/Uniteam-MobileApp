package it.polito.uniteam.classes

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    RESEARCH, WRITING, PRESENTATION, STUDY, MEETING, PROGRAMMING, DESIGN, EXPERIMENTATION, WORK, ADMINISTRATION
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
                    modifier = Modifier.align(Alignment.Center)
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
