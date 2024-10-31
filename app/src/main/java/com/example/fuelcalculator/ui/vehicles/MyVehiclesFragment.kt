package com.example.fuelcalculator.ui.vehicles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.db.VehicleDBHelper
import com.example.fuelcalculator.data.model.Vehicle
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.ui.auth.AuthActivity
import com.example.fuelcalculator.ui.common.VehicleCategories
import com.example.fuelcalculator.ui.vehicles.adapter.CategoryAdapter
import com.example.fuelcalculator.ui.vehicles.adapter.VehicleAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class MyVehiclesFragment : Fragment() {

    private lateinit var categoryAdapter: CategoryAdapter
    private var currentCategory: String = "All Cars"

    private val authManager = FirebaseAuthManager()

    private lateinit var vehicleAdapter: VehicleAdapter

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
        setupRecyclerView()
        setupClickListeners()
        refreshVehicleList()
    }

    private fun initializeViews(view: View) {
        //Initialize recycler views
        rvCategories = view.findViewById(R.id.rvCategories)
        rvVehicles = view.findViewById(R.id.rvVehicles)

        //Initialize buttons
        btnAddVehicle = view.findViewById(R.id.btnAddVehicle)
        btnSetActive = view.findViewById(R.id.btnSetActive)
    }

    private fun setupRecyclerView() {

        categoryAdapter = CategoryAdapter { category ->
            currentCategory = category
            filterVehicles()
        }

        rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        vehicleAdapter = VehicleAdapter()
        rvVehicles.apply {
            adapter = vehicleAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun filterVehicles() {
        try {
            val dbHelper = VehicleDBHelper(requireContext())
            val allVehicles = dbHelper.getVehiclesForUser(authManager.requireCurrentUserId())

            val filteredVehicles = when (currentCategory) {
                "All Cars" -> allVehicles
                else -> allVehicles.filter { it.vehicleType == currentCategory }
            }

            vehicleAdapter.submitList(filteredVehicles)
        } catch (e: IllegalStateException) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }


    private fun setupClickListeners() {
        btnAddVehicle.setOnClickListener {
            showAddVehicleDialog()
        }

        btnSetActive.setOnClickListener {
            showSetActiveVehicleDialog()
        }
    }

    private fun showAddVehicleDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Vehicle")
            .setView(R.layout.dialog_add_vehicle)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Get dialog view references
        val dialogView = dialog.findViewById<View>(android.R.id.content)!!
        setupDialogSpinners(dialogView)

        // Override positive button to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (validateInputs(dialogView)) {
                saveVehicle(dialogView)
                dialog.dismiss()
            }
        }
    }

    private fun showSetActiveVehicleDialog() {
        try {
            val dbHelper = VehicleDBHelper(requireContext())
            val vehicles = dbHelper.getVehiclesForUser(authManager.requireCurrentUserId())

            val vehicleNames = vehicles.map { "${it.brand} ${it.model} (${it.year})" }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Set Active Vehicle")
                .setItems(vehicleNames.toTypedArray()) { _, which ->
                    // Set the selected vehicle as active
                    val selectedVehicle = vehicles[which]
                    dbHelper.setActiveVehicle(selectedVehicle.vehicleId, authManager.requireCurrentUserId())
                    refreshVehicleList()
                }
                .show()
        } catch (e: IllegalStateException) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun validateInputs(dialogView: View): Boolean {
        val fuelType = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerFuelType).text.toString()
        val vehicleType = dialogView.findViewById<AutoCompleteTextView>(R.id.etVehicleType).text.toString()
        val brand = dialogView.findViewById<TextInputEditText>(R.id.etBrand).text.toString()
        val model = dialogView.findViewById<TextInputEditText>(R.id.etModel).text.toString()
        val engineSize = dialogView.findViewById<TextInputEditText>(R.id.etEngineSize).text.toString()
        val transmission = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerTransmission).text.toString()
        val yearStr = dialogView.findViewById<TextInputEditText>(R.id.etYear).text.toString()
        val efficiencyStr = dialogView.findViewById<TextInputEditText>(R.id.etFuelEfficiency).text.toString()
        val mileageStr = dialogView.findViewById<TextInputEditText>(R.id.etMileage).text.toString()
        val region = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerRegion).text.toString()

        // Validation checks
        when {
            fuelType.isEmpty() -> showError("Please select a fuel type")
            vehicleType.isEmpty() -> showError("Please enter a vehicle type")
            brand.isEmpty() -> showError("Please enter a brand")
            model.isEmpty() -> showError("Please enter a model")
            engineSize.isEmpty() -> showError("Please enter an engine size")
            transmission.isEmpty() -> showError("Please select a transmission type")
            yearStr.isEmpty() -> showError("Please enter a year")
            efficiencyStr.isEmpty() -> showError("Please enter fuel efficiency")
            mileageStr.isEmpty() -> showError("Please enter mileage")
            region.isEmpty() -> showError("Please select a region")
            yearStr.toIntOrNull() == null -> showError("Please enter a valid year")
            yearStr.toInt() !in 1900..getCurrentYear() -> showError("Please enter a valid year between 1900 and ${getCurrentYear()}")
            efficiencyStr.toDoubleOrNull() == null -> showError("Please enter a valid fuel efficiency")
            efficiencyStr.toDouble() <= 0 -> showError("Fuel efficiency must be greater than 0")
            mileageStr.toIntOrNull() == null -> showError("Please enter a valid mileage")
            mileageStr.toInt() < 0 -> showError("Mileage cannot be negative")
            else -> return true
        }
        return false
    }

    private fun saveVehicle(dialogView: View) {
        try {
            val userId = authManager.requireCurrentUserId()
            val vehicle = Vehicle(
                userId = userId,
                fuelType = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerFuelType).text.toString(),
                vehicleType = dialogView.findViewById<AutoCompleteTextView>(R.id.etVehicleType).text.toString(),
                brand = dialogView.findViewById<TextInputEditText>(R.id.etBrand).text.toString(),
                model = dialogView.findViewById<TextInputEditText>(R.id.etModel).text.toString(),
                engineSize = dialogView.findViewById<TextInputEditText>(R.id.etEngineSize).text.toString(),
                transmission = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerTransmission).text.toString(),
                year = dialogView.findViewById<TextInputEditText>(R.id.etYear).text.toString().toInt(),
                fuelEfficiency = dialogView.findViewById<TextInputEditText>(R.id.etFuelEfficiency).text.toString().toDouble(),
                mileage = dialogView.findViewById<TextInputEditText>(R.id.etMileage).text.toString().toInt(),
                region = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerRegion).text.toString()
            )

            val dbHelper = VehicleDBHelper(requireContext())
            dbHelper.addVehicle(vehicle)
            refreshVehicleList()
        } catch (e: IllegalStateException) {
            //Handle authentication error
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun refreshVehicleList() {
        filterVehicles()
    }

    private fun setupDialogSpinners(dialogView: View) {
        // Fuel Type Spinner
        val fuelTypeSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerFuelType)
        val fuelTypes = arrayOf("Petrol 93", "Petrol 95", "Diesel 50ppm", "Diesel 500ppm")
        fuelTypeSpinner.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, fuelTypes))

        // Transmission Spinner
        val transmissionSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerTransmission)
        val transmissionTypes = arrayOf("Manual", "Automatic")
        transmissionSpinner.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, transmissionTypes))

        // Region Spinner
        val regionSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerRegion)
        val regions = arrayOf("Inland", "Coastal")
        regionSpinner.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, regions))

        // Vehicle Type Spinner
        val vehicleTypeSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.etVehicleType)
        val categories = VehicleCategories.CATEGORIES.filter { it != "All Cars" }
        vehicleTypeSpinner.setAdapter(
            ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        )
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

}
