package com.example.incidentaid;

import android.content.Context;
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
import android.widget.AdapterView;
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

public class Incident_History extends AppCompatActivity {

    private Context myContext;
    private Method method;
    public String temp;
    private TextView title;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Incident");
    public ArrayList<String> product;
    private ArrayList<String> allalert;

    ArrayAdapter<String> arrayAdapter;
    ListView history;

    String people_name[], address, names, lat, lon, stat, date, time, all_clear, eva, may, parr, res, uti;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_history);

        Login.username = Login.pref.getString("username", null);
        Login.snapshot_parent = Login.pref.getString("user_id", null);
        Login.firestation = Login.pref.getString("firestation", null);
        Login.jobtitle = Login.pref.getString("jobtitle", null);
        Login.values_for_profile = Login.pref.getString("values_for_profile", null);
        Login.who_is_the_user = Login.pref.getString("user_role", null);


        myContext = Incident_History.this;
        method = new Method(myContext);
        title = (TextView) findViewById(R.id.title);
        Intent intent = getIntent();
        temp = intent.getStringExtra("username");

        if (Login.who_is_the_user.equals("captain")) {
            title.setText("CAPTAIN: " + temp.toUpperCase());
        } else {
            title.setText("PERSONNEL: " + temp.toUpperCase());
        }
        history = (ListView) findViewById(R.id.all_history);


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                product = new ArrayList<>();

                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("captain").getValue().toString().equals(Login.snapshot_parent)) {
                            // Log.e("qweqweqweqwe", snapshot.getKey().toString());
                            product.add("ADDRESS : " + snapshot.child("address").getValue(String.class) + " & DATE_TIME : " + snapshot.getKey().toUpperCase());
                        }
                    }

                    arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, product) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView tv = (TextView) view.findViewById(android.R.id.text1);
                            tv.setTextSize(14);
                            Spannable wordtoSpan = new SpannableString(tv.getText());
                            String temp = getItem(position);
                            int add_ind = temp.indexOf("ADDRESS");
                            int dt_ind = temp.indexOf("DATE_TIME");
                            wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), add_ind, add_ind + 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), dt_ind, dt_ind + 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv.setText(wordtoSpan);

                            return view;
                        }
                    };
                    history.setAdapter(arrayAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String seperate[] = adapterView.getItemAtPosition(i).toString().split(" & ");
                String address[] = seperate[0].split(" : ");
                String datetime[] = seperate[1].split(" : ");

                startActivity(new Intent(Incident_History.this, Incident_History_Single_View.class)
                        .putExtra("key",datetime[1])
                        .putExtra("address",address[1])
                );
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
