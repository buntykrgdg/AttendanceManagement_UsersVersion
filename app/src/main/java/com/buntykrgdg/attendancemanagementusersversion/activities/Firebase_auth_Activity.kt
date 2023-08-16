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
import com.buntykrgdg.attendancemanagementusersversion.classes.Employee
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.classes.LeaveRequest
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

class Firebase_auth_Activity : AppCompatActivity() {
    lateinit var txtChangenumber: TextView
    lateinit var getotp: EditText
    lateinit var btnverifyotp: Button
    lateinit var enteredOtp: String
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var progressbarofotpverification: ProgressBar
    private lateinit var employeedetails: Employee
    private lateinit var institutename: String
    private lateinit var instituteid: String
    private var database = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_auth)

        txtChangenumber = findViewById(R.id.txtChangenumber)
        getotp =findViewById(R.id.getotp)
        btnverifyotp = findViewById(R.id.btnverifyotp)
        progressbarofotpverification = findViewById(R.id.progressbarofotpverification)

        firebaseAuth = FirebaseAuth.getInstance()

        txtChangenumber.setOnClickListener{
            val intent = Intent(this@Firebase_auth_Activity, Login_Activity::class.java)
            startActivity(intent)
        }

        employeedetails = intent.getSerializableExtra("employeedetails") as Employee
        instituteid = intent.getStringExtra("instituteid").toString()

        getinstitutename()

        btnverifyotp.setOnClickListener {
            enteredOtp = getotp.text.toString()
            if(enteredOtp.isEmpty()){
                Toast.makeText(applicationContext, "Enter your OTP first", Toast.LENGTH_SHORT).show()
            }
            else{
                progressbarofotpverification.visibility = View.VISIBLE
                val receivedcode: String = intent.getStringExtra("otp").toString()
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(receivedcode, enteredOtp)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) = CoroutineScope(Dispatchers.IO).launch {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { p0 ->
            if (p0.isSuccessful) {
                progressbarofotpverification.visibility = View.INVISIBLE
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                getinstitutename()
                savetosharedpreferences(employeedetails)
                val intent = Intent(this@Firebase_auth_Activity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                if (p0.exception is FirebaseAuthInvalidCredentialsException) {
                    progressbarofotpverification.visibility = View.INVISIBLE
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getinstitutename() = CoroutineScope(Dispatchers.IO).launch {
        val databaseref = database.collection("Institutions").document(instituteid)
        val querySnapshot = databaseref.get().await()
        institutename = querySnapshot.get("name").toString()
        Log.d("name", institutename)
    }

    private fun savetosharedpreferences(employee: Employee) = CoroutineScope(
        Dispatchers.IO).launch{
        val sharedPref = getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("EmpID", employee.EmpId)
            putString("EmpInstituteName", institutename)
            putString("EmpInstituteId", instituteid)
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