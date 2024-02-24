package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.AllLogsAdapter
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentLogsBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class LogsFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var logsList=arrayListOf<String>()
    private lateinit var logsAdapter: AllLogsAdapter
    private lateinit var tempArrayList: ArrayList<String>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var empphno: String

    private var fragmentLogsBinding: FragmentLogsBinding? = null
    private val binding get() = fragmentLogsBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentLogsBinding = FragmentLogsBinding.inflate(inflater, container, false)
        layoutManager= LinearLayoutManager(activity)
        tempArrayList = ArrayList()
        logsList = ArrayList()

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empphno = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()
        }

        logsAdapter = AllLogsAdapter(activity as Context, tempArrayList)
        binding.recyclerviewAllLogs.adapter = logsAdapter
        binding.recyclerviewAllLogs.layoutManager = layoutManager

        getLogList()

        binding.swipeToRefreshAllLogs.setOnRefreshListener {
           getLogList()
        }

        binding.searchviewAllLogs.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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
                    binding.recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(logsList)
                    binding.recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLogList() = CoroutineScope(Dispatchers.IO).launch {
        val updatedLogsList = ArrayList<String>()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Logs")

        try {
            val querySnapshot = dbRef.get().await()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.forEach { document ->
                    document.getString("Date")?.let { updatedLogsList.add(it) }
                }
                UtilFunctions.sortLeaveRequestByTimestamp2(updatedLogsList)
                withContext(Dispatchers.Main) {
                    logsList.clear()
                    tempArrayList.clear()
                    logsList.addAll(updatedLogsList)
                    tempArrayList.addAll(updatedLogsList)
                    binding.recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                }
            } else {
                showToast("No Logs found")
            }
        } catch (e: Exception) {
            showToast(e.message ?: "Error fetching logs")
        } finally {
            withContext(Dispatchers.Main) {
                binding.progresslayoutAllLogs.visibility = View.GONE
                binding.swipeToRefreshAllLogs.isRefreshing = false
            }
        }
    }

    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(activity as Context, message, Toast.LENGTH_SHORT).show()
        }
    }

}