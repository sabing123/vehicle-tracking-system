package com.example.vehicle_tracking_system;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.vehicle_tracking_system.login.DriverLoginRegisterActivity;
import com.example.vehicle_tracking_system.login.userloginregisterActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void open_driver(View V) {

        startActivity(new Intent(MainActivity.this, DriverLoginRegisterActivity.class));
    }

    public void open_passenger(View V) {
        startActivity(new Intent(MainActivity.this, userloginregisterActivity.class));
    }
}