package com.example.incidentaid;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Captain_DashBoard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context mContext;
    private Method method;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListView all_people, on_duty;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userinfo, myRef, myRef1;
    private FirebaseAuth auth;
    private Context myContext;
    private TextView title, name, job;
    public static String job_of_person, fire_station_lat, fire_station_long, token, person_token;
    ArrayList<String> list = new ArrayList<>();
    List<String> call = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter, arrayAdapter1;
    ArrayList<String> product, onduty, ondutyjob, id_list, token_list;
    HashMap<String, String> name_job;
    private String filladdress, fillpincode;
    FloatingActionButton fab;
    public static String mypref = "mypref";
    public static SharedPreferences pref, pref1;
    public SharedPreferences.Editor edit;


    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        myContext = Captain_DashBoard.this;
        method = new Method(myContext);
        pref = getSharedPreferences(mypref, MODE_PRIVATE);
        edit = pref.edit();

        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("User");
        myRef1 = FirebaseDatabase.getInstance().getReference("Fire_Station");

        userinfo = FirebaseDatabase.getInstance().getReference("User");
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        Login.username = Login.pref.getString("username", null);
        Login.snapshot_parent = Login.pref.getString("user_id", null);
        Login.firestation = Login.pref.getString("firestation", null);
        Login.jobtitle = Login.pref.getString("jobtitle", null);
        Login.values_for_profile = Login.pref.getString("values_for_profile", null);
        Login.who_is_the_user = Login.pref.getString("user_role", null);

        title = (TextView) findViewById(R.id.title);
        all_people = (ListView) findViewById(R.id.all_people);
        on_duty = (ListView) findViewById(R.id.on_duty);
        title.setText("CAPTAIN: " + Login.username.toUpperCase());

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
                    }
                });

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    startActivity(new Intent(Captain_DashBoard.this, Login.class));
                    finish();
                }
            }
        };
        findUser();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        TextView name = (TextView) headerView.findViewById(R.id.name);
        TextView job = (TextView) headerView.findViewById(R.id.job);
        name.setText(Login.username.toUpperCase());
        job.setText(Login.jobtitle.toUpperCase());


        myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(Login.firestation)) {
                            Log.e("123", snapshot.child("Latitude").getValue().toString());
                            Log.e("123", snapshot.child("Longitude").getValue().toString());
                            fire_station_lat = snapshot.child("Latitude").getValue().toString();
                            fire_station_long = snapshot.child("Longitude").getValue().toString();

                            edit.putString("fire_station_lan", fire_station_lat);
                            edit.putString("fire_station_long", fire_station_long);
                            edit.commit();


                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setColorFilter(Color.WHITE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap map = new HashMap();
                map.put("token", token);
                userinfo.child(Login.snapshot_parent).updateChildren(map);

                FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.child("captain").getValue().toString().equals(Login.snapshot_parent)) {
                                    if (snapshot.child("status").getValue().toString().equals("open")) {
                                        startActivity(new Intent(Captain_DashBoard.this, Incident_Cmd_DashBoard.class)
                                                .putExtra("long", snapshot.child("longitude").getValue().toString())
                                                .putExtra("lat", snapshot.child("latitude").getValue().toString())
                                                .putExtra("address", snapshot.child("address").getValue().toString())
                                                .putExtra("captain", snapshot.child("captain").getValue().toString())
                                                .putExtra("personnel", snapshot.child("personnel").getValue().toString())
                                                .putExtra("date", snapshot.child("date").getValue().toString())
                                                .putExtra("note_reference", snapshot.child("note_reference").getValue().toString())
                                                .putExtra("notification", snapshot.child("notification").getValue().toString())
//                                                .putExtra("time", snapshot.child("time").getValue().toString())
//                                                .putExtra("status", snapshot.child("status").getValue().toString())
                                                .putExtra("incident_id", snapshot.getKey().toString())
//                                                .putExtra("all_clear", snapshot.child("alert").child("all_clear").getValue().toString())
//                                                .putExtra("cross_ventilation", snapshot.child("alert").child("cross_ventilation").getValue().toString())
//                                                .putExtra("evacuate", snapshot.child("alert").child("evacuate").getValue().toString())
//                                                .putExtra("mayday", snapshot.child("alert").child("mayday").getValue().toString())
//                                                .putExtra("par", snapshot.child("alert").child("par").getValue().toString())
//                                                .putExtra("rescue", snapshot.child("alert").child("rescue").getValue().toString())
//                                                .putExtra("utility", snapshot.child("alert").child("utility").getValue().toString())
//                                                .putExtra("vertical_ventilation", snapshot.child("alert").child("vertical_ventilation").getValue().toString())
                                        );
                                    }

                                    else {
                                        startActivity(new Intent(Captain_DashBoard.this, Captain_Create_Incident.class)
                                                .putExtra("username", Login.username)
                                                .putExtra("FSaddress", Login.firestation)
                                                .putExtra("long", fire_station_long)
                                                .putExtra("lat", fire_station_lat)
                                                .putExtra("id_list", id_list.toString())
                                                .putExtra("token_list", token_list.toString())
                                                .putExtra("notification", "")
                                        );
                                    }

                                }
                            }
                        } else {
                            startActivity(new Intent(Captain_DashBoard.this, Captain_Create_Incident.class)
                                    .putExtra("username", Login.username)
                                    .putExtra("FSaddress", Login.firestation)
                                    .putExtra("long", fire_station_long)
                                    .putExtra("lat", fire_station_lat)
                                    .putExtra("id_list", id_list.toString())
                                    .putExtra("token_list", token_list.toString())
                                    .putExtra("notification", "")
                            );
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                product = new ArrayList<>();
                onduty = new ArrayList<>();
                ondutyjob = new ArrayList<>();
                id_list = new ArrayList<>();
                token_list = new ArrayList<>();

                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Log.e("qweqwe1", snapshot.child("firestation").getValue().toString());
                    Log.e("qweqwe", Login.firestation);

                    if (snapshot.child("firestation").getValue().toString().equals(Login.firestation) &&
                            snapshot.child("role").getValue().toString().equals("not_captain")) {
                        Log.e("qweqwe2", snapshot.child("name").getValue().toString());
                        product.add(snapshot.child("name").getValue().toString().toUpperCase());
                    }

                    if (snapshot.child("firestation").getValue().toString().equals(Login.firestation) &&
                            snapshot.child("role").getValue().toString().equals("on_duty")) {
                        Log.e("qweqwe2", snapshot.child("name").getValue().toString());
                        onduty.add(snapshot.child("name").getValue().toString().toUpperCase() + " (" + snapshot.child("job_title").getValue().toString().toUpperCase() + ")");
                        id_list.add(snapshot.getKey());
                        token_list.add(snapshot.child("token").getValue().toString());
                    }
                }

                onduty.add(0, Login.username.toUpperCase() + " (" + Login.jobtitle.toUpperCase() + ")");
                id_list.add(0, Login.snapshot_parent);
                arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, product) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);
                        tv.setTextSize(14);
                        return view;
                    }
                };
                all_people.setAdapter(arrayAdapter);

