package it.polito.uniteam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class GeneralViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    fun getTeams() = model.getTeams()
    fun getAllTeamsMembersHome() = model.getAllTeamsMembersHome()
    fun getTeamById(id: String) = model.getTeamById(id)
}