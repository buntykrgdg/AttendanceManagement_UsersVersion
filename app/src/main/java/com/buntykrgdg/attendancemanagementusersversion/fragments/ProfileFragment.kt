package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.buntykrgdg.attendancemanagementusersversion.activities.LoginActivity
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Employee
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentProfileBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

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
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var fragmentProfileBinding: FragmentProfileBinding? = null
    private val binding get() = fragmentProfileBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

        loadEmployeeDetails()

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.swipeToRefreshProfile.setOnRefreshListener {
            getEmployeeDetails()
        }
        return binding.root
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
        binding.txtUpperName.text = "$empfname $emplname"
        binding.txtUpperDepartment.text = empdepartment
        binding.EmpFirstName.setText(empfname)
        binding.EmpMiddleName.setText(empmname)
        binding.EmpLastName.setText(emplname)
        binding.EmpInstitute.setText("$instituteName($instituteId)")
        binding.EmpDesignation.setText(empdesignation)
        binding.EmpDepartment.setText(empdepartment)
        binding.EmpDOB.setText(empdob)
        binding.EmpDOA.setText(empdoa)
        binding.EmpPhoneNo.setText(empphno)
        binding.EmpEmailId.setText(empemail)

        binding.swipeToRefreshProfile.isRefreshing = false
    }
    private fun logout() {// Logout from the account, clear shared preferences and start Login activity
        val dialogBuilder = AlertDialog.Builder(activity as Context)
        dialogBuilder.setTitle("Logout")
        dialogBuilder.setMessage("Do you want to logout of the app?")
        dialogBuilder.setPositiveButton("Yes") { _, _ ->
            firebaseAuth.signOut()
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
        try{
            val doc = db.collection("Institutions").document(instituteId).collection("Employees")
                .document(empphno).get().await()
            if(doc.exists()){
                val employee = doc.toObject<Employee>()
                if(employee!=null) {
                    val sharedPref = activity?.getSharedPreferences(
                        "AttendanceManagementUV",
                        Context.MODE_PRIVATE
                    )
                    if (sharedPref != null) {
                        with(sharedPref.edit()) {
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
                }
            }else{
                withContext(Dispatchers.Main){
                    UtilFunctions.showToast(activity as Context, "Employee not found")
                }
            }
        }catch (e: Exception){
            UtilFunctions.showToast(activity as Context, e.message)
        }
        binding.swipeToRefreshProfile.isRefreshing = false
        reloadFragment()
    }

    private fun reloadFragment(){ // Complete
        val frg: Fragment? = activity?.supportFragmentManager?.findFragmentByTag("ProfileFragment")
        val ft: FragmentTransaction? = activity?.supportFragmentManager?.beginTransaction()
        if (frg != null) {
            if (ft != null) {
                ft.detach(frg)
                ft.attach(frg)
                ft.commit()
            }
        }
    }
}