package com.example.incidentaid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Incident_Cmd_DashBoard extends AppCompatActivity implements OnMapReadyCallback, LocationListener, OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Timer myTimer, myTimer1, myTimer2;
    private Button note, street, dashboard, notification;
    private String llong, llat, adde, note_detail, incident_id, notification_id, all_clear_button, evacuate_button, mayday_button, par_button, rescue_button, utility_button;

    private TextView header;
    private Context mContext;
    private Method method;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Incident");
    private DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference("Alert");
    private DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference("Notification");
    private GoogleMap mMap;
    private ImageButton all_clear_b, evacuate_b, mayday_b, par_b, rescue_b, utility_b;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    Marker mCurrLocationMarker;
    LatLng p1 = null; //new LatLng(37.350870, -121.933775);
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private FusedLocationProviderClient fusedLocationClient;
    public int transfer_control;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_incident_cmd_dashboard);

        Login.username = Login.pref.getString("username", null);
        Captain_DashBoard.fire_station_long = Captain_DashBoard.pref.getString("fire_station_long", null);
        Captain_DashBoard.fire_station_lat = Captain_DashBoard.pref.getString("fire_station_lan", null);

//        Captain_Create_Incident.inc_lat = Captain_Create_Incident.pref.getString("inc_lat",null);
//
//        Log.e("office2",Captain_Create_Incident.inc_lat+"");


        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }


        mContext = Incident_Cmd_DashBoard.this;
        method = new Method(mContext);
        note = (Button) findViewById(R.id.note);
        street = (Button) findViewById(R.id.street);
        dashboard = (Button) findViewById(R.id.dashboard);
        notification = (Button) findViewById(R.id.notification);
        header = (TextView) findViewById(R.id.header);

        all_clear_b = (ImageButton) findViewById(R.id.allclearbut);
        evacuate_b = (ImageButton) findViewById(R.id.evacuatebut);
        mayday_b = (ImageButton) findViewById(R.id.maydaybut);
        par_b = (ImageButton) findViewById(R.id.parbut);
        rescue_b = (ImageButton) findViewById(R.id.rescuebut);
        utility_b = (ImageButton) findViewById(R.id.utilitiesbut);


        header.setText("CAPTAIN/INCIDENT COMMANDER: " + Login.username.toUpperCase());


        Intent intent = getIntent();
        llong = intent.getStringExtra("long");
        llat = intent.getStringExtra("lat");
        adde = intent.getStringExtra("address");
        incident_id = intent.getStringExtra("incident_id");
        notification_id = intent.getStringExtra("notification");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        p1 = new LatLng(Double.parseDouble(llat), Double.parseDouble(llong));

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                check_control();
                setButton();
            }
        }, 0, 2000);


        myTimer1 = new Timer();
        myTimer1.schedule(new TimerTask() {
            @Override
            public void run() {
                last_know_location();
            }
        }, 0, 15000);






        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_note, null);
                builder.setCancelable(false);
                builder.setView(dialogView);
                final EditText note = (EditText) dialogView.findViewById(R.id.note);
                final Button note_save = (Button) dialogView.findViewById(R.id.note_save);

                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.child("captain").getValue(String.class).equals(Login.snapshot_parent) && snapshot.getKey().equals(incident_id)) {
                                    note.setText(snapshot.child("note_reference").getValue(String.class));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                note_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        note_detail = note.getText().toString();
                        if (TextUtils.isEmpty(note_detail)) {
                            Toast.makeText(mContext, "Fields Empty", Toast.LENGTH_SHORT).show();
                        } else {
                            method.save_note(Login.snapshot_parent, note_detail);
                            Toast.makeText(getApplicationContext(), "NOTE SAVE", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            startActivity(getIntent());
                        }
                    }
                });

            }
        });

        street.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Incident_Cmd_DashBoard.this, StreetView.class)
                        .putExtra("lat", llat)
                        .putExtra("long", llong)
                        .putExtra("adde", adde));
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Incident_Cmd_DashBoard.this, Notification.class)
                        .putExtra("incident_id", incident_id)
                        .putExtra("notification_id", notification_id)


                );
            }
        });

        dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Incident_Cmd_DashBoard.this, Incident_Cmd_Noti_DashBoard.class)
                        .putExtra("incident_id", incident_id)
                        .putExtra("notification_id", notification_id)
                        .putExtra("transfer_control", transfer_control + "")
                );
            }
        });
    }

    private void check_control() {
        FirebaseDatabase.getInstance().getReference("Incident").child(incident_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("status").getValue(String.class).equals("open")) {
                    transfer_control = dataSnapshot.child("transfer_control").getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void last_know_location() {

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object

                            HashMap map = new HashMap<>();
                            map.put("last_latitude", location.getLatitude() + "");
                            map.put("last_longitude", location.getLongitude() + "");
                            FirebaseDatabase.getInstance().getReference("User").child(Login.snapshot_parent).updateChildren(map);

                            myTimer2 = new Timer();
                            myTimer2.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mark_location_personnel();
                                }
                            }, 0, 20000);

                        }
                    }
                });
    }

    private void mark_location_personnel() {


        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() != 0) {
                    mMap.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = snapshot.child("name").getValue(String.class);
                        double lat = Double.parseDouble(snapshot.child("last_latitude").getValue(String.class));
                        double lon = Double.parseDouble(snapshot.child("last_longitude").getValue(String.class));
                        String rol = snapshot.child("role").getValue(String.class);
                        LatLng scu = new LatLng(lat, lon);

                        String mayday_lat = snapshot.child("mayday_latitude").getValue(String.class);
                        String mayday_lon = snapshot.child("mayday_longitude").getValue(String.class);


                        if (rol.equals("captain")) {
                            mMap.addMarker(new MarkerOptions().position(scu).title("IC " + name.toUpperCase()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        } else {
                            mMap.addMarker(new MarkerOptions().position(scu).title("FR " + name.toUpperCase()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        }

                        if (mayday_lat != null && mayday_lon != null) {
                            LatLng mayday_latlng = new LatLng(Double.parseDouble(mayday_lat), Double.parseDouble(mayday_lon));
                            mMap.addMarker(new MarkerOptions().position(mayday_latlng).title("MAYDAY MAYDAY " + name.toUpperCase()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

                        }


                    }
                    LatLng origin = new LatLng(Double.parseDouble(Captain_DashBoard.fire_station_lat), Double.parseDouble(Captain_DashBoard.fire_station_long));
                    mMap.addMarker(new MarkerOptions().position(origin).title("Fire Station").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    LatLng end = new LatLng(Double.parseDouble(llat), Double.parseDouble(llong));
                    mMap.addMarker(new MarkerOptions().position(end).title("Incident").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    loadmappath();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setButton() {

        myRef1.child(notification_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Log.e("alert", snapshot.getKey());
                        if (snapshot.getKey().equals("all_clear")) {
                            if (snapshot.child("status").getValue().toString().equals("true")) {
                                all_clear_button = "true";
                                all_clear_b.setBackgroundResource(R.drawable.all_clear_on);
                            } else {
                                all_clear_button = "false";
                                all_clear_b.setBackgroundResource(R.drawable.all_clear_off);
                            }
                        }
                        if (snapshot.getKey().equals("evacuate")) {
                            if (snapshot.child("status").getValue().toString().equals("true")) {
                                evacuate_button = "true";
                                evacuate_b.setBackgroundResource(R.drawable.eva_on);
                            } else {
                                evacuate_button = "false";
                                evacuate_b.setBackgroundResource(R.drawable.eva_off);
                            }
                        }
                        if (snapshot.getKey().equals("mayday")) {
                            if (snapshot.child("status").getValue().toString().equals("true")) {
                                mayday_button = "true";
                                mayday_b.setBackgroundResource(R.drawable.mayday_on);
                            } else {
                                mayday_button = "false";
                                mayday_b.setBackgroundResource(R.drawable.mayday_off);
                            }
                        }
                        if (snapshot.getKey().equals("par")) {
                            if (snapshot.child("status").getValue().toString().equals("true")) {
                                par_button = "true";
                                par_b.setBackgroundResource(R.drawable.par_on);
                            } else {
                                par_button = "false";
                                par_b.setBackgroundResource(R.drawable.par_off);
                            }
                        }
                        if (snapshot.getKey().equals("rescue")) {
                            if (snapshot.child("status").getValue().toString().equals("true")) {
                                rescue_button = "true";
                                rescue_b.setBackgroundResource(R.drawable.rescue_on);
                            } else {
                                rescue_button = "false";
                                rescue_b.setBackgroundResource(R.drawable.rescue_off);
                            }
                        }
                        if (snapshot.getKey().equals("utility")) {
                            if (snapshot.child("status").getValue().toString().equals("true")) {
                                utility_button = "true";
                                utility_b.setBackgroundResource(R.drawable.utility_on);
                            } else {
                                utility_button = "false";
                                utility_b.setBackgroundResource(R.drawable.utility_off);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        par_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (transfer_control == 0) {


                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this);
                    }

                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send PAR Command ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id)) {
                                                        if (snapshot.child("par").child("send").getValue(Integer.class) == snapshot.child("par").child("received").getValue(Integer.class)) {
                                                            Log.e("SendReceived", "yes");
                                                            continue_fun();
                                                        } else {
                                                            Log.e("SendReceived", "no");
                                                            method.showalert("Alert", "Not Received ACK From All So Can't Send..");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        private void continue_fun() {

                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id)) {

                                                                if (par_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("par").updateChildren(map);
                                                                    par_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("par").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("par").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "PAR Is Called On", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "PAR On Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("par").updateChildren(map);
                                                                    par_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("par").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("par").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "PAR Is Called OFF", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "PAR off Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    method.showalert("Warning Cant Send Cmd", "You are not authorized");
                }
            }
        });


        all_clear_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transfer_control == 0) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id)) {
                                                        if (snapshot.child("all_clear").child("send").getValue(Integer.class) == snapshot.child("all_clear").child("received").getValue(Integer.class)) {
                                                            Log.e("SendReceived", "yes");
                                                            continue_fun();
                                                        } else {
                                                            Log.e("SendReceived", "no");
                                                            method.showalert("Alert", "Not Received ACK From All So Can't Send..");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        private void continue_fun() {


                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id)) {

                                                                if (all_clear_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("all_clear").updateChildren(map);
                                                                    all_clear_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("all_clear").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("all_clear").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "All Clear Is Called On", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "All Clear On Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("all_clear").updateChildren(map);
                                                                    all_clear_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("all_clear").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("all_clear").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "ALL Clear Is Called OFF", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "All Clear off Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });


                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    method.showalert("Warning Cant Send Cmd", "You are not authorized");
                }
            }
        });


        evacuate_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transfer_control == 0) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id)) {
                                                        if (snapshot.child("evacuate").child("send").getValue(Integer.class) == snapshot.child("evacuate").child("received").getValue(Integer.class)) {
                                                            Log.e("SendReceived", "yes");
                                                            continue_fun();
                                                        } else {
                                                            Log.e("SendReceived", "no");
                                                            method.showalert("Alert", "Not Received ACK From All So Can't Send..");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        private void continue_fun() {


                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id)) {
                                                                if (evacuate_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("evacuate").updateChildren(map);
                                                                    evacuate_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("evacuate").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("evacuate").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Evacuate Is Called On", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Evacuate On Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("evacuate").updateChildren(map);
                                                                    evacuate_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("evacuate").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("evacuate").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Evacuate Is Called OFF", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Evacuate Off Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                } else {
                    method.showalert("Warning Cant Send Cmd", "You are not authorized");
                }
            }
        });


        utility_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transfer_control == 0) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id)) {
                                                        if (snapshot.child("utility").child("send").getValue(Integer.class) == snapshot.child("utility").child("received").getValue(Integer.class)) {
                                                            Log.e("SendReceived", "yes");
                                                            continue_fun();
                                                        } else {
                                                            Log.e("SendReceived", "no");
                                                            method.showalert("Alert", "Not Received ACK From All So Can't Send..");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        private void continue_fun() {


                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id)) {
                                                                if (utility_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("utility").updateChildren(map);
                                                                    utility_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("utility").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("utility").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Utility Is Called On", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Utility On Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("utility").updateChildren(map);
                                                                    utility_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("utility").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("utility").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Utility Is Called OFF", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Utility Off Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }


                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                } else {
                    method.showalert("Warning Cant Send Cmd", "You are not authorized");
                }
            }
        });


        rescue_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transfer_control == 0) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id)) {
                                                        if (snapshot.child("rescue").child("send").getValue(Integer.class) == snapshot.child("rescue").child("received").getValue(Integer.class)) {
                                                            Log.e("SendReceived", "yes");
                                                            continue_fun();
                                                        } else {
                                                            Log.e("SendReceived", "no");
                                                            method.showalert("Alert", "Not Received ACK From All So Can't Send..");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        private void continue_fun() {


                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id)) {
                                                                if (rescue_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("rescue").updateChildren(map);
                                                                    rescue_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("rescue").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("rescue").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Rescue Is Called On", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Rescue On Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("rescue").updateChildren(map);
                                                                    rescue_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("rescue").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("rescue").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Rescue Is Called OFF", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Rescue Off Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                } else {
                    method.showalert("Warning Cant Send Cmd", "You are not authorized");
                }
            }
        });


        mayday_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (transfer_control == 0) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Incident_Cmd_DashBoard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id)) {
                                                        if (snapshot.child("mayday").child("send").getValue(Integer.class) == snapshot.child("mayday").child("received").getValue(Integer.class)) {
                                                            Log.e("SendReceived", "yes");
                                                            continue_fun();
                                                        } else {
                                                            Log.e("SendReceived", "no");
                                                            method.showalert("Alert", "Not Received ACK From All So Can't Send..");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        private void continue_fun() {


                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id)) {
                                                                if (mayday_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("mayday").updateChildren(map);
                                                                    mayday_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("mayday").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("mayday").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Mayday Is Called On", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());

                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Mayday On Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("mayday").updateChildren(map);
                                                                    mayday_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("mayday").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id).child("mayday").updateChildren(map);


                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        if (snapshot.getKey().equals(str)) {
                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                            method.sendFCMPush("Alert", "Mayday Is Called OFF", snapshot.child("token").getValue(String.class));
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                                    String DateandTime = sdf.format(new Date());
                                                                    HashMap hm = new HashMap();
                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "Mayday Off Alert Sent To All");
                                                                    myRef2.child(notification_id).updateChildren(hm);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }


                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                } else {
                    method.showalert("Warning Cant Send Cmd", "You are not authorized");
                }
            }
        });


    }


    private void loadmappath() {
        String url = getUrl();
        Log.d("onMapClick", url.toString());
        Incident_Cmd_DashBoard.FetchUrl FetchUrl = new Incident_Cmd_DashBoard.FetchUrl();
        FetchUrl.execute(url);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    private String getUrl() {

        String str_origin = "origin=" + Captain_DashBoard.fire_station_lat + "," + Captain_DashBoard.fire_station_long;
        String str_dest = "destination=" + llat + "," + llong;
        String sensor = "sensor=true";
        String mode = "mode=driving";
        String alternatives = "alternatives=false";
        String key = "key=" + BuildConfig.Google_Map_Key;
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + alternatives + "&" + mode + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        // Log.e("urlurl", url);
        return url;
    }


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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

//        LatLng scu = new LatLng(Double.parseDouble(llat), Double.parseDouble(llong));
//        mMap.addMarker(new MarkerOptions().position(scu).title(adde).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        show_map(Double.parseDouble(llat), Double.parseDouble(llong));
        loadmappath();

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
                                ActivityCompat.requestPermissions(Incident_Cmd_DashBoard.this,
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
        mMap.addMarker(new MarkerOptions().position(scu).title("Incident Address: " + adde.toUpperCase()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


        LatLng scu1 = new LatLng(Double.parseDouble(Captain_DashBoard.fire_station_lat), Double.parseDouble(Captain_DashBoard.fire_station_long));
        mMap.addMarker(new MarkerOptions().position(scu1).title(Login.firestation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu1));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
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
        loadmappath();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Incident_Cmd_DashBoard.this, Captain_DashBoard.class));
        super.onBackPressed();
    }
}
