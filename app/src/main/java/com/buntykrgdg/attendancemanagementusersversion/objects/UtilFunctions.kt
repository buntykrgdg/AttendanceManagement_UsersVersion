package com.buntykrgdg.attendancemanagementusersversion.objects

import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.CheckInOutLog
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Notice
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

object UtilFunctions {
    private fun convertStringToTimestamp(dateString: String): Long {
        val format = SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss a", Locale.US)
        val date = format.parse(dateString)
        return date?.time ?: 0
    }

    private fun convertStringToTimestamp2(dateString: String): Long {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.US)
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

    fun sortCheckInCheckOutByTimestamp(list: ArrayList<CheckInOutLog>) {
        list.sortWith { checkInOutLog1, checkInOutLog2 ->
            val timestamp1 = convertStringToTimestamp(checkInOutLog1.timestamp ?: "")
            val timestamp2 = convertStringToTimestamp(checkInOutLog2.timestamp ?: "")
            timestamp2.compareTo(timestamp1) // Descending order, use timestamp1.compareTo(timestamp2) for ascending
        }
    }

    fun sortLeaveRequestByTimestamp2(list: ArrayList<String>) {
        list.sortWith { leaveRequest1, leaveRequest2 ->
            val timestamp1 = convertStringToTimestamp2(leaveRequest1?: "")
            val timestamp2 = convertStringToTimestamp2(leaveRequest2?: "")
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