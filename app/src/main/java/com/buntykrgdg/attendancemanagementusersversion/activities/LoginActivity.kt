package com.buntykrgdg.attendancemanagementusersversion.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.User
import com.buntykrgdg.attendancemanagementusersversion.databinding.ActivityLoginBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
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
    private lateinit var countryCode: String
    private lateinit var phoneNumber: String
    private lateinit var number: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var codeSent: String
    private lateinit var companyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        countryCode = binding.countrycodepicker.selectedCountryCodeWithPlus

        binding.countrycodepicker.setOnCountryChangeListener {
            countryCode = binding.countrycodepicker.selectedCountryCodeWithPlus
        }

        //initAppIntegrity()

        binding.btnSendOtpButton.setOnClickListener {
            companyId = binding.CompanyID.text.toString()
            if(companyId == ""){
                UtilFunctions.showToast(applicationContext, "Please enter Company ID")
            }
            else{
                number = binding.edtxtGetphonenumber.text.toString()
                if (number.isEmpty()) {
                    UtilFunctions.showToast(applicationContext, "Please enter your number")
                        
                } else if (number.length < 10) {
                    UtilFunctions.showToast(
                        applicationContext,
                        "Please enter correct number"
                    )
                } else {
                    binding.progressbarofmain.visibility = View.VISIBLE
                    checkIfRegistered(companyId,number)
                }
            }
        }

        callBacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                UtilFunctions.showToast(applicationContext, "Verification Completed")
                binding.progressbarofmain.visibility = View.INVISIBLE
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                UtilFunctions.showToast(applicationContext, "VerificationFailed")
                binding.progressbarofmain.visibility = View.INVISIBLE
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                UtilFunctions.showToast(applicationContext, "Code Sent")
                binding.progressbarofmain.visibility = View.INVISIBLE
                codeSent = p0
                val intent = Intent(this@LoginActivity, FirebaseAuthActivity::class.java)
                intent.putExtra("otp", codeSent)
                intent.putExtra("instituteid", companyId)
                intent.putExtra("phNumber", countryCode+number)
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
//            UtilFunctions.showToast(applicationContext, "Unexpected error Occurred")
//        }
//    }

    private fun checkIfRegistered(instituteId: String, pNumber: String) = CoroutineScope(Dispatchers.IO).launch{
        val db = FirebaseFirestore.getInstance()
        try{
            val doc = db.collection("Users").document(countryCode+pNumber).get().await()
            if (doc.exists()){
                val user = doc.toObject<User>()
                if (user != null) {
                    if (user.instituteID == instituteId && user.role == "Employee") {
                        withContext(Dispatchers.Main) {
                            binding.progressbarofmain.visibility = View.VISIBLE
                        }
                        phoneNumber = countryCode + pNumber
                        val options: PhoneAuthOptions =
                            PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(phoneNumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(this@LoginActivity)
                                .setCallbacks(callBacks)
                                .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }else if (user.instituteID == instituteId && user.role == "Employer"){
                        withContext(Dispatchers.Main) {
                            UtilFunctions.showToast(
                                applicationContext,
                                "You are not registered as Employee"
                            )
                            binding.progressbarofmain.visibility = View.INVISIBLE
                        }
                    }else {
                        withContext(Dispatchers.Main) {
                            UtilFunctions.showToast(
                                applicationContext,
                                "Please enter correct Institute ID"
                            )
                            binding.progressbarofmain.visibility = View.INVISIBLE
                        }
                    }
                }
            }else {
                withContext(Dispatchers.Main) {
                    UtilFunctions.showToast(
                        applicationContext,
                        "Please enter registered number"
                    )
                    binding.progressbarofmain.visibility = View.INVISIBLE
                }
            }
    }catch (e: Exception){
            withContext(Dispatchers.Main) {
                UtilFunctions.showToast(
                    applicationContext,
                    e.message
                )
                binding.progressbarofmain.visibility = View.INVISIBLE
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