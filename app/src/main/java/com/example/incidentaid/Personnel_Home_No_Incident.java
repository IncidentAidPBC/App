package com.example.incidentaid;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Personnel_Home_No_Incident extends AppCompatActivity {


    Button status, status_incident;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_no_incident);

        status = (Button) findViewById(R.id.status);
        status_incident = (Button) findViewById(R.id.status_incident);
        Login.username = Login.pref.getString("username", null);
        Login.snapshot_parent = Login.pref.getString("user_id", null);
        Login.firestation = Login.pref.getString("firestation", null);
        Login.jobtitle = Login.pref.getString("jobtitle", null);
        Login.values_for_profile = Login.pref.getString("values_for_profile", null);
        Login.who_is_the_user = Login.pref.getString("user_role", null);
        Login.alert_token = Login.pref.getString("alert_token", null);

        status.setEnabled(false);
        status_incident.setEnabled(false);

        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()!=0){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(snapshot.getKey().equals(Login.snapshot_parent)){
                            if(snapshot.child("role").getValue(String.class).equals("not_captain")){
                                status.setText("OFF DUTY");
                            }else {
                                status.setText("ON DUTY");

                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()!=0){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(!snapshot.child("status").getValue(String.class).equals("open") && !snapshot.child("personnel").getValue(String.class).contains(Login.snapshot_parent)){
                            status_incident.setText("No Active Incident");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
}
