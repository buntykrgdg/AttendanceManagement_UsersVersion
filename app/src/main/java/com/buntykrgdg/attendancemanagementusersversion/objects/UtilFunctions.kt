package com.buntykrgdg.attendancemanagementusersversion.objects

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.CheckInOutLog
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Notice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

    fun millisToString(timestamp: String): String {
        val sdf = SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss a", Locale.getDefault())
        return sdf.format(timestamp.toLong())
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimestampAndDate(): Pair<String, String> {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val timestamp = Calendar.getInstance().timeInMillis
        val formattedDate = dateFormat.format(timestamp)
        return Pair(timestamp.toString(), formattedDate)
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

    fun millisToDate(millis: Long): String {
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val date = Date(millis)
        return formatter.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun stringToMillis(dateString: String): Long {
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        return try {
            val date = formatter.parse(dateString)
            date.time
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // Or throw an exception if preferred
        }
    }
    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun showToast(context: Context?, message: String?) {
        if (context == null || message == null) return // Safety check for null context or message
        mainScope.launch {
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}