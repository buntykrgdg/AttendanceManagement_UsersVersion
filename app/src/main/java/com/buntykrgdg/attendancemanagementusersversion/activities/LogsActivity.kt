package com.buntykrgdg.attendancemanagementusersversion.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.DateLogsAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.CheckInOutLog
import com.buntykrgdg.attendancemanagementusersversion.databinding.ActivityLogsBinding
import com.buntykrgdg.attendancemanagementusersversion.databinding.ActivityMainBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var logsList=arrayListOf<CheckInOutLog>()
    private lateinit var logsAdapter: DateLogsAdapter
    private lateinit var tempArrayList: ArrayList<CheckInOutLog>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var date: String
    private lateinit var empId: String
    private lateinit var empphno: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        layoutManager= LinearLayoutManager(this@LogsActivity)
        tempArrayList = ArrayList()
        logsList = ArrayList()

        date = intent.getStringExtra("date").toString()
        val sharedPref = getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empId = sharedPref.getString("EmpID", "Your EmpID").toString()
            empphno = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()
        }

        logsAdapter = DateLogsAdapter(this@LogsActivity, tempArrayList)
        binding.recyclerviewDateLogs.adapter = logsAdapter
        binding.recyclerviewDateLogs.layoutManager = layoutManager

        getLogList()

        binding.swipeToRefreshDateLogs.setOnRefreshListener {
            getLogList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLogList() = CoroutineScope(Dispatchers.IO).launch {
        // Clear lists on the IO thread to avoid unnecessary context switches
        logsList.clear()
        tempArrayList.clear()

        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno)
            .collection("Logs").document(date)
            .collection("CheckInCheckOut")

        try {
            val querySnapshot = dbRef.get().await()
            if (!querySnapshot.isEmpty) {
                // Directly use mapNotNull to filter non-null mapped objects and add them to logsList
                logsList.addAll(querySnapshot.documents.mapNotNull { it.toObject<CheckInOutLog>() })
                tempArrayList.addAll(logsList)
                UtilFunctions.sortCheckInCheckOutByTimestamp(tempArrayList)

                // Perform UI updates on the Main thread
                withContext(Dispatchers.Main) {
                    binding.recyclerviewDateLogs.adapter?.notifyDataSetChanged()
                }
            } else {
                showToast("No logs found")
            }
        } catch (e: Exception) {
            showToast(e.message ?: "Error fetching logs")
        } finally {
            // Ensure the progress indicator and swipe refresh are updated in the Main thread after operation
            withContext(Dispatchers.Main) {
                binding.progresslayoutDateLogs.visibility = View.GONE
                binding.swipeToRefreshDateLogs.isRefreshing = false
            }
        }
    }

    // Helper function to show Toast messages on the main thread
    private fun showToast(message: String) = CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(this@LogsActivity, message, Toast.LENGTH_SHORT).show()
    }
}