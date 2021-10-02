package com.example.vehicle_tracking_system;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vehicle_tracking_system.ui.DiverMapsActivity;
import com.example.vehicle_tracking_system.ui.UserMapsActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class ProfileActivity extends AppCompatActivity {

    private String getType;

    private CircleImageView profileImageView;
    private EditText NameEditText, phoneEditText, DriverbusName, DriverbusNumber;
    private ImageView btn_close, btn_save;
    private TextView btnprofile;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private String Checker = "";
    private Uri imageuri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getType = getIntent().getStringExtra("type");

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);
        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");


        profileImageView = findViewById(R.id.profile_image);

        NameEditText = findViewById(R.id.user_name);
        phoneEditText = findViewById(R.id.contact_num);
        DriverbusName = findViewById(R.id.driver_car_name);
        DriverbusNumber = findViewById(R.id.driver_bus_number);

        if (getType.equals("Drivers")) {
            DriverbusName.setVisibility(View.VISIBLE);
            DriverbusNumber.setVisibility(View.VISIBLE);
        }

        btn_close = findViewById(R.id.btn_close);
        btn_save = findViewById(R.id.btn_save);
        btnprofile = findViewById(R.id.btn_change_pp);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getType.equals("Drivers")) {
                    startActivity(new Intent(ProfileActivity.this, DiverMapsActivity.class));
                } else {
                    startActivity(new Intent(ProfileActivity.this, UserMapsActivity.class));
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Checker.equals("clicked")) {
                    ValidateController();

                } else {
                    validateandSaveInformation();
                }
            }
        });

        btnprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Checker = "clicked";
                CropImage.activity().setAspectRatio(1, 1).start(ProfileActivity.this);
            }
        });
        getUserInformation();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageuri = result.getUri();
            profileImageView.setImageURI(imageuri);
        } else {

            if (getType.equals("Drivers")) {
                startActivity(new Intent(ProfileActivity.this, DiverMapsActivity.class));
            } else {
                startActivity(new Intent(ProfileActivity.this, UserMapsActivity.class));
            }
            Toast.makeText(this, "Error! Try Again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void ValidateController() {
        if (TextUtils.isEmpty(NameEditText.getText().toString())) {
            Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneEditText.getText().toString())) {
            Toast.makeText(this, "Please Provide Your Contact Number", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(DriverbusName.getText().toString())) {
            Toast.makeText(this, "Please Enter Your Bus Name", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(DriverbusNumber.getText().toString())) {
            Toast.makeText(this, "Please Enter Your Bus Number", Toast.LENGTH_SHORT).show();
        } else if (Checker.equals("clicked")) {
            uploadProfilePictures();
        }
    }

    private void uploadProfilePictures() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating User Information");
        progressDialog.setMessage("Please wait, while updating your information");
        progressDialog.show();
        if (imageuri != null) {
            final StorageReference fileRef = storageProfilePicRef
                    .child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageuri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", mAuth.getCurrentUser().getUid());
                        userMap.put("name", NameEditText.getText().toString());
                        userMap.put("phone", phoneEditText.getText().toString());
                        userMap.put("image", myUrl);

                        if (getType.equals("Drivers")) {
                            userMap.put("BusName", DriverbusName.getText().toString());
                            userMap.put("BusNumber", DriverbusNumber.getText().toString());
                        }

                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                        progressDialog.dismiss();
                        if (getType.equals("Drivers")) {

                            startActivity(new Intent(ProfileActivity.this, DiverMapsActivity.class));
                        } else {
                            startActivity(new Intent(ProfileActivity.this, UserMapsActivity.class));
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please Select Image..", Toast.LENGTH_SHORT).show();
        }

    }


    private void validateandSaveInformation() {

        if (TextUtils.isEmpty(NameEditText.getText().toString())) {
            Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneEditText.getText().toString())) {
            Toast.makeText(this, "Please Provide Your Contact Number", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(DriverbusName.getText().toString())) {
            Toast.makeText(this, "Please Enter Your Bus Name", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(DriverbusNumber.getText().toString())) {
            Toast.makeText(this, "Please Enter Your Bus Number", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("name", NameEditText.getText().toString());
            userMap.put("phone", phoneEditText.getText().toString());

            if (getType.equals("Drivers")) {
                userMap.put("BusName", DriverbusName.getText().toString());
                userMap.put("BusNumber", DriverbusNumber.getText().toString());
            }

            databaseReference.child(mAuth.getCurrentUser().getUid())
                    .updateChildren(userMap);
            if (getType.equals("Drivers")) {
                startActivity(new Intent(ProfileActivity.this, DiverMapsActivity.class));
            } else {
                startActivity(new Intent(ProfileActivity.this, UserMapsActivity.class));
            }
        }
    }


    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();

                    NameEditText.setText(name);
                    phoneEditText.setText(phone);

                    if (getType.equals("Drivers")) {
                        String busName = snapshot.child("BusName").getValue().toString();
                        String busNumber = snapshot.child("BusNumber").getValue().toString();
                        DriverbusName.setText(busName);
                        DriverbusNumber.setText(busNumber);
                    }

                    if (snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}