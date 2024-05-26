package it.polito.uniteam.gui.statistics

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.components.Legends
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.LegendLabel
import co.yml.charts.common.model.LegendsConfig
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import it.polito.uniteam.Factory
import it.polito.uniteam.isVertical
import kotlin.random.Random

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverallSpentHoursChart(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val memberSpentHours = vm.getOverallSpentHours()
    if(memberSpentHours!=null) {
        val colorPaletteList = vm.colorPaletteSpentHours
        val donutChartData = PieChartData(
            slices = memberSpentHours.entries.mapIndexed { index, entry ->
                PieChartData.Slice(entry.key, entry.value, colorPaletteList[entry.key]!!)
            },
            plotType = PlotType.Donut
        )
        val donutChartConfig = PieChartConfig(
            chartPadding = if(isVertical()) 15 else 30,
            backgroundColor = MaterialTheme.colorScheme.background,
            //labelVisible = true,
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
            legendLabelList = memberSpentHours.entries.mapIndexed { index, entry ->
                LegendLabel(colorPaletteList[entry.key]!!, entry.key)
            },
            gridColumnCount = 2
        )
        Log.i("diooo",legendsConfig.toString())
        key(memberSpentHours) {
            Box(contentAlignment = Alignment.Center) {
                DonutPieChart(
                    modifier = Modifier
                        .fillMaxHeight(0.7f),
                    donutChartData,
                    donutChartConfig,
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
        if(vm.isFiltersApplied()) {
            Row(modifier = Modifier.fillMaxSize(),horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "No Spent Hours Stats Found.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize(),horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "No Spent Hours Stats Yet.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}