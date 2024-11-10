package com.example.fuelcalculator.ui.vehicles.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.model.Vehicle

class VehicleAdapter : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var vehicles: List<Vehicle> = emptyList()

    class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val vehicleName: TextView = itemView.findViewById(R.id.tvVehicleName)
        private val vehicleDetails: TextView = itemView.findViewById(R.id.tvVehicleDetails)
        private val activeStatus: ImageView = itemView.findViewById(R.id.ivActiveStatus)

        fun bind(vehicle: Vehicle) {
            vehicleName.text = "${vehicle.brand} ${vehicle.model}"
            vehicleDetails.text = buildString {
                append(vehicle.year)
                append(" • ")
                append(vehicle.engineSize)
                append(" • ")
                append(vehicle.transmission)
                append(" • ")
                append("${vehicle.mileage} km")
            }
            activeStatus.visibility = if (vehicle.isActive) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    fun submitList(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}