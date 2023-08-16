package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.activities.Login_Activity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Profile_Fragment : Fragment() {

    private lateinit var txtUpperName: TextView
    private lateinit var txtUpperDepartment: TextView
    private lateinit var EmpFirstName: TextInputEditText
    private lateinit var EmpMiddleName: TextInputEditText
    private lateinit var EmpLastName: TextInputEditText
    private lateinit var EmpInstitute: TextInputEditText
    private lateinit var EmpDesignation: TextInputEditText
    private lateinit var EmpDepartment: TextInputEditText
    private lateinit var EmpDOB: TextInputEditText
    private lateinit var EmpDOA: TextInputEditText
    private lateinit var EmpPhoneNo: TextInputEditText
    private lateinit var EmpEmailId: TextInputEditText
    private lateinit var btnLogout: Button

    private lateinit var instituteId: String
    private lateinit var instituteName: String
    private lateinit var empid: String
    private lateinit var empfname: String
    private lateinit var empmname: String
    private lateinit var emplname: String
    private lateinit var empdepartment: String
    private lateinit var empdesignation: String
    private lateinit var empdob: String
    private lateinit var empdoa: String
    private lateinit var empphno: String
    private lateinit var empemail: String

    private val firebaseauth = FirebaseAuth.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_, container, false)

        txtUpperName = view.findViewById(R.id.txtUpperName)
        txtUpperDepartment = view.findViewById(R.id.txtUpperDepartment)
        EmpFirstName = view.findViewById(R.id.EmpFirstName)
        EmpMiddleName = view.findViewById(R.id.EmpMiddleName)
        EmpLastName = view.findViewById(R.id.EmpLastName)
        EmpInstitute = view.findViewById(R.id.EmpInstitute)
        EmpDesignation = view.findViewById(R.id.EmpDesignation)
        EmpDepartment = view.findViewById(R.id.EmpDepartment)
        EmpDOB = view.findViewById(R.id.EmpDOB)
        EmpDOA = view.findViewById(R.id.EmpDOA)
        EmpPhoneNo = view.findViewById(R.id.EmpPhoneNo)
        EmpEmailId = view.findViewById(R.id.EmpEmailId)
        btnLogout = view.findViewById(R.id.btnLogout)

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your EmpID").toString()
            instituteName = sharedPref.getString("EmpInstituteName", "Your EmpID").toString()
            empid = sharedPref.getString("EmpID", "Your EmpID").toString()
            empfname = sharedPref.getString("FName", "Fname").toString()
            empmname = sharedPref.getString("MName", "Mname").toString()
            emplname = sharedPref.getString("LName", "Lname").toString()
            empdepartment = sharedPref.getString("Department", "Department").toString()
            empdesignation = sharedPref.getString("Designation", "Designation").toString()
            empdob = sharedPref.getString("DateOfBirth", "DateOfBirth").toString()
            empdoa = sharedPref.getString("DateOfAppointment", "DateOfAppointment").toString()
            empphno = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()
            empemail = sharedPref.getString("EmailId", "EmailId").toString()
        }
        btnLogout.setOnClickListener {
            logout()
        }

        txtUpperName.text = "$empfname $emplname"
        txtUpperDepartment.text = empdepartment
        EmpFirstName.setText(empfname)
        EmpMiddleName.setText(empmname)
        EmpLastName.setText(emplname)
        EmpInstitute.setText("$instituteName($instituteId)")
        EmpDesignation.setText(empdesignation)
        EmpDepartment.setText(empdepartment)
        EmpDOB.setText(empdob)
        EmpDOA.setText(empdoa)
        EmpPhoneNo.setText(empphno)
        EmpEmailId.setText(empemail)

        return view
    }

    private fun logout() {// Logout from the account, clear shared preferences and start Login/Register avtivity
        val dialogBuilder = AlertDialog.Builder(activity as Context)
        dialogBuilder.setTitle("Logout")
        dialogBuilder.setMessage("Do you want to logout of the app?")
        dialogBuilder.setPositiveButton("Yes") { _, _ ->
            firebaseauth.signOut()
            val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
            val editor = sharedPref?.edit()
            editor?.clear()
            editor?.apply()
            val intent = Intent(activity as Context, Login_Activity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}