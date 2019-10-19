package com.example.incidentaid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recruit extends AppCompatActivity {

    private Context myContext;
    private Method method;
    private EditText name, address, email, pincode;
    private Button register, qualification, job_title;
    private Spinner firestation;
    private TextView selected_qualification, selected_job_title;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef, myRef1, myRef2, myRef3;
    private FirebaseDatabase mFirebaseDatabase;

    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "gmail.com"; // "scu.edu";

    private String nname, eemail, aaddress, ppincode, qqualification, jjob_title, ffirestation;

    List<String> all_qualification = new ArrayList<>();
    List<String> all_job_title = new ArrayList<>();

    String[] listItems;
    boolean[] checkedItems;

    String[] listItems1;
    boolean[] checkedItems1;


    ArrayList<Integer> mUserItems = new ArrayList<>();
    ArrayList<Integer> mUserItems1 = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_destop_interface);

        myContext = Recruit.this;
        method = new Method(myContext);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        myRef = FirebaseDatabase.getInstance().getReference("Qualification");
        myRef1 = FirebaseDatabase.getInstance().getReference("Job_Title");
        myRef2 = FirebaseDatabase.getInstance().getReference("User");
        myRef3 = FirebaseDatabase.getInstance().getReference("Fire_Station");

        name = (EditText) findViewById(R.id.name);
        address = (EditText) findViewById(R.id.address);
        pincode = (EditText) findViewById(R.id.pincode);
        email = (EditText) findViewById(R.id.email);
        qualification = (Button) findViewById(R.id.qualification);
        job_title = (Button) findViewById(R.id.job_title);
        firestation = (Spinner) findViewById(R.id.firestation);
        register = (Button) findViewById(R.id.register);
        selected_qualification = (TextView) findViewById(R.id.selected_qualification);
        selected_job_title = (TextView) findViewById(R.id.selected_job_title);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> product = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    product.add(snapshot.getKey());
                }

                String[] products = new String[product.size()];
                product.toArray(products);
                // Log.e("asasas1", String.valueOf(product));
                // Log.e("arr", Arrays.toString(products));

                ArrayList<Single_item> list = new ArrayList<>();

                listItems = products;
                checkedItems = new boolean[listItems.length];
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> product = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    product.add(snapshot.getKey());
                }

                String[] products = new String[product.size()];
                product.toArray(products);
                // Log.e("asasas1", String.valueOf(product));


                ArrayList<Single_item> list = new ArrayList<>();

                listItems1 = products;
                checkedItems1 = new boolean[listItems1.length];
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> product = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    product.add(snapshot.getKey() + " " + snapshot.child("Address").getValue(String.class));
                }

                String[] products = new String[product.size()];
                product.add(0, "Select Fire Station");
                product.toArray(products);
                // Log.e("asasas1", String.valueOf(product));

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Recruit.this, android.R.layout.simple_spinner_item, product) {
                    @Override
                    public boolean isEnabled(int position) {
                        if (position == 0)
                            return false;
                        else
                            return true;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View spinnerview = super.getDropDownView(position, convertView, parent);
                        TextView spinnertextview = (TextView) spinnerview;
                        if (position == 0)
                            spinnertextview.setTextColor(Color.parseColor("#bcbcbb"));
                        else
                            spinnertextview.setTextColor(Color.BLACK);
                        return spinnerview;
                    }
                };
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                firestation.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        qualification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Recruit.this);
                mBuilder.setTitle("Select Qualification");
                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        if (isChecked) {
                            mUserItems.add(position);
                        } else {
                            mUserItems.remove((Integer.valueOf(position)));
                        }
                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String item = "";
                        for (int i = 0; i < mUserItems.size(); i++) {
                            item = item + listItems[mUserItems.get(i)];
                            if (i != mUserItems.size() - 1) {
                                item = item + ", ";
                            }
                        }

                        selected_qualification.setText(item);
                    }
                });

                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            checkedItems[i] = false;
                            mUserItems.clear();
                            selected_qualification.setText("");
                        }
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });


        job_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Recruit.this);
                mBuilder.setTitle("Select Job Title");
                mBuilder.setMultiChoiceItems(listItems1, checkedItems1, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        if (isChecked) {
                            mUserItems1.add(position);
                        } else {
                            mUserItems1.remove((Integer.valueOf(position)));
                        }
                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String item = "";
                        for (int i = 0; i < mUserItems1.size(); i++) {
                            item = item + listItems1[mUserItems1.get(i)];
                            if (i != mUserItems1.size() - 1) {
                                item = item + ", ";
                            }
                        }

                        selected_job_title.setText(item);
                    }
                });

                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < checkedItems1.length; i++) {
                            checkedItems1[i] = false;
                            mUserItems1.clear();
                            selected_job_title.setText("");
                        }
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nname = name.getText().toString().trim().toLowerCase();
                aaddress = address.getText().toString().trim();
                ppincode = pincode.getText().toString().trim();
                eemail = email.getText().toString().trim();
                qqualification = selected_qualification.getText().toString();
                jjob_title = selected_job_title.getText().toString();
                ffirestation = firestation.getSelectedItem().toString().substring(0,14);


                // Log.e("123", jjob_title);

