package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.CheckInOutLog
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.activities.LoginActivity
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Employee
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.classes.Leaves
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentNewRequestBinding
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentProfileBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class NewRequestFragment : Fragment() {
    private var fragmentNewRequestBinding: FragmentNewRequestBinding? = null
    private val binding get() = fragmentNewRequestBinding!!
    
    private var database = FirebaseFirestore.getInstance()
    private var currentCL: Double = 0.0
    private var currentHPL: Double = 0.0
    private var currentEL: Double = 0.0

    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("hh:mm:ss a")

    private lateinit var instituteid: String
    private lateinit var institutename: String
    private lateinit var empid: String
    private lateinit var empname: String
    private lateinit var empdepartment: String
    private lateinit var empdesignation: String
    private lateinit var empphno: String
    private lateinit var empStatus: String

    private lateinit var halfdaysession: String
    private lateinit var halfdayleavetype: String
    private lateinit var halfdaynoofleaves: String
    private lateinit var onedayleavetype: String
    private lateinit var onedaynoofleaves: String
    private lateinit var morethan1dayleavetype: String
    private lateinit var morethan1daynoofleaves: String

    private val daysfinder = Leaves()

    private var MorethanOnedayFromselected: String = "null"
    private var MorethanOnedayToselected: String = "null"

    private lateinit var checkOutReasonAdapter: ArrayAdapter<String>
    

    private val firebaseauth = FirebaseAuth.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentNewRequestBinding = FragmentNewRequestBinding.inflate(inflater, container, false)

        val view = inflater.inflate(R.layout.fragment_blank, container, false)

        binding.txtDate.text = getDate()
        binding.txtDay.text = getDay()
        binding.txtSession.text = getSession()
        
        val reasons = listOf("Bank", "Home", "Emergency", "Others")
        checkOutReasonAdapter = ArrayAdapter(
            activity as Context,
            android.R.layout.simple_spinner_dropdown_item,
            reasons
        )
        binding.CheckOutReasonSpinner.adapter = checkOutReasonAdapter


        binding.btnCheckIn.setOnClickListener {
            updateStatus("Checked In")
            updateLog("---", "Checked In")
        }
        binding.btnCheckOut.setOnClickListener {
            updateStatus("Checked Out")
            updateLog(binding.CheckOutReasonSpinner.selectedItem.toString(), "Checked Out")
        }

        val firstFragment = LogsFragment()
        binding.txtMyLogs.setOnClickListener {
            setCurrentFragment(firstFragment)
        }

        val sharedPref =
            activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            Log.d("instituteid", "Not Null")
            instituteid = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            institutename = sharedPref.getString("EmpInstituteName", "Your InsName").toString()
            empid = sharedPref.getString("EmpID", "Your EmpID").toString()
            val employeefname = sharedPref.getString("FName", "Fname")
            val employeemname = sharedPref.getString("MName", "Mname")
            val employeelname = sharedPref.getString("LName", "Lname")
            empname = "$employeefname $employeemname $employeelname"
            empdepartment = sharedPref.getString("Department", "Department").toString()
            empdesignation = sharedPref.getString("Designation", "Designation").toString()
            empphno = sharedPref.getString("PhoneNumber", "PhoneNumber").toString()

            empStatus = sharedPref.getString("status", "Checked Out").toString()
            currentCL = sharedPref.getString("CL", "0")?.toDouble()!!
            currentHPL = sharedPref.getString("HPL", "0")?.toDouble()!!
            currentEL = sharedPref.getString("EL", "0")?.toDouble()!!

            FirebaseMessaging.getInstance().subscribeToTopic(instituteid)

            if (binding.txtSession.text.toString() != "No session running" &&
            binding.txtSession.text.toString() != "Holiday" &&
            binding.txtSession.text.toString() != "Break"
            ) {
                getCurrentStatusFromSP()
                getCurrentStatus()
            } else {
                binding.CheckOutReasonSpinner.isEnabled = false
                binding.btnCheckIn.isEnabled = false
                binding.btnCheckOut.isEnabled = false
                binding.txtCheckInOutStatus.text = binding.txtSession.text.toString()
            }
            updateRemainingLeaves()
            getEmployeeDetails()
        }
        getRemainingLeaves()

        binding.RBHalfDay.isChecked = true
        binding.RLHalfDay.visibility = View.VISIBLE
        binding.RLOneDay.visibility = View.GONE
        binding.RLMorethan1.visibility = View.GONE

        binding.RDLeaveRange.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.RBHalfDay -> {
                    binding.RLHalfDay.visibility = View.VISIBLE
                    binding.RLOneDay.visibility = View.GONE
                    binding.RLMorethan1.visibility = View.GONE
                }

                R.id.RBOneDay -> {
                    binding.RLHalfDay.visibility = View.GONE
                    binding.RLOneDay.visibility = View.VISIBLE
                    binding.RLMorethan1.visibility = View.GONE
                }

                R.id.RBMorethanOne -> {
                    binding.RLHalfDay.visibility = View.GONE
                    binding.RLOneDay.visibility = View.GONE
                    binding.RLMorethan1.visibility = View.VISIBLE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        binding.RGHalfDay.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.HalfDaymorningRadioBtn -> {
                    halfdaysession = "morning"
                }

                R.id.HalfDayafternoonRadioBtn -> {
                    halfdaysession = "afternoon"
                }
            }
        }

        binding.halfdayleavedate.setOnClickListener {//Half day leave
            showDatePicker(binding.halfdayleavedate)
        }
        binding.btnHalfDaydate.setOnClickListener {//Half day leave
            showDatePicker(binding.halfdayleavedate)
        }

        binding.RGHalfDayLeaveTypeSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.HalfDayCLRadioBtn -> {
                    // User selected CL
                    halfdayleavetype = "CL"
                    binding.txtHalfDayNoofleaves.text = daysfinder.HalfDayLeave()[0].toString()
                    halfdaynoofleaves = daysfinder.HalfDayLeave()[0].toString()
                    if (currentCL < halfdaynoofleaves.toDouble()) {
                        binding.txtHalfDayNote.visibility = View.VISIBLE
                        binding.txtHalfDayNote.text = "You do not have sufficient CL's"
                    } else binding.txtHalfDayNote.visibility = View.GONE
                }

                R.id.HalfDayHPLRadioBtn -> {
                    // User selected HPL
                    halfdayleavetype = "HPL"
                    binding.txtHalfDayNoofleaves.text = daysfinder.HalfDayLeave()[1].toString()
                    halfdaynoofleaves = daysfinder.HalfDayLeave()[1].toString()
                    if (currentHPL < halfdaynoofleaves.toDouble()) {
                        binding.txtHalfDayNote.visibility = View.VISIBLE
                        binding.txtHalfDayNote.text = "You do not have sufficient HPL's"
                    } else binding.txtHalfDayNote.visibility = View.GONE
                }

                R.id.HalfDayELRadioBtn -> {
                    // User selected EL
                    halfdayleavetype = "EL"
                    binding.txtHalfDayNoofleaves.text = daysfinder.HalfDayLeave()[2].toString()
                    halfdaynoofleaves = daysfinder.HalfDayLeave()[2].toString()
                    if (currentEL < halfdaynoofleaves.toDouble()) {
                        binding.txtHalfDayNote.visibility = View.VISIBLE
                        binding.txtHalfDayNote.text = "You do not have sufficient EL's"
                    } else binding.txtHalfDayNote.visibility = View.GONE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        binding.btnSendRequestHalfDay.setOnClickListener {
            if (binding.halfdayleavedate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.RGHalfDay.checkedRadioButtonId == -1) {
                Toast.makeText(activity as Context, "Please select session", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.RGHalfDayLeaveTypeSelection.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select type of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.ETHalfDayleaveReason.text.toString() == "") {
                Toast.makeText(activity as Context, "Please enter the reason", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.txtHalfDayNote.visibility == View.VISIBLE) {
                Toast.makeText(
                    activity as Context,
                    "You don't have sufficient leaves",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val currentTimeMillis = System.currentTimeMillis()
                val sdf = SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss a", Locale.getDefault())
                val formattedDateTime = sdf.format(Date(currentTimeMillis))
                val databaseref1 =
                    Firebase.firestore.collection("Institutions/${instituteid}/Leaves")
                        .document(formattedDateTime)
                val databaseref2 =
                    Firebase.firestore.collection("Institutions/$instituteid/Employees/$empphno/Leaves")
                        .document(formattedDateTime)
                val leaveRequest = LeaveRequest(
                    formattedDateTime,
                    empphno, empid, instituteid, empname, empdepartment, empdesignation,
                    binding.halfdayleavedate.text.toString(),
                    halfdaysession,
                    binding.halfdayleavedate.text.toString(),
                    halfdaysession, halfdayleavetype,
                    halfdaynoofleaves, binding.ETHalfDayleaveReason.text.toString(), "", "Pending"
                )

                binding.halfdayleavedate.text.clear()
                binding.ETHalfDayleaveReason.text.clear()
                binding.RGHalfDayLeaveTypeSelection.clearCheck()
                binding.RGHalfDay.clearCheck()
                binding.txtHalfDayNoofleaves.text = "0"
                halfdaysession = ""
                halfdayleavetype = ""
                halfdaynoofleaves = ""
                sendleaverequest(leaveRequest, databaseref1)
                sendleaverequest(leaveRequest, databaseref2)
            }
        }

        binding.onedayleavedate.setOnClickListener {// One day leave
            showDatePicker(binding.onedayleavedate)
        }

        binding.btnSelectonedayleavedate.setOnClickListener {//Half day leave
            showDatePicker(binding.onedayleavedate)
        }

        binding.RGOneDayLeaveTypeSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.OneDayCLRadioBtn -> {
                    // User selected CL
                    onedayleavetype = "CL"
                    binding.txtOneDayNoofleaves.text = daysfinder.FullDayLeave()[0].toString()
                    onedaynoofleaves = daysfinder.FullDayLeave()[0].toString()
                    if (currentCL < onedaynoofleaves.toDouble()) {
                        binding.txtOneDayNote.visibility = View.VISIBLE
                        binding.txtOneDayNote.text = "You do not have sufficient CL's"
                    } else binding.txtOneDayNote.visibility = View.GONE
                }

                R.id.OneDayHPLRadioBtn -> {
                    // User selected HPL
                    onedayleavetype = "HPL"
                    binding.txtOneDayNoofleaves.text = daysfinder.FullDayLeave()[1].toString()
                    onedaynoofleaves = daysfinder.FullDayLeave()[1].toString()
                    if (currentHPL < onedaynoofleaves.toDouble()) {
                        binding.txtOneDayNote.visibility = View.VISIBLE
                        binding.txtOneDayNote.text = "You do not have sufficient HPL's"
                    } else binding.txtOneDayNote.visibility = View.GONE
                }

                R.id.OneDayELRadioBtn -> {
                    // User selected EL
                    onedayleavetype = "EL"
                    binding.txtOneDayNoofleaves.text = daysfinder.FullDayLeave()[2].toString()
                    onedaynoofleaves = daysfinder.FullDayLeave()[2].toString()
                    if (currentEL < onedaynoofleaves.toDouble()) {
                        binding.txtOneDayNote.visibility = View.VISIBLE
                        binding.txtOneDayNote.text = "You do not have sufficient EL's"
                    } else binding.txtOneDayNote.visibility = View.GONE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        binding.btnSendRequestOneDay.setOnClickListener {
            if (binding.onedayleavedate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.RGOneDayLeaveTypeSelection.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select type of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.ETonedayleaveReason.text.toString() == "") {
                Toast.makeText(activity as Context, "Please enter the reason", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.txtOneDayNote.visibility == View.VISIBLE) {
                Toast.makeText(
                    activity as Context,
                    "You don't have sufficient leaves",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val currentTimeMillis = System.currentTimeMillis()
                val sdf = SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss a", Locale.getDefault())
                val formattedDateTime = sdf.format(Date(currentTimeMillis))
                val databaseref1 =
                    Firebase.firestore.collection("Institutions/${instituteid}/Leaves")
                        .document(formattedDateTime)
                val databaseref2 =
                    Firebase.firestore.collection("Institutions/$instituteid/Employees/$empphno/Leaves")
                        .document(formattedDateTime)
                val leaveRequest = LeaveRequest(
                    formattedDateTime,
                    empphno, empid, instituteid, empname, empdepartment, empdesignation,
                    binding.onedayleavedate.text.toString(),
                    "morning",
                    binding.onedayleavedate.text.toString(),
                    "afternoon", onedayleavetype,
                    onedaynoofleaves, binding.ETonedayleaveReason.text.toString(), "", "Pending"
                )

                binding.onedayleavedate.text.clear()
                binding.ETonedayleaveReason.text.clear()
                binding.RGOneDayLeaveTypeSelection.clearCheck()
                onedayleavetype = ""
                onedaynoofleaves = ""
                sendleaverequest(leaveRequest, databaseref1)
                sendleaverequest(leaveRequest, databaseref2)
            }
        }

        binding.leavefromdate.setOnClickListener {// More than 1 day leave
            showDatePicker(binding.leavefromdate)
        }
        binding.leavetodate.setOnClickListener {// More than 1 day leave
            showDatePicker(binding.leavetodate)
        }
        binding.btnSelectfromdate.setOnClickListener {// More than 1 day leave
            showDatePicker(binding.leavefromdate)
        }
        binding.btnSelecttodate.setOnClickListener {
            showDatePicker(binding.leavetodate)
        }

        binding.RGradiofrom.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.FrommorningRadioBtn -> {
                    // User selected morning session
                    MorethanOnedayFromselected = "morning"
                    updateNoOfLeaves()
                }

                R.id.FromafternoonRadioBtn -> {
                    // User selected afternoon session
                    MorethanOnedayFromselected = "afternoon"
                    updateNoOfLeaves()
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        binding.RGradioto.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.TomorningRadioBtn -> {
                    // User selected morning session
                    MorethanOnedayToselected = "morning"
                    updateNoOfLeaves()
                }

                R.id.ToafternoonRadioBtn -> {
                    // User selected afternoon session
                    MorethanOnedayToselected = "afternoon"
                    updateNoOfLeaves()
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        binding.RGMorethanOneDayLeaveTypeSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.MorethanOneDayCLRadioBtn -> {
                    // User selected CL
                    morethan1dayleavetype = "CL"
                    morethan1daynoofleaves = daysfinder.MorethanoneLeave(
                        binding.leavefromdate.text.toString(),
                        MorethanOnedayFromselected,
                        binding.leavetodate.text.toString(),
                        MorethanOnedayToselected
                    )[0].toString()
                    binding.txtMorethanOneDayNoofleaves.text = morethan1daynoofleaves
                    if (currentCL < morethan1daynoofleaves.toDouble()) {
                        binding.txtMoreThanOneDayNote.visibility = View.VISIBLE
                        binding.txtMoreThanOneDayNote.text = "You do not have sufficient CL's"
                    } else binding.txtMoreThanOneDayNote.visibility = View.GONE
                }

                R.id.MorethanOneDayCLRadioBtn -> {
                    // User selected HPL
                    morethan1dayleavetype = "HPL"
                    morethan1daynoofleaves = daysfinder.MorethanoneLeave(
                        binding.leavefromdate.text.toString(),
                        MorethanOnedayFromselected,
                        binding.leavetodate.text.toString(),
                        MorethanOnedayToselected
                    )[1].toString()
                    binding.txtMorethanOneDayNoofleaves.text = morethan1daynoofleaves
                    if (currentHPL < morethan1daynoofleaves.toDouble()) {
                        binding.txtMoreThanOneDayNote.visibility = View.VISIBLE
                        binding.txtMoreThanOneDayNote.text = "You do not have sufficient HPL's"
                    } else binding.txtMoreThanOneDayNote.visibility = View.GONE
                }

                R.id.MorethanOneDayELRadioBtn -> {
                    // User selected EL
                    morethan1dayleavetype = "EL"
                    morethan1daynoofleaves = daysfinder.MorethanoneLeave(
                        binding.leavefromdate.text.toString(),
                        MorethanOnedayFromselected,
                        binding.leavetodate.text.toString(),
                        MorethanOnedayToselected
                    )[2].toString()
                    binding.txtMorethanOneDayNoofleaves.text = morethan1daynoofleaves
                    if (currentEL < morethan1daynoofleaves.toDouble()) {
                        binding.txtMoreThanOneDayNote.visibility = View.VISIBLE
                        binding.txtMoreThanOneDayNote.text = "You do not have sufficient EL's"
                    } else binding.txtMoreThanOneDayNote.visibility = View.GONE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        binding.btnSendRequestMorethan1.setOnClickListener {
            if (binding.leavefromdate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the first day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.RGradiofrom.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select session of From date",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.leavetodate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the last day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.RGradioto.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select session of To date",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.RGMorethanOneDayLeaveTypeSelection.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select type of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.ETleaveReason.text.toString() == "") {
                Toast.makeText(activity as Context, "Please enter the reason", Toast.LENGTH_SHORT)
                    .show()
            } else if (daysfinder.isToDateBeforeFromDate(
                    binding.leavefromdate.text.toString(),
                    binding.leavetodate.text.toString()
                )
            ) {
                Toast.makeText(
                    activity as Context,
                    "Invalid 'From' and 'To' date",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.txtMoreThanOneDayNote.visibility == View.VISIBLE) {
                Toast.makeText(
                    activity as Context,
                    "You don't have sufficient leaves",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val currentTimeMillis = System.currentTimeMillis()
                val sdf = SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss a", Locale.getDefault())
                val formattedDateTime = sdf.format(Date(currentTimeMillis))
                val databaseref1 =
                    Firebase.firestore.collection("Institutions/${instituteid}/Leaves")
                        .document(formattedDateTime)
                val databaseref2 =
                    Firebase.firestore.collection("Institutions/$instituteid/Employees/$empphno/Leaves")
                        .document(formattedDateTime)
                val leaveRequest = LeaveRequest(
                    formattedDateTime,
                    empphno, empid, instituteid, empname, empdepartment, empdesignation,
                    binding.leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    binding.leavetodate.text.toString(),
                    MorethanOnedayToselected, morethan1dayleavetype,
                    morethan1daynoofleaves, binding.ETleaveReason.text.toString(), "", "Pending"
                )

                binding.RGradiofrom.clearCheck()
                binding.RGradioto.clearCheck()
                binding.RGMorethanOneDayLeaveTypeSelection.clearCheck()
                binding.leavefromdate.text.clear()
                binding.leavetodate.text.clear()
                binding.ETleaveReason.text.clear()
                MorethanOnedayFromselected = ""
                MorethanOnedayToselected = ""
                morethan1dayleavetype = ""
                morethan1daynoofleaves = ""
                sendleaverequest(leaveRequest, databaseref1)
                sendleaverequest(leaveRequest, databaseref2)
            }
        }

        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val currentDate = Date()
                val dateString = dateFormat.format(currentDate)
                binding.txtTime.text = dateString
                handler.postDelayed(this, 1000)
            }
        })
        return binding.root
    }

    private fun sendleaverequest(leaveRequest: LeaveRequest, databaseref: DocumentReference) =
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.progressbarofNewRequest.visibility = View.VISIBLE
            }
            try {
                databaseref.set(leaveRequest).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity as Context, "Leave request sent", Toast.LENGTH_SHORT)
                        .show()
                    val title = "MLR"
                    val message = "$empname has requested for a leave"
                    sendEmployerNotification(title, message)
                    binding.progressbarofNewRequest.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity as Context, e.message, Toast.LENGTH_SHORT).show()
                    binding.progressbarofNewRequest.visibility = View.GONE
                }
            }
        }

    private fun updateLog(reason: String, status: String) =
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.progressbarofNewRequest.visibility = View.VISIBLE
            }
            try {
                val folderName = getDate()
                val currentTimeMillis = System.currentTimeMillis()
                val sdf = SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss a", Locale.getDefault())
                val formattedDateTime = sdf.format(Date(currentTimeMillis))
                val map = mutableMapOf<String, Any>()
                map["Date"] = folderName
                val databaseRef1 =
                    Firebase.firestore.collection("Institutions/${instituteid}/Employees/${empphno}/Logs")
                        .document(folderName)
                databaseRef1.set(map, SetOptions.merge()).await()
                val databaseRef2 =
                    Firebase.firestore.collection("Institutions/${instituteid}/Employees/${empphno}/Logs/${folderName}/CheckInCheckOut")
                        .document(formattedDateTime)
                val newLog = CheckInOutLog(
                    formattedDateTime.toString(),
                    reason,
                    status
                )
                databaseRef2.set(newLog).await()
                withContext(Dispatchers.Main) {
                    //Toast.makeText(activity as Context, "Log updated", Toast.LENGTH_SHORT).show()
                    val title = "MLR"
                    val message: String = if (status == "Checked In") "$empname has checked in"
                    else "$empname has checked out for $reason"

                    sendEmployerNotification(title, message)
                    binding.progressbarofNewRequest.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity as Context, e.message, Toast.LENGTH_SHORT).show()
                    binding.progressbarofNewRequest.visibility = View.GONE
                }
            }
        }

    private fun getRemainingLeaves() = CoroutineScope(Dispatchers.IO).launch {
        val databaseRef =
            database.collection("Institutions").document(instituteid).collection("Employees")
                .document(empphno)
        try{
            val querySnapshot = databaseRef.get().await()
            val employee = querySnapshot.toObject<Employee>()
            if (employee != null) {
                currentCL = employee.EmpCL!!
                currentHPL = employee.EmpHPL!!
                currentEL = employee.EmpEL!!
            }
            withContext(Dispatchers.Main) {
                updateRemainingLeaves()
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    activity as Context,
                    e.message + "Get Remaining leaves",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentStatus() = CoroutineScope(Dispatchers.IO).launch {
        try{
            val doc = database.collection("Institutions")
                .document(instituteid).collection("Employees")
                .document(empphno).get().await()
            val status = doc.get("status").toString()
            withContext(Dispatchers.Main) {
                if (status == "Checked Out") {
                    binding.btnCheckOut.isEnabled = false
                    binding.CheckOutReasonSpinner.isEnabled = false
                    binding.btnCheckIn.isEnabled = true
                    //binding.txtCheckInOutStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
                } else {
                    binding.btnCheckOut.isEnabled = true
                    binding.CheckOutReasonSpinner.isEnabled = true
                    binding.btnCheckIn.isEnabled = false
                    //binding.txtCheckInOutStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
                }
                binding.txtCheckInOutStatus.text = "You have $status"
            }
        }catch (e: Exception){
            Toast.makeText(
                activity as Context,
                e.message +"Get current status",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCurrentStatusFromSP(){
        if (empStatus == "Checked Out") {
            binding.btnCheckOut.isEnabled = false
            binding.CheckOutReasonSpinner.isEnabled = false
            binding.btnCheckIn.isEnabled = true
            //binding.txtCheckInOutStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
        } else {
            binding.btnCheckOut.isEnabled = true
            binding.CheckOutReasonSpinner.isEnabled = true
            binding.btnCheckIn.isEnabled = false
            //binding.txtCheckInOutStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
        }
        binding.txtCheckInOutStatus.text = "You have $empStatus"
    }

    private fun updateStatus(status: String) = CoroutineScope(Dispatchers.IO).launch{
        val map = mutableMapOf<String, Any>()
        map["status"] = status
        val databaseRef = database.collection("Institutions").document(instituteid).collection("Employees")
            .document(empphno)
        try{
            databaseRef.set(map, SetOptions.merge()).await()
            withContext(Dispatchers.Main){
                Toast.makeText(activity as Context, status, Toast.LENGTH_SHORT).show()
                val sharedPref = requireActivity().getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString("status", status)
                    apply()
                }
            }
            getCurrentStatus()
        }catch (e: Exception){
            Toast.makeText(
                activity as Context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateRemainingLeaves() = CoroutineScope(Dispatchers.IO).launch {
        try{
            binding.txtCLcount.text = currentCL.toString()
            binding.txtHPLcount.text = currentHPL.toString()
            binding.txtELcount.text = currentEL.toString()

            val sharedPref =
                activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putString("CL", currentCL.toString())
                    putString("HPL", currentHPL.toString())
                    putString("EL", currentEL.toString())
                    apply()
                }
            }
        }catch (e:Exception){
            Log.d("Exp", e.message.toString())
        }
    }

    private fun getSession(): String {
        // Get the current time using the Calendar class
        val currentTime = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.time = currentTime

        // Get the hour and minute of the current time
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Determine if it's morning or afternoon
        return if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            "Holiday"
        } else {
            val session =
                if ((hour == 9 && minute >= 30) || (hour in 10..12) || (hour == 13 && minute <= 30)) {
                    "Morning Session"
                } else if ((hour == 13 && minute >= 30) && (hour < 14)) {
                    "Break"
                } else if ((hour in 14..17) || (hour == 17 && minute <= 30)) {
                    "Evening Session"
                } else {
                    "No session running"
                }
            session
        }
    }

    private fun getDay(): CharSequence {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayNames =
            arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        return dayNames[dayOfWeek - 1] // Output: the name of the current day of the week (e.g. "Wednesday")
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(): String {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        val dateFormat = SimpleDateFormat("dd MMM yyyy")
        return dateFormat.format(currentDate)
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
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun updateNoOfLeaves() {
        val typeCheckedId = binding.RGMorethanOneDayLeaveTypeSelection.checkedRadioButtonId
        if (typeCheckedId != -1) {
            val checkedRadioButton: RadioButton = binding.root.findViewById(typeCheckedId)
            val checkedItemText: String = checkedRadioButton.text.toString()

            if (checkedItemText == "CL") {
                binding.txtMorethanOneDayNoofleaves.text = daysfinder.MorethanoneLeave(
                    binding.leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    binding.leavetodate.text.toString(),
                    MorethanOnedayToselected
                )[0].toString()
            }
            if (checkedItemText == "HPL") {
                binding.txtMorethanOneDayNoofleaves.text = daysfinder.MorethanoneLeave(
                    binding.leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    binding.leavetodate.text.toString(),
                    MorethanOnedayToselected
                )[1].toString()
            }
            if (checkedItemText == "EL") {
                binding.txtMorethanOneDayNoofleaves.text = daysfinder.MorethanoneLeave(
                    binding.leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    binding.leavetodate.text.toString(),
                    MorethanOnedayToselected
                )[2].toString()
            }
        }
    }


    private fun sendEmployerNotification(title:String, message:String) =
        CoroutineScope(Dispatchers.IO).launch {
            database = FirebaseFirestore.getInstance()
            val doc = database.collection("Institutions").document(instituteid).get().await()
            val fcmtoken = doc.get("fcmToken").toString()
            val url = "https://fcm.googleapis.com/fcm/send"
            val jsonObject = JSONObject()
            val notificationObject = JSONObject()
            notificationObject.put("title", title)
            notificationObject.put(
                "message",
                message
            )
            jsonObject.put("data", notificationObject)
            jsonObject.put("to", fcmtoken)

            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                {
                    // Handle the response here
                },
                {
                    // Handle the error here
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "key=AAAA1m6Yg-Q:APA91bEmjnY1kP2GxEHQv50q28QQ5yQwMvvYE4CKS3EH_nkhgc5zneZfVKmZdPXUR2YfTLz6O3Sd9Xaz9ROY4rCxaGP4ctGZhZhvQk9AFaqRcrJJ0JYpxAtpFZApfgdArgfLQG-FHb9k"
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
            Volley.newRequestQueue(activity as Context).add(request)
        }

    private fun setCurrentFragment(fragment: Fragment)=
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.flFragment,fragment)
            addToBackStack(null)
            commit()
        }

    private fun logout() {// Logout from the account, clear shared preferences and start Login activity
        firebaseauth.signOut()
        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.clear()
        editor?.apply()
        val intent = Intent(activity as Context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun getFcmToken() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()

            // Continue only if the shared preferences are available
            val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
            val instituteId = sharedPref?.getString("EmpInstituteId", "")
            val employeePhno = sharedPref?.getString("PhoneNumber", "")
            if (instituteId.isNullOrEmpty() || employeePhno.isNullOrEmpty()) {
                Log.d(ContentValues.TAG, "No institute ID/employee ID found")
                return@launch
            }

            val map = mutableMapOf<String, Any>("fcmToken" to fcmToken)
            val databaseRef = FirebaseFirestore.getInstance().collection("Institutions")
                .document(instituteId)
                .collection("Employees")
                .document(employeePhno)

            databaseRef.set(map, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "Error fetching FCM token or updating Firestore: ${e.message}")
        }
    }

    private fun getEmployeeDetails()= CoroutineScope(Dispatchers.IO).launch{
        val db = FirebaseFirestore.getInstance()
        try {
            val doc = db.collection("Institutions").document(instituteid)
                .collection("Employees").document(empphno).get().await()
            if(!doc.exists()){
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        activity as Context,
                        "Your account has been deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                val employee = doc.toObject<Employee>()!!
                if (employee.EmpPhoneNo != empphno){
                    withContext(Dispatchers.Main){
                        Toast.makeText(activity as Context, "Your number has been changed by Employer", Toast.LENGTH_LONG).show()
                        logout()
                    }
                }
                else{
                    val fcmStatus = doc.get("fcmToken")
                    if (fcmStatus == null){
                        getFcmToken()
                    }
                    val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
                    if (sharedPref != null) {
                        with (sharedPref.edit()) {
                            putString("EmpID", employee.EmpId)
                            putString("EmpInstituteName", institutename)
                            putString("EmpInstituteId", instituteid)
                            putString("FName", employee.EmpFirstName)
                            putString("MName", employee.EmpMiddleName)
                            putString("LName", employee.EmpLastName)
                            putString("Designation", employee.EmpDesignation)
                            putString("Department", employee.EmpDepartment)
                            putString("DateOfBirth", employee.EmpDOB)
                            putString("DateOfAppointment", employee.EmpDOA)
                            putString("PhoneNumber", employee.EmpPhoneNo)
                            putString("EmailId", employee.EmpEmailId)
                            putString("status", "Checked Out")
                            employee.EmpCL?.let { putString("CL", it.toString()) }
                            employee.EmpHPL?.let { putString("HPL", it.toString()) }
                            employee.EmpEL?.let { putString("EL", it.toString()) }
                            apply()
                        }
                    }
                }
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    activity as Context,
                    e.message + "Get employee details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
