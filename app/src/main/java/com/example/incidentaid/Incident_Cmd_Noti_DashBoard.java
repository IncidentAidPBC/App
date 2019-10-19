package com.example.incidentaid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Incident_Cmd_Noti_DashBoard extends AppCompatActivity {

    private int transfer_control;
    private String incident_id, notification_id;
    private Button all_clear_b, evacuate_b, mayday_b, par_b, rescue_b, utility_b, close_incident;
    private Context mContext;
    private Method method;
    private TextView header;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Alert");
    private DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference("Notification");
    private DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference("Incident");
    HashMap<String, String> map;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_incident_cmd_noti_dashboard);


        Intent intent = getIntent();
        incident_id = intent.getStringExtra("incident_id");
        notification_id = intent.getStringExtra("notification");
        transfer_control = Integer.parseInt(intent.getStringExtra("transfer_control"));

        mContext = Incident_Cmd_Noti_DashBoard.this;
        method = new Method(mContext);
        map = new HashMap<>();

        header = (TextView) findViewById(R.id.header);
        header.setText("INCIDENT COMMANDER: " + Login.username.toUpperCase());


        all_clear_b = (Button) findViewById(R.id.allclearbut);
        evacuate_b = (Button) findViewById(R.id.evacuatebut);
        mayday_b = (Button) findViewById(R.id.maydaybut);
        par_b = (Button) findViewById(R.id.parbut);
        rescue_b = (Button) findViewById(R.id.rescuebut);
        utility_b = (Button) findViewById(R.id.utilitiesbut);
        close_incident = (Button) findViewById(R.id.close_incident);


        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> list;
                if (dataSnapshot.getChildrenCount() != 0) {
                    list = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        list.add(snapshot.getKey() + ":" + snapshot.child("name").getValue(String.class));
                    }
                    for (int i = 0; i < list.size(); i++) {
                        map.put(list.get(i).split(":")[0], list.get(i).split(":")[1]);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Log.e("hashmap", map + "");
        map.put("1", "A");
        map.put("0", " ");

        par_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("Alert");
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(incident_id)) {
                                    String temp = snapshot.child("par").getValue().toString();
                                    temp = temp.replaceAll("\\{", "");
                                    temp = temp.replaceAll("\\}", "");
                                    String arr[] = temp.split(", ");

                                    Arrays.sort(arr);
                                    ArrayList<String> namearr = new ArrayList<>();

                                    for (String str : arr) {
                                        String name = str.split("=")[0];
                                        String value = str.split("=")[1];
                                        if (map.containsKey(name))
                                            namearr.add(map.get(name).toUpperCase() + "  " + map.get(value));
                                    }

                                    String final_result = namearr.toString().replace("[", "").replace("]", "").replace(", ", " | ");

                                    if (snapshot.child("par").child("received").getValue().toString().equals(snapshot.child("par").child("send").getValue().toString())) {
                                        method.showalert("PAR" + " ACK", final_result);
                                    } else {
                                        method.showalert("PAR" + " REC", final_result);
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
        });


        all_clear_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.e("hashmap", map + "");

                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("Alert");
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(incident_id)) {
                                    String temp = snapshot.child("all_clear").getValue().toString();
                                    temp = temp.replaceAll("\\{", "");
                                    temp = temp.replaceAll("\\}", "");
                                    String arr[] = temp.split(", ");
                                    Arrays.sort(arr);

                                    ArrayList<String> namearr = new ArrayList<>();

                                    for (String str : arr) {
                                        String name = str.split("=")[0];
                                        String value = str.split("=")[1];
                                        if (map.containsKey(name))
                                            namearr.add(map.get(name).toUpperCase() + "  " + map.get(value));
                                    }

                                    String final_result = namearr.toString().replace("[", "").replace("]", "").replace(", ", " | ");

                                    if (snapshot.child("all_clear").child("received").getValue().toString().equals(snapshot.child("all_clear").child("send").getValue().toString())) {
                                        method.showalert("ALL CLEAR" + " ACK", final_result);

                                    } else {
                                        method.showalert("ALL CLEAR" + " REC", final_result);
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
        });


        evacuate_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("Alert");
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(incident_id)) {
                                    String temp = snapshot.child("evacuate").getValue().toString();
                                    temp = temp.replaceAll("\\{", "");
                                    temp = temp.replaceAll("\\}", "");
                                    String arr[] = temp.split(", ");
                                    Arrays.sort(arr);

                                    ArrayList<String> namearr = new ArrayList<>();

                                    for (String str : arr) {
                                        String name = str.split("=")[0];
                                        String value = str.split("=")[1];
                                        if (map.containsKey(name))
                                            namearr.add(map.get(name).toUpperCase() + "  " + map.get(value));
                                    }

                                    String final_result = namearr.toString().replace("[", "").replace("]", "").replace(", ", " | ");

                                    if (snapshot.child("evacuate").child("received").getValue().toString().equals(snapshot.child("evacuate").child("send").getValue().toString())) {
                                        method.showalert("EVACUATE" + " ACK", final_result);
                                    } else {
                                        method.showalert("EVACUATE" + " REC", final_result);
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
        });


        utility_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("Alert");
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(incident_id)) {
                                    String temp = snapshot.child("utility").getValue().toString();
                                    temp = temp.replaceAll("\\{", "");
                                    temp = temp.replaceAll("\\}", "");
                                    String arr[] = temp.split(", ");
                                    Arrays.sort(arr);

                                    ArrayList<String> namearr = new ArrayList<>();

                                    for (String str : arr) {
                                        String name = str.split("=")[0];
                                        String value = str.split("=")[1];
                                        if (map.containsKey(name))
                                            namearr.add(map.get(name).toUpperCase() + "  " + map.get(value));
                                    }

                                    String final_result = namearr.toString().replace("[", "").replace("]", "").replace(", ", " | ");

                                    if (snapshot.child("utility").child("received").getValue().toString().equals(snapshot.child("utility").child("send").getValue().toString())) {
                                        method.showalert("UTILITY" + " ACK", final_result);
                                    } else {
                                        method.showalert("UTILITY" + " REC", final_result);
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
        });


        rescue_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("Alert");
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(incident_id)) {
                                    String temp = snapshot.child("rescue").getValue().toString();
                                    temp = temp.replaceAll("\\{", "");
                                    temp = temp.replaceAll("\\}", "");
                                    String arr[] = temp.split(", ");
                                    Arrays.sort(arr);

                                    ArrayList<String> namearr = new ArrayList<>();

                                    for (String str : arr) {
                                        String name = str.split("=")[0];
                                        String value = str.split("=")[1];
                                        if (map.containsKey(name))
                                            namearr.add(map.get(name).toUpperCase() + "  " + map.get(value));
                                    }

                                    String final_result = namearr.toString().replace("[", "").replace("]", "").replace(", ", " | ");

                                    if (snapshot.child("rescue").child("received").getValue().toString().equals(snapshot.child("rescue").child("send").getValue().toString())) {
                                        method.showalert("RESCUE" + " ACK", final_result);
                                    } else {
                                        method.showalert("RESCUE" + " REC", final_result);
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
        });


        mayday_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Query userQuery = FirebaseDatabase.getInstance().getReference().child("Alert");
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(incident_id)) {
                                    String temp = snapshot.child("mayday").getValue().toString();
                                    temp = temp.replaceAll("\\{", "");
                                    temp = temp.replaceAll("\\}", "");
                                    String arr[] = temp.split(", ");

                                    Arrays.sort(arr);

                                    ArrayList<String> namearr = new ArrayList<>();

                                    for (String str : arr) {
                                        String name = str.split("=")[0];
                                        String value = str.split("=")[1];
                                        if (map.containsKey(name))
                                            namearr.add(map.get(name).toUpperCase() + "  " + map.get(value));
                                    }

                                    String final_result = namearr.toString().replace("[", "").replace("]", "").replace(", ", " | ");


                                    if (snapshot.child("mayday").child("received").getValue().toString().equals(snapshot.child("mayday").child("send").getValue().toString())) {
                                        method.showalert("MAYDAY" + " ACK", final_result);
                                    } else {
                                        method.showalert("MAYDAY" + " REC", final_result);
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
        });


        close_incident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(transfer_control == 0) {

                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Incident_Cmd_Noti_DashBoard.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Incident_Cmd_Noti_DashBoard.this);
                    }
                    builder.setTitle("Confirmation")
                            .setMessage("Want to close the incident?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                    String DateandTime = sdf.format(new Date());
                                    HashMap hm = new HashMap();
                                    hm.put(DateandTime, "Incident Commander Close The Incident");
                                    FirebaseDatabase.getInstance().getReference("Notification").child(incident_id).updateChildren(hm);

                                    HashMap hm1 = new HashMap();
                                    hm1.put("status", "close");
                                    FirebaseDatabase.getInstance().getReference("Incident").child(incident_id).updateChildren(hm1);


                                    FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    HashMap reset_mayday = new HashMap();
                                                    reset_mayday.put("mayday_latitude", null);
                                                    reset_mayday.put("mayday_longitude", null);
                                                    FirebaseDatabase.getInstance().getReference("User").child(snapshot.getKey()).updateChildren(reset_mayday);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    startActivity(new Intent(Incident_Cmd_Noti_DashBoard.this, Captain_DashBoard.class));

                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();

                }else{
                    method.showalert("Warning Cant Send Cmd","You are not authorized");
                }


                }
            });



    }
}
