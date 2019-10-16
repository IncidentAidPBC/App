package com.example.incidentaid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Battalion_Chief_Noti_Dashboard extends AppCompatActivity {

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
        setContentView(R.layout.activity_battalion_chief_noti_dashboard);



        Intent intent = getIntent();
        incident_id = intent.getStringExtra("incident_id");
        notification_id = intent.getStringExtra("notification");

        mContext = Battalion_Chief_Noti_Dashboard.this;
        method = new Method(mContext);
        map = new HashMap<>();

        header = (TextView) findViewById(R.id.header);
        header.setText("Battalion Chief / IC: " + Login.username.toUpperCase());


        all_clear_b = (Button) findViewById(R.id.allclearbut);
        evacuate_b = (Button) findViewById(R.id.evacuatebut);
        mayday_b = (Button) findViewById(R.id.maydaybut);
        par_b = (Button) findViewById(R.id.parbut);
        rescue_b = (Button) findViewById(R.id.rescuebut);
        utility_b = (Button) findViewById(R.id.utilitiesbut);
        close_incident = (Button) findViewById(R.id.close_incident);



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
                                    String receive = arr[arr.length-3].split("=")[1];
                                    String send = arr[arr.length-2].split("=")[1];
                                    if (receive.equals(send)) {
                                        method.showalert("PAR" + " ACK", "E3 - A");
                                    } else {
                                        method.showalert("PAR" + " REC", "E3 - ?");
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
                Log.e("hashmap", map + "");

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
                                    String receive = arr[arr.length-3].split("=")[1];
                                    String send = arr[arr.length-2].split("=")[1];
                                    if (receive.equals(send)) {
                                        method.showalert("All Clear" + " ACK", "E3 - A");
                                    } else {
                                        method.showalert("All Clear" + " REC", "E3 - ?");
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
                                    String receive = arr[arr.length-3].split("=")[1];
                                    String send = arr[arr.length-2].split("=")[1];
                                    if (receive.equals(send)) {
                                        method.showalert("Evacuate" + " ACK", "E3 - A");
                                    } else {
                                        method.showalert("Evacuate" + " REC", "E3 - ?");
                                    }                                }
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
                                    String receive = arr[arr.length-3].split("=")[1];
                                    String send = arr[arr.length-2].split("=")[1];
                                    if (receive.equals(send)) {
                                        method.showalert("Utility" + " ACK", "E3 - A");
                                    } else {
                                        method.showalert("Utility" + " REC", "E3 - ?");
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
                                    String receive = arr[arr.length-3].split("=")[1];
                                    String send = arr[arr.length-2].split("=")[1];
                                    if (receive.equals(send)) {
                                        method.showalert("Rescue" + " ACK", "E3 - A");
                                    } else {
                                        method.showalert("Rescue" + " REC", "E3 - ?");
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
                                    String receive = arr[arr.length-3].split("=")[1];
                                    String send = arr[arr.length-2].split("=")[1];
                                    if (receive.equals(send)) {
                                        method.showalert("Mayday" + " ACK", "E3 - A");
                                    } else {
                                        method.showalert("Mayday" + " REC", "E3 - ?");
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


                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Battalion_Chief_Noti_Dashboard.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Battalion_Chief_Noti_Dashboard.this);
                }
                builder.setTitle("Confirmation")
                        .setMessage("Want to close the incident?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                String DateandTime = sdf.format(new Date());
                                HashMap hm = new HashMap();
                                hm.put(DateandTime, "BC / Incident Commander Close The Incident");
                                FirebaseDatabase.getInstance().getReference("Notification").child(incident_id).updateChildren(hm);

                                HashMap hm1 = new HashMap();
                                hm1.put("status", "close");
                                FirebaseDatabase.getInstance().getReference("Incident").child(incident_id).updateChildren(hm1);


                                FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getChildrenCount()!=0){
                                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                HashMap reset_mayday = new HashMap();
                                                reset_mayday.put("mayday_latitude",null);
                                                reset_mayday.put("mayday_longitude",null);
                                                FirebaseDatabase.getInstance().getReference("User").child(snapshot.getKey()).updateChildren(reset_mayday);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

//                                startActivity(new Intent(Battalion_Chief_Noti_Dashboard.this, Captain_DashBoard.class));

                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    }
}
