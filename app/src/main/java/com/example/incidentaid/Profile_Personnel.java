package com.example.incidentaid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Profile_Personnel extends AppCompatActivity {

    private Context mContext;
    private TextView fullname, jobtitle, firestation;
    private EditText password, address, pincode;
    private Button update;
    private DatabaseReference userinfo;
    private Method method;
    private static final String TAG = "Profile";
    public static final String PASSWORD_PATTERN = "^.*(?=.{4,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$";
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String display_pincode, display_address;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_profile);


        mContext = Profile_Personnel.this;
        method = new Method(mContext);
        method.setupFirebaseAuth();

        Login.username = Login.pref.getString("username", null);
        Login.snapshot_parent = Login.pref.getString("user_id", null);
        Login.jobtitle = Login.pref.getString("jobtitle", null);
        Login.firestation = Login.pref.getString("firestation", null);
        Login.who_is_the_user = Login.pref.getString("user_role", null);


        fullname = (TextView) findViewById(R.id.fullname);
        jobtitle = (TextView) findViewById(R.id.job_title);
        firestation = (TextView) findViewById(R.id.fire_station);
        address = (EditText) findViewById(R.id.new_address);
        pincode = (EditText) findViewById(R.id.new_pincode);
        password = (EditText) findViewById(R.id.new_password);

        userinfo = FirebaseDatabase.getInstance().getReference("User");

        fullname.setText(Login.username.toUpperCase() + " PROFILE (" + Login.snapshot_parent + ")");
        jobtitle.setText("JOB TITLE: " + Login.jobtitle.toUpperCase());
        firestation.setText("FIRE STATION #" + Login.firestation.toUpperCase().charAt(Login.firestation.length() - 1));
        update = (Button) findViewById(R.id.update);

        Intent intent = getIntent();
        display_address = intent.getStringExtra("filladdress");
        display_pincode = intent.getStringExtra("fillpincode");

        address.setText(display_address);
        pincode.setText(display_pincode);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String new_password = password.getText().toString();
                final String new_address = address.getText().toString();
                final String new_pincode = pincode.getText().toString();

                if (TextUtils.isEmpty(new_address)) {
                    Toast.makeText(getApplicationContext(), "Address Field Empty", Toast.LENGTH_SHORT).show();
                    address.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(new_pincode)) {
                    Toast.makeText(getApplicationContext(), "Pincode Field Empty", Toast.LENGTH_SHORT).show();
                    address.requestFocus();
                    return;
                }

                if (new_pincode.length() != 5) {
                    Toast.makeText(getApplicationContext(), "Pincode Invalid", Toast.LENGTH_SHORT).show();
                    pincode.requestFocus();
                    return;
                }

                // update only address and pincode
                if (new_password.length() == 0) {
                    HashMap map = new HashMap();
                    map.put("address", new_address);
                    map.put("pincode", new_pincode);
                    userinfo.child(Login.snapshot_parent).updateChildren(map);
                    Toast.makeText(Profile_Personnel.this, "Address and Pincode Updated", Toast.LENGTH_LONG).show();
                    finish();
                }


                // update password and/or address and pincode
                if (new_password.length() != 0) {

//                    if (!new_password.matches(PASSWORD_PATTERN)) {
//                        Toast.makeText(getApplicationContext(), "Password Error Must Contain Capital, Small, Number, Special Character Each", Toast.LENGTH_LONG).show();
//                        password.requestFocus();
//                        return;
//                    }

                    if (new_address.equals(display_address) && new_pincode.equals(display_pincode)) {
                        user.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Profile_Personnel.this, "Password has been updated", Toast.LENGTH_SHORT).show();
                                    finish();
//                                startActivity(new Intent(Profile.this,Incident_Cmd_Home.class));
                                } else {
                                    Log.e(TAG, "Error in updating Profile", task.getException());
                                    Toast.makeText(Profile_Personnel.this, "Failed to Update Password.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {

                        user.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    HashMap map = new HashMap();
                                    map.put("address", new_address);
                                    map.put("pincode", new_pincode);
                                    userinfo.child(Login.snapshot_parent).updateChildren(map);
                                    Toast.makeText(Profile_Personnel.this, "Address and Pincode Updated", Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    Log.e(TAG, "Error in updating Profile", task.getException());
                                    Toast.makeText(Profile_Personnel.this, "Failed to Update.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Profile_Personnel.this, Personnel_Home.class));
        super.onBackPressed();


    }
}
