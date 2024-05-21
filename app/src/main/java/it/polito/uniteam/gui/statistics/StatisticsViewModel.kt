package it.polito.uniteam.gui.statistics

import android.util.Log
import android.util.TypedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel

class StatisticsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    var selectedChart by mutableStateOf(chartType.PLANNED_SPENT_HOURS_RATIO)
        private set

    var selectedChartValue by mutableStateOf("")

    fun changeChart(chart: String) {
        selectedChart = chartType.getValue(chart)
        selectedChartValue = ""
    }

}

// grafico a barre per ogni team member ore pianificate e spese effettivamente
// grafico a torta su totale ore spese per ogni membro
// grafico a torta su totale KPI team KPI di ogni membro
enum class chartType {
    PLANNED_SPENT_HOURS_RATIO {
        override fun toString() = "PLANNED SPENT HOURS RATIO"
    },
    OVERALL_SPENT_HOURS {
        override fun toString() = "OVERALL SPENT HOURS"
    },
    OVERALL_TEAM_KPI {
        override fun toString() = "OVERALL TEAM KPI"
    };
    abstract override fun toString(): String

    companion object {
        fun getValue(value: String): chartType {
            return when(value) {
                PLANNED_SPENT_HOURS_RATIO.toString() -> PLANNED_SPENT_HOURS_RATIO
                OVERALL_SPENT_HOURS.toString() -> OVERALL_SPENT_HOURS
                OVERALL_TEAM_KPI.toString() -> OVERALL_TEAM_KPI
                else -> throw IllegalArgumentException("Wrong String Provided")
            }
        }
    }
}
