package com.buntykrgdg.attendancemanagementusersversion.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.adapters.AllNoticesAdapter
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Notice
import com.buntykrgdg.attendancemanagementusersversion.databinding.FragmentNoticesBinding
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
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var allNoticesList = arrayListOf<Notice>()
    private lateinit var allNoticesAdapter: AllNoticesAdapter
    private lateinit var tempArrayList: ArrayList<Notice>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var instituteId: String

    private var fragmentNoticesBinding: FragmentNoticesBinding? = null
    private val binding get() = fragmentNoticesBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        getAllNoticesList()

        binding.searchviewAllNotices.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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
                    binding.recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                }
                else{
                    tempArrayList.clear()
                    tempArrayList.addAll(allNoticesList)
                    binding.recyclerviewAllNotices.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllNoticesList() = CoroutineScope(Dispatchers.IO).launch {
        allNoticesList.clear()
        tempArrayList.clear()
        val dbRef = db.collection("Institutions")
            .document(instituteId)
            .collection("Notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        try{
            val querySnapshot = dbRef.get().await()
            val allNoticesList = querySnapshot.documents.mapNotNull { it.toObject<Notice>() }
            tempArrayList.addAll(allNoticesList)
            UtilFunctions.sortNoticeByTimestamp(tempArrayList)
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