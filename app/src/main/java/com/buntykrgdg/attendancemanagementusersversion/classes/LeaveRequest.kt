package com.buntykrgdg.attendancemanagementusersversion.classes

data class LeaveRequest (
    val timestamp: String? = "",
    val employeeid: String?= "",
    val instituteid: String?= "",
    val employeename: String?= "",
    val employeedepartment: String?= "",
    val employeedesignation: String?= "",
    val fromdate: String?= "",
    val fromsession: String?= "",
    val todate: String?= "",
    val tosession: String?= "",
    val leavetype: String?= "",
    val noofleaves: String?= "",
    val reason: String?= "",
    val note: String?= "",
    val status: String?= ""
    )