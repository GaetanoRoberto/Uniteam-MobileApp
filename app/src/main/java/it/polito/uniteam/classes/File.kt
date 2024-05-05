package it.polito.uniteam.classes

import android.net.Uri

data class File (
    var user: Member,
    var filename: String,
    var date: String,
    var uri: Uri
)