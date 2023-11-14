package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.AllLogsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.ArrayList
import java.util.Locale

class LogsFragment : Fragment() {
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeToRefreshAllLogs: SwipeRefreshLayout
    private lateinit var recyclerviewAllLogs: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var logsList=arrayListOf<String>()
    private lateinit var logsAdapter: AllLogsAdapter
    private lateinit var searchView: SearchView
    private lateinit var tempArrayList: ArrayList<String>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var empId: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logs, container, false)
        swipeToRefreshAllLogs=view.findViewById(R.id.swipeToRefreshAllLogs)
        recyclerviewAllLogs=view.findViewById(R.id.recyclerviewAllLogs)
        progressLayout=view.findViewById(R.id.progresslayoutAllLogs)
        progressBar=view.findViewById(R.id.progressbarAllLogs)
        layoutManager= LinearLayoutManager(activity)
        tempArrayList = ArrayList()
        logsList = ArrayList()
        searchView = view.findViewById(R.id.searchviewAllLogs)

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empId = sharedPref.getString("EmpID", "Your EmpID").toString()
        }

        logsAdapter = AllLogsAdapter(activity as Context, tempArrayList)
        recyclerviewAllLogs.adapter = logsAdapter
        recyclerviewAllLogs.layoutManager = layoutManager

        getLogList()

        swipeToRefreshAllLogs.setOnRefreshListener {
           getLogList()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                tempArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())

                if (searchText.isNotEmpty()){
                    logsList.forEach{
                        if (it.lowercase(Locale.getDefault()).contains(searchText)){
                            tempArrayList.add(it)
                        }
                    }
                    recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(logsList)
                    recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLogList() = CoroutineScope(Dispatchers.IO).launch {
        logsList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees")
            .document(empId).collection("Logs")//.document("13 Nov 2023").collection("CheckInCheckOut")
        val querySnapshot = dbRef.get().await()
        Log.d("log", querySnapshot.toString())
        for(document in querySnapshot.documents){
            logsList.add(document.get("Date").toString())
        }
        Log.d("log", logsList.toString())
        tempArrayList.addAll(logsList)
        UtilFunctions.sortLeaveRequestByTimestamp2(tempArrayList)
        withContext(Dispatchers.Main){
            recyclerviewAllLogs.adapter?.notifyDataSetChanged()
            progressLayout.visibility = View.GONE
            swipeToRefreshAllLogs.isRefreshing = false
        }
    }
}