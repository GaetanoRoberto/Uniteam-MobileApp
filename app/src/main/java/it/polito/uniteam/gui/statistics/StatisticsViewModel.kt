package it.polito.uniteam.gui.statistics

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.MemberDBFinal
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.TaskDBFinal
import java.time.LocalDate
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


    var initialTeamTasks: List<TaskDBFinal> = listOf()//getTeam(teamId.toInt()).tasks.filter { it.status!=Status.TODO }.toList()
    var initialTeamMembers: List<MemberDBFinal> = listOf()// getTeam(teamId.toInt()).members.toList()
    var teamTasks = mutableStateOf(initialTeamTasks.map { it.copy() })
    var teamMembers = mutableStateOf(initialTeamMembers.map { it.copy() })
    // Update teamTasks and teamMembers when initial lists change
    //Stati per la gestione degli ExpandableRow
    val assigneeExpanded = mutableStateOf(false)
    val tasksExpanded = mutableStateOf(false)
    val categoryExpanded = mutableStateOf(false)
    val priorityExpanded = mutableStateOf(false)
    val statusExpanded = mutableStateOf(false)
    val datesExpanded = mutableStateOf(false)
    //Stati per la gestione dei filtri
    var lastAppliedFilters = mutableStateOf<Map<String, Any>>(mapOf())
    val selectedMembers = mutableStateMapOf<MemberDBFinal, Boolean>()
    val selectedTasks = mutableStateMapOf<TaskDBFinal, Boolean>()
    val selectedCategory = mutableStateMapOf<Category, Boolean>()
    val selectedPriority = mutableStateMapOf<Priority, Boolean>()
    val selectedStatus = mutableStateMapOf<Status, Boolean>()
    val selectedStart = mutableStateOf<LocalDate?>(null)
    val selectedEnd = mutableStateOf<LocalDate?>(null)

    fun isFiltersApplied(): Boolean {
        return lastAppliedFilters.value.isNotEmpty() || selectedStart.value!=null || selectedEnd.value!=null
    }
    fun applyTasksFilters(task: TaskDBFinal, lastAppliedFilters: Map<String, Any>): Boolean {
        var keep = true

        val selectedTasks = lastAppliedFilters["selectedTasks"] as? Map<*, *> ?: emptyMap<TaskDBFinal, Boolean>()
        val selectedMembers = lastAppliedFilters["selectedMembers"] as? Map<*, *> ?: emptyMap<MemberDBFinal, Boolean>()
        val selectedCategory = lastAppliedFilters["selectedCategory"] as? Map<*, *> ?: emptyMap<Category, Boolean>()
        val selectedPriority = lastAppliedFilters["selectedPriority"] as? Map<*, *> ?: emptyMap<Priority, Boolean>()
        val selectedStatus = lastAppliedFilters["selectedStatus"] as? Map<*, *> ?: emptyMap<Status, Boolean>()

        if (selectedTasks.isNotEmpty())
            keep = keep && selectedTasks.filterValues{ it as Boolean }.keys.contains(task)
        if (selectedMembers.isNotEmpty()) {
            val selectedMembers2 = selectedMembers as Map<MemberDBFinal, Boolean>
            keep = keep && task.members.any { memberId -> selectedMembers2.any { it.key.id == memberId } }
        }
        if (selectedCategory.isNotEmpty())
            keep = keep && selectedCategory.filterValues{ it as Boolean }.keys.contains(task.category)
        if (selectedPriority.isNotEmpty())
            keep = keep && selectedPriority.filterValues{ it as Boolean }.keys.contains(task.priority)
        if (selectedStatus.isNotEmpty())
            keep = keep && selectedStatus.filterValues{ it as Boolean }.keys.contains(task.status)

        Log.i("prova",keep.toString())
        return keep
    }
    fun applyMembersFilters(member: MemberDBFinal, lastAppliedFilters: Map<String, Any>): Boolean {
        var keep = true

        val selectedMembers = lastAppliedFilters["selectedMembers"] as? Map<*, *> ?: emptyMap<MemberDBFinal, Boolean>()

        if (selectedMembers.isNotEmpty())
            keep = keep && selectedMembers.filterValues{ it as Boolean }.keys.contains(member)

        return keep
    }

    fun getPlannedSpentHoursRatio(): Map<String, Pair<Float, Float>>? {
        if(teamTasks.value.isEmpty()) {
            return null
        }
        val memberPlannedSpent = mutableMapOf<String, Pair<Float, Float>>()
        teamTasks.value.forEach { task ->
            val n_members = task.members.size
            task.members.forEach { memberId ->
                val member = teamMembers.value.find{ it.id == memberId }
                if(member != null) {
                    if(memberPlannedSpent.contains(member.username)) {
                        val plannedSpent = memberPlannedSpent.remove(member.username)!!
                        val spentTime = task.spentTime.get(member.id)?: Pair(0f,0f)
                        val sum = Pair(plannedSpent.first,(plannedSpent.second + pairToNumber(spentTime)))
                        memberPlannedSpent.put(member.username,sum)
                    } else {
                        val spentTime = task.spentTime.get(member.id)?: Pair(0f,0f)
                        memberPlannedSpent.put(member.username,Pair((pairToNumber(task.estimatedTime)/n_members).toFloat(),pairToNumber(spentTime)))
                    }
                }
            }
        }
        return memberPlannedSpent.toMap()
    }


    var colorPaletteSpentHours = mapOf<String, Color>()
        private set
    fun updateColorPaletteSpentHours(spentHours: Map<String, Float>?) {
        colorPaletteSpentHours = if(spentHours!=null) {
            val colors =generateDistinctColors(spentHours.size)
            spentHours.entries.withIndex().associate { (index,entry) ->
                entry.key to colors[index]
            }
        } else mapOf()
    }

    var colorPaletteTeamKpi = mapOf<String, Color>()
        private set

    fun updateColorPaletteTeamKPI(memberKPI: Map<String, Float>?) {
        colorPaletteTeamKpi = if(memberKPI!=null) {
            val colors =generateDistinctColors(memberKPI.size)
            memberKPI.entries.withIndex().associate { (index,entry) ->
                entry.key to colors[index]
            }
        } else mapOf()
    }

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
            task.members.forEach { memberId ->
                val member = teamMembers.value.find { it.id == memberId }
                if(member != null) {
                    allSpentHours+=pairToNumber(task.spentTime.get(member.id)?:Pair(0,0))
                }
            }
        }
        if(allSpentHours == 0f) {
            return null
        }
        teamTasks.value.forEach { task->
            task.members.forEach { memberId ->
                val member = teamMembers.value.find { it.id == memberId }
                if(member != null) {
                    if(overAllSpentHours.contains(member.username)) {
                        val prevSpentHours = overAllSpentHours.remove(member.username)!!
                        val spentHours = task.spentTime.get(member.id)
                        if (spentHours!=null) {
                            overAllSpentHours.put(member.username,100*(pairToNumber(spentHours)/allSpentHours) + prevSpentHours)
                        } else {
                            overAllSpentHours.put(member.username,prevSpentHours)
                        }
                    } else {
                        val spentHours = task.spentTime.get(member.id)
                        if (spentHours!=null) {
                            overAllSpentHours.put(member.username,100*(pairToNumber(spentHours)/allSpentHours))
                        } else {
                            overAllSpentHours.put(member.username,0f)
                        }
                    }
                }
            }
        }
        updateColorPaletteSpentHours(overAllSpentHours.toMap())
        return overAllSpentHours.toMap()
    }

    fun getOverallTeamKPI(): Map<String,Float>? {
        // KPI = n_task_completed
        var allTeamKPI = 0
        val overAllTeamKpi = mutableMapOf<String,Float>()
        teamTasks.value.forEach { task->
            task.members.forEach { memberId ->
                val member = teamMembers.value.find { it.id == memberId }
                if(member != null) {
                    allTeamKPI+= if(task.status==Status.COMPLETED) 1 else 0
                }
            }
        }
        if(allTeamKPI == 0) {
            return null
        }
        teamTasks.value.forEach { task->
            task.members.forEach { memberId ->
                val member = teamMembers.value.find { it.id == memberId }
                if(member != null) {
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
        updateColorPaletteTeamKPI(overAllTeamKpi.toMap())
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
