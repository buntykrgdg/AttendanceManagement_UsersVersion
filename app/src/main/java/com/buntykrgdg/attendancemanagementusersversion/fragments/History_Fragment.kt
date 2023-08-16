package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buntykrgdg.attendancemanagementusersversion.classes.LeaveHistoryAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class History_Fragment : Fragment() {

    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeToRefreshAllLeaves: SwipeRefreshLayout
    private lateinit var recyclerviewAllLeaveRequests: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var LeaveRequestList=arrayListOf<LeaveRequest>()
    private lateinit var LeaveHistoryAdapter: LeaveHistoryAdapter
    private lateinit var searchView: SearchView
    private lateinit var tempArrayList: ArrayList<LeaveRequest>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteid: String
    private lateinit var empid: String

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
        LeaveRequestList = ArrayList()
        searchView = view.findViewById(R.id.searchviewAllLeaveRequests)
        LeaveHistoryAdapter = LeaveHistoryAdapter(activity as Context, tempArrayList)
        recyclerviewAllLeaveRequests.adapter = LeaveHistoryAdapter
        recyclerviewAllLeaveRequests.layoutManager = layoutManager

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteid = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empid = sharedPref.getString("EmpID", "Your EmpID").toString()
        }

        refreshLeaves()
        getLeaveRequestList()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                tempArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())

                if (searchText.isNotEmpty()){
                    LeaveRequestList.forEach{
                        if (it.timestamp?.lowercase(Locale.getDefault())?.contains(searchText) == true){
                            tempArrayList.add(it)
                        }
                    }
                    recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(LeaveRequestList)
                    recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return view
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun getLeaveRequestList() = CoroutineScope(Dispatchers.IO).launch {
        LeaveRequestList.clear()
        tempArrayList.clear()
        val dbref = db.collection("Institutions").document(instituteid)
            .collection("Employees")
            .document(empid).collection("Leaves").orderBy("timestamp", Query.Direction.DESCENDING)
        val querySnapshot = dbref.get().await()
        for(document in querySnapshot.documents){
            document.toObject<LeaveRequest>()?.let { LeaveRequestList.add(it) }
        }
        tempArrayList.addAll(LeaveRequestList)
        withContext(Dispatchers.Main){
            recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
            progressLayout.visibility = View.GONE
            swipeToRefreshAllLeaves.isRefreshing = false
        }
    }

    private fun refreshLeaves(){
        swipeToRefreshAllLeaves.setOnRefreshListener {
            getLeaveRequestList()
        }
    }

}