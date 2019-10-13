package com.example.incidentaid;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Incident_History_Single_View extends AppCompatActivity {


    public String key, address, all_people;
    private TextView lat, lon, stat, inc_cmd_name, pers_name, date, time, allcal, paral, resal, mayal, utial, evaal, header;
    public ListView all_history;
    ArrayList<String> product;
    ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_history_single);

        Login.snapshot_parent = Login.pref.getString("user_id", null);

        Intent intent = getIntent();
        key = intent.getStringExtra("key");
        address = intent.getStringExtra("address");

        lat = (TextView) findViewById(R.id.lat);
        lon = (TextView) findViewById(R.id.lon);
        stat = (TextView) findViewById(R.id.stat);
        inc_cmd_name = (TextView) findViewById(R.id.inc_cmd);
        pers_name = (TextView) findViewById(R.id.personnel);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        allcal = (TextView) findViewById(R.id.all_clear);
        paral = (TextView) findViewById(R.id.par);
        resal = (TextView) findViewById(R.id.rescue);
        mayal = (TextView) findViewById(R.id.mayday);
        utial = (TextView) findViewById(R.id.utility);
        evaal = (TextView) findViewById(R.id.evacuate);
        header = (TextView) findViewById(R.id.inc_address);
        all_history = (ListView) findViewById(R.id.all_history);

        header.setText(address.toUpperCase());

        Toast.makeText(getApplicationContext(), key + address, Toast.LENGTH_SHORT).show();

        FirebaseDatabase.getInstance().getReference("Alert").child(key).child("all_clear").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allcal.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Alert").child(key).child("evacuate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                evaal.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Alert").child(key).child("mayday").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mayal.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Alert").child(key).child("par").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                paral.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Alert").child(key).child("rescue").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                resal.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Alert").child(key).child("utility").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                utial.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(key)) {
                            final String inc_cmd = snapshot.child("captain").getValue(String.class);
                            String person = snapshot.child("personnel").getValue(String.class);
                            all_people = inc_cmd + "," + person;
                            Log.e("asdasdasd", all_people);

                            FirebaseDatabase.getInstance().getReference().child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                ArrayList<String> res;

                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    res = new ArrayList<>();
                                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                                        if (all_people.contains(snapshot1.getKey())) {
                                            Log.e("asdasdasdasd", snapshot1.child("name").getValue(String.class));
                                            res.add(snapshot1.child("name").getValue(String.class));
                                        }
                                    }
                                    Log.e("asdasdasdasd123", String.valueOf(res));
                                    inc_cmd_name.setText(res.get(0));
                                    String temp = "";
                                    for (int i = 1; i < res.size(); i++) {
                                        temp += res.get(i) + ", ";
                                    }
                                    pers_name.setText(temp);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                            lat.setText(snapshot.child("latitude").getValue(String.class));
                            lon.setText(snapshot.child("longitude").getValue(String.class));
                            stat.setText(snapshot.child("status").getValue(String.class));
                            date.setText(snapshot.child("date").getValue(String.class));
                            time.setText(snapshot.child("time").getValue(String.class));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Notification").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                product = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(key))
                            product.add(snapshot.getValue().toString().toUpperCase());
                    }
                }

                Log.e("product", String.valueOf(product));

                String temp = String.valueOf(product);
                temp = temp.replaceAll("\\{", "");
                temp = temp.replaceAll("\\}", "");

                String arr[] = temp.split(", ");
                List l = new ArrayList();
                for (String str : arr) {
                    l.add(str);
                }
                Collections.sort(l);
                Log.e("List", l.toString());

                arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, l) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);
                        tv.setTextSize(14);
                        Spannable wordtoSpan = new SpannableString(tv.getText());
                        String temp = getItem(position);
                        wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv.setText(wordtoSpan);
                        return view;
                    }
                };
                all_history.setAdapter(arrayAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
