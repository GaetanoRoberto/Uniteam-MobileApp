package it.polito.uniteam

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.gui.calendar.Calendar
import it.polito.uniteam.gui.showtaskdetails.taskDetails
import it.polito.uniteam.gui.tasklist.TaskList
import it.polito.uniteam.gui.userprofile.UserProfileScreen

class Factory(context: Context): ViewModelProvider.Factory {
    // assuming that an application provides a model, if not throw an error
    val model: UniTeamModel = (context.applicationContext as? UniTeamApplication)?.model ?: throw IllegalArgumentException("Bad application class")

    private val viewModelClasses = arrayOf(
        Calendar::class.java,
        taskDetails::class.java,
        TaskList::class.java,
        UserProfileScreen::class.java
    )
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        for (viewModelClass in viewModelClasses) {
            if (modelClass.isAssignableFrom(viewModelClass)) {
                return viewModelClass.getConstructor(UniTeamModel::class.java).newInstance(model) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
        /*return if (modelClass.isAssignableFrom(StringViewModel::class.java)) {
            StringViewModel(model) as T
        } else if (modelClass.isAssignableFrom(CountViewModel::class.java)) {
            CountViewModel(model) as T
        } else throw IllegalArgumentException("Unknown ViewModel Class")*/
    }
}

class UniTeamApplication: Application() {
    // one model will be created and stored in application object when process begins
    val model = UniTeamModel()
}