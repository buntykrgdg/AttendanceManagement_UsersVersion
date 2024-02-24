package com.buntykrgdg.attendancemanagementusersversion.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Employee
import com.buntykrgdg.attendancemanagementusersversion.databinding.ActivityFirebaseAuthBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirebaseAuthBinding
    private lateinit var enteredOtp: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var employeeDetails: Employee
    private lateinit var instituteName: String
    private lateinit var instituteId: String
    private lateinit var phNumber: String

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirebaseAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        firebaseAuth = FirebaseAuth.getInstance()

        binding.txtChangenumber.setOnClickListener{
            val intent = Intent(this@FirebaseAuthActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        instituteId = intent.getStringExtra("instituteid").toString()
        phNumber = intent.getStringExtra("phNumber").toString()

        binding.btnverifyotp.setOnClickListener {
            enteredOtp = binding.getotp.text.toString()
            if(enteredOtp.isEmpty()){
                
                UtilFunctions.showToast(applicationContext, "Enter your OTP first")
            }
            else{
                binding.progressbarofotpverification.visibility = View.VISIBLE
                val receivedCode: String = intent.getStringExtra("otp").toString()
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(receivedCode, enteredOtp)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) = CoroutineScope(Dispatchers.IO).launch {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { p0 ->
            if (p0.isSuccessful) {
                executeInOrder()
            } else {
                if (p0.exception is FirebaseAuthInvalidCredentialsException) {
                    binding.progressbarofotpverification.visibility = View.INVISIBLE
                    UtilFunctions.showToast(applicationContext, "Login Failed")
                }
            }
        }
    }

    private fun executeInOrder() = CoroutineScope(Dispatchers.IO).launch {
        val task1 = getEmployeeDetails()
        task1.join()
        val task2 = updateUid()
        task2.join()
        val task3 = getInstituteName()
        task3.join()
        saveToSharedPreferences(employeeDetails)
    }

    private fun getEmployeeDetails() = CoroutineScope(Dispatchers.IO).launch {
        val db = FirebaseFirestore.getInstance()
        try {
            val doc = db.collection("Institutions").document(instituteId)
                .collection("Employees").document(phNumber).get().await()
            if(!doc.exists()){
                withContext(Dispatchers.Main) {
                    UtilFunctions.showToast(
                        applicationContext,
                        "Employee Not found",
                    )
                }
            }else{
                employeeDetails = doc.toObject<Employee>()!!
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main) {
                UtilFunctions.showToast(
                    applicationContext,
                    e.message,
                )
            }
        }
    }
    private fun updateUid() = CoroutineScope(Dispatchers.IO).launch {
        val db = FirebaseFirestore.getInstance()
        val dbRef = db.collection("Institutions").document(instituteId).collection("Employees").document(phNumber)
        val map = mutableMapOf<String, Any>()
        map["uid"] = firebaseAuth.uid.toString()
        try{
            dbRef.update(map).await()
        }catch (e: Exception){
            withContext(Dispatchers.Main) {
                UtilFunctions.showToast(
                    applicationContext,
                    e.message,
                )
            }
        }
    }
    private fun getInstituteName() = CoroutineScope(Dispatchers.IO).launch {
        val db = FirebaseFirestore.getInstance()
        try{
            val doc = db.collection("Institutions").document(instituteId).get().await()
            if(doc.exists()){
                instituteName = doc.get("name").toString()
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                UtilFunctions.showToast(
                    applicationContext,
                    e.message,
                )
            }
        }
    }

    private fun saveToSharedPreferences(employee: Employee) = CoroutineScope(Dispatchers.IO).launch{
        val sharedPref = getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
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
        withContext(Dispatchers.Main){
            UtilFunctions.showToast(applicationContext, "Login Successful")
            val intent = Intent(this@FirebaseAuthActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}