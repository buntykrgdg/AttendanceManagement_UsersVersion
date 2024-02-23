package com.buntykrgdg.attendancemanagementusersversion.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.buntykrgdg.attendancemanagementusersversion.fragments.NoticesFragment
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.databinding.ActivityMainBinding
import com.buntykrgdg.attendancemanagementusersversion.fragments.HistoryFragment
import com.buntykrgdg.attendancemanagementusersversion.fragments.NewRequestFragment
import com.buntykrgdg.attendancemanagementusersversion.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isInternetPermissionGranted = false
    private var isNotificationPermissionGranted = false
    private var isManageStoragePermissionGranted = false
    private var isReadStoragePermissionGranted = false
    private var isWriteStoragePermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firstFragment = NewRequestFragment()
        val secondFragment = HistoryFragment()
        val thirdFragment = ProfileFragment()
        val fourthFragment = NoticesFragment()

        setCurrentFragment(firstFragment)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.newRequest_menu ->setCurrentFragment(firstFragment)
                R.id.history_menu ->setCurrentFragment(secondFragment)
                R.id.profile_menu ->setCurrentFragment(thirdFragment)
                R.id.notices_menu ->setCurrentFragment(fourthFragment)
            }
            true
        }


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->

            isInternetPermissionGranted = permissions[Manifest.permission.INTERNET] ?: isInternetPermissionGranted
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

        isInternetPermissionGranted = ContextCompat.checkSelfPermission(
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

        if (!isInternetPermissionGranted){

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