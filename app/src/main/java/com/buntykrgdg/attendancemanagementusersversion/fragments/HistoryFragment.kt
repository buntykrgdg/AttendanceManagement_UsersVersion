package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.LeaveHistoryAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentHistoryBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class HistoryFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var leaveRequestList=arrayListOf<LeaveRequest>()
    private lateinit var leaveHistoryAdapter: LeaveHistoryAdapter
    private lateinit var tempArrayList: ArrayList<LeaveRequest>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var empId: String
    private lateinit var empphno: String
    private lateinit var leaveRequestsStatusAdapter: ArrayAdapter<String>

    private var fragmentHistoryBinding: FragmentHistoryBinding? = null
    private val binding get() = fragmentHistoryBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentHistoryBinding = FragmentHistoryBinding.inflate(inflater, container, false)

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empId = sharedPref.getString("EmpID", "Your EmpID").toString()
            empphno = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()
        }

        leaveHistoryAdapter = LeaveHistoryAdapter(activity as Context, instituteId, empId, empphno, tempArrayList)
        binding.recyclerviewAllLeaveRequests.adapter = leaveHistoryAdapter
        binding.recyclerviewAllLeaveRequests.layoutManager = layoutManager

        binding.swipeToRefreshAllLeaves.setOnRefreshListener {
            if(binding.LeaveRequestsStatusSpinner.selectedItem.toString() == "All") getLeaveRequestList("")
            else  getLeaveRequestList(binding.LeaveRequestsStatusSpinner.selectedItem.toString())
        }

        val statusOptions = listOf("All", "Accepted", "Rejected", "Pending")
        leaveRequestsStatusAdapter = ArrayAdapter(activity as Context, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        binding.LeaveRequestsStatusSpinner.adapter = leaveRequestsStatusAdapter
        binding.LeaveRequestsStatusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(statusOptions[position]){
                    "All" -> getLeaveRequestList("")
                    "Accepted" -> getLeaveRequestList("Accepted")
                    "Rejected" -> getLeaveRequestList("Rejected")
                    "Pending" -> getLeaveRequestList("Pending")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.searchviewAllLeaveRequests.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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
                    binding.recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(leaveRequestList)
                    binding.recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLeaveRequestList(status: String) = CoroutineScope(Dispatchers.IO).launch {
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Leaves")
        val query = if (status.isNotBlank()) dbRef.whereEqualTo("status", status) else dbRef
        val newLeaveRequestList = mutableListOf<LeaveRequest>()

        try {
            val querySnapshot = query.get().await()
            querySnapshot.documents.mapNotNullTo(newLeaveRequestList) { doc ->
                doc.toObject<LeaveRequest>()
            }
            withContext(Dispatchers.Main){
                leaveRequestList.clear()
                tempArrayList.clear()
            }
            if (newLeaveRequestList.isEmpty()) {
                showToast("No leaves found")
            } else {
                val arrayList = ArrayList(newLeaveRequestList)
                UtilFunctions.sortLeaveRequestByTimestamp(arrayList)
                withContext(Dispatchers.Main) {
                    leaveRequestList.addAll(arrayList)
                    tempArrayList.addAll(arrayList)
                }
            }
            withContext(Dispatchers.Main) {
                binding.recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            showToast(e.message ?: "Error fetching leave requests")
        } finally {
            withContext(Dispatchers.Main) {
                binding.progresslayoutHistoryFragment.visibility = View.GONE
                binding.swipeToRefreshAllLeaves.isRefreshing = false
            }
        }
    }

    // Helper function to show Toast messages on the main thread
    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(activity as Context, message, Toast.LENGTH_SHORT).show()
        }
    }
}