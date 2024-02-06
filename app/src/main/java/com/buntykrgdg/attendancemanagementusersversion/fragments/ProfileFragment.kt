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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.activities.LoginActivity
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Employee
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var swipeToRefreshProfile: SwipeRefreshLayout

    private lateinit var txtUpperName: TextView
    private lateinit var txtUpperDepartment: TextView
    private lateinit var empFirstName: TextInputEditText
    private lateinit var empMiddleName: TextInputEditText
    private lateinit var empLastName: TextInputEditText
    private lateinit var empInstitute: TextInputEditText
    private lateinit var empDesignation: TextInputEditText
    private lateinit var empDepartment: TextInputEditText
    private lateinit var empDOB: TextInputEditText
    private lateinit var empDOA: TextInputEditText
    private lateinit var empPhoneNo: TextInputEditText
    private lateinit var empEmailId: TextInputEditText
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
    private val db = FirebaseFirestore.getInstance()
    private val firebaseauth = FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_, container, false)

        swipeToRefreshProfile=view.findViewById(R.id.swipeToRefreshProfile)
        txtUpperName = view.findViewById(R.id.txtUpperName)
        txtUpperDepartment = view.findViewById(R.id.txtUpperDepartment)
        empFirstName = view.findViewById(R.id.EmpFirstName)
        empMiddleName = view.findViewById(R.id.EmpMiddleName)
        empLastName = view.findViewById(R.id.EmpLastName)
        empInstitute = view.findViewById(R.id.EmpInstitute)
        empDesignation = view.findViewById(R.id.EmpDesignation)
        empDepartment = view.findViewById(R.id.EmpDepartment)
        empDOB = view.findViewById(R.id.EmpDOB)
        empDOA = view.findViewById(R.id.EmpDOA)
        empPhoneNo = view.findViewById(R.id.EmpPhoneNo)
        empEmailId = view.findViewById(R.id.EmpEmailId)
        btnLogout = view.findViewById(R.id.btnLogout)

        loadEmployeeDetails()

        btnLogout.setOnClickListener {
            logout()
        }

        swipeToRefreshProfile.setOnRefreshListener {
            getEmployeeDetails()
        }
        
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun loadEmployeeDetails(){
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
        txtUpperName.text = "$empfname $emplname"
        txtUpperDepartment.text = empdepartment
        empFirstName.setText(empfname)
        empMiddleName.setText(empmname)
        empLastName.setText(emplname)
        empInstitute.setText("$instituteName($instituteId)")
        empDesignation.setText(empdesignation)
        empDepartment.setText(empdepartment)
        empDOB.setText(empdob)
        empDOA.setText(empdoa)
        empPhoneNo.setText(empphno)
        empEmailId.setText(empemail)

        swipeToRefreshProfile.isRefreshing = false
    }
    private fun logout() {// Logout from the account, clear shared preferences and start Login activity
        val dialogBuilder = AlertDialog.Builder(activity as Context)
        dialogBuilder.setTitle("Logout")
        dialogBuilder.setMessage("Do you want to logout of the app?")
        dialogBuilder.setPositiveButton("Yes") { _, _ ->
            firebaseauth.signOut()
            val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
            val editor = sharedPref?.edit()
            editor?.clear()
            editor?.apply()
            val intent = Intent(activity as Context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun getEmployeeDetails()= CoroutineScope(Dispatchers.IO).launch{
        val dbref = db.collection("Institutions").document(instituteId).collection("Employees")
        val querySnapshot = dbref.whereEqualTo("empId", empid).get().await()
        for (document in querySnapshot) {
            val employee = document.toObject<Employee>()
            val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
            if (sharedPref != null) {
                with (sharedPref.edit()) {
                    putString("EmpID", employee.EmpId)
                    putString("EmpInstituteName", instituteName)
                    putString("EmpInstituteId", instituteId)
                    putString("FName", employee.EmpFirstName)
                    putString("MName", employee.EmpMiddleName)
                    putString("LName", employee.EmpLastName)
                    putString("Designation", employee.EmpDesignation)
                    putString("Department", employee.EmpDepartment)
                    putString("DateOfBirth", employee.EmpDOB)
                    putString("DateOfAppointment", employee.EmpDOA)
                    putString("PhoneNumber", employee.EmpPhoneNo)
                    putString("EmailId", employee.EmpEmailId)
                    putString("status", "Checked Out")
                    employee.EmpCL?.let { putString("CL", it.toString()) }
                    employee.EmpHPL?.let { putString("HPL", it.toString()) }
                    employee.EmpEL?.let { putString("EL", it.toString()) }
                    apply()
                }
            }
            withContext(Dispatchers.Main){
                loadEmployeeDetails()
            }
        }
    }
}