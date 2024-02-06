package com.buntykrgdg.attendancemanagementusersversion.classes.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buntykrgdg.attendancemanagementusersversion.activities.LogsActivity
import com.buntykrgdg.attendancemanagementusersversion.R

class AllLogsAdapter (val context: Context, private val allLogsList:ArrayList<String>): RecyclerView.Adapter<AllLogsAdapter.AllLogsViewHolder>(){
    class AllLogsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logsItem: RelativeLayout = view.findViewById(R.id.logsItem)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllLogsViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_logs_single_row,parent,false)
        return AllLogsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return allLogsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AllLogsViewHolder, position: Int) {
        val log = allLogsList[position]
        holder.txtDate.text = log
        holder.logsItem.setOnClickListener {
            val intent = Intent(context, LogsActivity::class.java)
            intent.putExtra("date", log)
            context.startActivity(intent)
        }
    }

}