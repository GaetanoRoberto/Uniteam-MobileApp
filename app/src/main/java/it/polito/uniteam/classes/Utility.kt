package it.polito.uniteam.classes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.UUID


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

enum class CategoryRole {
    NONE, RESEARCHER, WRITER, PRESENTER, ORGANIZER, PROGRAMMER, DESIGNER, EXPERIMENTER, EMPLOYEE, ADMINISTRATOR
}

fun String.isRepetition(): Boolean{
    if(this.equals("DAILY") || this.equals("WEEKLY") || this.equals("MONTHLY") || this.equals("NONE")){
        return true
    }
    else{
        return false
    }
}

enum class parseReturnType {
    TIME,DATETIME,DATE
}


fun parseToLocalDate(date: Date, returnType: parseReturnType = parseReturnType.DATETIME): Any {
    val calendar = Calendar.getInstance().apply { time = date }

    return when (returnType) {
        parseReturnType.TIME -> {
            Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ISO_LOCAL_TIME).slice(IntRange(0,4))
        }
        parseReturnType.DATETIME -> {
            Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
        parseReturnType.DATE -> {
            LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, // Months are 0-based in Calendar
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

fun handleInputString(input: String): String {
    Log.i("trim",input)
    var output = input.trim().replace(Regex("\\n+"), "\n\n")
    Log.i("trim",output)
    output = output.trim('\n')
    Log.i("trim",output)
    return output
}

fun CompressImage(context: Context, sourceUri: Uri, maxFileSizeKB: Int = 500): Uri {
    val inputStream = context.contentResolver.openInputStream(sourceUri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    var quality = 100
    var streamLength: Int
    val byteArrayOutputStream = ByteArrayOutputStream()

    do {
        byteArrayOutputStream.reset()
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        streamLength = byteArray.size
        quality -= 5
    } while (streamLength > maxFileSizeKB * 1024 && quality > 0)

    // Create a new file to save the compressed image
    val compressedFile = File(context.cacheDir, UUID.randomUUID().toString())
    val outputStream = FileOutputStream(compressedFile)
    outputStream.use {
        it.write(byteArrayOutputStream.toByteArray())
    }

    // Return the URI of the compressed file
    return Uri.fromFile(compressedFile)
}

@Composable
fun MemberIcon(modifierScale: Modifier = Modifier.scale(0.8f), modifierPadding: Modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp), member: MemberDBFinal, enableNavigation: Boolean = true, dialogAction: () -> Unit = {}, isLoggedMember: Boolean = false) {
    val navController = NavControllerManager.getNavController()

    Box(modifier = modifierScale) {
        if (member.profileImage != Uri.EMPTY) {
            Image(
                painter = rememberAsyncImagePainter(member.profileImage),
                contentDescription = null,
                modifier = if (enableNavigation) Modifier
                    .then(modifierPadding)
                    .scale(2f)
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = { if(!isLoggedMember) { dialogAction(); navController.navigate("OtherUserProfile/${member.id}"); } else { dialogAction(); navController.navigate("Profile")} })
                else
                    Modifier
                        .then(modifierPadding)
                        .scale(2f)
                        .size(22.dp)
                        .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = member.fullName.trim().split(' ');
            var initialsValue = initials
                .mapNotNull { it.firstOrNull()?.toString() }
                .first().uppercase();
            if (initials.size >= 2) {
                initialsValue += initials
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .last().uppercase()
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
                    .clickable(onClick = { if(!isLoggedMember) { dialogAction(); navController.navigate("OtherUserProfile/${member.id}"); } else { dialogAction(); navController.navigate("Profile")} })
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
fun TeamIcon(
    team: TeamDBFinal,
    modifierPadding: Modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp),
    modifierScale: Modifier = if (team.image != Uri.EMPTY) Modifier.scale(1.7f) else Modifier.scale(0.8f)
) {
    val navController = NavControllerManager.getNavController()

    Box(modifier = modifierScale) {
        if (team.image != Uri.EMPTY) {
            Image(
                painter = rememberAsyncImagePainter(team.image),
                contentDescription = null,
                modifier = Modifier
                    .then(modifierPadding)
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable { navController.navigate("TeamDetails/${team.id}") },
                contentScale = ContentScale.Crop
            )
        } else {
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
                    .clickable { navController.navigate("TeamDetails/${team.id}") }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.people),
                    contentDescription = "Image",
                    modifier = Modifier
                        //.padding(40.dp, 0.dp, 40.dp, 0.dp)
                        .scale(1.5f)
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
    errorMsg: MutableState<String>,
    hoursCallback: (hours:String) -> Unit = {},
    minutesCallback: (minutes:String) -> Unit = {}
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
                    hoursCallback(value)
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
                    minutesCallback(value)
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

@Preview
@Composable
fun LoadingSpinner() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
        Spacer(modifier = Modifier.padding(5.dp))
        Text(text = "Loading...", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(modifier = Modifier.padding(5.dp))
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize(0.2f),
            color = Color.White,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Preview
@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    text: String = "Log in with Google",
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .height(48.dp)
            .width(250.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp
            )
        }
    }
}