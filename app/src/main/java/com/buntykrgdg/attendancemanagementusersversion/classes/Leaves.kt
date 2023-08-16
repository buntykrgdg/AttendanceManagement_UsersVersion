package com.buntykrgdg.attendancemanagementusersversion.classes

import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class Leaves {

    fun HalfDayLeave(): List<Any> { return listOf<Any>(0.5, 1, 1) }

    fun FullDayLeave(): List<Int> { return listOf(1, 2, 1) }

    fun MorethanoneLeave(start: String, startsession: String, end: String, endsession: String): List<Double> {
        val numberOfDays = (calculateNumberOfDays(start, end)) + 1
        Log.d("Days", numberOfDays.toString())
        var cl = 0.0
        var hpl = 0.0
        var el = 0.0
        if(startsession == "morning" && endsession == "afternoon"){
            cl = numberOfDays.toDouble()
            hpl = cl*2
            el = numberOfDays.toDouble()
        }
        else if( startsession == "morning" && endsession == "morning"){
            cl = (numberOfDays - 0.5)
            hpl = cl*2
            el = numberOfDays.toDouble()
        }
        else if( startsession == "afternoon" && endsession == "morning"){
            cl = (numberOfDays - 1).toDouble()
            hpl = cl*2
            el = numberOfDays.toDouble()
        }
        else if( startsession == "afternoon" && endsession == "afternoon"){
            cl = (numberOfDays - 0.5)
            hpl = cl*2
            el = numberOfDays.toDouble()
        }
        Log.d("Leaves", cl.toString())
        Log.d("Leaves", hpl.toString())
        Log.d("Leaves", el.toString())
        return listOf(cl, hpl, el)
    }

    fun calculateNumberOfDays(startDateStr: String, endDateStr: String): Long {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val startDate = LocalDate.parse(startDateStr, formatter)
        val endDate = LocalDate.parse(endDateStr, formatter)
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    fun isToDateBeforeFromDate(fromDate: String, toDate: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return try {
            val fromDateTime = dateFormat.parse(fromDate)
            val toDateTime = dateFormat.parse(toDate)
            toDateTime.before(fromDateTime)
        } catch (e: Exception) {
            // Handle parsing errors if any
            false
        }
    }

}