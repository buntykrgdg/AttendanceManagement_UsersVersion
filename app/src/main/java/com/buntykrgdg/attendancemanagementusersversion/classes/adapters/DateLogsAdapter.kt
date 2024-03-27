package com.buntykrgdg.attendancemanagementusersversion.classes.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.classes.dataclasses.CheckInOutLog
import com.buntykrgdg.attendancemanagementusersversion.R
import com.buntykrgdg.attendancemanagementusersversion.objects.UtilFunctions

class DateLogsAdapter (val context: Context, private val allLogsList:ArrayList<CheckInOutLog>): RecyclerView.Adapter<DateLogsAdapter.DateLogsViewHolder>(){
    class DateLogsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTimeStamp: TextView = view.findViewById(R.id.txtdateTimeStamp)
        val txtReason: TextView = view.findViewById(R.id.txtReason)
        val txtReasonHead: TextView = view.findViewById(R.id.txtdateReasonHead)
        val imgInOut: ImageView = view.findViewById(R.id.imgInOut)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateLogsViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_datelog_single_row,parent,false)
        return DateLogsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return allLogsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DateLogsViewHolder, position: Int) {
        val log = allLogsList[position]
        holder.txtTimeStamp.text = log.timestamp?.let { UtilFunctions.millisToString(it) }
        when(log.status){
            "Checked In" -> {
                holder.txtReasonHead.visibility = View.GONE
                holder.txtReason.visibility = View.GONE
                holder.imgInOut.setImageResource(R.drawable.arrowin)
            }

            "Checked Out" -> {
                holder.txtReason.text = log.reason
                holder.imgInOut.setImageResource(R.drawable.outs)
            }
        }
    }

}