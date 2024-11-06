package com.example.fuelcalculator.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fuelcalculator.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CalculatorFragment : Fragment() {

    private lateinit var calculationTypeDropdown: TextInputLayout
    private lateinit var dropdownText: AutoCompleteTextView
    private lateinit var inputContainer: ViewGroup
    private lateinit var btnCalculate: MaterialButton

    private enum class CalculationType(val displayName: String) {
        FUEL_CONSUMPTION("Fuel Consumption (L)"),
        FUEL_EFFICIENCY("Fuel Efficiency (L/100km)"),
        TRAVEL_DISTANCE("Travel Distance (km)"),
        FUEL_COST("Fuel Cost Calculations")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calculator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupDropdown()
        setupCalculateButton()
    }

    private fun initializeViews(view: View) {
        calculationTypeDropdown = view.findViewById(R.id.calculationTypeDropdown)
        dropdownText = view.findViewById(R.id.dropdownText)
        inputContainer = view.findViewById(R.id.inputContainer)
        btnCalculate = view.findViewById(R.id.btnCalculate)
    }

    private fun setupDropdown() {
        val items = CalculationType.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, items)
        dropdownText.setAdapter(adapter)

        dropdownText.setOnItemClickListener { _, _, position, _ ->
            //Clear previous inputs
            inputContainer.removeAllViews()

            //Load appropriate input layout
            val inputLayout = when (CalculationType.values()[position]) {
                CalculationType.FUEL_CONSUMPTION -> R.layout.layout_fuel_consumption_input
                CalculationType.FUEL_EFFICIENCY -> R.layout.layout_fuel_efficiency_input
                CalculationType.TRAVEL_DISTANCE -> R.layout.layout_travel_distance_input
                CalculationType.FUEL_COST -> R.layout.layout_fuel_cost_input
            }

            //Inflate the layout
            LayoutInflater.from(requireContext())
                .inflate(inputLayout, inputContainer, true)

            //Set up cost type dropdown if fuel cost is selected
            if (CalculationType.values()[position] == CalculationType.FUEL_COST) {
                setupCostTypeDropdown()
            }
        }
    }

    private fun setupCostTypeDropdown() {
        val costTypeDropdown = inputContainer.findViewById<AutoCompleteTextView>(R.id.actvCostType)
        val costTypes = arrayOf(
            "Total Fuel Cost",
            "Cost per Distance",
            "Distance with Budget"
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, costTypes)
        costTypeDropdown?.setAdapter(adapter)

        //Handle cost type selection to show/hide relevant fields
        costTypeDropdown?.setOnItemClickListener { _, _, position, _ ->
            val costInputContainer = inputContainer.findViewById<LinearLayout>(R.id.costInputContainer)
            costInputContainer?.let {
                updateCostInputFields(costTypes[position], it)
            }
        }
    }

    private fun updateCostInputFields(costType: String, container: LinearLayout) {
        //Hide all fields initially
        container.findViewById<TextInputLayout>(R.id.tilFuelAmount)?.visibility = View.GONE
        container.findViewById<TextInputLayout>(R.id.tilPricePerUnit)?.visibility = View.GONE
        container.findViewById<TextInputLayout>(R.id.tilDistance)?.visibility = View.GONE
        container.findViewById<TextInputLayout>(R.id.tilBudget)?.visibility = View.GONE

        //Show relevant fields based on calculation type
        when (costType) {
            "Total Fuel Cost" -> {
                container.findViewById<TextInputLayout>(R.id.tilFuelAmount)?.visibility = View.VISIBLE
                container.findViewById<TextInputLayout>(R.id.tilPricePerUnit)?.visibility = View.VISIBLE
            }
            "Cost per Distance" -> {
                container.findViewById<TextInputLayout>(R.id.tilFuelAmount)?.visibility = View.VISIBLE
                container.findViewById<TextInputLayout>(R.id.tilPricePerUnit)?.visibility = View.VISIBLE
                container.findViewById<TextInputLayout>(R.id.tilDistance)?.visibility = View.VISIBLE
            }
            "Distance with Budget" -> {
                container.findViewById<TextInputLayout>(R.id.tilBudget)?.visibility = View.VISIBLE
                container.findViewById<TextInputLayout>(R.id.tilPricePerUnit)?.visibility = View.VISIBLE
                container.findViewById<TextInputLayout>(R.id.tilEfficiency)?.visibility = View.VISIBLE
            }
        }
    }

    private fun setupCalculateButton() {
        btnCalculate.setOnClickListener {
            val selectedType = CalculationType.values().find {
                it.displayName == dropdownText.text.toString()
            } ?: return@setOnClickListener

            when (selectedType) {
                CalculationType.FUEL_CONSUMPTION -> calculateFuelConsumption()
                CalculationType.FUEL_EFFICIENCY -> calculateFuelEfficiency()
                CalculationType.TRAVEL_DISTANCE -> calculateTravelDistance()
                CalculationType.FUEL_COST -> calculateFuelCost()
            }
        }
    }

    private fun calculateFuelConsumption() {
        val distance = inputContainer.findViewById<TextInputEditText>(R.id.etDistance)
            ?.text?.toString()?.toDoubleOrNull() ?: return
        val efficiency = inputContainer.findViewById<TextInputEditText>(R.id.etEfficiency)
            ?.text?.toString()?.toDoubleOrNull() ?: return

        val consumption = distance * efficiency / 100
        showResult("Fuel Consumption", "${String.format("%.2f", consumption)} L")
    }

    //Helper function to show calculation result TODO: Add to a fixed element
    private fun showResult(title: String, result: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(result)
            .setPositiveButton("OK", null)
            .show()
    }

    //Calculate fuel efficiency function
    private fun calculateFuelEfficiency() {
        val consumption = inputContainer.findViewById<TextInputEditText>(R.id.etFuelConsumption)
            ?.text?.toString()?.toDoubleOrNull()
        val distance = inputContainer.findViewById<TextInputEditText>(R.id.etDistance)
            ?.text?.toString()?.toDoubleOrNull()

        if (consumption == null || distance == null) {
            Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (distance == 0.0) {
            Toast.makeText(context, "Distance cannot be zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Fuel Efficiency (L/100km) = (Fuel Consumption × 100) ÷ Distance
        val efficiency = (consumption * 100) / distance
        showResult(
            "Fuel Efficiency",
            "${String.format("%.2f", efficiency)} L/100km"
        )
    }

    //Calculate travel distance based on current fuel efficiency
    private fun calculateTravelDistance() {
        val fuelAmount = inputContainer.findViewById<TextInputEditText>(R.id.etFuelAmount)
            ?.text?.toString()?.toDoubleOrNull()
        val efficiency = inputContainer.findViewById<TextInputEditText>(R.id.etEfficiency)
            ?.text?.toString()?.toDoubleOrNull()

        if (fuelAmount == null || efficiency == null) {
            Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (efficiency == 0.0) {
            Toast.makeText(context, "Efficiency cannot be zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Distance (km) = Fuel Amount × (100 ÷ L/100km)
        val distance = fuelAmount * (100 / efficiency)
        showResult(
            "Travel Distance",
            "${String.format("%.2f", distance)} km"
        )
    }

    //Calculate cost of fuel calls
    private fun calculateFuelCost() {
        // Get the selected cost calculation type
        val costType = inputContainer.findViewById<AutoCompleteTextView>(R.id.actvCostType)
            ?.text?.toString() ?: return

        when (costType) {
            "Total Fuel Cost" -> calculateTotalFuelCost()
            "Cost per Distance" -> calculateCostPerDistance()
            "Distance with Budget" -> calculateDistanceWithBudget()
        }
    }

    //Calculate total fuel cost
    private fun calculateTotalFuelCost() {
        val consumption = inputContainer.findViewById<TextInputEditText>(R.id.etFuelConsumption)
            ?.text?.toString()?.toDoubleOrNull()
        val pricePerUnit = inputContainer.findViewById<TextInputEditText>(R.id.etPricePerUnit)
            ?.text?.toString()?.toDoubleOrNull()

        if (consumption == null || pricePerUnit == null) {
            Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        // Total Fuel Cost = Fuel Consumption × Price per unit
        val totalCost = consumption * pricePerUnit
        showResult(
            "Total Fuel Cost",
            "R ${String.format("%.2f", totalCost)}"
        )
    }

    //Calculate cost per distance
    private fun calculateCostPerDistance() {
        val totalCost = inputContainer.findViewById<TextInputEditText>(R.id.etFuelConsumption)
            ?.text?.toString()?.toDoubleOrNull()?.let { consumption ->
                inputContainer.findViewById<TextInputEditText>(R.id.etPricePerUnit)
                    ?.text?.toString()?.toDoubleOrNull()?.let { price ->
                        consumption * price
                    }
            }
        val distance = inputContainer.findViewById<TextInputEditText>(R.id.etDistance)
            ?.text?.toString()?.toDoubleOrNull()

        if (totalCost == null || distance == null) {
            Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (distance == 0.0) {
            Toast.makeText(context, "Distance cannot be zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Cost per Distance = Total Fuel Cost ÷ Distance traveled
        val costPerKm = totalCost / distance
        showResult(
            "Cost per Distance",
            "R ${String.format("%.2f", costPerKm)} per km"
        )
    }

    //Calculate distance you can travel with certain amount of fuel
    private fun calculateDistanceWithBudget() {
        val budget = inputContainer.findViewById<TextInputEditText>(R.id.etBudget)
            ?.text?.toString()?.toDoubleOrNull()
        val pricePerUnit = inputContainer.findViewById<TextInputEditText>(R.id.etPricePerUnit)
            ?.text?.toString()?.toDoubleOrNull()
        val efficiency = inputContainer.findViewById<TextInputEditText>(R.id.etEfficiency)
            ?.text?.toString()?.toDoubleOrNull()

        if (budget == null || pricePerUnit == null || efficiency == null) {
            Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (pricePerUnit == 0.0 || efficiency == 0.0) {
            Toast.makeText(context, "Price and efficiency cannot be zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Distance possible with budget = Budget ÷ (Price per unit ÷ Fuel Efficiency)
        val possibleDistance = (budget / pricePerUnit) * (100 / efficiency)
        showResult(
            "Possible Distance with Budget",
            "${String.format("%.2f", possibleDistance)} km"
        )
    }

    // Helper function to show multiple results TODO: Call it
    private fun showDetailedResult(title: String, results: Map<String, String>) {
        val message = results.entries.joinToString("\n\n") { (label, value) ->
            "$label: $value"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

}