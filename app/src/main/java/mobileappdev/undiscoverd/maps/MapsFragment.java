package mobileappdev.undiscoverd.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mobileappdev.undiscoverd.MainActivity;
import mobileappdev.undiscoverd.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * Class for displaying Maps and retrieving locations from user
 */
public class MapsFragment extends Fragment implements OnClickListener, OnMapClickListener {

    private MapView myMapView;
    private GoogleMap googleMap;

    private double currentLatitude;
    private double currentLongitude;

    private Marker newLocationMarker;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDatabase;
    private String mUserID;

    private List<MapsLocation> mPreviousTrips;

    public static MapsFragment newFragment(){
        return new MapsFragment();
    }

    // Location permission is requested on MainActivity
    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        System.out.println("MapsFragment = onCreateView");

        firebaseAuth = FirebaseAuth.getInstance();
        mUserID = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        // as we set up the Maps, get all of the user's previous trips to add to Map later
        mPreviousTrips = new ArrayList<>();
        getUserTrips();

        myMapView = rootView.findViewById(R.id.mapView);
        myMapView.onCreate(savedInstanceState);
        myMapView.onResume();

        newLocationMarker = null;

        // Find and add buttons to this Fragment's onClick method
        Button addCurrentLocation = rootView.findViewById(R.id.add_current_location);
        addCurrentLocation.setOnClickListener(this);
        Button addOtherLocation = rootView.findViewById(R.id.add_other_location);
        addOtherLocation.setOnClickListener(this);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        });

        // TODO: add error handling for locations not found
        // Get last known location (current location)
        Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLoc == null) {
            lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        final double latitude = lastKnownLoc.getLatitude();
        final double longitude = lastKnownLoc.getLongitude();
        currentLatitude = latitude;
        currentLongitude = longitude;

        myMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // Move map down because of top action bar
                googleMap.setPadding(0, 100, 0, 0);

                // Pull the user's trips from the Firebase database and add pins to the map
                // Places pins everywhere the current user has visited already
                addTripsToMap();

                // Start on current location in Google Maps
                LatLng currentLocation = new LatLng(latitude, longitude);
                String coordinatesString = String.format(Locale.getDefault(), "(%.2f, %.2f)", latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").snippet(coordinatesString).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                // Move camera to current location
                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                // Remove Navigation and GPS Pointer buttons
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                googleMap.setOnMapClickListener(MapsFragment.this);
            }
        });
        return rootView;
    }

    /**
     * Code for each button on fragment.
     * Because this class is a Fragment and not Activity, this is easy way to do this.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Add current location to database
            case R.id.add_current_location:
                MapsLocation currentLocation = getLocation(currentLatitude, currentLongitude);
                storeMapsLocation(currentLocation);
                break;
            // Add other (user-selected) location to database
            case R.id.add_other_location:
                if (newLocationMarker != null) {
                    MapsLocation otherLocation = getLocation(
                            newLocationMarker.getPosition().latitude,
                            newLocationMarker.getPosition().longitude
                    );
                    storeMapsLocation(otherLocation);
                } else {
                    Toast.makeText(getContext(), "No other location marked", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Create MapsLocation object from latitude and longitude.
     */
    private MapsLocation getLocation(double latitude, double longitude) {
        System.out.println("MapFragment - getLocation");
        Geocoder myGeocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> geocoderAddress = myGeocoder.getFromLocation(latitude, longitude, 1);
            if (geocoderAddress.get(0).getLocality() == null || geocoderAddress.get(0).getAdminArea() == null || geocoderAddress.get(0).getCountryName() == null) {
                return null;
            }
            return new MapsLocation(
                    latitude,
                    longitude,
                    geocoderAddress.get(0).getLocality(),
                    geocoderAddress.get(0).getAdminArea(),
                    geocoderAddress.get(0).getCountryName()
            );
        } catch (Exception e) {
            Toast.makeText(getContext(), "Address cannot be detected from location", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    /**
     * Adds marker on map where user clicks.
     * Note: user can only add 1 marker
     */
    @Override
    public void onMapClick(LatLng latLng) {
        // if there was already a marker the user added, remove it
        if (newLocationMarker != null) {
            newLocationMarker.remove();
        }
        String coordinatesString = String.format(Locale.getDefault(), "(%.2f, %.2f)", latLng.latitude, latLng.longitude);
        newLocationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title(coordinatesString));
    }

    /**
     * Store the user's visited locations in the Firebase database after they add it through the
     * Maps fragment while using the app.
     */
    private void storeMapsLocation(MapsLocation location) {
        DatabaseReference tripsDB = firebaseDatabase;
        String key = tripsDB.child("Trips").child(mUserID).push().getKey();
        tripsDB.child("Trips").child(mUserID).child(key).setValue(location);
    }

    /**
     * Upon booting up the map fragment, adds the current user's previous trips as pins to the
     * Google Maps API.
     */
    private void addTripsToMap(){
        System.out.println("Maps Fragment - addTripsToMap");

        for (MapsLocation trip : mPreviousTrips){
            System.out.println("Adding trip to map: " + trip.getCity());

            LatLng latLng = new LatLng(trip.getLatitude(), trip.getLongitude());

            String coords = String.format(Locale.getDefault(), "(%.2f, %.2f",
                    latLng.latitude, latLng.longitude);
            googleMap.addMarker(new MarkerOptions().position(latLng).title(coords).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }

    private void getUserTrips() {
        System.out.println("MainActivity - getUserTrips");
        // so need to get the current user's trips

        System.out.println("Current user is: " + mUserID);

        Query query = FirebaseDatabase.getInstance().getReference().child("Trips").child(mUserID);
        System.out.println("Query is: " + query.toString());

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Trips").child(mUserID);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // now loop through each of the returned Trips and save them to a list of Trips
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    double latitude = (double) ds.child("latitude").getValue();
                    double longitude = (double) ds.child("longitude").getValue();
                    String city = ds.child("city").getValue().toString();
                    String state = ds.child("state").getValue().toString();
                    String country = ds.child("country").getValue().toString();

                    mPreviousTrips.add(new MapsLocation(latitude, longitude, city, state, country));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        myMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        myMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        myMapView.onLowMemory();
    }

}
