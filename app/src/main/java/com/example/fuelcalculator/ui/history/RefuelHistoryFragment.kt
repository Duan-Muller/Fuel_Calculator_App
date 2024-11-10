package com.example.fuelcalculator.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.db.RefuelDBHelper
import com.example.fuelcalculator.data.model.RefuelRecord
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Locale

class RefuelHistoryFragment : Fragment() {
    private lateinit var rvHistory: RecyclerView
    private lateinit var historyAdapter: RefuelHistoryAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var refuelDBHelper: RefuelDBHelper
    private lateinit var authManager: FirebaseAuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_refuel_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refuelDBHelper = RefuelDBHelper(requireContext())
        authManager = FirebaseAuthManager()

        toolbar = view.findViewById(R.id.toolbar)
        rvHistory = view.findViewById(R.id.rvHistory)

        setupToolbar()
        setupRecyclerView()
        loadRefuelHistory()
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = RefuelHistoryAdapter()
        rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadRefuelHistory() {
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId != null) {
            val history = refuelDBHelper.getRefuelHistory(currentUserId)
            val groupedHistory = groupHistoryByMonth(history)
            historyAdapter.submitList(groupedHistory)
        }
    }

    private fun groupHistoryByMonth(history: List<RefuelRecord>): List<HistoryItem> {
        val groupedItems = mutableListOf<HistoryItem>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        // Group records by month and year
        history.groupBy { record ->
            val date = dateFormat.parse(record.dateRecorded)
            monthYearFormat.format(date!!)
        }.forEach { (monthYear, records) ->
            groupedItems.add(HistoryItem.Header(monthYear))
            records.forEach { record ->
                groupedItems.add(HistoryItem.RefuelItem(record))
            }
        }

        return groupedItems
    }
}

// HistoryItem.kt
sealed class HistoryItem {
    data class Header(val monthYear: String) : HistoryItem()
    data class RefuelItem(val refuelRecord: RefuelRecord) : HistoryItem()
}

// RefuelHistoryAdapter.kt
class RefuelHistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<HistoryItem> = emptyList()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_REFUEL = 1
    }

    fun submitList(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryItem.Header -> TYPE_HEADER
            is HistoryItem.RefuelItem -> TYPE_REFUEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_REFUEL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_refuel_history, parent, false)
                RefuelViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HistoryItem.Header -> (holder as HeaderViewHolder).bind(item)
            is HistoryItem.RefuelItem -> (holder as RefuelViewHolder).bind(item.refuelRecord)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMonthYear: TextView = itemView.findViewById(R.id.tvMonthYear)

        fun bind(header: HistoryItem.Header) {
            tvMonthYear.text = header.monthYear
        }
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
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            return try {
                val date = inputFormat.parse(dateStr)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}