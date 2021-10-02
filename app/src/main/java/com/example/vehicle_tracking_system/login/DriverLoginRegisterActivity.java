package com.example.vehicle_tracking_system.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vehicle_tracking_system.ProfileActivity;
import com.example.vehicle_tracking_system.R;
import com.example.vehicle_tracking_system.ui.DiverMapsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginRegisterActivity extends AppCompatActivity {

    private EditText input_driver_email, input_driver_password;
    private Button btn_driver_signin, btn_driver_Register;
    private TextView link_driver_register, driver_status;

    private FirebaseAuth mauth;
    private ProgressDialog dialog;

    private DatabaseReference DriverDatabaseRef;
    private String onlineDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);


        mauth = FirebaseAuth.getInstance();



        dialog = new ProgressDialog(this);
        input_driver_email = findViewById(R.id.input_driver_email);
        input_driver_password = findViewById(R.id.input_driver_password);


        btn_driver_signin = findViewById(R.id.btn_driver_signin);
        btn_driver_Register = findViewById(R.id.btn_driver_Register);


        link_driver_register = findViewById(R.id.link_driver_register);
        driver_status = findViewById(R.id.driver_status);


        btn_driver_Register.setVisibility(View.INVISIBLE);
        btn_driver_Register.setEnabled(false);

        link_driver_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_driver_signin.setVisibility(View.INVISIBLE);
                link_driver_register.setVisibility(View.INVISIBLE);

                driver_status.setText("Driver Register System");
                btn_driver_Register.setVisibility(View.VISIBLE);
                btn_driver_Register.setEnabled(true);

            }
        });

//Registration Diver
        btn_driver_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Demail = input_driver_email.getText().toString();
                String Dpassword = input_driver_password.getText().toString();

                RegisterDriver(Demail, Dpassword);

            }
        });

//        login Driver
        btn_driver_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Demail = input_driver_email.getText().toString();
                String Dpassword = input_driver_password.getText().toString();
                DriverLogin(Demail,Dpassword);
            }
        });


    }
    private void DriverLogin(String demail, String dpassword) {
        if (TextUtils.isEmpty(demail)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(dpassword)) {
            Toast.makeText(this, "Please enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            dialog.setTitle("Driver Login");
            dialog.setMessage("Loading... ");
            dialog.show();

            mauth.signInWithEmailAndPassword(demail, dpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(DriverLoginRegisterActivity.this, "Successfully Login...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                Intent intent = new Intent(DriverLoginRegisterActivity.this, ProfileActivity.class);
                                intent.putExtra("type","Drivers");
                                startActivity(intent);

                            } else {
                                Toast.makeText(DriverLoginRegisterActivity.this, "Failed to Login. Please Try Again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
        }
    }
    private void RegisterDriver(String demail, String dpassword) {
        if (TextUtils.isEmpty(demail)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(dpassword)) {
            Toast.makeText(this, "Please enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            dialog.setTitle("Loading...");
            dialog.setMessage("Please Wait, Until completing Registration Finished ");
            dialog.show();

            mauth.createUserWithEmailAndPassword(demail, dpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                onlineDriverID = mauth.getCurrentUser().getUid();
                                DriverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Drivers").child(onlineDriverID);

                                DriverDatabaseRef.setValue(true);
                                startActivity(new Intent(DriverLoginRegisterActivity.this, DiverMapsActivity.class));
                                Toast.makeText(DriverLoginRegisterActivity.this, "Successfully Register Driver Information...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                            } else {
                                Toast.makeText(DriverLoginRegisterActivity.this, "Failed to Register Details. Please Try Again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
        }

    }


}