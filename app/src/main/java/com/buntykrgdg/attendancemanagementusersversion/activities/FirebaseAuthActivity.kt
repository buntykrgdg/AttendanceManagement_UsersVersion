package com.buntykrgdg.attendancemanagementusersversion.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Employee
import com.buntykrgdg.attendancemanagementusersversion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthActivity : AppCompatActivity() {
    private lateinit var txtChangeNumber: TextView
    private lateinit var getOtp: EditText
    private lateinit var btnVerifyOtp: Button
    private lateinit var enteredOtp: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressbarOfOtpVerification: ProgressBar
    private lateinit var employeeDetails: Employee
    private lateinit var instituteName: String
    private lateinit var instituteId: String
    private var database = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_auth)
        txtChangeNumber = findViewById(R.id.txtChangenumber)
        getOtp =findViewById(R.id.getotp)
        btnVerifyOtp = findViewById(R.id.btnverifyotp)
        progressbarOfOtpVerification = findViewById(R.id.progressbarofotpverification)
        firebaseAuth = FirebaseAuth.getInstance()

        txtChangeNumber.setOnClickListener{
            val intent = Intent(this@FirebaseAuthActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        employeeDetails = intent.getSerializableExtra("employeedetails") as Employee
        instituteId = intent.getStringExtra("instituteid").toString()

        getInstituteName()

        btnVerifyOtp.setOnClickListener {
            enteredOtp = getOtp.text.toString()
            if(enteredOtp.isEmpty()){
                Toast.makeText(applicationContext, "Enter your OTP first", Toast.LENGTH_SHORT).show()
            }
            else{
                progressbarOfOtpVerification.visibility = View.VISIBLE
                val receivedCode: String = intent.getStringExtra("otp").toString()
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(receivedCode, enteredOtp)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) = CoroutineScope(Dispatchers.IO).launch {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { p0 ->
            if (p0.isSuccessful) {
                progressbarOfOtpVerification.visibility = View.INVISIBLE
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                getInstituteName()
                saveToSharedPreferences(employeeDetails)
                val intent = Intent(this@FirebaseAuthActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                if (p0.exception is FirebaseAuthInvalidCredentialsException) {
                    progressbarOfOtpVerification.visibility = View.INVISIBLE
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getInstituteName() = CoroutineScope(Dispatchers.IO).launch {
        val databaseRef = database.collection("Institutions").document(instituteId)
        val querySnapshot = databaseRef.get().await()
        instituteName = querySnapshot.get("name").toString()
        Log.d("name", instituteName)
    }

    private fun saveToSharedPreferences(employee: Employee) = CoroutineScope(
        Dispatchers.IO).launch{
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
            employee.EmpCL?.let { putString("CL", it.toString()) }
            employee.EmpHPL?.let { putString("HPL", it.toString()) }
            employee.EmpEL?.let { putString("EL", it.toString()) }
            apply()
        }
    }
}