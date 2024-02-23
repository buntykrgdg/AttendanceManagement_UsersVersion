package com.buntykrgdg.attendancemanagementusersversion.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.User
import com.buntykrgdg.attendancemanagementusersversion.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var countrycode: String
    private lateinit var phonenumber: String
    private lateinit var number: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var codesent: String
    private lateinit var companyid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        countrycode = binding.countrycodepicker.selectedCountryCodeWithPlus

        binding.countrycodepicker.setOnCountryChangeListener {
            countrycode = binding.countrycodepicker.selectedCountryCodeWithPlus
        }

        //initAppIntegrity()

        binding.btnSendOtpButton.setOnClickListener {
            companyid = binding.CompanyID.text.toString()
            if(companyid == ""){
                Toast.makeText(applicationContext, "Please enter Company ID", Toast.LENGTH_SHORT).show()
            }
            else{
                number = binding.edtxtGetphonenumber.text.toString()
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
                    binding.progressbarofmain.visibility = View.VISIBLE
                    checkIfRegistered(companyid,number)
                }
            }
        }

        callBacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Toast.makeText(applicationContext, "Verification Completed", Toast.LENGTH_SHORT).show()
                binding.progressbarofmain.visibility = View.INVISIBLE
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(applicationContext, "VerificationFailed", Toast.LENGTH_SHORT).show()
                binding.progressbarofmain.visibility = View.INVISIBLE
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                Toast.makeText(applicationContext, "Code Sent", Toast.LENGTH_SHORT).show()
                binding.progressbarofmain.visibility = View.INVISIBLE
                codesent = p0
                val intent = Intent(this@LoginActivity, FirebaseAuthActivity::class.java)
                intent.putExtra("otp", codesent)
                intent.putExtra("instituteid", companyid)
                intent.putExtra("phNumber", countrycode+number)
                startActivity(intent)
            }
        }
    }

//    private fun initAppIntegrity(){
//        try{
//            Firebase.initialize(context = this)
//            Firebase.appCheck.installAppCheckProviderFactory(
//                PlayIntegrityAppCheckProviderFactory.getInstance(),
//            )
//        }catch (e: Exception){
//            Toast.makeText(applicationContext, "Unexpected error Occurred", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun checkIfRegistered(instituteId: String, pNumber: String) = CoroutineScope(Dispatchers.IO).launch{
        val db = FirebaseFirestore.getInstance()
        try{
            val doc = db.collection("Users").document(countrycode+pNumber).get().await()
            if (doc.exists()){
                Log.d("user00", doc.toString())
                val user = doc.toObject<User>()
                Log.d("user00", user.toString())
                if (user != null) {
                    if (user.instituteID == instituteId && user.role == "Employee") {
                        withContext(Dispatchers.Main) {
                            binding.progressbarofmain.visibility = View.VISIBLE
                        }
                        phonenumber = countrycode + pNumber
                        val options: PhoneAuthOptions =
                            PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(phonenumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(this@LoginActivity)
                                .setCallbacks(callBacks)
                                .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }else if (user.instituteID == instituteId && user.role == "Employer"){
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "You are not registered as Employee",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.progressbarofmain.visibility = View.INVISIBLE
                        }
                    }else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "Please enter correct Institute ID",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.progressbarofmain.visibility = View.INVISIBLE
                        }
                    }
                }
            }else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Please enter registered number",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressbarofmain.visibility = View.INVISIBLE
                }
            }
    }catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressbarofmain.visibility = View.INVISIBLE
                Log.d("user00", e.message.toString())
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