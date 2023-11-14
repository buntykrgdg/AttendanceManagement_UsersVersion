package com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses

data class Employee(var EmpId: String? = null,
                    var EmpFirstName: String? = null,
                    var EmpMiddleName: String? = null,
                    var EmpLastName: String? = null,
                    var EmpDesignation: String? = null,
                    var EmpDepartment: String? = null,
                    var EmpDOB: String? = null,
                    var EmpDOA: String? = null,
                    var EmpPhoneNo: String? = null,
                    var EmpEmailId: String? = null,
                    var EmpCL: Double? = null,
                    var EmpHPL: Double? = null,
                    var EmpEL: Double? = null): java.io.Serializable
