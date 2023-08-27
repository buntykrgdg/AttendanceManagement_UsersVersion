package com.buntykrgdg.attendancemanagementusersversion.classes.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.classes.LeaveRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LeaveHistoryAdapter (val context: Context, val instId: String, val empId: String, val LeaveRequestList:ArrayList<LeaveRequest>): RecyclerView.Adapter<LeaveHistoryAdapter.LeaveRequestViewHolder>(){

    private val db = FirebaseFirestore.getInstance()
    class LeaveRequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val leaveItem : RelativeLayout = view.findViewById(R.id.leaveItem)
        val LeaveDate : TextView =view.findViewById(R.id.txtLeaveRequestDate)
        val LeaveReason : TextView =view.findViewById(R.id.txtLeaveRequestReason)
        val Leavestatus : TextView =view.findViewById(R.id.txtLeaveRequestStatus)
        val btnDeleteRequest : ImageButton =view.findViewById(R.id.btnDeleteRequest)
        val FromDate : TextView =view.findViewById(R.id.txtLeaveRequestFrom)
        val ToDate : TextView =view.findViewById(R.id.txtLeaveRequestTo)
        val LeaveType : TextView =view.findViewById(R.id.txtLeaveRequestLeaveType)
        val NoOfLeaves : TextView =view.findViewById(R.id.txtLeaveRequestNoOfLeaves)
        val Note : TextView =view.findViewById(R.id.txtLeaveRequestNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveRequestViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_leave_request_single_row,parent,false)

        return LeaveRequestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return LeaveRequestList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaveRequestViewHolder, position: Int) {
        holder.btnDeleteRequest.visibility = View.GONE
        val Leaverequest = LeaveRequestList[position]
        holder.LeaveDate.text = Leaverequest.timestamp
        holder.LeaveReason.text = Leaverequest.reason
        holder.Leavestatus.text = Leaverequest.status
        holder.FromDate.text = Leaverequest.fromdate
        holder.ToDate.text = Leaverequest.todate
        holder.LeaveType.text = Leaverequest.leavetype
        holder.NoOfLeaves.text = Leaverequest.noofleaves
        holder.Note.text = Leaverequest.note
        if(Leaverequest.status == "Pending"){
            holder.btnDeleteRequest.visibility = View.VISIBLE
        }

        holder.btnDeleteRequest.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setTitle("Delete Request")
            alertBuilder.setMessage("Do you want to delete this request?")
            alertBuilder.setPositiveButton("Delete"){_,_->
                deleteRequest(Leaverequest.timestamp.toString())
                LeaveRequestList.removeAt(position)
                notifyDataSetChanged()
            }
            alertBuilder.setNegativeButton("No"){_,_->

            }
            alertBuilder.setNeutralButton("Cancel"){_,_->

            }
            alertBuilder.show()
        }
    }

    private fun deleteRequest(docId: String) = CoroutineScope(Dispatchers.IO).launch {
        val dbref = db.collection("Institutions").document(instId)
            .collection("Employees")
            .document(empId).collection("Leaves").document(docId)
        dbref.delete().await()

        val query = db.collection("Institutions").document(instId)
            .collection("Leaves").whereEqualTo("timestamp", docId).whereEqualTo("employeeid", empId)
        val docs = query.get().await()
        for(doc in docs.documents){
            val docref = doc.reference
            docref.delete().await()
        }
        withContext(Dispatchers.Main){
            Toast.makeText(context, "Leave Deleted", Toast.LENGTH_SHORT).show()
        }
    }
}