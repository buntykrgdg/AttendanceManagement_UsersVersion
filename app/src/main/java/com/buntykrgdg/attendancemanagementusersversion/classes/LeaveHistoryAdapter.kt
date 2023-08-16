package com.buntykrgdg.attendancemanagementusersversion.classes

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.R

class LeaveHistoryAdapter (val context: Context, val LeaveRequestList:ArrayList<LeaveRequest>): RecyclerView.Adapter<LeaveHistoryAdapter.LeaveRequestViewHolder>(){

    class LeaveRequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val LeaveDate : TextView =view.findViewById(R.id.txtLeaveRequestDate)
        val LeaveReason : TextView =view.findViewById(R.id.txtLeaveRequestReason)
        val Leavestatus : TextView =view.findViewById(R.id.txtLeaveRequestStatus)
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
        val Leaverequest = LeaveRequestList[position]
        holder.LeaveDate.text = Leaverequest.timestamp
        holder.LeaveReason.text = Leaverequest.reason
        holder.Leavestatus.text = Leaverequest.status
        holder.FromDate.text = Leaverequest.fromdate
        holder.ToDate.text = Leaverequest.todate
        holder.LeaveType.text = Leaverequest.leavetype
        holder.NoOfLeaves.text = Leaverequest.noofleaves
        holder.Note.text = Leaverequest.note
    }
}