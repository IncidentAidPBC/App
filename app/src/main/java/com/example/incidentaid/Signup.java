package com.example.incidentaid;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class Signup extends AppCompatActivity implements Serializable {

    private Context mContext;
    private Button register;
    private EditText uniq_id, email, password;


    private String uuniq_id, eemail, ppassword;
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "gmail.com";
    public static final String PASSWORD_PATTERN = "^.*(?=.{4,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$";

    private DatabaseReference myRef, myRef1, myRef2;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Method method;

    private static final String TAG = "Register";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_signup);


        mContext = Signup.this;
        method = new Method(mContext);
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("User");

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        uniq_id = (EditText) findViewById(R.id.uniq_id);
        register = (Button) findViewById(R.id.register);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                eemail = email.getText().toString().trim();
                ppassword = password.getText().toString();
                uuniq_id = uniq_id.getText().toString();
                myRef1 = myRef.child(uuniq_id);
                myRef2 = myRef1.child("email");

                // Log.e("qweqweqwe", eemail + ppassword + uuniq_id);


                if (TextUtils.isEmpty(uuniq_id)) {
                    Toast.makeText(getApplicationContext(), "Enter Your ID", Toast.LENGTH_SHORT).show();
                    uniq_id.requestFocus();
                    return;
                }

                if (uuniq_id.length() != 5) {
                    Toast.makeText(getApplicationContext(), "ID must be a 5 digit", Toast.LENGTH_SHORT).show();
                    uniq_id.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(eemail)) {
                    Toast.makeText(getApplicationContext(), "Enter Your Email", Toast.LENGTH_SHORT).show();
                    email.requestFocus();
                    return;
                }

                if (!eemail.matches(EMAIL_PATTERN)) {
                    Toast.makeText(getApplicationContext(), "Enter Valid Email", Toast.LENGTH_SHORT).show();
                    email.requestFocus();
                    return;
                }

//                if (TextUtils.isEmpty(ppassword)) {
//                    Toast.makeText(getApplicationContext(), "Password Field Empty", Toast.LENGTH_SHORT).show();
//                    password.requestFocus();
//                    return;
//                }
//
//                if (!ppassword.matches(PASSWORD_PATTERN)) {
//                    Toast.makeText(getApplicationContext(), "Password Error Must Contain Capital, Small, Number, Special Character Each", Toast.LENGTH_LONG).show();
//                    password.requestFocus();
//                    return;
//                }

//                register.setEnabled(false);


                myRef.child(uuniq_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Log.e("asd",dataSnapshot.child("email").getValue(String.class)+"");
                        if (dataSnapshot.child("email").getValue(String.class) == null) {
                            // Log.e("asd", "ID or Email Not Found");
                            Toast.makeText(getApplicationContext(), "ID or Email Not Found", Toast.LENGTH_SHORT).show();
                        } else if (dataSnapshot.child("email").getValue(String.class).equals(eemail)) {
                            // Log.e("asd", "email found");
                            method.registerNewEmail(uuniq_id, eemail, ppassword);
                            Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
                        } else {
                            // Log.e("asd", "ID or Email Not Found");
                            Toast.makeText(getApplicationContext(), "ID or Email Not Found", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
//                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            if (snapshot.getKey().toString().equals(uuniq_id)) {
                               // Log.e("asdasd", "yes " + " " + uuniq_id + " " + snapshot.getKey());
                               // Log.e("asdasd1", snapshot.child(uuniq_id));
//                            } else {
                               // Log.e("asdasd", "no" + " " + uuniq_id + " " + snapshot.getKey());
//                            }
//                        }
//
                       // Log.e("asdasd",dataSnapshot.child(uuniq_id).child("email").getValue().toString());
//
////                        if (dataSnapshot.child(uuniq_id).child("email").getValue().toString().equals(eemail)) {
////                            method.registerNewEmail(uuniq_id, eemail, ppassword);
////                            Toast.makeText(getApplicationContext(), "welcome", Toast.LENGTH_SHORT).show();
////                        } else {
////                            Toast.makeText(getApplicationContext(), "ID or Email Not Present", Toast.LENGTH_SHORT).show();
////                            return;
////                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
