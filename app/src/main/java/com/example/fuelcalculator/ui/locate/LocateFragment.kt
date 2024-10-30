package com.example.fuelcalculator.ui.locate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.fuelcalculator.R
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.button.MaterialButton
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.material.textfield.TextInputEditText

@Suppress("DEPRECATION")
class LocateFragment : Fragment(), OnMapReadyCallback {

    private val TAG = "LocateFragment"

    private var googleMap: GoogleMap? = null
    private lateinit var btnStartTrip: MaterialButton
    private lateinit var btnVisitHistory: MaterialButton
    private lateinit var searchEditText: TextInputEditText
    private var lastKnownLocation: LatLng? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Permission granted
                lastKnownLocation?.let { searchNearbyFuelStations(it) }
                enableMyLocation()
            }
            else -> {
                Toast.makeText(
                    context,
                    "Location permission is required for full functionality",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initializeViews(view)
        setupSearchBar()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        btnStartTrip = view.findViewById(R.id.btnStartTrip)
        btnVisitHistory = view.findViewById(R.id.btnVisitHistory)
        searchEditText = view.findViewById(R.id.etSearch)
    }

    private fun setupSearchBar() {
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as? AutocompleteSupportFragment

        autocompleteFragment?.apply {
            view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_bar)?.apply {
                background = null
            }

            setHint("Search Fuel Station or Area")
            setPlaceFields(listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.TYPES
            ))
        }

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let { latLng ->
                    lastKnownLocation = latLng
                    moveCameraToLocation(latLng)
                    searchNearbyFuelStations(latLng)
                }
            }

            override fun onError(status: Status) {
                when (status.statusCode) {
                    CommonStatusCodes.NETWORK_ERROR -> {
                        // Show error for network issues
                        Toast.makeText(
                            context,
                            "Please check your internet connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // Show error for timeout
                        Toast.makeText(
                            context,
                            "Search timed out, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CommonStatusCodes.CANCELED -> {
                        // User canceled, no need to show error
                        Log.d(TAG, "Search canceled by user")
                    }
                    else -> {
                        // Log other errors but don't show to user
                        Log.e(TAG, "Places search error: ${status.statusMessage}")
                    }
                }
            }
        })
    }

    private fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun searchNearbyFuelStations(location: LatLng) {
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
            return
        }

        // Create location bias
        val bias = com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
            LatLng(
                location.latitude - 0.05,  // roughly 5km south
                location.longitude - 0.05   // roughly 5km west
            ),
            LatLng(
                location.latitude + 0.05,  // roughly 5km north
                location.longitude + 0.05   // roughly 5km east
            )
        )

        // Create the places request
        val request = com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
            .builder()
            .setLocationBias(bias)
            .setTypesFilter(listOf("gas_station"))
            .setQuery("")  // empty query to get all results
            .build()

        // Search for nearby fuel stations
        Places.createClient(requireContext()).findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                response.autocompletePredictions.forEach { prediction ->
                    // Get place details for each prediction
                    val placeRequest = com.google.android.libraries.places.api.net.FetchPlaceRequest.newInstance(
                        prediction.placeId,
                        listOf(Place.Field.NAME, Place.Field.LAT_LNG)
                    )

                    Places.createClient(requireContext()).fetchPlace(placeRequest)
                        .addOnSuccessListener { placeResponse ->
                            val place = placeResponse.place
                            place.latLng?.let { latLng ->
                                addFuelStationMarker(latLng, place.name ?: "Fuel Station")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                context,
                                "Error fetching place details: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Error finding fuel stations: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun moveCameraToLocation(location: LatLng) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
    }

    private fun addFuelStationMarker(location: LatLng, title: String) {
        googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title(title)
                .snippet("Fuel Station")
        )
    }

    private fun setupClickListeners() {
        btnStartTrip.setOnClickListener {
            // TODO: Implement start trip functionality
        }

        btnVisitHistory.setOnClickListener {
            // TODO: Implement visit history functionality
        }
    }

    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Error enabling location: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (checkLocationPermissions()) {
            enableMyLocation()
        } else {
            requestLocationPermissions()
        }

        // Set default location (Johannesburg)
        val defaultLocation = LatLng(-26.2041, 28.0473)
        moveCameraToLocation(defaultLocation)
    }
}