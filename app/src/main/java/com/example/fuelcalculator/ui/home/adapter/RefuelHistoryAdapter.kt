package com.example.fuelcalculator.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.model.RefuelRecord
import java.text.SimpleDateFormat
import java.util.Locale

class RefuelHistoryAdapter : RecyclerView.Adapter<RefuelHistoryAdapter.RefuelViewHolder>() {

    private var refuelHistory: List<RefuelRecord> = listOf()

    fun updateData(newHistory: List<RefuelRecord>) {
        refuelHistory = newHistory.sortedByDescending { it.dateRecorded }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefuelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_refuel_history, parent, false)
        return RefuelViewHolder(view)
    }

    override fun getItemCount() = refuelHistory.size

    override fun onBindViewHolder(holder: RefuelViewHolder, position: Int) {
        holder.bind(refuelHistory[position])
    }

    class RefuelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName: TextView = itemView.findViewById(R.id.tvStationName)
        private val tvRefuelInfo: TextView = itemView.findViewById(R.id.tvRefuelInfo)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(record: RefuelRecord) {
            tvStationName.text = record.location
            tvRefuelInfo.text = "${record.status} • ${formatDate(record.dateRecorded)}"
            tvAmount.text = "R%.2f".format(record.price)
        }

        private fun formatDate(dateStr: String): String {
            try {
                // Parse the full date time string
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                // Format to display only the date
                val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                return outputFormat.format(date!!)
            } catch (e: Exception) {
                return dateStr // Fallback in case of parsing error
            }
        }
    }
}