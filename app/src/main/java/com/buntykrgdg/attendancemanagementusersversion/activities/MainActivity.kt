package com.buntykrgdg.attendancemanagementusersversion.activities

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.buntykrgdg.attendancemanagementusersversion.fragments.NoticesFragment
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.fragments.HistoryFragment
import com.buntykrgdg.attendancemanagementusersversion.fragments.NewRequestFragment
import com.buntykrgdg.attendancemanagementusersversion.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isLocationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getfcmtoken()
        FirebaseMessaging.getInstance().subscribeToTopic("all")

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val firstFragment = NewRequestFragment()
        val secondFragment = HistoryFragment()
        val thirdFragment = ProfileFragment()
        val fourthFragment = NoticesFragment()

        setCurrentFragment(firstFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.newRequest_menu ->setCurrentFragment(firstFragment)
                R.id.history_menu ->setCurrentFragment(secondFragment)
                R.id.profile_menu ->setCurrentFragment(thirdFragment)
                R.id.notices_menu ->setCurrentFragment(fourthFragment)
            }
            true
        }


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->

            isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted

        }

        requestPermission()
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }

    private fun requestPermission(){

        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        val permissionRequest : MutableList<String> = ArrayList()

        if (!isReadPermissionGranted){

            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        }

        if (!isLocationPermissionGranted){

            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)

        }

        if (permissionRequest.isNotEmpty()){

            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }

    private fun getfcmtoken() = CoroutineScope(Dispatchers.IO).launch{
        FirebaseMessaging.getInstance().token   //Only to be added in admins code
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                // Get the FCM token
                val fcmToken = task.result
                Log.d("FCM", "FCM registration token: $fcmToken")
                val database = FirebaseFirestore.getInstance()
                val sharedPref = getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
                val instituteId = sharedPref.getString("EmpInstituteId", "")
                val employeeId = sharedPref.getString("EmpID", "")
                if (instituteId != null && employeeId != null) {
                    val map = mutableMapOf<String, Any>()
                    map["fcmToken"] = fcmToken
                    val databaseref = database.collection("Institutions")
                        .document(instituteId)
                        .collection("Employees")
                        .document(employeeId)
                    databaseref.set(map, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "FCM token added to database successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(ContentValues.TAG, "FCM token could not be added to database", e)
                        }
                } else {
                    Log.d(ContentValues.TAG, "No institute ID/employee ID found")
                }
            }
    }
}