package it.polito.uniteam.gui.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.uniteam.Factory
import it.polito.uniteam.R
import it.polito.uniteam.gui.showtaskdetails.Demo_ExposedDropdownMenuBox


@Composable
fun Statistics(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Statistics", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(10.dp,5.dp,0.dp,0.dp))
            IconButton(
                onClick = {  },
                modifier = Modifier
                    .scale(1.5f)
                    .padding(0.dp, 5.dp, 10.dp, 0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.filters),
                    contentDescription = "Access filters",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(15.dp))
            Demo_ExposedDropdownMenuBox(
                label = "Chart Type:",
                currentValue = vm.selectedChart.toString(),
                values = chartType.entries.map { it.toString() },
                onChange = vm::changeChart
            )
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                when(vm.selectedChart){
                    chartType.PLANNED_SPENT_HOURS_RATIO -> {
                        Text(text = "Hold on a Chart Bar to see details")
                        BarChart(vm = vm)
                    }
                    chartType.OVERALL_SPENT_HOURS -> {
                        if(vm.getOverallSpentHours()!=null)
                            Text(text = "Click on the Chart to see/hide details")
                        OverallSpentHoursChart(vm = vm)
                    }
                    chartType.OVERALL_TEAM_KPI -> {
                        if(vm.getOverallTeamKPI()!=null)
                            Text(text = "Click on the Chart to see/hide details")
                        OverallTeamKPIChart(vm = vm)
                    }
                }
            }
        }
    }
}