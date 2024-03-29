package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.LeaveHistoryAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentHistoryBinding
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class HistoryFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var leaveRequestList=ArrayList<LeaveRequest>()
    private lateinit var leaveHistoryAdapter: LeaveHistoryAdapter
    private lateinit var tempArrayList: ArrayList<LeaveRequest>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    private lateinit var empid: String
    private lateinit var instituteName: String
    private lateinit var empfname: String
    private lateinit var empmname: String
    private lateinit var emplname: String
    private lateinit var empphno: String
    private lateinit var empemail: String
    private var currentCL: Double = 0.0
    private var currentHPL: Double = 0.0
    private var currentEL: Double = 0.0
    private lateinit var leaveRequestsStatusAdapter: ArrayAdapter<String>

    private val limit = 4
    private var isLoadingMore = false
    private var querySnapshot: QuerySnapshot? = null

    private var fragmentHistoryBinding: FragmentHistoryBinding? = null
    private val binding get() = fragmentHistoryBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentHistoryBinding = FragmentHistoryBinding.inflate(inflater, container, false)

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteId = sharedPref.getString("EmpInstituteId", "Your EmpID").toString()
            instituteName = sharedPref.getString("EmpInstituteName", "Your EmpID").toString()
            empid = sharedPref.getString("EmpID", "Your EmpID").toString()
            empfname = sharedPref.getString("FName", "Fname").toString()
            empmname = sharedPref.getString("MName", "Mname").toString()
            emplname = sharedPref.getString("LName", "Lname").toString()

            empphno = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()
            empemail = sharedPref.getString("EmailId", "EmailId").toString()
            currentCL = sharedPref.getString("CL", "0")?.toDouble()!!
            currentHPL = sharedPref.getString("HPL", "0")?.toDouble()!!
            currentEL = sharedPref.getString("EL", "0")?.toDouble()!!
        }


        layoutManager= LinearLayoutManager(activity as Context)
        tempArrayList = ArrayList()
        leaveRequestList = ArrayList()
        leaveHistoryAdapter = LeaveHistoryAdapter(activity as Context, instituteId, empid, empphno, tempArrayList)
        binding.recyclerviewAllLeaveRequests.adapter = leaveHistoryAdapter
        binding.recyclerviewAllLeaveRequests.layoutManager = layoutManager

        binding.DateHFLYT.setEndIconOnClickListener {
            binding.DateHF.text?.clear()
            binding.LeaveRequestsStatusSpinner.isEnabled = true
            if(binding.LeaveRequestsStatusSpinner.selectedItem.toString() == "All") getLeaveRequestList("")
            else  getLeaveRequestList(binding.LeaveRequestsStatusSpinner.selectedItem.toString())
        }
        binding.DateHF.setOnClickListener {
            showDatePicker(binding.DateHF)
        }

        binding.swipeToRefreshAllLeaves.setOnRefreshListener {
            if(binding.LeaveRequestsStatusSpinner.isEnabled) {
                if (binding.LeaveRequestsStatusSpinner.selectedItem.toString() == "All") getLeaveRequestList("")
                else getLeaveRequestList(binding.LeaveRequestsStatusSpinner.selectedItem.toString())
            }else{
                getDateLeaveRequestList(binding.DateHF.text.toString())
            }
        }

        binding.recyclerviewAllLeaveRequests.addOnScrollListener(scrollListener)

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
        return binding.root
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (binding.LeaveRequestsStatusSpinner.isEnabled){
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val isLastItem = visibleItemCount + firstVisibleItemPosition >= totalItemCount
                val isAtEnd = !recyclerView.canScrollVertically(1) // Check for scrolling down
                if (isAtEnd && !isLoadingMore) {
                    // Load more data logic here
                    isLoadingMore = true // Set flag to avoid multiple loads
                    if(binding.LeaveRequestsStatusSpinner.selectedItem.toString() == "All"){
                        loadMoreData()
                    }
                    else{
                        loadMoreData(binding.LeaveRequestsStatusSpinner.selectedItem.toString())
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreData(status: String? = null) {
        binding.progresslayoutHistoryFragment.visibility = View.VISIBLE
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Leaves")
        val lastDocument = querySnapshot?.documents?.lastOrNull()
        if(lastDocument == null){
            isLoadingMore = false
            binding.progresslayoutHistoryFragment.visibility = View.GONE
            return
        }

        val nextQuery = if (status != null) {
            dbRef.whereEqualTo("status", status).orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong()).startAfter(lastDocument)
        } else {
            dbRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong()).startAfter(lastDocument)
        }

        // Call Firebase to fetch next batch of data using nextQuery
        CoroutineScope(Dispatchers.IO).launch {
            try {
                querySnapshot = nextQuery.get().await()
                if (querySnapshot!!.isEmpty) {
                    // No more data available
                    return@launch
                }
                val nextLeaveRequestList = querySnapshot!!.toObjects(LeaveRequest::class.java)
                withContext(Dispatchers.Main) {
                    tempArrayList.addAll(nextLeaveRequestList)
                    binding.recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoadingMore = false // Reset flag after successful update
                withContext(Dispatchers.Main) {
                    binding.progresslayoutHistoryFragment.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDateLeaveRequestList(Date:String) = CoroutineScope(Dispatchers.IO).launch{
        withContext(Dispatchers.Main) {
            binding.progresslayoutHistoryFragment.visibility = View.VISIBLE
        }
        val query = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Leaves").whereEqualTo("date", Date).orderBy("timestamp", Query.Direction.DESCENDING)
        val newLeaveRequestList = mutableListOf<LeaveRequest>()

        try {
            querySnapshot = query.get().await()
            querySnapshot!!.documents.mapNotNullTo(newLeaveRequestList) { doc ->
                doc.toObject<LeaveRequest>()
            }
            withContext(Dispatchers.Main){
                leaveRequestList.clear()
                tempArrayList.clear()
            }
            if (newLeaveRequestList.isEmpty()) {
                UtilFunctions.showToast(activity as Context,"No leaves found")
            } else {
                val arrayList = ArrayList(newLeaveRequestList)
                withContext(Dispatchers.Main) {
                    leaveRequestList.addAll(arrayList)
                    tempArrayList.addAll(arrayList)
                }
            }
            withContext(Dispatchers.Main) {
                binding.recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            UtilFunctions.showToast(activity as Context, e.message ?: "Error fetching leave requests")
        } finally {
            withContext(Dispatchers.Main) {
                binding.progresslayoutHistoryFragment.visibility = View.GONE
                binding.swipeToRefreshAllLeaves.isRefreshing = false
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun getLeaveRequestList(status: String) = CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            binding.progresslayoutHistoryFragment.visibility = View.VISIBLE
        }
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Employees").document(empphno).collection("Leaves")
        val query = if (status.isNotBlank()) dbRef.whereEqualTo("status", status).orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong())
        else dbRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong())
        val newLeaveRequestList = mutableListOf<LeaveRequest>()

        try {
            querySnapshot = query.get().await()
            querySnapshot!!.documents.mapNotNullTo(newLeaveRequestList) { doc ->
                doc.toObject<LeaveRequest>()
            }
            withContext(Dispatchers.Main){
                leaveRequestList.clear()
                tempArrayList.clear()
            }
            if (newLeaveRequestList.isEmpty()) {
                UtilFunctions.showToast(activity as Context,"No leaves found")
            } else {
                val arrayList = ArrayList(newLeaveRequestList)
                withContext(Dispatchers.Main) {
                    leaveRequestList.addAll(arrayList)
                    tempArrayList.addAll(arrayList)
                }
            }
            withContext(Dispatchers.Main) {
                binding.recyclerviewAllLeaveRequests.adapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            UtilFunctions.showToast(activity as Context, e.message ?: "Error fetching leave requests")
        } finally {
            withContext(Dispatchers.Main) {
                binding.progresslayoutHistoryFragment.visibility = View.GONE
                binding.swipeToRefreshAllLeaves.isRefreshing = false
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
            requireContext(),
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formattedDate = selectedDate.format(
                    DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy",
                        Locale.getDefault()
                    )
                )
                view.setText(formattedDate)
                getDateLeaveRequestList(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
        binding.LeaveRequestsStatusSpinner.isEnabled = false
    }

    private fun exportXlsFile(leavesList: ArrayList<LeaveRequest>) = CoroutineScope(Dispatchers.IO).launch {
        val currentTimeMillis = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd_MM_yy__HH_mm_ss", Locale.getDefault())
        val folderName = dateFormat.format(Date(currentTimeMillis))
        val xlsFileName = "${empid}_$folderName.xls"
        val parentFolderName = "MLR"

        //val externalStorageDir = Environment.getExternalStorageDirectory()
        val externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create the parent folder if it doesn't exist
        val parentFolder = File(externalStorageDir, parentFolderName)
        if (!parentFolder.exists()) {
            parentFolder.mkdir()
        }

        // Create the subfolder within the parent folder
        val subFolder = File(parentFolder, "Individual Reports")
        if (!subFolder.exists()) {
            subFolder.mkdir()
        }

        // Create the XLS file inside the subfolder
        val xlsFile = File(subFolder, xlsFileName)
        val workbook: Workbook = HSSFWorkbook() // Use HSSFWorkbook for .xls format
        val empDetailsSheet: Sheet = workbook.createSheet("Employee Details")
        createEmployeeDetailsSheet(empDetailsSheet)
        val clSheet: Sheet = workbook.createSheet("CL")
        val hplSheet: Sheet = workbook.createSheet("HPL")
        val elSheet: Sheet = workbook.createSheet("EL")
        val allSheet: Sheet = workbook.createSheet("All")

        val headerRow1: Row = clSheet.createRow(0)
        val headerRow2: Row = hplSheet.createRow(0)
        val headerRow3: Row = elSheet.createRow(0)
        val headerRow4: Row = allSheet.createRow(0)
        val headerCells = arrayOf(
            "TimeStamp", "From", "From Session", "To", "To Session",
            "Leave Type", "No of leaves", "Reason", "Status"
        )

        for ((colIndex, header) in headerCells.withIndex()) {
            val cell1: Cell = headerRow1.createCell(colIndex)
            cell1.setCellValue(header)
            val cell2: Cell = headerRow2.createCell(colIndex)
            cell2.setCellValue(header)
            val cell3: Cell = headerRow3.createCell(colIndex)
            cell3.setCellValue(header)
            val cell4: Cell = headerRow4.createCell(colIndex)
            cell4.setCellValue(header)
        }

        var clRowNum = 1
        var hplRowNum = 1
        var elRowNum = 1
        var allRowNum = 1

        for (document in leavesList) {
            when (document.leavetype) {
                "CL" -> createRowWithDocumentData(clSheet, clRowNum++, document)
                "HPL" -> createRowWithDocumentData(hplSheet, hplRowNum++, document)
                "EL" -> createRowWithDocumentData(elSheet, elRowNum++, document)
            }
            createRowWithDocumentData(allSheet, allRowNum++, document)
        }
        val fileOut = FileOutputStream(xlsFile)
        workbook.write(fileOut)
        fileOut.close()
        withContext(Dispatchers.Main){
            Toast.makeText(activity as Context, "File exported to$parentFolder", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createEmployeeDetailsSheet(sheet: Sheet) {
        val headerRow1: Row = sheet.createRow(0)

        val headerCells = arrayOf(
            "Employee ID", "First Name", "Middle Name", "Last Name", "Current CL",
            "Current HPL", "Current EL"
        )

        for ((colIndex, header) in headerCells.withIndex()) {
            val cell1: Cell = headerRow1.createCell(colIndex)
            cell1.setCellValue(header)
        }

        val row: Row = sheet.createRow(1)
        row.createCell(0).setCellValue(empid)
        row.createCell(1).setCellValue(empfname)
        row.createCell(2).setCellValue(empmname)
        row.createCell(3).setCellValue(emplname)
        row.createCell(4).setCellValue(currentCL)
        row.createCell(5).setCellValue(currentHPL)
        row.createCell(6).setCellValue(currentEL)
    }

    private fun createRowWithDocumentData(sheet: Sheet, rowNum: Int, document: LeaveRequest) {
        val row: Row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue(document.timestamp.toString())
        row.createCell(1).setCellValue(document.fromdate.toString())
        row.createCell(2).setCellValue(document.fromsession.toString())
        row.createCell(3).setCellValue(document.todate.toString())
        row.createCell(4).setCellValue(document.tosession.toString())
        row.createCell(5).setCellValue(document.leavetype.toString())
        row.createCell(6).setCellValue(document.noofleaves.toString())
        row.createCell(7).setCellValue(document.reason.toString())
        row.createCell(8).setCellValue(document.status.toString())
    }
}