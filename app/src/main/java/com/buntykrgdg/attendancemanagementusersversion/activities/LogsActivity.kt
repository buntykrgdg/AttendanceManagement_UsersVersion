package com.buntykrgdg.attendancemanagementusersversion.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.CheckInOutLog
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.DateLogsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LogsActivity : AppCompatActivity() {
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeToRefreshDateLogs: SwipeRefreshLayout
    private lateinit var recyclerviewDatelogs: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var logsList=arrayListOf<CheckInOutLog>()
    private lateinit var logsAdapter: DateLogsAdapter
    private lateinit var tempArrayList: ArrayList<CheckInOutLog>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var date: String
    private lateinit var empId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        swipeToRefreshDateLogs=findViewById(R.id.swipeToRefreshDateLogs)
        recyclerviewDatelogs=findViewById(R.id.recyclerviewDateLogs)
        progressLayout=findViewById(R.id.progresslayoutDateLogs)
        progressBar=findViewById(R.id.progressbarDateLogs)
        layoutManager= LinearLayoutManager(this@LogsActivity)
        tempArrayList = ArrayList()
        logsList = ArrayList()

        date = intent.getStringExtra("date").toString()
        val sharedPref = getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empId = sharedPref.getString("EmpID", "Your EmpID").toString()
        }

        logsAdapter = DateLogsAdapter(this@LogsActivity, tempArrayList)
        recyclerviewDatelogs.adapter = logsAdapter
        recyclerviewDatelogs.layoutManager = layoutManager

        getLogList()

        swipeToRefreshDateLogs.setOnRefreshListener {
            getLogList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLogList() = CoroutineScope(Dispatchers.IO).launch {
        logsList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees")
            .document(empId).collection("Logs").document(date).collection("CheckInCheckOut")
        val querySnapshot = dbRef.get().await()
        Log.d("log", querySnapshot.toString())
        for(document in querySnapshot.documents){
            document.toObject<CheckInOutLog>()?.let { logsList.add(it) }
        }
        Log.d("log", logsList.toString())
        tempArrayList.addAll(logsList)
        UtilFunctions.sortCheckInCheckOutByTimestamp(tempArrayList)
        withContext(Dispatchers.Main){
            recyclerviewDatelogs.adapter?.notifyDataSetChanged()
            progressLayout.visibility = View.GONE
            swipeToRefreshDateLogs.isRefreshing = false
        }
    }
}