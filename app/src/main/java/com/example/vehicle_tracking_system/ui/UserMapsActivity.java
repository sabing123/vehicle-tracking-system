package com.example.vehicle_tracking_system.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.vehicle_tracking_system.MainActivity;
import com.example.vehicle_tracking_system.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;


    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;

    private Button btn_locate_bus;
    private String passengerID;
    private LatLng PassengerPickUpLocation;
    private int radius = 1;
    private Boolean driverFound = false, requestType = false;
    private String driverFoundID;

    private FirebaseAuth mAuth;
    private FirebaseUser dCurrentUser;
    Marker DriverMarker, pickupMarker;
    GeoQuery geoQuery;

    private DatabaseReference PassengerDatabaseRef;
    private DatabaseReference DriverAvailableRef;
    private DatabaseReference DriverRef;
    private DatabaseReference DriverLocationRef;

    private ValueEventListener DriverLocationRefListener;


    private TextView txtname, txtphone, txtbusname, txtbusnumber;
    private CircleImageView profilepic, btn_user_logout;
    private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);


        btn_locate_bus = findViewById(R.id.btn_locate_bus);

        mAuth = FirebaseAuth.getInstance();
        dCurrentUser = mAuth.getCurrentUser();
        passengerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        PassengerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Passenger Request");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");


        txtname = findViewById(R.id.driver_name);
        txtphone = findViewById(R.id.driver_phone);
        txtbusname = findViewById(R.id.driver_bus_name);
        txtbusnumber = findViewById(R.id.driver_bus_number);
        profilepic = findViewById(R.id.driver_profile_image);
        relativeLayout = findViewById(R.id.rell);
        btn_user_logout = findViewById(R.id.btn_user_logout);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btn_user_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LogoutUser();
            }
        });

        btn_locate_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestType) {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListener);

                    if (driverFound != null) {
                        DriverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("CustomerRideID");

                        DriverRef.removeValue();

                        driverFound = null;
                    }

                    driverFound = false;
                    radius = 1;

                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(PassengerDatabaseRef);
                    geoFire.removeLocation(customerId);
                    if (pickupMarker != null) {
                        pickupMarker.remove();
                    }

                    if (DriverMarker != null) {
                        DriverMarker.remove();
                    }

                    btn_locate_bus.setText("Your Bus is Here");
                    relativeLayout.setVisibility(View.GONE);

                } else {

                    requestType = true;

                    GeoFire geoFire = new GeoFire(PassengerDatabaseRef);
                    geoFire.setLocation(passengerID, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));

                    PassengerPickUpLocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(PassengerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_user)));

                    btn_locate_bus.setText("Locating Your Bus...");
                    GetClosestDriverCab();
                }


            }
        });

    }

    private void GetClosestDriverCab() {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(PassengerPickUpLocation.latitude, PassengerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundID = key;

                    DriverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", passengerID);
                    DriverRef.updateChildren(driverMap);

                    GettingDriverLocation();
                    btn_locate_bus.setText("Looking For Drive Location");

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius = radius + 1;
                    GetClosestDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void GettingDriverLocation() {

        DriverLocationRefListener = DriverLocationRef.child(driverFoundID).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && requestType) {
                    List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 1;
                    btn_locate_bus.setText("Driver Found");

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedDriverInformation();

                    if (driverLocationMap.get(0) != null) {
                        LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());

                    }
                    if (driverLocationMap.get(1) != null) {
                        LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());

                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    if (DriverMarker != null) {
                        DriverMarker.remove();
                    }

                    Location Location1 = new Location("");
                    Location1.setLatitude(PassengerPickUpLocation.latitude);
                    Location1.setLongitude(PassengerPickUpLocation.longitude);

                    Location Location2 = new Location("");
                    Location2.setLatitude(DriverLatLng.latitude);
                    Location2.setLongitude(DriverLatLng.longitude);

                    float Distance = Location1.distanceTo(Location2);

                    if (Distance < 500) {
                        btn_locate_bus.setText("Driver's Arrived");
                        btn_locate_bus.setEnabled(false);
                    } else {
                        btn_locate_bus.setText("Driver Found : " + String.valueOf(Distance));
                    }

                    DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng)
                            .title("Your Bus is Here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus)));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getAssignedDriverInformation() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverFoundID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();
                    String busname = snapshot.child("BusName").getValue().toString();
                    String busnum = snapshot.child("BusNumber").getValue().toString();
                    txtname.setText(name);
                    txtphone.setText(phone);
                    txtbusname.setText(busname);
                    txtbusnumber.setText(busnum);

                    if (snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilepic);
                    }
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                buildGoogleAPIClient();
                mMap.setMyLocationEnabled(true);
            }

            @Override
            public void onConnected(@Nullable Bundle bundle) {
                locationRequest = new LocationRequest();
                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(1000);
                locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


            }

            @Override
            public void onConnectionSuspended(int i) {

            }


            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }

            @Override
            public void onLocationChanged(Location location) {
                lastlocation = location;

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                mMap.getUiSettings().setZoomControlsEnabled(true);

            }

            protected synchronized void buildGoogleAPIClient() {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                googleApiClient.connect();

            }

            @Override
            public void onPointerCaptureChanged(boolean hasCapture) {

            }


            @Override
            protected void onStop() {
                super.onStop();
            }

            private void LogoutUser() {
                Intent intent = new Intent(UserMapsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }

        }