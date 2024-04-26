package it.polito.uniteam.gui.calendar

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import it.polito.uniteam.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.stream.Collectors
import java.util.stream.Stream

class Calendar : ViewModel() {
    val today: LocalDate
        get() {
            return LocalDate.now()
        }


    fun getData(startDate: LocalDate = today, lastSelectedDate: LocalDate): CalendarUiModel {
        val firstDayOfWeek = startDate.with(DayOfWeek.MONDAY)
        val endDayOfWeek = firstDayOfWeek.plusDays(7)
        val visibleDates = getDatesBetween(firstDayOfWeek, endDayOfWeek)
        return toUiModel(visibleDates, lastSelectedDate)
    }

    private fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val numOfDays = ChronoUnit.DAYS.between(startDate, endDate)
        return Stream.iterate(startDate) { date ->
            date.plusDays(/* daysToAdd = */ 1)
        }
            .limit(numOfDays)
            .collect(Collectors.toList())
    }

    private fun toUiModel(
        dateList: List<LocalDate>,
        lastSelectedDate: LocalDate
    ): CalendarUiModel {
        return CalendarUiModel(
            selectedDate = toItemUiModel(lastSelectedDate, true),
            visibleDates = dateList.map {
                toItemUiModel(it, it.isEqual(lastSelectedDate))
            },
        )
    }

    private fun toItemUiModel(date: LocalDate, isSelectedDate: Boolean) = CalendarUiModel.Date(
        isSelected = isSelectedDate,
        isToday = date.isEqual(today),
        date = date,
    )

}
@Preview(showSystemUi = true)
@Composable
fun CalendarAppPreview() {
    CalendarApp(
        modifier = Modifier.padding(1.dp)
    )
}

@Composable
fun CalendarApp(modifier: Modifier = Modifier) {
    val dataSource = Calendar()
    // get CalendarUiModel from CalendarDataSource, and the lastSelectedDate is Today.
    var calendarUiModel by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {//vertical
            Column(
                modifier = modifier
                    .fillMaxSize()

            ) {
                Header(startDate = calendarUiModel.startDate,endDate = calendarUiModel.endDate,
                    onPrevClickListener = { startDate ->
                        // refresh the CalendarUiModel with new data
                        // by get data with new Start Date (which is the startDate-1 from the visibleDates)
                        val finalStartDate = startDate.minusDays(1)
                        calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)
                    },
                    onNextClickListener = { endDate ->
                        // refresh the CalendarUiModel with new data
                        // by get data with new Start Date (which is the endDate+2 from the visibleDates)
                        val finalStartDate = endDate.plusDays(2)
                        calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)
                    },
                    onTodayClickListener = {
                        val finalStartDate = calendarUiModel.selectedDate.date
                        calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)

                    }

                )
                //VERTICALE
                VerticalDayEventScheduler(data = calendarUiModel)
                VerticalTasksToAssign()
        }
        }else{//orizzontale
            Column(
                modifier = modifier
                    .fillMaxSize()
            ) {
            Header(startDate = calendarUiModel.startDate,endDate = calendarUiModel.endDate,
                onPrevClickListener = { startDate ->
                    // refresh the CalendarUiModel with new data
                    // by get data with new Start Date (which is the startDate-1 from the visibleDates)
                    val finalStartDate = startDate.minusDays(1)
                    calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)
                },
                onNextClickListener = { endDate ->
                    // refresh the CalendarUiModel with new data
                    // by get data with new Start Date (which is the endDate+2 from the visibleDates)
                    val finalStartDate = endDate.plusDays(2)
                    calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)
                },
                onTodayClickListener = {
                    val finalStartDate = calendarUiModel.selectedDate.date
                    calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)

                }

            )
            HorizontalDayEventScheduler(data = calendarUiModel)

        }
        }}


    }

