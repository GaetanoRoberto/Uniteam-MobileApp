package it.polito.uniteam


enum class Repetition{
    DAILY, WEEKLY, MONTHLY, NONE

}

fun String.isRepetition(): Boolean{
    if(this.equals("DAILY") || this.equals("WEEKLY") || this.equals("MONTHLY") || this.equals("NONE")){
        return true
    }
    else{
        return false
    }
}


enum class Status{
    TODO, IN_PROGRESS, COMPLETED
}