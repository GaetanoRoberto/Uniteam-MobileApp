package it.polito.uniteam

import android.app.Application
import it.polito.uniteam.UniTeamModel

class UniTeamApplication: Application() {
    // one model will be created and stored in application object when process begins
    val model = UniTeamModel()
}