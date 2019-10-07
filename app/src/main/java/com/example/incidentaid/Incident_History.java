package com.example.incidentaid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Incident_History extends AppCompatActivity {

    ListView history;
    private Context myContext;
    private Method method;
    private String temp;
    private TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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

        history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                method.showalert("Incident History", (String) adapterView.getItemAtPosition(i));
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
//        startActivity(new Intent(Incident_History.this, Captain_DashBoard.class));
//        super.onBackPressed();

    }

}