/// on duty listview
                arrayAdapter1 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, onduty) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);
                        tv.setTextSize(14);
//                        tv.setTextColor(Color.BLACK);
                        if (getItem(position).contains("EMT") && getItem(position).contains("FIRE")) {
                            tv.setTextColor(Color.BLUE);
                        } else if (getItem(position).contains("Para") && getItem(position).contains("FIRE")) {
                            tv.setTextColor(Color.RED);
                        } else {
                            tv.setTextColor(Color.MAGENTA);
                        }

                        if (position == 0) {
                            tv.setTypeface(null, Typeface.BOLD);
                            tv.setTextColor(Color.BLACK);
                        }
                        return view;
                    }

                    @Override
                    public boolean isEnabled(int position) {
                        if (position == 0) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                };
                on_duty.setAdapter(arrayAdapter1);
                Log.e("id_list", id_list.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        all_people.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i,
                                    long l) {

                final String name_of_person = (String) adapterView.getItemAtPosition(i);
                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("User");
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.child("name").getValue().toString().equals(name_of_person.toLowerCase())) {

                                    final String getparent = snapshot.getKey();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Captain_DashBoard.this);
                                    LayoutInflater inflater = getLayoutInflater();
                                    View dialogView = inflater.inflate(R.layout.activity_options, null);
                                    builder.setCancelable(false);
                                    builder.setView(dialogView);
                                    final TextView name = (TextView) dialogView.findViewById(R.id.name);
                                    final TextView job = (TextView) dialogView.findViewById(R.id.job);

                                    final Button onduty = (Button) dialogView.findViewById(R.id.on_duty);
                                    name.setText(name_of_person.toUpperCase());
                                    job_of_person = snapshot.child("job_title").getValue().toString();
                                    person_token = snapshot.child("token").getValue().toString();
                                    job.setText(job_of_person.toUpperCase());

                                    final AlertDialog dialog = builder.create();
                                    dialog.show();
                                    dialog.setCancelable(true);
                                    dialog.setCanceledOnTouchOutside(true);

                                    onduty.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
