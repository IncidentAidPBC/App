package com.example.incidentaid;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText email;
    private Button send_email;
    private FirebaseAuth auth;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_forgotpassword);

        email = (EditText) findViewById(R.id.email);
        send_email = (Button) findViewById(R.id.btn_reset_password);
        auth = FirebaseAuth.getInstance();

        send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user_email = email.getText().toString().trim();

                if (TextUtils.isEmpty(user_email)) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    email.requestFocus();
                    return;
                }

                if (!user_email.matches(Signup.EMAIL_PATTERN)) {
                    Toast.makeText(getApplicationContext(), "Please Check The Format Of Email", Toast.LENGTH_SHORT).show();
                    email.requestFocus();
                    return;
                }

                auth.sendPasswordResetEmail(user_email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPassword.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(ForgotPassword.this, Login.class));
                                } else {
                                    Toast.makeText(ForgotPassword.this, "Incorrect Email Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
