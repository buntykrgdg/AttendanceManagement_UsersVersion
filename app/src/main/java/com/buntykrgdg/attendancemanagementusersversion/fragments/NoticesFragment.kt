package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.AllNoticesAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Notice
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentNoticesBinding
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

class NoticesFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var allNoticesList = arrayListOf<Notice>()
    private lateinit var allNoticesAdapter: AllNoticesAdapter
    private lateinit var tempArrayList: ArrayList<Notice>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String

    private var currentPage = 0
    private val limit = 4
    private var isLoadingMore = false
    private var querySnapshot: QuerySnapshot? = null

    private var fragmentNoticesBinding: FragmentNoticesBinding? = null
    private val binding get() = fragmentNoticesBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentNoticesBinding = FragmentNoticesBinding.inflate(inflater, container, false)
        
        layoutManager= LinearLayoutManager(activity as Context)
        tempArrayList = ArrayList()
        allNoticesList = ArrayList()
        allNoticesAdapter = AllNoticesAdapter(activity as Context, tempArrayList)
        binding.recyclerviewAllNotices.adapter = allNoticesAdapter
        binding.recyclerviewAllNotices.layoutManager = layoutManager

        val sharedPref = activity?.getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        if (sharedPref != null) instituteId = sharedPref.getString("EmpInstituteId", "Your InsID").toString()

        binding.swipeToRefreshAllNotices.setOnRefreshListener {
            getAllNoticesList()
        }
        binding.recyclerviewAllNotices.addOnScrollListener(scrollListener)
        getAllNoticesList()

        return binding.root
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val isAtEnd = !recyclerView.canScrollVertically(1) // Check for scrolling down
            if (isAtEnd && !isLoadingMore) {
                isLoadingMore = true
                loadMoreData()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreData() {
        binding.progresslayoutAllNotices.visibility = View.VISIBLE
        val lastDocument = querySnapshot?.documents?.lastOrNull()
        if(lastDocument == null){
            isLoadingMore = false
            binding.progresslayoutAllNotices.visibility = View.GONE
            return
        }
        val dbRef = db.collection("Institutions").document(instituteId)
            .collection("Notices").orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong()).startAfter(lastDocument)
        currentPage++
        CoroutineScope(Dispatchers.IO).launch {
            try {
                querySnapshot = dbRef.get().await()
                if (querySnapshot!!.isEmpty) {
                    // No more data available
                    return@launch
                }
                val nextLeaveRequestList = querySnapshot!!.toObjects(Notice::class.java)
                withContext(Dispatchers.Main) {
                    tempArrayList.addAll(nextLeaveRequestList)
                    binding.recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoadingMore = false // Reset flag after successful update
                withContext(Dispatchers.Main) {
                    binding.progresslayoutAllNotices.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllNoticesList() = CoroutineScope(Dispatchers.IO).launch {
        allNoticesList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions")
            .document(instituteId)
            .collection("Notices")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong())
        try{
            querySnapshot = dbRef.get().await()
            val allNoticesList = querySnapshot!!.documents.mapNotNull { it.toObject<Notice>() }
            tempArrayList.addAll(allNoticesList)
        }catch (e: Exception) {
            UtilFunctions.showToast(activity as Context, e.message ?: "Error fetching logs")
        }finally {
            withContext(Dispatchers.Main) {
                binding.recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                binding.progresslayoutAllNotices.visibility = View.GONE
                binding.swipeToRefreshAllNotices.isRefreshing = false
            }
        }
    }
}