//                                            Toast.makeText(getApplicationContext(), "duty", Toast.LENGTH_SHORT).show();
                                            name_job = new HashMap<>();
                                            name_job.put(name_of_person, job_of_person);
                                            Log.e("wer", name_job.toString());
                                            HashMap map = new HashMap();
                                            map.put("role", "on_duty");
                                            userinfo.child(getparent).updateChildren(map);
                                            method.sendFCMPush("Duty", "You are on duty", person_token);

                                            dialog.cancel();
                                            startActivity(getIntent());
                                        }
                                    });
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
            }
        });

        on_duty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int i,
                                    long l) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Captain_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Captain_DashBoard.this);
                }
                builder.setTitle("Reset")
                        .setMessage("Want to remove?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("User");
                                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                String temp = adapterView.getItemAtPosition(i).toString();
                                                String temp1[] = temp.split(" ");
                                                String cmp = temp1[0] + " " + temp1[1];
//                                                Toast.makeText(Captain_DashBoard.this, cmp, Toast.LENGTH_LONG).show();


                                                if (snapshot.child("name").getValue().toString().equals(cmp.toLowerCase())) {

                                                    person_token = snapshot.child("token").getValue().toString();
                                                    final String getparent = snapshot.getKey();
                                                    HashMap map = new HashMap();
                                                    map.put("role", "not_captain");
                                                    userinfo.child(getparent).updateChildren(map);
                                                    method.sendFCMPush("Duty", "You are remove from duty", person_token);

//                                                    Toast.makeText(Captain_DashBoard.this, "Removed", Toast.LENGTH_SHORT).show();
                                                    startActivity(getIntent());
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
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void findUser() {
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("email").getValue(String.class).equals(Login.values_for_profile)) {

                        filladdress = snapshot.child("address").getValue(String.class);
                        fillpincode = snapshot.child("pincode").getValue(String.class);
//                        Toast.makeText(getApplicationContext(), filladdress + " " + fillpincode, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {


            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(Captain_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(Captain_DashBoard.this);
            }
            builder.setTitle("Exit")
                    .setMessage("Want to exit the app?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            Intent a = new Intent(Intent.ACTION_MAIN);
//                            a.addCategory(Intent.CATEGORY_HOME);
//                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(a);
                            finishAffinity();
//            startActivity(new Intent(MainActivity.this, Captain_Home.class));
//                            Captain_DashBoard.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_logout) {
//            return true;
//        }
//
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        Fragment fragment = null;
//        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.profile) {
            startActivity(new Intent(Captain_DashBoard.this, Profile_Captain.class).putExtra("filladdress", filladdress).putExtra("fillpincode", fillpincode));
        } else if (id == R.id.history) {
            startActivity(new Intent(Captain_DashBoard.this, Incident_History.class).putExtra("username", Login.username));
        } else if (id == R.id.logout) {

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(Captain_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(Captain_DashBoard.this);
            }
            builder.setTitle("Logout")
                    .setMessage("Want to logout from your account?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            signOut();
                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            finishAffinity();
                            startActivity(homeIntent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
//        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void signOut() {
        mAuth.signOut();
    }

}