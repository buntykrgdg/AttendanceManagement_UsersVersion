package com.buntykrgdg.attendancemanagementusersversion.classes.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.LeaveRequest
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LeaveHistoryAdapter (val context: Context, private val instId: String, private val empId: String, private val empPhNo: String, private val leaveRequestList:ArrayList<LeaveRequest>): RecyclerView.Adapter<LeaveHistoryAdapter.LeaveRequestViewHolder>(){

    class LeaveRequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val leaveDate : TextView =view.findViewById(R.id.txtLeaveRequestDate)
        val leaveReason : TextView =view.findViewById(R.id.txtLeaveRequestReason)
        val leaveStatus : TextView =view.findViewById(R.id.txtLeaveRequestStatus)
        val btnDeleteRequest : ImageButton =view.findViewById(R.id.btnDeleteRequest)
        val fromDate : TextView =view.findViewById(R.id.txtLeaveRequestFrom)
        val toDate : TextView =view.findViewById(R.id.txtLeaveRequestTo)
        val leaveType : TextView =view.findViewById(R.id.txtLeaveRequestLeaveType)
        val noOfLeaves : TextView =view.findViewById(R.id.txtLeaveRequestNoOfLeaves)
        val note : TextView =view.findViewById(R.id.txtLeaveRequestNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveRequestViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_leave_request_single_row,parent,false)
        return LeaveRequestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return leaveRequestList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaveRequestViewHolder, position: Int) {
        holder.btnDeleteRequest.visibility = View.GONE
        val leaveRequest = leaveRequestList[position]
        holder.leaveDate.text = leaveRequest.timestamp?.let { UtilFunctions.millisToString(it) }
        holder.leaveReason.text = leaveRequest.reason
        holder.leaveStatus.text = leaveRequest.status
        holder.fromDate.text = leaveRequest.fromdate
        holder.toDate.text = leaveRequest.todate
        holder.leaveType.text = leaveRequest.leavetype
        holder.noOfLeaves.text = leaveRequest.noofleaves
        holder.note.text = leaveRequest.note
        if(leaveRequest.status == "Pending"){
            holder.btnDeleteRequest.visibility = View.VISIBLE
        }

        holder.btnDeleteRequest.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setTitle("Delete Request")
            alertBuilder.setMessage("Do you want to delete this request?")
            alertBuilder.setPositiveButton("Delete"){_,_->
                deleteRequest(leaveRequest.timestamp.toString(), position)
            }
            alertBuilder.setNegativeButton("No"){_,_->
            }
            alertBuilder.setNeutralButton("Cancel"){_,_->
            }
            alertBuilder.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteRequest(docId: String, position: Int) = CoroutineScope(Dispatchers.IO).launch {
        val db = FirebaseFirestore.getInstance()
        // Initial attempt to delete the leave request from the employee's collection
        val employeeLeaveRef = db.collection("Institutions").document(instId)
            .collection("Employees").document(empPhNo)
            .collection("Leaves").document(docId)

        val generalLeaveRef = db.collection("Institutions").document(instId)
            .collection("Leaves").document(docId)

        try {
            // Delete the document from the employee's collection
            employeeLeaveRef.delete().await()

            // Attempt to retrieve and delete the document from the general leaves collection
            val doc = generalLeaveRef.get().await()
            if (doc.exists() && doc.toObject<LeaveRequest>()?.employeeid == empId) {
                generalLeaveRef.delete().await()
                UtilFunctions.showToast(context,"Leave Deleted")
            } else {
                UtilFunctions.showToast(context,"Leave request not found")
            }

            // Update UI on success
            withContext(Dispatchers.Main) {
                leaveRequestList.removeAt(position)
                notifyDataSetChanged() // Ensure this is called on an adapter instance
            }
        } catch (e: Exception) {
            // Handle exceptions for both delete operations
            UtilFunctions.showToast(context,e.message ?: "An error occurred")
        }
    }
}