//                if (TextUtils.isEmpty(nname)) {
//                    Toast.makeText(getApplicationContext(), "Enter Your First Name", Toast.LENGTH_SHORT).show();
//                    name.requestFocus();
//                    return;
//                }
//
//                if (TextUtils.isEmpty(eemail)) {
//                    Toast.makeText(getApplicationContext(), "Enter Your Email", Toast.LENGTH_SHORT).show();
//                    email.requestFocus();
//                    return;
//                }
//
//                if (!eemail.matches(EMAIL_PATTERN)) {
//                    Toast.makeText(getApplicationContext(), "Enter Valid Email", Toast.LENGTH_SHORT).show();
//                    email.requestFocus();
//                    return;
//                }
//
//                if (TextUtils.isEmpty(aaddress)) {
//                    Toast.makeText(getApplicationContext(), "Address Field Empty", Toast.LENGTH_SHORT).show();
//                    address.requestFocus();
//                    return;
//                }
//
//                if (TextUtils.isEmpty(ppincode)) {
//                    Toast.makeText(getApplicationContext(), "PinCode Field Empty", Toast.LENGTH_SHORT).show();
//                    pincode.requestFocus();
//                    return;
//                }
//
//                if (ppincode.length() != 5) {
//                    Toast.makeText(getApplicationContext(), "PinCode Invalid", Toast.LENGTH_SHORT).show();
//                    pincode.requestFocus();
//                    return;
//                }
//
//                if (TextUtils.isEmpty(qqualification)) {
//                    Toast.makeText(getApplicationContext(), "Please Select Qualification", Toast.LENGTH_LONG).show();
//                    qualification.requestFocus();
//                    return;
//                }
//
//                if (TextUtils.isEmpty(jjob_title)) {
//                    Toast.makeText(getApplicationContext(), "Please Select Qualification", Toast.LENGTH_LONG).show();
//                    qualification.requestFocus();
//                    return;
//                }
//
//                if (ffirestation.equalsIgnoreCase("Select Fire Station")) {
//                    Toast.makeText(getApplicationContext(), "Please Select Fire Station", Toast.LENGTH_LONG).show();
//                    firestation.requestFocus();
//                    return;
//                }


                String temp[] = qqualification.split(", ");
                Arrays.sort(temp);

                StringBuilder sb = new StringBuilder();
                for (String str : temp) {
                    sb.append(str).append(", ");
                }
                qqualification = sb.toString();
                qqualification = qqualification.substring(0, qqualification.length() - 2);

                // Log.e("qqqqq", qqualification);


                //=============================
                String temp1[] = jjob_title.split(", ");
                Arrays.sort(temp1);

                StringBuilder sb1 = new StringBuilder();
                for (String str : temp1) {
                    sb1.append(str).append(", ");
                }
                jjob_title = sb1.toString();

                jjob_title = jjob_title.substring(0, jjob_title.length() - 2);

                // Log.e("qqqqq", jjob_title);


                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Recruit.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Recruit.this);
                }
                builder.setTitle("Confirmation")
                        .setMessage("Save the Personnel Information ?")
                        .setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                register.setEnabled(false);
                                // Log.e("qweqweqweqwe", qqualification);
                                // Log.e("qweqweqweqwe", jjob_title);
                                myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            if (ds.getChildrenCount() != 0) {
                                                if (ds.child("email").getValue(String.class).equals(eemail)) {
                                                    Toast.makeText(Recruit.this, "Email Already Present", Toast.LENGTH_SHORT).show();
                                                    for (int i = 0; i < checkedItems.length; i++) {
                                                        checkedItems[i] = false;
                                                        mUserItems.clear();
                                                        selected_qualification.setText("");
                                                    }
                                                    for (int i = 0; i < checkedItems1.length; i++) {
                                                        checkedItems1[i] = false;
                                                        mUserItems1.clear();
                                                        selected_job_title.setText("");
                                                    }
                                                    register.setEnabled(true);
                                                    return;
                                                }
                                            }
                                        }

                                        // Log.e("qwe", qqualification);
                                        // Log.e("qwe", jjob_title);

                                        method.registerNewRecruit(nname, eemail, qqualification, jjob_title, aaddress, ppincode, ffirestation);


                                        DatabaseReference myref = FirebaseDatabase.getInstance().getReference("User");
                                        myref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getChildrenCount() != 0) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        if (snapshot.child("firestation").getValue().toString().equals(ffirestation) &&
                                                                snapshot.child("role").getValue().toString().equals("captain")) {
                                                            String token = snapshot.child("token").getValue().toString();
                                                            String title = "New Recruit Added";
                                                            String msg = "New Personnel Added";
                                                            method.sendFCMPush(title, msg, token);

                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        name.setText("");
                                        address.setText("");
                                        pincode.setText("");
                                        email.setText("");
                                        for (int i = 0; i < checkedItems.length; i++) {
                                            checkedItems[i] = false;
                                            mUserItems.clear();
                                            selected_qualification.setText("");
                                        }
                                        for (int i = 0; i < checkedItems1.length; i++) {
                                            checkedItems1[i] = false;
                                            mUserItems1.clear();
                                            selected_job_title.setText("");
                                        }
                                        firestation.setSelection(0);
                                        register.setEnabled(true);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

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

