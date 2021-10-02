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
import com.example.vehicle_tracking_system.ui.UserMapsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class userloginregisterActivity extends AppCompatActivity {

    private EditText input_user_email, input_user_password;
    private TextView link_user_register, user_Status;
    private Button btn_user_signup, btn_user_login;

    private FirebaseAuth mauth;
    private ProgressDialog dialog;

    private DatabaseReference UserDatabaseRef;
    private String onlineUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userloginregister);
        mauth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);

        input_user_email = findViewById(R.id.input_customer_email);
        input_user_password = findViewById(R.id.input_customer_password);

        //login system
        user_Status = findViewById(R.id.customer_Status);
        link_user_register = findViewById(R.id.link_customer_Register);

        btn_user_signup = findViewById(R.id.btn_customer_signup);
        btn_user_login = findViewById(R.id.btn_customer_login);


        btn_user_signup.setVisibility(View.INVISIBLE);
        btn_user_signup.setEnabled(false);

        link_user_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_user_login.setVisibility(View.INVISIBLE);
                user_Status.setText("User Register System");

                link_user_register.setVisibility(View.INVISIBLE);
                btn_user_signup.setVisibility(View.VISIBLE);
                btn_user_signup.setEnabled(true);
            }
        });

        btn_user_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Cemail = input_user_email.getText().toString();
                String Cpassword = input_user_password.getText().toString();

                UserRegister(Cemail, Cpassword);
            }
        });

        btn_user_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Cemail = input_user_email.getText().toString();
                String Cpassword = input_user_password.getText().toString();

                UserLogin(Cemail, Cpassword);
            }
        });
    }


    private void UserRegister(String cemail, String cpassword) {
        if (TextUtils.isEmpty(cemail)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cpassword)) {
            Toast.makeText(this, "Please enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            dialog.setIndeterminate(true);
            dialog.setTitle("Loading...");
            dialog.setMessage("Please Wait, Until completing Registration Finished ");
            dialog.show();

            mauth.createUserWithEmailAndPassword(cemail, cpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                onlineUserID = mauth.getCurrentUser().getUid();
                                UserDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Passengers").child(onlineUserID);
                               UserDatabaseRef.setValue(true);

                                Intent driverIntent = new Intent(userloginregisterActivity.this, UserMapsActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(userloginregisterActivity.this, "Successfully Register User Information...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(userloginregisterActivity.this, "Failed to Register Details. Please Try Again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
        }

    }


    private void UserLogin(String cemail, String cpassword) {

        if (TextUtils.isEmpty(cemail)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cpassword)) {
            Toast.makeText(this, "Please enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            dialog.setIndeterminate(true);
            dialog.setTitle("User Login");
            dialog.setMessage("Loading...");
            dialog.show();

            mauth.signInWithEmailAndPassword(cemail, cpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(userloginregisterActivity.this, UserMapsActivity.class));
                                Toast.makeText(userloginregisterActivity.this, "Successfully Login...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                Intent intent = new Intent(userloginregisterActivity.this, ProfileActivity.class);
                                intent.putExtra("type", "Customers");
                                startActivity(intent);

                            } else {
                                Toast.makeText(userloginregisterActivity.this, "Failed to Login User. Please Try Again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
        }
    }

}