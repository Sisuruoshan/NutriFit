package com.s23010253.nutrifit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class FitMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private EditText searchBox;


    // Filter buttons
    private Button btnAll, btnSupplementStore, btnGyms, btnFitnessCenter, btnSupermarket;
    private Button searchButton;

    // Bottom navigation
    ImageView navHome, navLocations, navProfile, navScan, navSetting;

    // Current selected filter
    private String currentFilter = "All";

    // Store the last searched location
    private LatLng searchedLatLng = null;
    // Define the search radius in meters (e.g., 5km)
    private static final float SEARCH_RADIUS_METERS = 5000f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitmap);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeViews() {
        // Filter buttons
        btnAll = findViewById(R.id.btn_all);
        btnSupplementStore = findViewById(R.id.btn_supplement_store);
        btnGyms = findViewById(R.id.btn_gyms);
        btnFitnessCenter = findViewById(R.id.btn_fitness_center);
        btnSupermarket = findViewById(R.id.btn_supermarket);
        searchBox = findViewById(R.id.search_box);


        // Search button
        searchButton = findViewById(R.id.search_button);

        // Bottom navigation
        navHome = findViewById(R.id.navHome);
        navLocations = findViewById(R.id.navLocation);
        navProfile = findViewById(R.id.navProfile);
        navScan = findViewById(R.id.navScan);
        navSetting = findViewById(R.id.navSetting);
    }

    private void setupClickListeners() {
        // Filter button listeners
        btnAll.setOnClickListener(v -> selectFilter("All", btnAll));
        btnSupplementStore.setOnClickListener(v -> selectFilter("Supplement Store", btnSupplementStore));
        btnGyms.setOnClickListener(v -> selectFilter("Gyms", btnGyms));
        btnFitnessCenter.setOnClickListener(v -> selectFilter("Fitness Center", btnFitnessCenter));
        btnSupermarket.setOnClickListener(v -> selectFilter("Supermarket", btnSupermarket));

        // Search button listener
        searchButton.setOnClickListener(v -> performSearch());

        // Bottom navigation listeners
        navHome.setOnClickListener(v -> navigateToHome());
        navLocations.setOnClickListener(v -> navigateToLocations());
        navProfile.setOnClickListener(v -> navigateToProfile());
        navScan.setOnClickListener(v -> navigateToScan());
        navSetting.setOnClickListener(v -> navigateToSetting());
    }

    private void selectFilter(String filter, Button selectedButton) {
        // Reset all buttons to unselected state
        resetFilterButtons();

        // Set selected button style (keep color #6D8315)
        selectedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6D8315));
        selectedButton.setTextColor(android.graphics.Color.BLACK);

        currentFilter = filter;
        Toast.makeText(this, "Filter: " + filter, Toast.LENGTH_SHORT).show();
        // Show filtered markers near searched location if available
        addSampleMarkers();
    }

    private void resetFilterButtons() {
        Button[] buttons = {btnAll, btnSupplementStore, btnGyms, btnFitnessCenter, btnSupermarket};
        for (Button button : buttons) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6D8315));
            button.setTextColor(android.graphics.Color.WHITE);
        }
    }

    private void performSearch() {
        String query = searchBox.getText().toString().trim();

        if (!query.isEmpty()) {
            geocodeAddress(query);
        } else {
            Toast.makeText(this, "Searching for " + currentFilter + " fitness centers...", Toast.LENGTH_SHORT).show();
            addSampleMarkers();
        }
    }


    // Sample data for all places by type and city
    private static class Place {
        LatLng location;
        String title;
        String type;
        String city;
        Place(LatLng location, String title, String type, String city) {
            this.location = location;
            this.title = title;
            this.type = type;
            this.city = city;
        }
    }

    private Place[] getSamplePlaces() {
        return new Place[] {
            // Colombo
            new Place(new LatLng(6.9271, 79.8612), "Colombo Supermarket", "Supermarket", "Colombo"),
            new Place(new LatLng(6.9275, 79.8620), "Colombo Gym", "Gyms", "Colombo"),
            new Place(new LatLng(6.9280, 79.8600), "Colombo Fitness Center", "Fitness Center", "Colombo"),
            new Place(new LatLng(6.9290, 79.8630), "Colombo Supplement Store", "Supplement Store", "Colombo"),
            // Matara
            new Place(new LatLng(5.9485, 80.5353), "Matara Supermarket", "Supermarket", "Matara"),
            new Place(new LatLng(5.9490, 80.5360), "Matara Gym", "Gyms", "Matara"),
            new Place(new LatLng(5.9500, 80.5370), "Matara Fitness Center", "Fitness Center", "Matara"),
            new Place(new LatLng(5.9510, 80.5380), "Matara Supplement Store", "Supplement Store", "Matara"),
            // Galle
            new Place(new LatLng(6.0329, 80.2170), "Galle Supermarket", "Supermarket", "Galle"),
            new Place(new LatLng(6.0335, 80.2180), "Galle Gym", "Gyms", "Galle"),
            new Place(new LatLng(6.0340, 80.2190), "Galle Fitness Center", "Fitness Center", "Galle"),
            new Place(new LatLng(6.0350, 80.2200), "Galle Supplement Store", "Supplement Store", "Galle"),
            // Default (San Francisco)
            new Place(new LatLng(37.7749, -122.4194), "Downtown Fitness Center", "Fitness Center", "San Francisco"),
            new Place(new LatLng(37.7849, -122.4094), "Bay Area Gym", "Gyms", "San Francisco"),
            new Place(new LatLng(37.7649, -122.4294), "Golden Gate Supermarket", "Supermarket", "San Francisco"),
            new Place(new LatLng(37.7549, -122.4394), "SF Supplement Store", "Supplement Store", "San Francisco")
        };
    }

    // Helper to extract city from address string (very basic, for demo)
    private String extractCityFromAddress(String address) {
        String lower = address.toLowerCase();
        if (lower.contains("matara")) return "Matara";
        if (lower.contains("colombo")) return "Colombo";
        if (lower.contains("galle")) return "Galle";
        if (lower.contains("san francisco")) return "San Francisco";
        return null;
    }

    private void addSampleMarkers() {
        if (mMap == null) return;
        mMap.clear();
        String city = null;
        if (searchedLatLng != null && lastSearchQuery != null) {
            city = extractCityFromAddress(lastSearchQuery);
        }
        for (Place place : getSamplePlaces()) {
            boolean typeMatch = currentFilter.equals("All") || place.type.equals(currentFilter);
            boolean cityMatch = (city == null) || place.city.equals(city);
            if (typeMatch && cityMatch) {
                mMap.addMarker(new MarkerOptions().position(place.location).title(place.title));
            }
        }
    }

    // Store last search query for city extraction
    private String lastSearchQuery = null;

    private void geocodeAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Address> results = geocoder.getFromLocationName(address, 1);
                runOnUiThread(() -> {
                    if (results == null || results.isEmpty()) {
                        Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Address loc = results.get(0);
                    LatLng p = new LatLng(loc.getLatitude(), loc.getLongitude());
                    searchedLatLng = p;
                    lastSearchQuery = address;
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(p).title(address));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p, 15f));
                    addSampleMarkers();
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error finding address", Toast.LENGTH_SHORT).show());
            }
        });
    }


    // Bottom navigation methods
    private void navigateToHome() {
        Toast.makeText(this, "Navigate to Home", Toast.LENGTH_SHORT).show();
    }

    private void navigateToLocations() {
        Toast.makeText(this, "Already on Locations", Toast.LENGTH_SHORT).show();
    }

    private void navigateToProfile() {
        Toast.makeText(this, "Navigate to Profile", Toast.LENGTH_SHORT).show();
    }

    private void navigateToScan() {
        Toast.makeText(this, "Navigate to Scan", Toast.LENGTH_SHORT).show();
    }

    private void navigateToSetting() {
        Toast.makeText(this, "Navigate to Settings", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set map style and settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Set initial location to San Francisco
        LatLng sanFrancisco = new LatLng(37.7749, -122.4194);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sanFrancisco, 12));

        // Check for location permission and enable location if granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Add initial markers
        addSampleMarkers();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Get current location and move camera
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
