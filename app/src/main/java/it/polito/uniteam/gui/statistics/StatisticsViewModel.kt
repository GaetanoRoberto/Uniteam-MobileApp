package it.polito.uniteam.gui.statistics

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Status
import kotlin.math.roundToInt
import kotlin.random.Random

class StatisticsViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle) : ViewModel() {
    fun getTeam(teamId: Int) = model.getTeam(teamId)
    var selectedChart by mutableStateOf(chartType.PLANNED_SPENT_HOURS_RATIO)
        private set

    var selectedChartValue by mutableStateOf("")

    fun changeChart(chart: String) {
        selectedChart = chartType.getValue(chart)
        selectedChartValue = ""
    }

    inline fun <reified T : Number> pairToNumber(pair: Pair<T,T>) : Float {
        return pair.first.toFloat() + pair.second.toFloat() / 60f
    }

    val teamId: String = checkNotNull(savedStateHandle["teamId"])
    var teamTasks = mutableStateOf(getTeam(teamId.toInt()).tasks.filter { it.status!=Status.TODO }.toList())
        private set
    var teamMembers = mutableStateOf(getTeam(teamId.toInt()).members.toList())
        private set

    fun getPlannedSpentHoursRatio(): Map<String, Pair<Float, Float>> {
        val memberPlannedSpent = mutableMapOf<String, Pair<Float, Float>>()
        teamTasks.value.forEach { task ->
            val n_members = task.members.size
            task.members.forEach { member ->
                if(teamMembers.value.contains(member)) {
                    if(memberPlannedSpent.contains(member.username)) {
                        val plannedSpent = memberPlannedSpent.remove(member.username)!!
                        val spentTime = task.spentTime.get(member)?: Pair(0f,0f)
                        val sum = Pair(plannedSpent.first,(plannedSpent.second + pairToNumber(spentTime)))
                        memberPlannedSpent.put(member.username,sum)
                    } else {
                        val spentTime = task.spentTime.get(member)?: Pair(0f,0f)
                        memberPlannedSpent.put(member.username,Pair((pairToNumber(task.estimatedTime)/n_members).toFloat(),pairToNumber(spentTime)))
                    }
                }
            }
        }
        return memberPlannedSpent.toMap()
    }

    val colorPaletteSpentHours = if(getOverallSpentHours()!=null) generateDistinctColors(getOverallSpentHours()?.size!!) else listOf()
    val colorPaletteTeamKpi = if(getOverallTeamKPI()!=null) generateDistinctColors(getOverallTeamKPI()?.size!!) else listOf()
    fun generateUniqueRandomColor(): Color {
        val random = Random.Default
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)
        return Color(red, green, blue)
    }

    fun generateDistinctColors(count: Int): List<Color> {
        val colors = mutableListOf<Color>()
        val step = 360 / count
        for (i in 0 until count) {
            val hue = (i * step) % 360
            colors.add(hslToColor(hue.toFloat(), 0.7f, 0.5f))
        }
        return colors
    }

    fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
        val c = (1 - kotlin.math.abs(2 * lightness - 1)) * saturation
        val x = c * (1 - kotlin.math.abs((hue / 60) % 2 - 1))
        val m = lightness - c / 2

        val (r, g, b) = when {
            hue < 60 -> Triple(c, x, 0f)
            hue < 120 -> Triple(x, c, 0f)
            hue < 180 -> Triple(0f, c, x)
            hue < 240 -> Triple(0f, x, c)
            hue < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            red = ((r + m) * 255).roundToInt(),
            green = ((g + m) * 255).roundToInt(),
            blue = ((b + m) * 255).roundToInt()
        )
    }
    fun getOverallSpentHours(): Map<String,Float>? {
        var allSpentHours = 0f
        val overAllSpentHours = mutableMapOf<String,Float>()
        teamTasks.value.forEach { task ->
            task.members.forEach { member ->
                if(teamMembers.value.contains(member)) {
                    allSpentHours+=pairToNumber(task.spentTime.get(member)?:Pair(0,0))
                }
            }
        }
        if(allSpentHours == 0f) {
            return null
        }
        teamTasks.value.forEach { task->
            task.members.forEach { member->
                if(teamMembers.value.contains(member)) {
                    if(overAllSpentHours.contains(member.username)) {
                        val prevSpentHours = overAllSpentHours.remove(member.username)!!
                        val spentHours = task.spentTime.get(member)
                        if (spentHours!=null) {
                            overAllSpentHours.put(member.username,100*(pairToNumber(spentHours)/allSpentHours) + prevSpentHours)
                        } else {
                            overAllSpentHours.put(member.username,prevSpentHours)
                        }
                    } else {
                        val spentHours = task.spentTime.get(member)
                        if (spentHours!=null) {
                            overAllSpentHours.put(member.username,100*(pairToNumber(spentHours)/allSpentHours))
                        } else {
                            overAllSpentHours.put(member.username,0f)
                        }
                    }
                }
            }
        }
        return overAllSpentHours.toMap()
    }

    fun getOverallTeamKPI(): Map<String,Float>? {
        // KPI = n_task_completed
        var allTeamKPI = 0
        val overAllTeamKpi = mutableMapOf<String,Float>()
        teamTasks.value.forEach { task->
            task.members.forEach { member->
                if(teamMembers.value.contains(member)) {
                    allTeamKPI+= if(task.status==Status.COMPLETED) 1 else 0
                }
            }
        }
        if(allTeamKPI == 0) {
            return null
        }
        teamTasks.value.forEach { task->
            task.members.forEach { member->
                if(teamMembers.value.contains(member)) {
                    if(overAllTeamKpi.contains(member.username)) {
                        val prevKPI = overAllTeamKpi.remove(member.username)
                        if(prevKPI!=null) {
                            if(task.status==Status.COMPLETED) {
                                overAllTeamKpi.put(member.username, 100*(1f/allTeamKPI) + prevKPI)
                            } else {
                                overAllTeamKpi.put(member.username, prevKPI)
                            }
                        }
                    } else {
                        if(task.status==Status.COMPLETED) {
                            overAllTeamKpi.put(member.username, 100*(1f/allTeamKPI))
                        } else {
                            overAllTeamKpi.put(member.username, 0f)
                        }
                    }
                }
            }
        }
        return overAllTeamKpi.toMap()
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
