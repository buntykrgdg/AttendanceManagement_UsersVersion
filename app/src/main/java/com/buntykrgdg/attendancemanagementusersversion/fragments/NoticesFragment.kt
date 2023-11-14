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
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.AllNoticesAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Notice
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
class NoticesFragment : Fragment() {
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeToRefreshAllNotices: SwipeRefreshLayout
    private lateinit var recyclerviewAllNotices: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var allNoticesList = arrayListOf<Notice>()
    private lateinit var allNoticesAdapter: AllNoticesAdapter
    private lateinit var searchView: SearchView
    private lateinit var tempArrayList: ArrayList<Notice>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notices_, container, false)
        swipeToRefreshAllNotices=view.findViewById(R.id.swipeToRefreshAllNotices)
        recyclerviewAllNotices=view.findViewById(R.id.recyclerviewAllNotices)
        progressLayout=view.findViewById(R.id.progresslayoutAllNotices)
        progressBar=view.findViewById(R.id.progressbarAllNotices)
        layoutManager= LinearLayoutManager(activity as Context)
        tempArrayList = ArrayList()
        allNoticesList = ArrayList()
        searchView = view.findViewById(R.id.searchviewAllNotices)
        allNoticesAdapter = AllNoticesAdapter(activity as Context, tempArrayList)
        recyclerviewAllNotices.adapter = allNoticesAdapter
        recyclerviewAllNotices.layoutManager = layoutManager

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()

        swipeToRefreshAllNotices.setOnRefreshListener {
            getAllNoticesList()
        }
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
                    allNoticesList.forEach{
                        if (it.message?.lowercase(Locale.getDefault())?.contains(searchText) == true){
                            tempArrayList.add(it)
                        }
                    }
                    recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(allNoticesList)
                    recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllNoticesList() = CoroutineScope(Dispatchers.IO).launch {
        allNoticesList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions")
            .document(instituteId)
            .collection("Notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val querySnapshot = dbRef.get().await()
        for (document in querySnapshot.documents) {
            Log.d("db", document.toString())
            document.toObject<Notice>()?.let { allNoticesList.add(it) }
        }
        tempArrayList.addAll(allNoticesList)
        UtilFunctions.sortNoticeByTimestamp(tempArrayList)
        withContext(Dispatchers.Main) {
            recyclerviewAllNotices.adapter?.notifyDataSetChanged()
            progressLayout.visibility = View.GONE
            swipeToRefreshAllNotices.isRefreshing = false
        }
    }
}