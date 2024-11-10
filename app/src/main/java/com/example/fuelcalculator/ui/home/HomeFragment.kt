package com.example.fuelcalculator.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.db.RefuelDBHelper
import com.example.fuelcalculator.data.db.VehicleDBHelper
import com.example.fuelcalculator.data.model.RefuelRecord
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.ui.home.adapter.RefuelHistoryAdapter
import com.example.fuelcalculator.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment :  Fragment(), HomeContract.View {

    //Presenter and Firebase Auth initialization
    private lateinit var presenter: HomeContract.Presenter
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var refuelDBHelper: RefuelDBHelper
    private lateinit var refuelHistoryAdapter: RefuelHistoryAdapter
    private lateinit var vehicleDBHelper: VehicleDBHelper

    //Views
    private lateinit var btnProfile: MaterialButton
    private lateinit var tvNoHistory: TextView
    private lateinit var tvExpenseAmount: TextView
    private lateinit var btnRefuel: MaterialButton
    private lateinit var btnReport: MaterialButton
    private lateinit var rvRefuelHistory : RecyclerView
    private lateinit var tvSeeAll : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = FirebaseAuthManager()
        refuelDBHelper = RefuelDBHelper(requireContext().applicationContext)
        vehicleDBHelper = VehicleDBHelper(requireContext().applicationContext)
        presenter = HomePresenter(this, authManager, refuelDBHelper)

        btnProfile = view.findViewById(R.id.btnProfile)
        val currentUserId = authManager.getCurrentUserId()

        lifecycleScope.launch {
            if (currentUserId != null) {
                authManager.getUserDisplayName(currentUserId).onSuccess { username ->
                    // Update the userInitialButton with the first letter of the username
                    btnProfile.text = username?.firstOrNull()?.toString() ?: "A"
                }.onFailure { error ->
                    // Handle the error
                    showError("Failed to get user display name: ${error.message}")
                }
            } else {
                // Handle the case where the user is not logged in
                showError("User is not logged in")
            }
        }

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadRefuelHistory()
        presenter.onViewCreated()
    }

    private fun loadRefuelHistory() {
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId != null) {
            val history = refuelDBHelper.getRefuelHistory(currentUserId)
            showRefuelHistory(history)
        } else {
            showError("User not logged in")
            showRefuelHistory(emptyList())
        }
    }

    private fun setupRecyclerView() {
        refuelHistoryAdapter = RefuelHistoryAdapter()
        rvRefuelHistory.apply {
            adapter = refuelHistoryAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun showRefuelHistory(history: List<RefuelRecord>) {
        if (history.isEmpty()) {
            rvRefuelHistory.visibility = View.GONE
            tvNoHistory.visibility = View.VISIBLE
        } else {
            rvRefuelHistory.visibility = View.VISIBLE
            tvNoHistory.visibility = View.GONE
            refuelHistoryAdapter.updateData(history)
        }
    }

    private fun initializeViews(view: View) {
        //Initialize page elements
        btnProfile = view.findViewById(R.id.btnProfile)
        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount)
        btnRefuel = view.findViewById(R.id.btnRefuel)
        btnReport = view.findViewById(R.id.btnReport)
        rvRefuelHistory = view.findViewById(R.id.rvRefuelHistory)
        tvSeeAll = view.findViewById(R.id.tvSeeAll)
        tvNoHistory = view.findViewById(R.id.tvNoHistory)
    }

    //Click listeners for buttons
    private fun setupClickListeners() {
        btnProfile.setOnClickListener {
            presenter.onProfileButtonClicked()
        }
        btnRefuel.setOnClickListener {
            showRefuelDialog()
        }
        btnReport.setOnClickListener {
            val userId = authManager.getCurrentUserId()
            if (userId != null) {
                val refuelEntries = refuelDBHelper.getRefuelHistory(userId)
                generatePDF(refuelEntries)
            } else {
                showError("User not logged in")
            }
        }
        tvSeeAll.setOnClickListener {
            presenter.onSeeAllClicked()
        }
    }

    private fun generatePDF(refuelEntries: List<RefuelRecord>) {
        val document = Document()
        val fileName = "refuel_entries_${System.currentTimeMillis()}.pdf"
        val filePath = requireContext().getExternalFilesDir(null)?.absolutePath + "/" + fileName

        try {
            PdfWriter.getInstance(document, FileOutputStream(filePath))
            document.open()

            // Group entries by month-year
            val groupedEntries = refuelEntries.groupBy { entry ->
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(entry.dateRecorded)
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            }

            // Create table for each month
            groupedEntries.forEach { (monthYear, entries) ->
                // Add month-year header
                document.add(Paragraph(monthYear, Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)))
                document.add(Paragraph("\n"))

                val table = PdfPTable(7) // 7 columns for the required fields
                table.widthPercentage = 100f
                table.setWidths(floatArrayOf(2.5f, 2.5f, 2f, 2f, 1.5f, 2f, 2f))

                // Add table headers
                arrayOf("Vehicle", "Date", "Location", "Price", "Liters", "Odometer", "Status").forEach {
                    val cell = PdfPCell(Phrase(it, Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)))
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    cell.backgroundColor = BaseColor.LIGHT_GRAY
                    table.addCell(cell)
                }

                // Add entries
                entries.forEach { entry ->
                    // Get vehicle details
                    val vehicle = vehicleDBHelper.getVehicleById(entry.vehicleId)
                    val vehicleText = "${vehicle?.brand ?: ""} ${vehicle?.model ?: ""}"

                    // Format date
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .parse(entry.dateRecorded)
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(date)

                    // Add cells
                    arrayOf(
                        vehicleText,
                        formattedDate,
                        entry.location,
                        String.format("R %.2f", entry.price),
                        String.format("%.2f L", entry.liters),
                        entry.odometerReading.toString(),
                        entry.status
                    ).forEach { cellText ->
                        val cell = PdfPCell(Phrase(cellText))
                        cell.horizontalAlignment = Element.ALIGN_CENTER
                        table.addCell(cell)
                    }
                }

                document.add(table)
                document.add(Paragraph("\n")) // Add space between tables
            }

            document.close()
            showMessage("PDF exported successfully: $filePath")
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to generate PDF")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showRefuelDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_refuel, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Initialize dialog views
        val fuelTypeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.fuelTypeInput)
        val stationInput = dialogView.findViewById<TextInputEditText>(R.id.stationInput)
        val priceInput = dialogView.findViewById<TextInputEditText>(R.id.priceInput)
        val refuelTypeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.refuelTypeInput)
        val litersInput = dialogView.findViewById<TextInputEditText>(R.id.litersInput)
        val odometerInput = dialogView.findViewById<TextInputEditText>(R.id.odometerInput)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)

        // Setup fuel type dropdown
        val fuelTypes = arrayOf("Petrol 93", "Petrol 95", "Diesel 50ppm", "Diesel 500ppm")
        val fuelTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            fuelTypes
        )
        fuelTypeInput.setAdapter(fuelTypeAdapter)

        // Setup refuel type dropdown using Android's built-in layout
        val refuelTypes = arrayOf("Full Refuel", "Partial Refuel")
        val refuelTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            refuelTypes
        )
        refuelTypeInput.setAdapter(refuelTypeAdapter)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val fuelType = fuelTypeInput.text.toString()
            val station = stationInput.text.toString()
            val price = priceInput.text.toString().toDoubleOrNull()
            val refuelType = refuelTypeInput.text.toString()
            val liters = litersInput.text.toString().toDoubleOrNull()
            val odometer = odometerInput.text.toString().toIntOrNull()

            if (validateInput(fuelType, station, price, refuelType, liters, odometer)) {
                val userId = authManager.getCurrentUser()?.uid

                if (userId != null) {
                    val activeVehicle = vehicleDBHelper.getActiveVehicle(userId)
                    if (activeVehicle == null) {
                        showError("Please set an active vehicle first")
                        return@setOnClickListener
                    }

                    // Update vehicle mileage with new odometer reading
                    vehicleDBHelper.updateVehicleMileage(activeVehicle.vehicleId, odometer!!)

                    val success = refuelDBHelper.addRefuelEntry(
                        fuelId = getFuelId(fuelType),
                        userId = userId,
                        vehicleId = activeVehicle.vehicleId,
                        location = station,
                        price = price!!,
                        status = refuelType,
                        liters = liters!!,
                        odometerReading = odometer
                    )

                    if (success != -1L) {
                        loadRefuelHistory()
                        presenter.onViewCreated()
                        dialog.dismiss()
                    } else {
                        showError("Failed to save refuel entry")
                    }
                } else {
                    showError("User not logged in")
                }
            }
        }

        dialog.show()
    }

    private fun validateInput(
        fuelType: String,
        station: String,
        price: Double?,
        refuelType: String,
        liters: Double?,
        odometer: Int?
    ): Boolean {
        val userId = authManager.getCurrentUser()?.uid
        val activeVehicle = if (userId != null) vehicleDBHelper.getActiveVehicle(userId) else null

        when {
            fuelType.isEmpty() -> {
                showError("Please select a fuel type")
                return false
            }
            station.isEmpty() -> {
                showError("Please enter a station name")
                return false
            }
            price == null -> {
                showError("Please enter a valid price")
                return false
            }
            refuelType.isEmpty() -> {
                showError("Please select refuel type")
                return false
            }
            liters == null -> {
                showError("Please enter valid liters amount")
                return false
            }
            odometer == null -> {
                showError("Please enter valid odometer reading")
                return false
            }
            activeVehicle != null && odometer != null && odometer < activeVehicle.mileage -> {
                showError("Odometer reading cannot be less than current mileage (${activeVehicle.mileage} km)")
                return false
            }
        }
        return true
    }

    private fun getFuelId(fuelType: String): Int {
        return when (fuelType) {
            "Petrol 93" -> 1
            "Petrol 95" -> 2
            "Diesel 50ppm" -> 3
            "Diesel 500ppm" -> 4
            else -> 1 // Default to Unleaded 93
        }
    }

    override fun navigateToReport() {
        TODO("Implement Navigation")
    }

    override fun navigateToProfile() {
        (activity as? MainActivity)?.navigateToSettings()
    }

    override fun navigateToAllHistory() {
        (activity as? MainActivity)?.navigateToRefuelHistory()
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        TODO("Not yet implemented")
    }

    override fun hideLoading() {
        TODO("Not yet implemented")
    }

    override fun updateExpenseAmount(amount: String) {
        tvExpenseAmount.text = amount
    }

}