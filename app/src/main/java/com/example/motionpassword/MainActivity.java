package com.example.motionpassword;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.motionpassword.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    final String TAG = "dev";

    private ActivityMainBinding binding;

    private Sensor accelerometer;
    private SensorManager mSensorManager;

    private int[] password = {1, 2, 1};
    private int[] input = new int[3];
    private int currentIndex = 0;

    private boolean searching = true;

    private Runnable timer;
    private int currentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        binding.progressBar.setMax(5);
        binding.progressBar.setProgress(0);

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateSensor();
                binding.button.setVisibility(View.GONE);
                binding.progressBar.post(timer);
                binding.textView.setText("0 0 0");
            }
        });

        timer = new Runnable() {
            @Override
            public void run() {
                if(currentTime < 6){
                    binding.progressBar.setProgress(currentTime++);
                    binding.progressBar.postDelayed(this, 1000);
                }
                else {
                    if(currentIndex < 2){
                        currentIndex++;
                        currentTime = 0;
                        binding.progressBar.post(this);
                    }
                    else {
                        binding.progressBar.setProgress(0);
                        currentIndex = 0;
                        currentTime = 0;
                        binding.button.setVisibility(View.VISIBLE);
                        if(Arrays.equals(input, password)) showSnackbar("Password is correct");
                        else showSnackbar("Password is incorrect! Try again");
                        input[0] = input[1] = input[2] = 0;
                    }
                }
            }
        };
    }

    public void activateSensor() {
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(accelerometer != null){
            mSensorManager.registerListener(this,  accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else Toast.makeText(this, "No sensor found!", Toast.LENGTH_SHORT).show();
    }

    public void deactivateSensor() {
        mSensorManager.unregisterListener(this);
    }

    public void showSnackbar(String message) {
       Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT);
       snackbar.show();
    }

    public void detectMotion() {
        input[currentIndex]++;
        String digit1 = input[0] == 0? "0 " : input[0] + " ";
        String digit2 = input[1] == 0? "0 " : input[1] + " ";
        String digit3 = input[2] == 0? "0" : input[2] + "";

        binding.textView.setText(digit1+digit2+digit3);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                float reading = sensorEvent.values[0]/sensorEvent.sensor.getMaximumRange()*100;
                if(searching){
                    if (Math.abs(reading) > 70) {
                        detectMotion();
                        searching = false;
                    }
                }
                else {
                    searching = reading < 1;
                }

                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        deactivateSensor();
    }
}