//OGGETTO TASK
@Composable
fun EventItem() {
    Column(
        modifier = Modifier
            //.fillMaxWidth()
            .width(102.dp)
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp)),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text("Task #1", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))  // Usa il peso per spingere il testo a destra
            Text("4h",
                style = MaterialTheme.typography.bodyLarge.copy(
                    //fontWeight = FontWeight.Bold,  // Testo in grassetto
                    color = MaterialTheme.colorScheme.primary // Cambio colore per maggiore visibilità
                ))
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.user_icon), // Assicurati di avere questa risorsa o sostituiscila
                contentDescription = "Profile",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))  // Distanziamento tra le icone
            Icon(
                painter = painterResource(id = R.drawable.user_icon), // Assicurati di avere questa risorsa o sostituiscila
                contentDescription = "Profile",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


//OGGETTO GIORNO DELLA SETTIMANA
@Composable
fun DayItem(date: CalendarUiModel.Date) {
    Card(
        modifier = Modifier
            .padding(vertical = 7.dp, horizontal = 1.dp)
            //.clickable { onClickListener(date) }
            .size(width = 48.dp, height = 68.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (date.isSelected) {
                Color.Magenta
            } else {
                MaterialTheme.colorScheme.primary
            }        ),
    ) {
            Text(
                text = date.day,// Lun Mar
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = date.date.dayOfMonth.toString(),// 15 16
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
    }
}
//RIGA CON GIORNO E TASK
@Composable
fun VerticalDayEventScheduler(data: CalendarUiModel) {
    Box(modifier = Modifier.height(420.dp)) { // Imposta un'altezza fissa e abilita lo scrolling verticale
        LazyColumn {
            items(items = data.visibleDates) { date ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DayItem(date)
                    //Row con i task assegnati per quel giorno
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                            .border(1.dp, Color.Gray),  // Aggiunge un bordo per visibilità
                    ) {
                        items(5) {  // Assumiamo che ci possano essere 5 eventi per giorno
                            EventItem()  // Questo elemento viene ripetuto, dovresti passare i dati reali qui
                        }
                    }
                }
            }
        }
    }
    }

@Composable
fun Header(startDate: CalendarUiModel.Date,endDate: CalendarUiModel.Date,
           onPrevClickListener: (LocalDate) -> Unit,
           onNextClickListener: (LocalDate) -> Unit,
           onTodayClickListener: () -> Unit
    ) {
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {//Verticale
            Column(
            //verticalAlignment = Alignment.CenterVertically
        ) {

            Row {
                Text(
                    text = "Team #1 - Tasks ",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                )
                Spacer(modifier = Modifier.weight(0.4f)) // Spazio flessibile per allineare la checkbox e il testo "My Tasks" alla fine
                Checkbox(
                    checked = true, // Imposta il valore desiderato della checkbox
                    onCheckedChange = { /* Azione quando la checkbox viene selezionata */ }
                )
                Text(
                    text = "My Tasks",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    //modifier = Modifier.padding(start = 4.dp),
                )
            }

            Row {
                Text(
                    text = startDate.date.month.toString() + " " + startDate.date.dayOfMonth + " - " + endDate.date.dayOfMonth + ", " + startDate.date.year,// " MAY, 22 - 28  (2024)",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                )
                IconButton(onClick = { onPrevClickListener(startDate.date) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Previous"
                    )
                }
                IconButton(onClick = { onNextClickListener(endDate.date) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Next"
                    )
                }
                Button(onClick = onTodayClickListener) {
                    Text("Today")
                }
            }

        }
    }else{//orizzontale
                Row {
                    Text(
                        text = startDate.date.month.toString() + " " + startDate.date.dayOfMonth + " - " + endDate.date.dayOfMonth + ", " + startDate.date.year,// " MAY, 22 - 28  (2024)",
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                    )
                    Checkbox(
                        checked = true, // Imposta il valore desiderato della checkbox
                        onCheckedChange = { /* Azione quando la checkbox viene selezionata */ }
                    )
                    Text(
                        text = "My Tasks",
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        //modifier = Modifier.padding(start = 4.dp),
                    )
                    IconButton(onClick = { onPrevClickListener(startDate.date) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Previous"
                        )
                    }
                    IconButton(onClick = { onNextClickListener(endDate.date) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Next"
                        )
                    }
                    Button(onClick = onTodayClickListener) {
                        Text("Today")
                    }
                }

            }
        }
}

@Composable
fun VerticalTasksToAssign() {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Tasks to complete",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
                LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    //.border(1.dp, Color.Gray),  // Aggiunge un bordo per visibilità
                ) {
                    items(5) {  // Assumiamo che ci possano essere 5 eventi per giorno
                        EventItem()  // Questo elemento viene ripetuto, dovresti passare i dati reali qui
                    }
                }
            }

        }
    }
@Composable
fun HorizontalDayEventScheduler(data: CalendarUiModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Colonna sinistra con le date e gli eventi
        Box(
            modifier = Modifier
                .weight(2.5f)//modifico questo per cambiare quanto è stretta la colonna di destra

        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = data.visibleDates) { date ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DayItem(date)
                        // Row con i task assegnati per quel giorno
                        LazyRow(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                                .border(1.dp, Color.Gray)  // Aggiunge un bordo per visibilità
                        ) {
                            items(5) { // Assumiamo che ci possano essere 5 eventi per giorno
                                EventItem() // Questo elemento viene ripetuto, dovresti passare i dati reali qui
                            }
                        }
                    }
                }
            }
        }

        // Colonna destra con "Your Tasks to complete"
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "Your Tasks to complete",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            ) {
                items(5) { index -> // Sostituisci il 3 con il numero corretto di righe necessarie
                    Row(
                        //horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EventItem() // Primo EventItem della riga
                        EventItem() // Secondo EventItem della riga
                    }
                }
            }
        }
    }
}

