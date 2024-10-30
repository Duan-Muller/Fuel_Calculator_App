package com.example.fuelcalculator.ui.vehicles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyVehiclesFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvVehicles: RecyclerView
    private lateinit var btnAddVehicle: FloatingActionButton
    private lateinit var btnSetActive: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_vehicles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        //TODO: Add and load data from DB
    }

    private fun initializeViews(view: View) {
        //Initialize recycler views
        rvCategories = view.findViewById(R.id.rvCategories)
        rvVehicles = view.findViewById(R.id.rvVehicles)

        //Initialize buttons
        btnAddVehicle = view.findViewById(R.id.btnAddVehicle)
        btnSetActive = view.findViewById(R.id.btnSetActive)
    }

    private fun setupClickListeners() {
        btnAddVehicle.setOnClickListener {
            //TODO: Add functionality
        }

        btnSetActive.setOnClickListener {
            //TODO: Add functionality
        }
    }
}
