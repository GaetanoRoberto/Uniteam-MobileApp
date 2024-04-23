package it.polito.uniteam.gui.tasklist

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Task


class TaskList: ViewModel() {
    var members = mutableStateOf<List<Member>>(emptyList())
        private set
    var tasks = mutableStateOf<List<Task>>(emptyList())
        private set

}