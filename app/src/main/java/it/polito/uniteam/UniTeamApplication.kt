package it.polito.uniteam

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import it.polito.uniteam.gui.calendar.Calendar
import it.polito.uniteam.gui.chat.ChatViewModel
import it.polito.uniteam.gui.notifications.NotificationsViewModel
//import it.polito.uniteam.gui.chat.ChatViewModel
import it.polito.uniteam.gui.showtaskdetails.taskDetails
import it.polito.uniteam.gui.statistics.StatisticsViewModel
import it.polito.uniteam.gui.tasklist.TaskList
import it.polito.uniteam.gui.userprofile.UserProfileScreen

class Factory(context: Context): ViewModelProvider.Factory {
    // assuming that an application provides a model, if not throw an error
    val model: UniTeamModel = (context.applicationContext as? UniTeamApplication)?.model ?: throw IllegalArgumentException("Bad application class")

    private val viewModelClasses = arrayOf(
        Calendar::class.java,
        taskDetails::class.java,
        TaskList::class.java,
        UserProfileScreen::class.java,
        NotificationsViewModel::class.java,
        ChatViewModel::class.java,
        StatisticsViewModel::class.java
    )

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        for (viewModelClass in viewModelClasses) {
            if (modelClass.isAssignableFrom(viewModelClass)) {
                return viewModelClass.getConstructor(UniTeamModel::class.java,SavedStateHandle::class.java).newInstance(model,extras.createSavedStateHandle()) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}

class UniTeamApplication: Application() {
    // one model will be created and stored in application object when process begins
    val model = UniTeamModel()
}