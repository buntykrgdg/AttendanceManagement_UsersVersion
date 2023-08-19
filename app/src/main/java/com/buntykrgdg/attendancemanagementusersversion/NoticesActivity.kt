package com.buntykrgdg.attendancemanagementusersversion

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buntykrgdg.attendancemanagementusersversion.classes.AllNoticesAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.Notice
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class NoticesActivity : AppCompatActivity() {
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeToRefreshAllNotices: SwipeRefreshLayout
    private lateinit var recyclerviewAllNotices: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var AllNoticesList = arrayListOf<Notice>()
    private lateinit var AllNoticesAdapter: AllNoticesAdapter
    private lateinit var searchView: SearchView
    private lateinit var tempArrayList: ArrayList<Notice>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notices)

        swipeToRefreshAllNotices=findViewById(R.id.swipeToRefreshAllNotices)
        recyclerviewAllNotices=findViewById(R.id.recyclerviewAllNotices)
        progressLayout=findViewById(R.id.progresslayoutAllNotices)
        progressBar=findViewById(R.id.progressbarAllNotices)
        layoutManager= LinearLayoutManager(this)
        tempArrayList = ArrayList()
        AllNoticesList = ArrayList()
        searchView = findViewById(R.id.searchviewAllNotices)
        AllNoticesAdapter = AllNoticesAdapter(this, tempArrayList)
        recyclerviewAllNotices.adapter = AllNoticesAdapter
        recyclerviewAllNotices.layoutManager = layoutManager

        val sharedPref = getSharedPreferences("AttendanceManagement", Context.MODE_PRIVATE)
        if (sharedPref != null) instituteid = sharedPref.getString("InstitutionId", "Your InsID").toString()

        refreshNotices()
        getAllNoticesList()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                tempArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())

                if (searchText.isNotEmpty()){
                    AllNoticesList.forEach{
                        if (it.message?.lowercase(Locale.getDefault())?.contains(searchText) == true){
                            tempArrayList.add(it)
                        }
                    }
                    recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(AllNoticesList)
                    recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllNoticesList() = CoroutineScope(Dispatchers.IO).launch {
        AllNoticesList.clear()
        tempArrayList.clear()
        val dbref = db.collection("Institutions")
            .document(instituteid)
            .collection("Notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val querySnapshot = dbref.get().await()
        for (document in querySnapshot.documents) {
            Log.d("db", document.toString())
            document.toObject<Notice>()?.let { AllNoticesList.add(it) }
        }
        tempArrayList.addAll(AllNoticesList)
        withContext(Dispatchers.Main) {
            recyclerviewAllNotices.adapter?.notifyDataSetChanged()
            progressLayout.visibility = View.GONE
            swipeToRefreshAllNotices.isRefreshing = false
        }
    }

    private fun refreshNotices() {
        swipeToRefreshAllNotices.setOnRefreshListener {
            getAllNoticesList()
        }
    }
}