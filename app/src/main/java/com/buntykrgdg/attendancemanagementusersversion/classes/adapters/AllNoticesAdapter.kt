package com.buntykrgdg.attendancemanagementusersversion.classes.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.Notice

class AllNoticesAdapter (val context: Context, private val allNoticesList:ArrayList<Notice>): RecyclerView.Adapter<AllNoticesAdapter.AllNoticesViewHolder>(){
    class AllNoticesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTimeStamp: TextView = view.findViewById(R.id.txtTimeStamp)
        val txtNotice: TextView = view.findViewById(R.id.txtNotice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllNoticesViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_notices_single_row,parent,false)
        return AllNoticesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return allNoticesList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AllNoticesViewHolder, position: Int) {
        val notice = allNoticesList[position]
        holder.txtTimeStamp.text = notice.timestamp
        holder.txtNotice.text = notice.message
    }
}