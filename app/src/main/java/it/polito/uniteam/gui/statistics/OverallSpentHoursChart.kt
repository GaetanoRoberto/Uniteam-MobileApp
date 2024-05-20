package it.polito.uniteam.gui.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.components.Legends
import co.yml.charts.common.model.LegendLabel
import co.yml.charts.common.model.LegendsConfig
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import it.polito.uniteam.Factory

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverallSpentHoursChart(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val colorPaletteList = listOf(Color(0xFF5F0A87),Color(0xFF20BF55),Color(0xFFEC9F05),Color(0xFFF53844))
    val donutChartData = PieChartData(
        slices = listOf(
            PieChartData.Slice("HP", 15f, colorPaletteList[0]),
            PieChartData.Slice("Dell", 30f, colorPaletteList[1]),
            PieChartData.Slice("Lenovo", 40f, colorPaletteList[2]),
            PieChartData.Slice("Asus", 15f, colorPaletteList[3])
        ),
        plotType = PlotType.Donut
    )
    val donutChartConfig = PieChartConfig(
        chartPadding = 15,
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
        legendLabelList = listOf(
            LegendLabel(colorPaletteList[1],"Dell"),
            LegendLabel(colorPaletteList[0],"HP"),
            LegendLabel(colorPaletteList[2],"Lenovo"),
            LegendLabel(colorPaletteList[3],"Asus"),
            LegendLabel(colorPaletteList[1],"Dell"),
            LegendLabel(colorPaletteList[0],"HP"),
            LegendLabel(colorPaletteList[2],"Lenovo"),
            LegendLabel(colorPaletteList[3],"Asus"),
            LegendLabel(colorPaletteList[1],"Dell"),
            LegendLabel(colorPaletteList[0],"HP"),
            LegendLabel(colorPaletteList[2],"Lenovo"),
            LegendLabel(colorPaletteList[3],"Asus"),
            LegendLabel(colorPaletteList[1],"Dell"),
            LegendLabel(colorPaletteList[0],"HP"),
            LegendLabel(colorPaletteList[2],"Lenovo"),
            LegendLabel(colorPaletteList[3],"Asus")
        ),
        gridColumnCount = 2
    )

    Box(contentAlignment = Alignment.Center) {
        DonutPieChart(
            modifier = Modifier
                .fillMaxHeight(0.7f),
            donutChartData,
            donutChartConfig,
            onSliceClick = {
                if(vm.selectedChartValue == "${it.label}: ${it.value} %") {
                    vm.selectedChartValue = ""
                } else {
                    vm.selectedChartValue = "${it.label}: ${it.value} %"
                }
            }
        )
        Text(text = vm.selectedChartValue, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
    }
    Legends(legendsConfig = legendsConfig)
}