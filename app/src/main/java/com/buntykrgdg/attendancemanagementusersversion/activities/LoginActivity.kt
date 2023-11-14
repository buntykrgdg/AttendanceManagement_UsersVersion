package com.buntykrgdg.attendancemanagementusersversion.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Employee
import com.buntykrgdg.attendancemanagementusersversion.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var edTxtGetPhoneNumber: EditText
    private lateinit var btnSendOtpButton: Button
    private lateinit var countyrycodepicker: CountryCodePicker
    private lateinit var countrycode: String
    private lateinit var phonenumber: String
    private lateinit var firebaseAuth: FirebaseAuth
    private var database = FirebaseFirestore.getInstance()
    private lateinit var progressbarofmain: ProgressBar
    private lateinit var callBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var codesent: String
    private lateinit var companyID: TextInputEditText
    private lateinit var employeedetails: Employee
    private lateinit var companyid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        countyrycodepicker = findViewById(R.id.countyrycodepicker)
        btnSendOtpButton = findViewById(R.id.btnSendOtpButton)
        edTxtGetPhoneNumber = findViewById(R.id.edtxtGetphonenumber)
        progressbarofmain = findViewById(R.id.progressbarofmain)
        companyID = findViewById(R.id.CompanyID)
        firebaseAuth = FirebaseAuth.getInstance()
        countrycode = countyrycodepicker.selectedCountryCodeWithPlus

        countyrycodepicker.setOnCountryChangeListener {
            countrycode = countyrycodepicker.selectedCountryCodeWithPlus
        }

        btnSendOtpButton.setOnClickListener {
            val number: String = edTxtGetPhoneNumber.text.toString()
            if (number.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter your number", Toast.LENGTH_SHORT)
                    .show()
            } else if (number.length < 10) {
                Toast.makeText(
                    applicationContext,
                    "Please enter correct number",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                progressbarofmain.visibility = View.VISIBLE
                companyid = companyID.text.toString()
                checkIfRegistered(companyid,number)
            }
        }

        callBacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Toast.makeText(applicationContext, "Verification Completed", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(applicationContext, "VerificationFailed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                Toast.makeText(applicationContext, "Code Sent", Toast.LENGTH_SHORT).show()
                progressbarofmain.visibility = View.INVISIBLE
                codesent = p0
                val intent = Intent(this@LoginActivity, FirebaseAuthActivity::class.java)
                intent.putExtra("otp", codesent)
                intent.putExtra("instituteid", companyid)
                intent.putExtra("employeedetails", employeedetails)
                startActivity(intent)
            }
        }
    }

    private fun checkIfRegistered(instituteId: String, pNumber: String) = CoroutineScope(Dispatchers.IO).launch{
        var status: String? = null
        val databaseRef = database.collection("Institutions").document(instituteId).collection("Employees")
        val querySnapshot = databaseRef.whereEqualTo("empPhoneNo", pNumber).get().await()
            for (document in querySnapshot) {
                val employee = document.toObject<Employee>()
                Log.d("new", employee.toString())
                if (employee.EmpPhoneNo == pNumber){
                    employeedetails = employee
                    status = "1"
                }
            }
            if (status == null) {
                withContext(Dispatchers.Main){
                    Toast.makeText(applicationContext, "Please enter registered number", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main){
                    progressbarofmain.visibility = View.VISIBLE
                }
                phonenumber = countrycode+pNumber
                val options: PhoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(phonenumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this@LoginActivity)
                    .setCallbacks(callBacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
                withContext(Dispatchers.Main){
                    progressbarofmain.visibility = View.INVISIBLE
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}