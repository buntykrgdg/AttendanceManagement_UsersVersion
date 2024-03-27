package com.buntykrgdg.attendancemanagementusersversion.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.DateLogsAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.CheckInOutLog
import com.buntykrgdg.attendancemanagementusersversion.databinding.ActivityLogsBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private lateinit var empPhNo: String
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
            empPhNo = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()
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
        logsList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empPhNo)
            .collection("Logs").document(date)
            .collection("CheckInCheckOut")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        try {
            val querySnapshot = dbRef.get().await()
            if (!querySnapshot.isEmpty) {
                logsList.addAll(querySnapshot.documents.mapNotNull { it.toObject<CheckInOutLog>() })
                tempArrayList.addAll(logsList)
                withContext(Dispatchers.Main) {
                    binding.recyclerviewDateLogs.adapter?.notifyDataSetChanged()
                }
            } else {
                UtilFunctions.showToast(this@LogsActivity,"No logs found")
            }
        } catch (e: Exception) {
            UtilFunctions.showToast(this@LogsActivity, e.message ?: "Error fetching logs")
        } finally {
            withContext(Dispatchers.Main) {
                binding.progresslayoutDateLogs.visibility = View.GONE
                binding.swipeToRefreshDateLogs.isRefreshing = false
            }
        }
    }
}