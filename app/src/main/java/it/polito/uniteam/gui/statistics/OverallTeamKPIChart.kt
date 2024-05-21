package it.polito.uniteam.gui.statistics

import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.components.Legends
import co.yml.charts.common.model.LegendLabel
import co.yml.charts.common.model.LegendsConfig
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import it.polito.uniteam.Factory

@Composable
fun OverallTeamKPIChart(vm: StatisticsViewModel = viewModel(factory = Factory(LocalContext.current))) {
    val colorPaletteList =
        listOf(Color(0xFF5F0A87), Color(0xFF20BF55), Color(0xFFEC9F05), Color(0xFFF53844))
    val pieChartData = PieChartData(
        slices = listOf(
            PieChartData.Slice("SciFi", 65f, Color(0xFF333333)),
            PieChartData.Slice("Comedy", 35f, Color(0xFF666a86)),
            PieChartData.Slice("Drama", 10f, Color(0xFF95B8D1)),
            PieChartData.Slice("Romance", 40f, Color(0xFFF53844))
        ),
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
        legendLabelList = listOf(
            LegendLabel(colorPaletteList[1], "Dell"),
            LegendLabel(colorPaletteList[0], "HP"),
            LegendLabel(colorPaletteList[2], "Lenovo"),
            LegendLabel(colorPaletteList[3], "Asus"),
            LegendLabel(colorPaletteList[1], "Dell"),
            LegendLabel(colorPaletteList[0], "HP"),
            LegendLabel(colorPaletteList[2], "Lenovo"),
            LegendLabel(colorPaletteList[3], "Asus"),
            LegendLabel(colorPaletteList[1], "Dell"),
            LegendLabel(colorPaletteList[0], "HP"),
            LegendLabel(colorPaletteList[2], "Lenovo"),
            LegendLabel(colorPaletteList[3], "Asus"),
            LegendLabel(colorPaletteList[1], "Dell"),
            LegendLabel(colorPaletteList[0], "HP"),
            LegendLabel(colorPaletteList[2], "Lenovo"),
            LegendLabel(colorPaletteList[3], "Asus")
        ),
        gridColumnCount = 2
    )

    Box(contentAlignment = Alignment.Center) {
        PieChart(
            modifier = Modifier
                .fillMaxHeight(0.7f),
            pieChartData,
            pieChartConfig,
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
