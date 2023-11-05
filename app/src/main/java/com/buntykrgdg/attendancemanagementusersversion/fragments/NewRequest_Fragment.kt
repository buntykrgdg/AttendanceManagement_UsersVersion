package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
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
import com.buntykrgdg.attendancemanagementusersversion.classes.Employee
import com.buntykrgdg.attendancemanagementusersversion.classes.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.classes.Leaves
import com.buntykrgdg.attendancemanagementusersversion.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class NewRequest_Fragment : Fragment() {
    private lateinit var txtDate: TextView
    private lateinit var txtDay: TextView
    private lateinit var txtTime: TextView
    private lateinit var txtSession: TextView
    private lateinit var txtCLcount: TextView
    private lateinit var txtHPLcount: TextView
    private lateinit var txtELcount: TextView
    private lateinit var progressbarofNewRequest: ProgressBar
    private lateinit var CVRemainingleaves: MaterialCardView
    private var database = FirebaseFirestore.getInstance()
    private var currentCL: Double = 0.0
    private var currentHPL: Double = 0.0
    private var currentEL: Double = 0.0

    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("hh:mm:ss a")

    private lateinit var RDLeaveRange: RadioGroup // Leave range selector
    private lateinit var RBHalfDay: RadioButton
    private lateinit var RBOneDay: RadioButton
    private lateinit var RBMorethanOne: RadioButton

    private lateinit var RLHalfDay: RelativeLayout // Half day leave
    private lateinit var halfdayleavedate: EditText
    private lateinit var RGHalfDay: RadioGroup
    private lateinit var HalfDaymorningRadioBtn: RadioButton
    private lateinit var HalfDayafternoonRadioBtn: RadioButton
    private lateinit var btnHalfDaydate: ImageButton
    private lateinit var RGHalfDayLeaveTypeSelection: RadioGroup
    private lateinit var HalfDayCLRadioBtn: RadioButton
    private lateinit var HalfDayHPLRadioBtn: RadioButton
    private lateinit var HalfDayELRadioBtn: RadioButton
    private lateinit var txtHalfDayNoofleaves: TextView
    private lateinit var txtHalfDayNote: TextView
    private lateinit var ETHalfDayleaveReason: EditText
    private lateinit var btnSendRequestHalfDay: Button

    private lateinit var RLOneDay: RelativeLayout // One day leave
    private lateinit var onedayleavedate: EditText
    private lateinit var btnSelectonedayleavedate: ImageButton
    private lateinit var RGOneDayLeaveTypeSelection: RadioGroup
    private lateinit var OneDayCLRadioBtn: RadioButton
    private lateinit var OneDayHPLRadioBtn: RadioButton
    private lateinit var OneDayELRadioBtn: RadioButton
    private lateinit var txtOneDayNoofleaves: TextView
    private lateinit var txtOneDayNote: TextView
    private lateinit var ETonedayleaveReason: EditText
    private lateinit var btnSendRequestOneDay: Button

    private lateinit var RLMorethan1: RelativeLayout //More than 1 day leave
    private lateinit var leavefromdate: EditText
    private lateinit var RGradiofrom: RadioGroup
    private lateinit var FrommorningRadioBtn: RadioButton
    private lateinit var FromafternoonRadioBtn: RadioButton
    private lateinit var btnSelectfromdate: ImageButton
    private lateinit var leavetodate: EditText
    private lateinit var RGradioto: RadioGroup
    private lateinit var TomorningRadioBtn: RadioButton
    private lateinit var ToafternoonRadioBtn: RadioButton
    private lateinit var btnSelecttodate: ImageButton
    private lateinit var RGMorethanOneDayLeaveTypeSelection: RadioGroup
    private lateinit var MorethanOneDayCLRadioBtn: RadioButton
    private lateinit var MorethanOneDayHPLRadioBtn: RadioButton
    private lateinit var MorethanOneDayELRadioBtn: RadioButton
    private lateinit var txtMorethanOneDayNoofleaves: TextView
    private lateinit var txtMoreThanOneDayNote: TextView
    private lateinit var ETleaveReason: EditText
    private lateinit var btnSendRequestMorethan1: Button

    private lateinit var instituteid: String
    private lateinit var empid: String
    private lateinit var empname: String
    private lateinit var empdepartment: String
    private lateinit var empdesignation: String

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_request_, container, false)

        txtDate = view.findViewById(R.id.txtDate)
        txtDay = view.findViewById(R.id.txtDay)
        txtTime = view.findViewById(R.id.txtTime)
        txtSession = view.findViewById(R.id.txtSession)
        txtCLcount = view.findViewById(R.id.txtCLcount)
        txtHPLcount = view.findViewById(R.id.txtHPLcount)
        txtELcount = view.findViewById(R.id.txtELcount)
        progressbarofNewRequest = view.findViewById(R.id.progressbarofNewRequest)
        CVRemainingleaves = view.findViewById(R.id.CVRemainingleaves)
        txtDate.text = getDate()
        txtDay.text = getDay()
        txtSession.text = getsession()

        RLHalfDay = view.findViewById(R.id.RLHalfDay)
        HalfDaymorningRadioBtn = view.findViewById(R.id.HalfDaymorningRadioBtn)
        HalfDayafternoonRadioBtn = view.findViewById(R.id.HalfDayafternoonRadioBtn)
        RGHalfDay = view.findViewById(R.id.RGHalfDay)
        halfdayleavedate = view.findViewById(R.id.halfdayleavedate)
        btnHalfDaydate = view.findViewById(R.id.btnHalfDaydate)
        RGHalfDayLeaveTypeSelection = view.findViewById(R.id.RGHalfDayLeaveTypeSelection)
        HalfDayCLRadioBtn = view.findViewById(R.id.HalfDayCLRadioBtn)
        HalfDayHPLRadioBtn = view.findViewById(R.id.HalfDayHPLRadioBtn)
        HalfDayELRadioBtn = view.findViewById(R.id.HalfDayELRadioBtn)
        txtHalfDayNoofleaves = view.findViewById(R.id.txtHalfDayNoofleaves)
        txtHalfDayNote = view.findViewById(R.id.txtHalfDayNote)
        ETHalfDayleaveReason = view.findViewById(R.id.ETHalfDayleaveReason)
        btnSendRequestHalfDay = view.findViewById(R.id.btnSendRequestHalfDay)

        RLOneDay = view.findViewById(R.id.RLOneDay)
        onedayleavedate = view.findViewById(R.id.onedayleavedate)
        btnSelectonedayleavedate = view.findViewById(R.id.btnSelectonedayleavedate)
        RGOneDayLeaveTypeSelection = view.findViewById(R.id.RGOneDayLeaveTypeSelection)
        OneDayCLRadioBtn = view.findViewById(R.id.OneDayCLRadioBtn)
        OneDayHPLRadioBtn = view.findViewById(R.id.OneDayHPLRadioBtn)
        OneDayELRadioBtn = view.findViewById(R.id.OneDayELRadioBtn)
        txtOneDayNoofleaves = view.findViewById(R.id.txtOneDayNoofleaves)
        txtOneDayNote = view.findViewById(R.id.txtOneDayNote)
        ETonedayleaveReason = view.findViewById(R.id.ETonedayleaveReason)
        btnSendRequestOneDay = view.findViewById(R.id.btnSendRequestOneDay)

        RLMorethan1 = view.findViewById(R.id.RLMorethan1)
        leavefromdate = view.findViewById(R.id.leavefromdate)
        btnSelectfromdate = view.findViewById(R.id.btnSelectfromdate)
        leavetodate = view.findViewById(R.id.leavetodate)
        btnSelecttodate = view.findViewById(R.id.btnSelecttodate)
        RGradiofrom = view.findViewById(R.id.RGradiofrom)
        RGradioto = view.findViewById(R.id.RGradioto)
        FrommorningRadioBtn = view.findViewById(R.id.FrommorningRadioBtn)
        FromafternoonRadioBtn = view.findViewById(R.id.FromafternoonRadioBtn)
        TomorningRadioBtn = view.findViewById(R.id.TomorningRadioBtn)
        ToafternoonRadioBtn = view.findViewById(R.id.ToafternoonRadioBtn)
        RGMorethanOneDayLeaveTypeSelection =
            view.findViewById(R.id.RGMorethanOneDayLeaveTypeSelection)
        MorethanOneDayCLRadioBtn = view.findViewById(R.id.MorethanOneDayCLRadioBtn)
        MorethanOneDayHPLRadioBtn = view.findViewById(R.id.MorethanOneDayHPLRadioBtn)
        MorethanOneDayELRadioBtn = view.findViewById(R.id.MorethanOneDayELRadioBtn)
        txtMorethanOneDayNoofleaves = view.findViewById(R.id.txtMorethanOneDayNoofleaves)
        txtMoreThanOneDayNote = view.findViewById(R.id.txtMoreThanOneDayNote)
        ETleaveReason = view.findViewById(R.id.ETleaveReason)
        btnSendRequestMorethan1 = view.findViewById(R.id.btnSendRequestMorethan1)

        RDLeaveRange = view.findViewById(R.id.RDLeaveRange)
        RBHalfDay = view.findViewById(R.id.RBHalfDay)
        RBOneDay = view.findViewById(R.id.RBOneDay)
        RBMorethanOne = view.findViewById(R.id.RBMorethanOne)

        val sharedPref =
            activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            instituteid = sharedPref.getString("EmpInstituteId", "Your InsID").toString()
            empid = sharedPref.getString("EmpID", "Your EmpID").toString()
            val employeefname = sharedPref.getString("FName", "Fname")
            val employeemname = sharedPref.getString("MName", "Mname")
            val employeelname = sharedPref.getString("LName", "Lname")
            empname = "$employeefname $employeemname $employeelname"
            empdepartment = sharedPref.getString("Department", "Department").toString()
            empdesignation = sharedPref.getString("Designation", "Designation").toString()
            currentCL = sharedPref.getString("CL", "0")?.toDouble()!!
            currentHPL = sharedPref.getString("HPL", "0")?.toDouble()!!
            currentEL = sharedPref.getString("EL", "0")?.toDouble()!!
            updateremainingleaves()
        }

        getRemainingLeaves()

        RDLeaveRange.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.RBHalfDay -> {
                    RLHalfDay.visibility = View.VISIBLE
                    RLOneDay.visibility = View.GONE
                    RLMorethan1.visibility = View.GONE
                }

                R.id.RBOneDay -> {
                    RLHalfDay.visibility = View.GONE
                    RLOneDay.visibility = View.VISIBLE
                    RLMorethan1.visibility = View.GONE
                }

                R.id.RBMorethanOne -> {
                    RLHalfDay.visibility = View.GONE
                    RLOneDay.visibility = View.GONE
                    RLMorethan1.visibility = View.VISIBLE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        RGHalfDay.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.HalfDaymorningRadioBtn -> {
                    halfdaysession = "morning"
                }

                R.id.HalfDayafternoonRadioBtn -> {
                    halfdaysession = "afternoon"
                }
            }
        }

        halfdayleavedate.setOnClickListener {//Half day leave
            showDatePicker(halfdayleavedate)
        }
        btnHalfDaydate.setOnClickListener {//Half day leave
            showDatePicker(halfdayleavedate)
        }

        RGHalfDayLeaveTypeSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.HalfDayCLRadioBtn -> {
                    // User selected CL
                    halfdayleavetype = "CL"
                    txtHalfDayNoofleaves.text = daysfinder.HalfDayLeave()[0].toString()
                    halfdaynoofleaves = daysfinder.HalfDayLeave()[0].toString()
                    if (currentCL < halfdaynoofleaves.toDouble()) {
                        txtHalfDayNote.visibility = View.VISIBLE
                        txtHalfDayNote.text = "You do not have sufficient CL's"
                    } else txtHalfDayNote.visibility = View.GONE
                }

                R.id.HalfDayHPLRadioBtn -> {
                    // User selected HPL
                    halfdayleavetype = "HPL"
                    txtHalfDayNoofleaves.text = daysfinder.HalfDayLeave()[1].toString()
                    halfdaynoofleaves = daysfinder.HalfDayLeave()[1].toString()
                    if (currentHPL < halfdaynoofleaves.toDouble()) {
                        txtHalfDayNote.visibility = View.VISIBLE
                        txtHalfDayNote.text = "You do not have sufficient HPL's"
                    } else txtHalfDayNote.visibility = View.GONE
                }

                R.id.HalfDayELRadioBtn -> {
                    // User selected EL
                    halfdayleavetype = "EL"
                    txtHalfDayNoofleaves.text = daysfinder.HalfDayLeave()[2].toString()
                    halfdaynoofleaves = daysfinder.HalfDayLeave()[2].toString()
                    if (currentEL < halfdaynoofleaves.toDouble()) {
                        txtHalfDayNote.visibility = View.VISIBLE
                        txtHalfDayNote.text = "You do not have sufficient EL's"
                    } else txtHalfDayNote.visibility = View.GONE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        btnSendRequestHalfDay.setOnClickListener {
            if (halfdayleavedate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (RGHalfDay.checkedRadioButtonId == -1) {
                Toast.makeText(activity as Context, "Please select session", Toast.LENGTH_SHORT)
                    .show()
            } else if (RGHalfDayLeaveTypeSelection.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select type of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (ETHalfDayleaveReason.text.toString() == "") {
                Toast.makeText(activity as Context, "Please enter the reason", Toast.LENGTH_SHORT)
                    .show()
            } else if (txtHalfDayNote.visibility == View.VISIBLE) {
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
                    Firebase.firestore.collection("Institutions/$instituteid/Employees/$empid/Leaves")
                        .document(formattedDateTime)
                val leaveRequest = LeaveRequest(
                    formattedDateTime,
                    empid, instituteid, empname, empdepartment, empdesignation,
                    halfdayleavedate.text.toString(),
                    halfdaysession,
                    halfdayleavedate.text.toString(),
                    halfdaysession, halfdayleavetype,
                    halfdaynoofleaves, ETHalfDayleaveReason.text.toString(), "", "Pending"
                )

                halfdayleavedate.text.clear()
                ETHalfDayleaveReason.text.clear()
                RGHalfDayLeaveTypeSelection.clearCheck()
                RGHalfDay.clearCheck()
                txtHalfDayNoofleaves.text = "0"
                halfdaysession = ""
                halfdayleavetype = ""
                halfdaynoofleaves = ""
                sendleaverequest(leaveRequest, databaseref1)
                sendleaverequest(leaveRequest, databaseref2)
            }
        }

        onedayleavedate.setOnClickListener {// One day leave
            showDatePicker(onedayleavedate)
        }
        btnSelectonedayleavedate.setOnClickListener {// One day leave
            showDatePicker(onedayleavedate)
        }

        RGOneDayLeaveTypeSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.OneDayCLRadioBtn -> {
                    // User selected CL
                    onedayleavetype = "CL"
                    txtOneDayNoofleaves.text = daysfinder.FullDayLeave()[0].toString()
                    onedaynoofleaves = daysfinder.FullDayLeave()[0].toString()
                    if (currentCL < onedaynoofleaves.toDouble()) {
                        txtOneDayNote.visibility = View.VISIBLE
                        txtOneDayNote.text = "You do not have sufficient CL's"
                    } else txtOneDayNote.visibility = View.GONE
                }

                R.id.OneDayHPLRadioBtn -> {
                    // User selected HPL
                    onedayleavetype = "HPL"
                    txtOneDayNoofleaves.text = daysfinder.FullDayLeave()[1].toString()
                    onedaynoofleaves = daysfinder.FullDayLeave()[1].toString()
                    if (currentHPL < onedaynoofleaves.toDouble()) {
                        txtOneDayNote.visibility = View.VISIBLE
                        txtOneDayNote.text = "You do not have sufficient HPL's"
                    } else txtOneDayNote.visibility = View.GONE
                }

                R.id.OneDayELRadioBtn -> {
                    // User selected EL
                    onedayleavetype = "EL"
                    txtOneDayNoofleaves.text = daysfinder.FullDayLeave()[2].toString()
                    onedaynoofleaves = daysfinder.FullDayLeave()[2].toString()
                    if (currentEL < onedaynoofleaves.toDouble()) {
                        txtOneDayNote.visibility = View.VISIBLE
                        txtOneDayNote.text = "You do not have sufficient EL's"
                    } else txtOneDayNote.visibility = View.GONE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        btnSendRequestOneDay.setOnClickListener {
            if (onedayleavedate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (RGOneDayLeaveTypeSelection.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select type of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (ETonedayleaveReason.text.toString() == "") {
                Toast.makeText(activity as Context, "Please enter the reason", Toast.LENGTH_SHORT)
                    .show()
            } else if (txtOneDayNote.visibility == View.VISIBLE) {
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
                    Firebase.firestore.collection("Institutions/$instituteid/Employees/$empid/Leaves")
                        .document(formattedDateTime)
                val leaveRequest = LeaveRequest(
                    formattedDateTime,
                    empid, instituteid, empname, empdepartment, empdesignation,
                    onedayleavedate.text.toString(),
                    "morning",
                    onedayleavedate.text.toString(),
                    "afternoon", onedayleavetype,
                    onedaynoofleaves, ETonedayleaveReason.text.toString(), "", "Pending"
                )

                onedayleavedate.text.clear()
                ETonedayleaveReason.text.clear()
                RGOneDayLeaveTypeSelection.clearCheck()
                onedayleavetype = ""
                onedaynoofleaves = ""
                sendleaverequest(leaveRequest, databaseref1)
                sendleaverequest(leaveRequest, databaseref2)
            }
        }

        leavefromdate.setOnClickListener {// More than 1 day leave
            showDatePicker(leavefromdate)
        }
        leavetodate.setOnClickListener {// More than 1 day leave
            showDatePicker(leavetodate)
        }
        btnSelectfromdate.setOnClickListener {// More than 1 day leave
            showDatePicker(leavefromdate)
        }
        btnSelecttodate.setOnClickListener {
            showDatePicker(leavetodate)
        }

        RGradiofrom.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.FrommorningRadioBtn -> {
                    // User selected morning session
                    MorethanOnedayFromselected = "morning"
                    updatenofoleaves(view)
                }

                R.id.FromafternoonRadioBtn -> {
                    // User selected afternoon session
                    MorethanOnedayFromselected = "afternoon"
                    updatenofoleaves(view)
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        RGradioto.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.TomorningRadioBtn -> {
                    // User selected morning session
                    MorethanOnedayToselected = "morning"
                    updatenofoleaves(view)
                }

                R.id.ToafternoonRadioBtn -> {
                    // User selected afternoon session
                    MorethanOnedayToselected = "afternoon"
                    updatenofoleaves(view)
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        RGMorethanOneDayLeaveTypeSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.MorethanOneDayCLRadioBtn -> {
                    // User selected CL
                    morethan1dayleavetype = "CL"
                    morethan1daynoofleaves = daysfinder.MorethanoneLeave(
                        leavefromdate.text.toString(),
                        MorethanOnedayFromselected,
                        leavetodate.text.toString(),
                        MorethanOnedayToselected
                    )[0].toString()
                    txtMorethanOneDayNoofleaves.text = morethan1daynoofleaves
                    if (currentCL < morethan1daynoofleaves.toDouble()) {
                        txtMoreThanOneDayNote.visibility = View.VISIBLE
                        txtMoreThanOneDayNote.text = "You do not have sufficient CL's"
                    } else txtMoreThanOneDayNote.visibility = View.GONE
                }

                R.id.MorethanOneDayHPLRadioBtn -> {
                    // User selected HPL
                    morethan1dayleavetype = "HPL"
                    morethan1daynoofleaves = daysfinder.MorethanoneLeave(
                        leavefromdate.text.toString(),
                        MorethanOnedayFromselected,
                        leavetodate.text.toString(),
                        MorethanOnedayToselected
                    )[1].toString()
                    txtMorethanOneDayNoofleaves.text = morethan1daynoofleaves
                    if (currentHPL < morethan1daynoofleaves.toDouble()) {
                        txtMoreThanOneDayNote.visibility = View.VISIBLE
                        txtMoreThanOneDayNote.text = "You do not have sufficient HPL's"
                    } else txtMoreThanOneDayNote.visibility = View.GONE
                }

                R.id.MorethanOneDayELRadioBtn -> {
                    // User selected EL
                    morethan1dayleavetype = "EL"
                    morethan1daynoofleaves = daysfinder.MorethanoneLeave(
                        leavefromdate.text.toString(),
                        MorethanOnedayFromselected,
                        leavetodate.text.toString(),
                        MorethanOnedayToselected
                    )[2].toString()
                    txtMorethanOneDayNoofleaves.text = morethan1daynoofleaves
                    if (currentEL < morethan1daynoofleaves.toDouble()) {
                        txtMoreThanOneDayNote.visibility = View.VISIBLE
                        txtMoreThanOneDayNote.text = "You do not have sufficient EL's"
                    } else txtMoreThanOneDayNote.visibility = View.GONE
                }

                else -> {
                    // User unselected the selected session
                }
            }
        }

        btnSendRequestMorethan1.setOnClickListener {
            if (leavefromdate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the first day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (RGradiofrom.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select session of From date",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (leavetodate.text.toString() == "") {
                Toast.makeText(
                    activity as Context,
                    "Please select the last day of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (RGradioto.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select session of To date",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (RGMorethanOneDayLeaveTypeSelection.checkedRadioButtonId == -1) {
                Toast.makeText(
                    activity as Context,
                    "Please select type of leave",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (ETleaveReason.text.toString() == "") {
                Toast.makeText(activity as Context, "Please enter the reason", Toast.LENGTH_SHORT)
                    .show()
            } else if (daysfinder.isToDateBeforeFromDate(
                    leavefromdate.text.toString(),
                    leavetodate.text.toString()
                )
            ) {
                Toast.makeText(
                    activity as Context,
                    "Invalid 'From' and 'To' date",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (txtMoreThanOneDayNote.visibility == View.VISIBLE) {
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
                    Firebase.firestore.collection("Institutions/$instituteid/Employees/$empid/Leaves")
                        .document(formattedDateTime)
                val leaveRequest = LeaveRequest(
                    formattedDateTime,
                    empid, instituteid, empname, empdepartment, empdesignation,
                    leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    leavetodate.text.toString(),
                    MorethanOnedayToselected, morethan1dayleavetype,
                    morethan1daynoofleaves, ETleaveReason.text.toString(), "", "Pending"
                )

                RGradiofrom.clearCheck()
                RGradioto.clearCheck()
                RGMorethanOneDayLeaveTypeSelection.clearCheck()
                leavefromdate.text.clear()
                leavetodate.text.clear()
                ETleaveReason.text.clear()
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
                txtTime.text = dateString
                handler.postDelayed(this, 1000)
            }
        })
        return view
    }

    private fun sendleaverequest(leaveRequest: LeaveRequest, databaseref: DocumentReference) =
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                progressbarofNewRequest.visibility = View.VISIBLE
            }
            try {
                databaseref.set(leaveRequest).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity as Context, "Leave request sent", Toast.LENGTH_LONG)
                        .show()
                    sendEmployerNotification(activity as Context)
                    progressbarofNewRequest.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity as Context, e.message, Toast.LENGTH_LONG).show()
                    progressbarofNewRequest.visibility = View.GONE
                }
            }
        }

    private fun getRemainingLeaves() = CoroutineScope(Dispatchers.IO).launch {
        val databaseref =
            database.collection("Institutions").document(instituteid).collection("Employees")
                .document(empid)
        val querySnapshot = databaseref.get().await()
        val employee = querySnapshot.toObject<Employee>()
        if (employee != null) {
            currentCL = employee.EmpCL!!
            currentHPL = employee.EmpHPL!!
            currentEL = employee.EmpEL!!
        }
        withContext(Dispatchers.Main) {
            updateremainingleaves()
        }
    }

    private fun updateremainingleaves() {
        txtCLcount.text = currentCL.toString()
        txtHPLcount.text = currentHPL.toString()
        txtELcount.text = currentEL.toString()

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
    }

    private fun getsession(): String {
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


    private fun updatenofoleaves(view: View) {
        val typecheckedid = RGMorethanOneDayLeaveTypeSelection.checkedRadioButtonId
        if (typecheckedid != -1) {
            val checkedRadioButton: RadioButton = view.findViewById(typecheckedid)
            val checkedItemText: String = checkedRadioButton.text.toString()

            if (checkedItemText == "CL") {
                txtMorethanOneDayNoofleaves.text = daysfinder.MorethanoneLeave(
                    leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    leavetodate.text.toString(),
                    MorethanOnedayToselected
                )[0].toString()
            }
            if (checkedItemText == "HPL") {
                txtMorethanOneDayNoofleaves.text = daysfinder.MorethanoneLeave(
                    leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    leavetodate.text.toString(),
                    MorethanOnedayToselected
                )[1].toString()
            }
            if (checkedItemText == "EL") {
                txtMorethanOneDayNoofleaves.text = daysfinder.MorethanoneLeave(
                    leavefromdate.text.toString(),
                    MorethanOnedayFromselected,
                    leavetodate.text.toString(),
                    MorethanOnedayToselected
                )[2].toString()
            }
        }
    }

    private fun sendEmployerNotification(context: Context?) =
        CoroutineScope(Dispatchers.IO).launch {
            database = FirebaseFirestore.getInstance()
            val doc = database.collection("Institutions").document(instituteid).get().await()
            val fcmtoken = doc.get("fcmToken").toString()
            val url = "https://fcm.googleapis.com/fcm/send"
            val jsonObject = JSONObject()
            val notificationObject = JSONObject()
            notificationObject.put("title", "Attendance Management")
            notificationObject.put(
                "message",
                "Employee with employee ID: $empid has requested for a leave"
            )
            jsonObject.put("data", notificationObject)
            jsonObject.put("to", fcmtoken)

            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    // Handle the response here
                },
                { error ->
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
            Volley.newRequestQueue(context).add(request)
        }
}
