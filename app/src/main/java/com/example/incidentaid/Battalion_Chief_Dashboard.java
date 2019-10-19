package com.example.incidentaid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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
import java.util.Timer;
import java.util.TimerTask;

public class Battalion_Chief_Dashboard extends AppCompatActivity implements OnMapReadyCallback, LocationListener, OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private Timer myTimer, myTimer1, myTimer2;
    private Context myContext;
    private Method method;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myRef, myRef1, myRef2, myRef3, myRef4;
    public static String mypref = "mypref";
    public static SharedPreferences pref, pref1;
    public SharedPreferences.Editor edit;
    private TextView header;
    private ImageButton all_clear_b, evacuate_b, mayday_b, par_b, rescue_b, utility_b;
    private Button noti_dash, take_cmd;
    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    Marker mCurrLocationMarker;
    LatLng p1 = null; //new LatLng(37.350870, -121.933775);
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationClient;

    public static String incident_id_for_personnel, note_for_personnel, notification_id_for_personnel;
    public static String captain_id_for_personnel, inc_date_for_personnel, inc_time_for_personnel;
    public static String inc_lat_for_personnel, inc_long_for_personnel, personnels_id_for_personnel;
    public static String inc_status_for_personnel, inc_address_for_personnel, token;
    public String cur_lat, cur_lon;
    private String all_clear_button, evacuate_button, mayday_button, par_button, rescue_button, utility_button;
    public int transfer_control;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_battalion_chief);


        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        myContext = Battalion_Chief_Dashboard.this;
        method = new Method(myContext);
        pref = getSharedPreferences(mypref, MODE_PRIVATE);
        edit = pref.edit();

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("User");
        myRef1 = FirebaseDatabase.getInstance().getReference("Incident");
        myRef2 = FirebaseDatabase.getInstance().getReference("Fire_Station");
        myRef3 = FirebaseDatabase.getInstance().getReference("Alert");
        myRef4 = FirebaseDatabase.getInstance().getReference("Notification");


        Login.username = Login.pref.getString("username", null);
        Login.snapshot_parent = Login.pref.getString("user_id", null);
        Login.firestation = Login.pref.getString("firestation", null);
        Login.jobtitle = Login.pref.getString("jobtitle", null);
        Login.values_for_profile = Login.pref.getString("values_for_profile", null);
        Login.who_is_the_user = Login.pref.getString("user_role", null);
        Login.alert_token = Login.pref.getString("alert_token", null);

        Intent i = getIntent();
        incident_id_for_personnel = i.getStringExtra("incident_id_for_personnel");


        all_clear_b = (ImageButton) findViewById(R.id.allclearbut);
        evacuate_b = (ImageButton) findViewById(R.id.evacuatebut);
        mayday_b = (ImageButton) findViewById(R.id.maydaybut);
        par_b = (ImageButton) findViewById(R.id.parbut);
        rescue_b = (ImageButton) findViewById(R.id.rescuebut);
        utility_b = (ImageButton) findViewById(R.id.utilitiesbut);

        noti_dash = (Button) findViewById(R.id.noti_dashboard);
        take_cmd = (Button) findViewById(R.id.transfer);

        header = (TextView) findViewById(R.id.header);
        header.setText("BATTALION CHIEF/INCIDENT CMD : " + Login.username.toUpperCase());

        FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (/*snapshot.child("personnel").getValue(String.class).contains(Login.snapshot_parent) &&*/ snapshot.child("status").getValue(String.class).equals("open")) {
                            // Log.e("personnel", snapshot.getKey());
                            incident_id_for_personnel = snapshot.getKey();
                            note_for_personnel = snapshot.child("note_reference").getValue(String.class);
                            notification_id_for_personnel = snapshot.child("notification").getValue(String.class);
                            captain_id_for_personnel = snapshot.child("captain").getValue(String.class);
                            inc_date_for_personnel = snapshot.child("date").getValue(String.class);
                            inc_time_for_personnel = snapshot.child("time").getValue(String.class);
                            inc_lat_for_personnel = snapshot.child("latitude").getValue(String.class);
                            inc_long_for_personnel = snapshot.child("longitude").getValue(String.class);
                            personnels_id_for_personnel = snapshot.child("personnel").getValue(String.class);
                            inc_status_for_personnel = snapshot.child("status").getValue(String.class);
                            inc_address_for_personnel = snapshot.child("address").getValue(String.class);

                            edit.putString("incident_id_for_personnel", incident_id_for_personnel);
                            edit.putString("note_for_personnel", note_for_personnel);
                            edit.putString("notification_id_for_personnel", notification_id_for_personnel);
                            edit.putString("captain_id_for_personnel", captain_id_for_personnel);
                            edit.putString("inc_date_for_personnel", inc_date_for_personnel);
                            edit.putString("inc_time_for_personnel", inc_time_for_personnel);
                            edit.putString("inc_lat_for_personnel", inc_lat_for_personnel);
                            edit.putString("inc_long_for_personnel", inc_long_for_personnel);
                            edit.putString("personnels_id_for_personnel", personnels_id_for_personnel);
                            edit.putString("inc_status_for_personnel", inc_status_for_personnel);
                            edit.putString("inc_address_for_personnel", inc_address_for_personnel);
                            edit.commit();
                            // Log.e("mapready22", inc_lat_for_personnel + " " + inc_long_for_personnel + incident_id_for_personnel);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location objec
                    cur_lat = location.getLatitude() + "";
                    cur_lon = location.getLongitude() + "";
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
        }

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("qwe", "getInstanceId failed", task.getException());
                            return;
                        }
                        token = task.getResult().getToken();
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("qwe", msg);
                        // Toast.makeText(Battalion_Chief_Dashboard.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    startActivity(new Intent(Battalion_Chief_Dashboard.this, Login.class));
                    finish();
                }
            }
        };

        mapFragment.getMapAsync(this);

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                check_control();
                setButton();
            }
        }, 0, 10000);


        myTimer1 = new Timer();
        myTimer1.schedule(new TimerTask() {
            @Override
            public void run() {
                last_know_location();
            }
        }, 0, 5000);


        noti_dash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Battalion_Chief_Dashboard.this, Battalion_Chief_Noti_Dashboard.class)
                        .putExtra("incident_id", incident_id_for_personnel)
                        .putExtra("transfer_control", transfer_control + "")

                );
            }
        });


        take_cmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(myContext, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(myContext);
                }
                builder.setTitle("Control Transfer".toUpperCase())
                        .setMessage("Take Control")
                        .setPositiveButton("TRANSFER", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                HashMap map = new HashMap<>();
                                map.put("transfer_control", 1);
                                FirebaseDatabase.getInstance().getReference("Incident").child(incident_id_for_personnel).updateChildren(map);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                String DateandTime = sdf.format(new Date());
                                HashMap map1 = new HashMap<>();
                                map1.put(DateandTime, "Transfer Of Control");
                                FirebaseDatabase.getInstance().getReference("Notification").child(incident_id_for_personnel).updateChildren(map1);


                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            }
        });

    }


    private void check_control() {
        FirebaseDatabase.getInstance().getReference("Incident").child(incident_id_for_personnel).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void setButton() {

        myRef3.child(incident_id_for_personnel).addListenerForSingleValueEvent(new ValueEventListener() {
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

                method.showalert(transfer_control, myContext, "Confirmation", "Want to Send ?", incident_id_for_personnel, "par", par_button,
                        "Alert", "PAR Is Called Off", "PAR Is Called On", Login.snapshot_parent, Login.username, "PAR off Alert Sent To All", "PAR on Alert Sent To All");

//                if (transfer_control == 1) {
//
//
//                    AlertDialog.Builder builder;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this, android.R.style.Theme_Material_Dialog_Alert);
//                    } else {
//                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this);
//                    }
//
//                    builder.setTitle("Confirmation")
//                            .setMessage("Want to Send PAR Command ?")
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // Log.e("helper", "yes");
//
//
//                                    myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                            if (dataSnapshot.getChildrenCount() != 0) {
//                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                                    if (snapshot.getKey().equals(incident_id_for_personnel)) {
//                                                        if (snapshot.child("par").child("send").getValue(Integer.class) == snapshot.child("par").child("received").getValue(Integer.class)) {
//                                                            Log.e("SendReceived", "yes");
//                                                            continue_fun();
//                                                        } else {
//                                                            Log.e("SendReceived", "no");
//                                                            method.showalert("Alert", "Not Received ACK From All So Can't Send..");
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                        private void continue_fun() {
//
//                                            myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                    if (dataSnapshot.getChildrenCount() != 0) {
//                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                                            if (snapshot.getKey().equals(incident_id_for_personnel)) {
//
//                                                                if (par_button.equals("false")) {
//                                                                    HashMap map = new HashMap();
//                                                                    map.put("status", "true");
//                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("par").updateChildren(map);
//                                                                    par_button = "true";
//                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
//                                                                    // Log.e("helper", Arrays.toString(token_id));
//
//
//                                                                    map.put("received", 0);
//                                                                    map.put("send", token_id.length);
//                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("par").updateChildren(map);
//
//
//                                                                    for (final String str : token_id) {
//
//
//                                                                        map.put(str, "0");
//                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("par").updateChildren(map);
//
//
//                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                                            @Override
//                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                                                if (dataSnapshot.getChildrenCount() != 0) {
//                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                                                                        if (snapshot.getKey().equals(str)) {
//                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
//                                                                                            method.sendFCMPush("Alert", "PAR Is Called On", snapshot.child("token").getValue(String.class));
//                                                                                        }
//                                                                                    }
//                                                                                }
//                                                                            }
//
//                                                                            @Override
//                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                                                            }
//                                                                        });
//                                                                    }
//                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
//                                                                    String DateandTime = sdf.format(new Date());
//                                                                    HashMap hm = new HashMap();
//                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "PAR On Alert Sent To All");
//                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
//                                                                } else {
//                                                                    HashMap map = new HashMap();
//                                                                    map.put("status", "false");
//                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("par").updateChildren(map);
//                                                                    par_button = "false";
//                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
//                                                                    // Log.e("helper", Arrays.toString(token_id));
//
//
//                                                                    map.put("received", 0);
//                                                                    map.put("send", token_id.length);
//                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("par").updateChildren(map);
//
//
//                                                                    for (final String str : token_id) {
//
//
//                                                                        map.put(str, "0");
//                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("par").updateChildren(map);
//
//
//                                                                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                                            @Override
//                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                                                if (dataSnapshot.getChildrenCount() != 0) {
//                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                                                                        if (snapshot.getKey().equals(str)) {
//                                                                                            // Log.e("helper", snapshot.child("token").getValue(String.class));
//                                                                                            method.sendFCMPush("Alert", "PAR Is Called OFF", snapshot.child("token").getValue(String.class));
//                                                                                        }
//                                                                                    }
//                                                                                }
//                                                                            }
//
//                                                                            @Override
//                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                                                            }
//                                                                        });
//                                                                    }
//                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
//                                                                    String DateandTime = sdf.format(new Date());
//                                                                    HashMap hm = new HashMap();
//                                                                    hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send " + "PAR off Alert Sent To All");
//                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onCancelled(@NonNull DatabaseError databaseError) {
//                                                }
//                                            });
//
//
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                        }
//                                    });
//
//                                }
//                            })
//                            .setNegativeButton(android.R.string.no, null)
////                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .show();
//                } else {
//                    method.showalert("Warning Cant Send Cmd", "You are not authorized");
//                }
            }
        });


        all_clear_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transfer_control == 1) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id_for_personnel)) {
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


                                            myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id_for_personnel)) {

                                                                if (all_clear_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("all_clear").updateChildren(map);
                                                                    all_clear_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("all_clear").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("all_clear").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("all_clear").updateChildren(map);
                                                                    all_clear_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("all_clear").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("all_clear").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
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
                if (transfer_control == 1) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id_for_personnel)) {
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


                                            myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                                if (evacuate_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("evacuate").updateChildren(map);
                                                                    evacuate_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("evacuate").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("evacuate").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("evacuate").updateChildren(map);
                                                                    evacuate_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("evacuate").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("evacuate").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
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
                if (transfer_control == 1) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id_for_personnel)) {
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


                                            myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                                if (utility_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("utility").updateChildren(map);
                                                                    utility_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("utility").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("utility").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("utility").updateChildren(map);
                                                                    utility_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("utility").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("utility").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
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
                if (transfer_control == 1) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id_for_personnel)) {
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


                                            myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                                if (rescue_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("rescue").updateChildren(map);
                                                                    rescue_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("rescue").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("rescue").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("rescue").updateChildren(map);
                                                                    rescue_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));

                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("rescue").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("rescue").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
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


                if (transfer_control == 1) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Battalion_Chief_Dashboard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to Send ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Log.e("helper", "yes");


                                    myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.getKey().equals(incident_id_for_personnel)) {
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


                                            myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                                if (mayday_button.equals("false")) {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "true");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("mayday").updateChildren(map);
                                                                    mayday_button = "true";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("mayday").updateChildren(map);


                                                                    for (final String str : token_id) {

                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("mayday").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
                                                                } else {
                                                                    HashMap map = new HashMap();
                                                                    map.put("status", "false");
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("mayday").updateChildren(map);
                                                                    mayday_button = "false";
                                                                    String token_id[] = snapshot.child("personnel").getValue(String.class).split(",");
                                                                    // Log.e("helper", Arrays.toString(token_id));


                                                                    map.put("received", 0);
                                                                    map.put("send", token_id.length);
                                                                    FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("mayday").updateChildren(map);


                                                                    for (final String str : token_id) {


                                                                        map.put(str, "0");
                                                                        FirebaseDatabase.getInstance().getReference("Alert").child(incident_id_for_personnel).child("mayday").updateChildren(map);


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
                                                                    myRef4.child(incident_id_for_personnel).updateChildren(hm);
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

    private void last_know_location() {

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location objec
                    cur_lat = location.getLatitude() + "";
                    cur_lon = location.getLongitude() + "";


                    HashMap map = new HashMap<>();
                    map.put("last_latitude", cur_lat);
                    map.put("last_longitude", cur_lon);
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
                        } else if (rol.equals("battalion chief")) {
                            mMap.addMarker(new MarkerOptions().position(scu).title("BC " + name.toUpperCase()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        } else {
                            mMap.addMarker(new MarkerOptions().position(scu).title("FR " + name.toUpperCase()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        }

                        if (mayday_lat != null && mayday_lon != null) {
                            LatLng mayday_latlng = new LatLng(Double.parseDouble(mayday_lat), Double.parseDouble(mayday_lon));
                            mMap.addMarker(new MarkerOptions().position(mayday_latlng).title("MAYDAY MAYDAY " + name.toUpperCase()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                        }
                    }
                    LatLng end = new LatLng(Double.parseDouble(inc_lat_for_personnel), Double.parseDouble(inc_long_for_personnel));
                    mMap.addMarker(new MarkerOptions().position(end).title("Incident").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    loadmappath();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadmappath() {
        String url = getUrl();
        Log.d("onMapClick", url.toString());
        Battalion_Chief_Dashboard.FetchUrl FetchUrl = new Battalion_Chief_Dashboard.FetchUrl();
        FetchUrl.execute(url);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private String getUrl() {

        String str_origin = "origin=" + cur_lat + "," + cur_lon;
        String str_dest = "destination=" + inc_lat_for_personnel + "," + inc_long_for_personnel;
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
            Battalion_Chief_Dashboard.ParserTask parserTask = new Battalion_Chief_Dashboard.ParserTask();
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
//        show_map();
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
                                ActivityCompat.requestPermissions(Battalion_Chief_Dashboard.this,
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

    void show_map() {


        LatLng scu1 = new LatLng(Double.parseDouble(cur_lat), Double.parseDouble(cur_lon));
        mMap.addMarker(new MarkerOptions().position(scu1).title("Current Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu1));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


        LatLng scu11 = new LatLng(Double.parseDouble(inc_lat_for_personnel), Double.parseDouble(inc_long_for_personnel));
        mMap.addMarker(new MarkerOptions().position(scu11).title(Login.firestation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu11));
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

}
