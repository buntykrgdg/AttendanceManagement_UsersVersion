package com.buntykrgdg.attendancemanagementusersversion.classes

import com.google.firebase.firestore.ServerTimestamp

data class Notice(
    val timestamp: String? = null,
    val message: String? = null,
    val serverTimestamp: ServerTimestamp
): java.io.Serializable

