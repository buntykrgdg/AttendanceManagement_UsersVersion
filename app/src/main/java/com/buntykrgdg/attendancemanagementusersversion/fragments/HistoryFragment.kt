package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.LeaveHistoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class HistoryFragment : Fragment() {
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeToRefreshAllLeaves: SwipeRefreshLayout
    private lateinit var recyclerviewAllLeaveRequests: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var leaveRequestList=arrayListOf<LeaveRequest>()
    private lateinit var leaveHistoryAdapter: LeaveHistoryAdapter
    private lateinit var searchView: SearchView
    private lateinit var tempArrayList: ArrayList<LeaveRequest>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var empId: String
    private lateinit var leaveRequestsStatusSpinner: Spinner
    private lateinit var leaveRequestsStatusAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_, container, false)
        swipeToRefreshAllLeaves=view.findViewById(R.id.swipeToRefreshAllLeaves)
        recyclerviewAllLeaveRequests=view.findViewById(R.id.recyclerviewAllLeaveRequests)
        progressLayout=view.findViewById(R.id.progresslayoutHistoryFragment)
        progressBar=view.findViewById(R.id.progressbarHistoryFragment)
        layoutManager= LinearLayoutManager(activity)
        tempArrayList = ArrayList()
        leaveRequestList = ArrayList()
        searchView = view.findViewById(R.id.searchviewAllLeaveRequests)
        leaveRequestsStatusSpinner = view.findViewById(R.id.LeaveRequestsStatusSpinner)

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empId = sharedPref.getString("EmpID", "Your EmpID").toString()
        }

        leaveHistoryAdapter = LeaveHistoryAdapter(activity as Context, instituteId, empId, tempArrayList)
        recyclerviewAllLeaveRequests.adapter = leaveHistoryAdapter
        recyclerviewAllLeaveRequests.layoutManager = layoutManager

        swipeToRefreshAllLeaves.setOnRefreshListener {
            if(leaveRequestsStatusSpinner.selectedItem.toString() == "All") getLeaveRequestList()
            else  getLeaveRequestListWithStatus(leaveRequestsStatusSpinner.selectedItem.toString())
        }

        val statusOptions = listOf("All", "Accepted", "Rejected", "Pending")
        leaveRequestsStatusAdapter = ArrayAdapter(activity as Context, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        leaveRequestsStatusSpinner.adapter = leaveRequestsStatusAdapter
        leaveRequestsStatusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(statusOptions[position]){
                    "All" -> getLeaveRequestList()
                    "Accepted" -> getLeaveRequestListWithStatus("Accepted")
                    "Rejected" -> getLeaveRequestListWithStatus("Rejected")
                    "Pending" -> getLeaveRequestListWithStatus("Pending")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
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
                    leaveRequestList.forEach{
                        if (it.timestamp?.lowercase(Locale.getDefault())?.contains(searchText) == true){
                            tempArrayList.add(it)
                        }
                    }
                    recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(leaveRequestList)
                    recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLeaveRequestList() = CoroutineScope(Dispatchers.IO).launch {
        leaveRequestList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees")
            .document(empId).collection("Leaves")
        val querySnapshot = dbRef.get().await()
        for(document in querySnapshot.documents){
            document.toObject<LeaveRequest>()?.let { leaveRequestList.add(it) }
        }
        tempArrayList.addAll(leaveRequestList)
        UtilFunctions.sortLeaveRequestByTimestamp(tempArrayList)
        withContext(Dispatchers.Main){
            recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
            progressLayout.visibility = View.GONE
            swipeToRefreshAllLeaves.isRefreshing = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLeaveRequestListWithStatus(status: String) = CoroutineScope(Dispatchers.IO).launch {
        leaveRequestList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees")
            .document(empId).collection("Leaves")
            .whereEqualTo("status", status)
        val querySnapshot = dbRef.get().await()
        for(document in querySnapshot.documents){
            document.toObject<LeaveRequest>()?.let { leaveRequestList.add(it) }
        }
        tempArrayList.addAll(leaveRequestList)
        UtilFunctions.sortLeaveRequestByTimestamp(tempArrayList)
        withContext(Dispatchers.Main){
            recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
            progressLayout.visibility = View.GONE
            swipeToRefreshAllLeaves.isRefreshing = false
        }
    }
}