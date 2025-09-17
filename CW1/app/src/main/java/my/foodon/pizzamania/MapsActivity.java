package my.foodon.pizzamania;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_SELECTED_LOCATION = "selected_location";
    public static final String EXTRA_PREVIOUS_LOCATION = "previous_location";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private LatLng lastSelectedLocation = null;
    private Marker selectedMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ✅ Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black_background)); // #060606
        }
        // Check if there's a previously selected location passed from main activity
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_PREVIOUS_LOCATION)) {
            double[] previousLocation = intent.getDoubleArrayExtra(EXTRA_PREVIOUS_LOCATION);
            if (previousLocation != null && previousLocation.length == 2) {
                lastSelectedLocation = new LatLng(previousLocation[0], previousLocation[1]);
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnConfirm = findViewById(R.id.btnConfirmLocation);
        btnConfirm.setOnClickListener(v -> {
            if (lastSelectedLocation != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_LOCATION, new double[]{
                        lastSelectedLocation.latitude,
                        lastSelectedLocation.longitude
                });
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();

        // If there's a previously selected location, show it on the map
        if (lastSelectedLocation != null) {
            selectedMarker = mMap.addMarker(new MarkerOptions()
                    .position(lastSelectedLocation)
                    .title("Previously Selected Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastSelectedLocation, 14));
        }

        mMap.setOnMapClickListener(latLng -> {
            lastSelectedLocation = latLng;

            // Remove previous marker
            if (selectedMarker != null) selectedMarker.remove();

            // Add new marker
            selectedMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location"));
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            // Only get current location if there's no previously selected location
            if (lastSelectedLocation == null) {
                // Get fresh current location
                fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY, null
                ).addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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