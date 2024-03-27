package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.AllLogsAdapter
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentLogsBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LogsFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var logsList = arrayListOf<String>()
    private lateinit var logsAdapter: AllLogsAdapter
    private lateinit var tempArrayList: ArrayList<String>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var empphno: String

    private val limit = 4
    private var isLoadingMore = false
    private var querySnapshot: QuerySnapshot? = null

    private var fragmentLogsBinding: FragmentLogsBinding? = null
    private val binding get() = fragmentLogsBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentLogsBinding = FragmentLogsBinding.inflate(inflater, container, false)
        layoutManager = LinearLayoutManager(activity)
        tempArrayList = ArrayList()
        logsList = ArrayList()

        val sharedPref =
            activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empphno = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()
        }

        logsAdapter = AllLogsAdapter(activity as Context, tempArrayList)
        binding.recyclerviewAllLogs.adapter = logsAdapter
        binding.recyclerviewAllLogs.layoutManager = layoutManager

        binding.recyclerviewAllLogs.addOnScrollListener(scrollListener)

        binding.DateHFLYT.setEndIconOnClickListener {
            binding.DateHF.text?.clear()
            getLogList()
        }
        binding.DateHF.setOnClickListener {
            showDatePicker(binding.DateHF)
        }

        getLogList()

        binding.swipeToRefreshAllLogs.setOnRefreshListener {
            if (binding.DateHF.text.toString() == "") {
                getLogList()
            } else {
                getDateLogsList(binding.DateHF.text.toString())
            }
        }
        return binding.root
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val isAtEnd = !recyclerView.canScrollVertically(1) // Check for scrolling down
            if (isAtEnd && !isLoadingMore) {
                isLoadingMore = true
                if (binding.DateHF.text.toString() == "") loadMoreData()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreData() {
        binding.progresslayoutAllLogs.visibility = View.VISIBLE
        val lastDocument = querySnapshot?.documents?.lastOrNull()
        if (lastDocument == null) {
            isLoadingMore = false
            binding.progresslayoutAllLogs.visibility = View.GONE
            return
        }
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Logs")
            .orderBy("Date", Query.Direction.DESCENDING).limit(limit.toLong())
            .startAfter(lastDocument)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                querySnapshot = dbRef.get().await()
                if (querySnapshot!!.isEmpty) {
                    // No more data available
                    return@launch
                }
                val nextLeaveRequestList = arrayListOf<String>()
                querySnapshot!!.documents.forEach { document ->
                    document.getString("Date")?.let { nextLeaveRequestList.add(it) }
                }
                withContext(Dispatchers.Main) {
                    tempArrayList.addAll(nextLeaveRequestList)
                    binding.recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoadingMore = false // Reset flag after successful update
                withContext(Dispatchers.Main) {
                    binding.progresslayoutAllLogs.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDateLogsList(Date: String) = CoroutineScope(Dispatchers.IO).launch {
        logsList.clear()
        tempArrayList.clear()
        val updatedLogsList = ArrayList<String>()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Logs").whereEqualTo("date", Date)
            .orderBy("Date", Query.Direction.DESCENDING).limit(limit.toLong())
        try {
            querySnapshot = dbRef.get().await()
            if (!querySnapshot!!.isEmpty) {
                querySnapshot!!.documents.forEach { document ->
                    document.getString("Date")?.let { updatedLogsList.add(it) }
                }
            } else {
                UtilFunctions.showToast(activity as Context, "No Logs found")
            }
        } catch (e: Exception) {
            UtilFunctions.showToast(activity as Context, e.message ?: "Error fetching logs")
        } finally {
            withContext(Dispatchers.Main) {
                logsList.addAll(updatedLogsList)
                tempArrayList.addAll(updatedLogsList)
                binding.recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                binding.progresslayoutAllLogs.visibility = View.GONE
                binding.swipeToRefreshAllLogs.isRefreshing = false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLogList() = CoroutineScope(Dispatchers.IO).launch {
        logsList.clear()
        tempArrayList.clear()
        val updatedLogsList = ArrayList<String>()
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Logs")
            .orderBy("Date", Query.Direction.DESCENDING).limit(limit.toLong())
        try {
            querySnapshot = dbRef.get().await()
            if (!querySnapshot!!.isEmpty) {
                querySnapshot!!.documents.forEach { document ->
                    document.getString("Date")?.let { updatedLogsList.add(it) }
                }
            } else {
                UtilFunctions.showToast(activity as Context, "No Logs found")
            }
        } catch (e: Exception) {
            UtilFunctions.showToast(activity as Context, e.message ?: "Error fetching logs")
        } finally {
            withContext(Dispatchers.Main) {
                logsList.addAll(updatedLogsList)
                tempArrayList.addAll(updatedLogsList)
                binding.recyclerviewAllLogs.adapter?.notifyDataSetChanged()
                binding.progresslayoutAllLogs.visibility = View.GONE
                binding.swipeToRefreshAllLogs.isRefreshing = false
            }
        }
    }

    private fun showDatePicker(view: EditText) {
        val c = android.icu.util.Calendar.getInstance()
        val year = c.get(android.icu.util.Calendar.YEAR)
        val month = c.get(android.icu.util.Calendar.MONTH)
        val day = c.get(android.icu.util.Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog with custom style
        val datePickerDialog = DatePickerDialog(
            activity as Context,
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formattedDate = selectedDate.format(
                    DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy",
                        Locale.getDefault()
                    )
                )
                view.setText(formattedDate)
                val date = UtilFunctions.stringToMillis(formattedDate).toString()
                getDateLogsList(date)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}