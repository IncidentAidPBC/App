package com.example.incidentaid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private EditText email, password;
    private Button signup, forgotpassword, login;

    private Context mContext;
    public static String username, who_is_the_user, snapshot_parent, values_for_profile, jobtitle, firestation, alert_token;
    public static String mypref = "mypref", token;
    public static SharedPreferences pref, pref1;
    public SharedPreferences.Editor edit;
    private DatabaseReference myref = FirebaseDatabase.getInstance().getReference("User");
    private FirebaseAuth mAuth;
    private String eemail, ppassword, path, userid;
    private DatabaseReference userinfo;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + BuildConfig.serverKey;
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mAuth = FirebaseAuth.getInstance();
        mContext = Login.this;
        /////////////////////////////////////////

        if (mAuth.getCurrentUser() != null) {

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() != 0) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("email").getValue().toString().equals(mAuth.getCurrentUser().getEmail())) {
                                Log.e("qweqweqwe", snapshot.child("role").getValue().toString());
                                path = snapshot.child("role").getValue(String.class);
                                userid = snapshot.getKey();

                                if (path.equals("captain")) {
                                    startActivity(new Intent(Login.this, Captain_DashBoard.class));
                                } else if (path.equals("battalion chief")) {

                                    FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.child("status").getValue(String.class).equals("open")) {
                                                        String incident_id_for_personnel = snapshot.getKey();
                                                        startActivity(new Intent(Login.this, Battalion_Chief_Dashboard.class)
                                                                .putExtra("incident_id_for_personnel",incident_id_for_personnel)
                                                        );
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    startActivity(new Intent(Login.this, No_Incident.class));

                                } else {
                                    FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() != 0) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    Log.e("personnel", snapshot.toString());

                                                    if (snapshot.child("personnel").getValue(String.class).contains(userid) && snapshot.child("status").getValue(String.class).equals("open")) {
                                                        Log.e("personnel123", snapshot.toString());
                                                        Toast.makeText(Login.this, "Welcome", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(Login.this, Personnel_Home.class));
                                                    }
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    startActivity(new Intent(Login.this, No_Incident.class));

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

        ////////////////////////////////
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById(R.id.login_password);

        signup = (Button) findViewById(R.id.signup);
        forgotpassword = (Button) findViewById(R.id.forgot);
        login = (Button) findViewById(R.id.login);

        pref = getSharedPreferences(mypref, MODE_PRIVATE);
        edit = pref.edit();
        String str = pref.getString("username", username);
        userinfo = FirebaseDatabase.getInstance().getReference("User");

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Signup.class));
            }
        });

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, ForgotPassword.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                username = email.getText().toString();
//                edit.putString("username", username);
//                edit.commit();

                eemail = email.getText().toString();
                ppassword = password.getText().toString();

                if ((TextUtils.isEmpty(eemail)) || !eemail.matches(Signup.EMAIL_PATTERN) || TextUtils.isEmpty(ppassword)) {
                    Toast.makeText(mContext, "Fields Empty Or Email Format Different", Toast.LENGTH_SHORT).show();
                } else {

                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w("qwe", "getInstanceId failed", task.getException());
                                        return;
                                    }

                                    // Get new Instance ID token
                                    token = task.getResult().getToken();

                                    // Log and toast
                                    String msg = getString(R.string.msg_token_fmt, token);
                                    Log.d("qwe", msg);
                                    Toast.makeText(Login.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            });


                    mAuth.signInWithEmailAndPassword(eemail, ppassword).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (task.isSuccessful()) {
                                try {
                                    if (user.isEmailVerified()) {

                                        myref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.child("email").getValue(String.class).equals(eemail)) {

                                                        snapshot_parent = snapshot.getKey();
                                                        values_for_profile = snapshot.child("email").getValue(String.class);

                                                        username = snapshot.child("name").getValue(String.class);
                                                        jobtitle = snapshot.child("job_title").getValue(String.class);
                                                        firestation = snapshot.child("firestation").getValue(String.class);
                                                        who_is_the_user = snapshot.child("role").getValue(String.class);
                                                        alert_token = token;

                                                        edit.putString("values_for_profile", values_for_profile);
                                                        edit.putString("user_id", snapshot_parent);
                                                        edit.putString("user_role", who_is_the_user);
                                                        edit.putString("username", username);
                                                        edit.putString("jobtitle", jobtitle);
                                                        edit.putString("firestation", firestation);
                                                        edit.putString("alert_token", alert_token);
                                                        edit.commit();

                                                        email.getText().clear();
                                                        password.getText().clear();
                                                        email.requestFocus();

                                                        if (who_is_the_user.equals("captain")) {
                                                            HashMap map = new HashMap();
                                                            map.put("token", token);
                                                            Log.e("shubham1", token);
                                                            userinfo.child(Login.snapshot_parent).updateChildren(map);
                                                            Toast.makeText(Login.this, "Welcome", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(Login.this, Captain_DashBoard.class);
                                                            startActivity(intent);
                                                        } else if (who_is_the_user.equals("battalion chief")) {

                                                            FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.child("status").getValue(String.class).equals("open")) {
                                                                                String incident_id_for_personnel = snapshot.getKey();
                                                                                startActivity(new Intent(Login.this, Battalion_Chief_Dashboard.class)
                                                                                .putExtra("incident_id_for_personnel",incident_id_for_personnel)
                                                                                );
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });

                                                            startActivity(new Intent(Login.this, No_Incident.class));


                                                        } else {

                                                            Toast.makeText(Login.this, "Welcome", Toast.LENGTH_SHORT).show();


                                                            HashMap map = new HashMap();
                                                            map.put("token", token);
                                                            Log.e("shubham2", token);
                                                            userinfo.child(Login.snapshot_parent).updateChildren(map);


                                                            FirebaseDatabase.getInstance().getReference("Incident").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            Log.e("personnel", snapshot.toString());

                                                                            if (snapshot.child("personnel").getValue(String.class).contains(snapshot_parent) && snapshot.child("status").getValue(String.class).equals("open")) {
                                                                                Log.e("personnel123", snapshot.toString());
                                                                                Toast.makeText(Login.this, "Welcome", Toast.LENGTH_SHORT).show();
                                                                                startActivity(new Intent(Login.this, Personnel_Home.class));
                                                                            }
                                                                        }

                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });

                                                            startActivity(new Intent(Login.this, No_Incident.class));


//
//                                                            Intent intent = new Intent(Login.this, No_Incident.class);
//                                                            startActivity(intent);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError
                                                                            databaseError) {

                                            }
                                        });
                                    } else {
                                        Toast.makeText(Login.this, "Email is not verified \n check your email inbox.", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().signOut();
                                    }
                                } catch (NullPointerException e) {

                                }
                            } else {
                                Toast.makeText(Login.this, "Authentication failed, check your email and password or sign up", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


}
