package com.buntykrgdg.attendancemanagementusersversion

import com.buntykrgdg.attendancemanagementusersversion.classes.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.classes.Notice
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

object UtilFunctions {
    private fun convertStringToTimestamp(dateString: String): Long {
        val format = SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss a", Locale.US)
        val date = format.parse(dateString)
        return date?.time ?: 0
    }

    fun sortLeaveRequestByTimestamp(list: ArrayList<LeaveRequest>) {
        list.sortWith { leaveRequest1, leaveRequest2 ->
            val timestamp1 = convertStringToTimestamp(leaveRequest1.timestamp ?: "")
            val timestamp2 = convertStringToTimestamp(leaveRequest2.timestamp ?: "")
            timestamp2.compareTo(timestamp1) // Descending order, use timestamp1.compareTo(timestamp2) for ascending
        }
    }

    fun sortNoticeByTimestamp(list: ArrayList<Notice>) {
        list.sortWith { notice1, notice2 ->
            val timestamp1 = convertStringToTimestamp(notice1.timestamp ?: "")
            val timestamp2 = convertStringToTimestamp(notice2.timestamp ?: "")
            timestamp2.compareTo(timestamp1) // Descending order, use timestamp1.compareTo(timestamp2) for ascending
        }
    }

}