package it.polito.uniteam.gui.statistics

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.components.Legends
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.LegendLabel
import co.yml.charts.common.model.LegendsConfig
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import it.polito.uniteam.Factory

@Composable
fun OverallTeamKPIChart(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val memberTeamKpi = vm.getOverallTeamKPI()
    if (memberTeamKpi != null) {
        val colorPaletteList = vm.colorPaletteTeamKpi
        val pieChartData = PieChartData(
            slices = memberTeamKpi.entries.mapIndexed { index, entry ->
                PieChartData.Slice(entry.key, entry.value, colorPaletteList[entry.key]!!)
            },
            plotType = PlotType.Pie
        )
        val pieChartConfig = PieChartConfig(
            chartPadding = 15,
            backgroundColor = MaterialTheme.colorScheme.background,
            showSliceLabels = false,
            labelVisible = true,
            labelColor = Color.White,
            //percentVisible = true,
            //percentageFontSize = 42.sp,
            strokeWidth = 120f,
            //isSumVisible = true,
            //percentColor = Color.Black,
            activeSliceAlpha = .9f,
            isAnimationEnable = true
        )
        val legendsConfig = LegendsConfig(
            legendLabelList = memberTeamKpi.entries.mapIndexed { index, entry ->
                LegendLabel(colorPaletteList[entry.key]!!, entry.key)
            },
            gridColumnCount = 2
        )

        key(memberTeamKpi) {
            Box(contentAlignment = Alignment.Center) {
                PieChart(
                    modifier = Modifier
                        .fillMaxHeight(0.7f).aspectRatio(1f),
                    pieChartData,
                    pieChartConfig,
                    onSliceClick = {
                        if (vm.selectedChartValue == "${it.label}: ${it.value.formatToSinglePrecision()} %") {
                            vm.selectedChartValue = ""
                        } else {
                            vm.selectedChartValue =
                                "${it.label}: ${it.value.formatToSinglePrecision()} %"
                        }
                    }
                )
                Text(
                    text = vm.selectedChartValue,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (legendsConfig.legendLabelList.size > 1) {
                Legends(legendsConfig = legendsConfig)
            } else {
                SingleLegend(config = legendsConfig, legendLabel = legendsConfig.legendLabelList[0])
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxSize(),horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "No KPI Stats Yet.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
