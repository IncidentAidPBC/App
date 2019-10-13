package com.example.incidentaid;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Notification extends AppCompatActivity {


    private String incident_id, notification_id, cap_id;
    TextView header;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Notification");
    ListView noti;
    ArrayList<String> product;
    ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_notification);

        Login.username = Login.pref.getString("username", null);
        Login.who_is_the_user = Login.pref.getString("user_role", null);


        Intent intent = getIntent();
        incident_id = intent.getStringExtra("incident_id");
        notification_id = intent.getStringExtra("notification_id");
        cap_id = intent.getStringExtra("cap_id");

        noti = (ListView) findViewById(R.id.all_notification);
        header = (TextView) findViewById(R.id.header);
        header.setText("INCIDENT CAMMANDER: " + Login.username.toUpperCase());

        if (Login.who_is_the_user.equals("captain")) {
            header.setText("INCIDENT COMMANDER: " + Login.username.toUpperCase());
        } else {
            header.setText("PERSONNEL: " + Login.username.toUpperCase());
        }


        myRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                product = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(notification_id))
                            product.add(snapshot.getValue().toString().toUpperCase());
                    }
                }

                String temp = product.get(0);
                temp = temp.replaceAll("\\{", "");
                temp = temp.replaceAll("\\}", "");

                String arr[] = temp.split(", ");
                List l = new ArrayList();
                for (String str : arr) {
                    if (str.contains(cap_id) || str.contains(Login.snapshot_parent)) {
                        l.add(str);
                    }
                }
                Collections.sort(l);
                Log.e("List", l.toString());

                arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, l) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);
                        tv.setTextSize(14);
                        return view;
                    }
                };
                noti.setAdapter(arrayAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}