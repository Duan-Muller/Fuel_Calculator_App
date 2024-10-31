package com.example.fuelcalculator.ui.vehicles.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.ui.common.VehicleCategories

class CategoryAdapter(
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private var selectedPosition = 0
    private val categories = VehicleCategories.CATEGORIES

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryText: TextView = itemView.findViewById(R.id.tvCategory)
        val categoryCard: CardView = itemView.findViewById(R.id.cardCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryText.text = category

        // Update selected state
        holder.categoryCard.setCardBackgroundColor(
            if (position == selectedPosition)
                ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.white)
        )

        holder.categoryText.setTextColor(
            if (position == selectedPosition)
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.black)
        )

        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onCategorySelected(category)
        }
    }

    override fun getItemCount() = categories.size
}