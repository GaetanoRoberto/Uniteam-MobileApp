package it.polito.uniteam

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import it.polito.uniteam.gui.teamDetails.TeamDetailsViewModel
import it.polito.uniteam.gui.availability.AvailabilityViewModel
import it.polito.uniteam.gui.availability.JoinViewModel
import it.polito.uniteam.gui.calendar.Calendar
import it.polito.uniteam.gui.chat.ChatViewModel
import it.polito.uniteam.gui.chatlist.ChatListViewModel
import it.polito.uniteam.gui.home.HomeViewModel
import it.polito.uniteam.gui.login.LoginViewModel
import it.polito.uniteam.gui.notifications.NotificationsViewModel
import it.polito.uniteam.gui.showtaskdetails.taskDetails
import it.polito.uniteam.gui.statistics.StatisticsViewModel
import it.polito.uniteam.gui.teamScreen.TeamScreenViewModel
import it.polito.uniteam.gui.userprofile.OtherUserProfileScreen
import it.polito.uniteam.gui.userprofile.UserProfileScreen
import it.polito.uniteam.gui.yourTasksCalendar.YourTasksCalendarViewModel

class Factory(context: Context): ViewModelProvider.Factory {
    // assuming that an application provides a model, if not throw an error
    val model: UniTeamModel = (context.applicationContext as? UniTeamApplication)?.model ?: throw IllegalArgumentException("Bad application class")

    private val viewModelClasses = arrayOf(
        Calendar::class.java,
        taskDetails::class.java,
        TeamScreenViewModel::class.java,
        UserProfileScreen::class.java,
        NotificationsViewModel::class.java,
        ChatViewModel::class.java,
        ChatListViewModel::class.java,
        StatisticsViewModel::class.java,
        YourTasksCalendarViewModel::class.java,
        OtherUserProfileScreen::class.java,
        AvailabilityViewModel::class.java,
        JoinViewModel::class.java,
        TeamDetailsViewModel::class.java,
        HomeViewModel::class.java,
        GeneralViewModel::class.java,
        LoginViewModel::class.java
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
    lateinit var model : UniTeamModel

    override fun onCreate() {
        super.onCreate()
        model = UniTeamModel(this)
    }
}