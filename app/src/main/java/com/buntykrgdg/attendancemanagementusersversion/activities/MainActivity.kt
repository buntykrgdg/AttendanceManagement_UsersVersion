package com.buntykrgdg.attendancemanagementusersversion.activities

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
    private var isInternetPermissiomGranted = false
    private var isNotificationPermissionGranted = false
    private var isManageStoragePermissionGranted = false
    private var isReadStoragePermissionGranted = false
    private var isWriteStoragePermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

            isInternetPermissiomGranted = permissions[Manifest.permission.INTERNET] ?: isInternetPermissiomGranted
            isNotificationPermissionGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: isNotificationPermissionGranted
            isReadStoragePermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadStoragePermissionGranted
            isWriteStoragePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWriteStoragePermissionGranted
            isManageStoragePermissionGranted = permissions[Manifest.permission.MANAGE_EXTERNAL_STORAGE] ?: isManageStoragePermissionGranted

        }

        requestPermission()
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission(){//Request all the required permissions at once

        isInternetPermissiomGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        isNotificationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        isManageStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isReadStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWriteStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


        val permissionRequest : MutableList<String> = java.util.ArrayList()

        if (!isInternetPermissiomGranted){

            permissionRequest.add(Manifest.permission.INTERNET)

        }

        if (!isNotificationPermissionGranted){

            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)

        }

        if (!isReadStoragePermissionGranted){

            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        }

        if (!isWriteStoragePermissionGranted){

            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        }

        if (!isManageStoragePermissionGranted){

            permissionRequest.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)

        }

        if (permissionRequest.isNotEmpty()){

            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }


}