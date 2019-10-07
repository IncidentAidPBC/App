package com.example.incidentaid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Captain_Create_Incident extends AppCompatActivity implements OnMapReadyCallback, OnStreetViewPanoramaReadyCallback,
        LocationListener, OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    public static String mypref = "mypref", token;
    public static SharedPreferences pref, pref1;
    public SharedPreferences.Editor edit;
    public static String incident_id, notification_id, inc_lat, inc_lon, inc_addr;
    private Context mContext;
    private Method method;
    private EditText address;
    private Button show, create_incident;
    private TextView heading;
    GoogleApiClient mGoogleApiClient;

    private GoogleMap mMap;
    private String temp, fs, Cap_address = null, aaddress, llong, llat, id_list, token_list;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    private StreetViewPanorama mStreetViewPanorama;
    LocationRequest mLocationRequest;
    LatLng p1 = null; //new LatLng(37.350870, -121.933775);
    LatLng p2 = null; // new LatLng(37.350870, -121.933775);
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myRef, myRef1, myRef2;
    Geocoder coder = new Geocoder(Captain_Create_Incident.this);
    ArrayList<String> product;
    ArrayList markerPoints = new ArrayList();

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_captain_create_incident);

        pref = getSharedPreferences(mypref, MODE_PRIVATE);
        edit = pref.edit();

        final StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Captain_DashBoard.fire_station_long = Captain_DashBoard.pref.getString("fire_station_long", null);
        Captain_DashBoard.fire_station_lat = Captain_DashBoard.pref.getString("fire_station_lan", null);


        mContext = Captain_Create_Incident.this;
        method = new Method(mContext);


        address = (EditText) findViewById(R.id.address);
        show = (Button) findViewById(R.id.getmap);
        create_incident = (Button) findViewById(R.id.incident_generation);
        heading = (TextView) findViewById(R.id.header);
        myRef = FirebaseDatabase.getInstance().getReference().child("Fire_Station");
        myRef = FirebaseDatabase.getInstance().getReference("Incident");
        myRef = FirebaseDatabase.getInstance().getReference("Notification");

        Intent intent = getIntent();
        temp = intent.getStringExtra("username");
        fs = intent.getStringExtra("FSaddress");
        llong = intent.getStringExtra("long");
        llat = intent.getStringExtra("lat");
        id_list = intent.getStringExtra("id_list");
        token_list = intent.getStringExtra("token_list");
        Log.e("id_list", id_list);

        Log.e("1234", llat + " " + llong);
        p1 = new LatLng(Double.parseDouble(llat), Double.parseDouble(llong));
        heading.setText("CAPTAIN: " + temp.toUpperCase());


        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mMap.clear();
                aaddress = address.getText().toString().trim();
                create_incident.setEnabled(true);
                show.setEnabled(false);

//                address.setFocusable(false);
                if (TextUtils.isEmpty(aaddress)) {
                    Toast.makeText(getApplicationContext(), "Address Field Empty", Toast.LENGTH_SHORT).show();
                    address.requestFocus();
                    return;
                }
                List<Address> addr;
                try {
                    addr = coder.getFromLocationName(address.getText().toString(), 5);
                    Address location = addr.get(0);
                    p2 = p1;
                    p1 = new LatLng(location.getLatitude(), location.getLongitude());
                    Toast.makeText(Captain_Create_Incident.this, p1.toString(), Toast.LENGTH_SHORT).show();
                    show_map(p1.latitude, p1.longitude);
                    onStreetViewPanoramaReady(mStreetViewPanorama);
                    loadmappath();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }

        });

        create_incident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aaddress = address.getText().toString().trim();
                if (TextUtils.isEmpty(aaddress)) {
                    Toast.makeText(getApplicationContext(), "Address Field Empty", Toast.LENGTH_SHORT).show();
                    address.requestFocus();
                    return;
                }

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Captain_Create_Incident.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Captain_Create_Incident.this);
                }
                builder.setTitle("Confirmation !!")
                        .setMessage("Do you want to create this incident ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                create_incident.setEnabled(false);
                                method.create_incident(aaddress, id_list, p1.latitude + "", p1.longitude + "", "note_reference");

                                token_list = token_list.replaceAll("\\[", "");
                                token_list = token_list.replaceAll("\\]", "");

                                String temp[] = token_list.split(", ");
                                for (String single_token : temp) {
                                    method.sendFCMPush("Alert", "An Incident Is Created", single_token);
                                }

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                String DateandTime = sdf.format(new Date());
                                incident_id = DateandTime;
                                inc_lat = p1.latitude + "";
                                inc_lon = p1.longitude + "";
                                inc_addr = aaddress;

                                edit.putString("incident_id", incident_id);
                                edit.putString("inc_lat", inc_lat);
                                edit.putString("inc_lon", inc_lon);
                                edit.putString("inc_addr", inc_addr);
                                edit.commit();

                                Log.e("office", Captain_Create_Incident.incident_id);

                                FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.getKey().equals(incident_id)) {

                                                    notification_id = snapshot.child("notification").getValue(String.class);
                                                    edit.putString("notification_id", notification_id);
                                                    edit.commit();

                                                    startActivity(new Intent(Captain_Create_Incident.this, Incident_Cmd_DashBoard.class)
                                                            .putExtra("notification", notification_id)
                                                            .putExtra("incident_id", incident_id)
                                                            .putExtra("address",aaddress)
                                                            .putExtra("lat",inc_lat)
                                                            .putExtra("long",inc_lon)
                                                    );
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadmappath() {
        String url = getUrl();
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    private String getUrl() {

        String str_origin = "origin=" + p2.latitude + "," + p2.longitude;
        String str_dest = "destination=" + p1.latitude + "," + p1.longitude;
        String sensor = "sensor=true";
        String mode = "mode=driving";
        String alternatives = "alternatives=false";
        String key = "key=" + BuildConfig.Google_Map_Key;
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + alternatives + "&" + mode + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        Log.e("urlurl", url);
        return url;
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        mStreetViewPanorama = streetViewPanorama;

        streetViewPanorama.setPosition(new LatLng(p1.latitude, p1.longitude));
        streetViewPanorama.setStreetNamesEnabled(true);
        streetViewPanorama.setPanningGesturesEnabled(true);
        streetViewPanorama.setZoomGesturesEnabled(true);
        streetViewPanorama.setUserNavigationEnabled(true);
        streetViewPanorama.animateTo(
                new StreetViewPanoramaCamera.Builder().
                        orientation(new StreetViewPanoramaOrientation(20, 20))
                        .zoom(streetViewPanorama.getPanoramaCamera().zoom)
                        .build(), 2000);

        streetViewPanorama.setOnStreetViewPanoramaChangeListener(panoramaChangeListener);
    }

    private StreetViewPanorama.OnStreetViewPanoramaChangeListener panoramaChangeListener = new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
        @Override
        public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
        }
    };

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(8);

                lineOptions.color(Color.BLUE);
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        LatLng scu = new LatLng(Double.parseDouble(llat), Double.parseDouble(llong));
        mMap.addMarker(new MarkerOptions().position(scu).title(fs).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void onConnectionSuspended(int i) {
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Captain_Create_Incident.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }


    void show_map(double lat, double lng) {

        LatLng scu = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(scu).title("Incident Place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        LatLng scu1 = new LatLng(p2.latitude, p2.longitude);
        mMap.addMarker(new MarkerOptions().position(scu1).title(fs).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu1));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(Captain_Create_Incident.this, Captain_DashBoard.class));
        super.onBackPressed();

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(latLng.toString());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
        }
//        loadmappath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}


