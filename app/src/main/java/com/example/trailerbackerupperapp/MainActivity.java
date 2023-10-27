package com.example.trailerbackerupperapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trailerbackerupper.R;
import com.example.trailerbackerupperapp.customwidgets.ArrowsView;
import com.example.trailerbackerupperapp.customwidgets.TrailerCameraView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrowsView arrowsView; /* a view object is created on the window, showing the navigation guidance arrows,
     the name of the object is the name of the class wherein it is defined and also the name of the element as referenced
     in assisted_mode.xml */
    private boolean debug = false;
    private GyroDetector gyro;
    ArrayList<TextView> debugViews;

    private Button gasButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assisted_mode); /* this is the mode with the guidance arrows aka ArrowsView */
        Thread clientmaker = new Thread(() -> new Client("172.17.61.115", 5000));
        clientmaker.start();
        setTestImage();
        initializeGyroscope();
        arrowsView = findViewById(R.id.ArrowsView); /* the ArrowView element being assigned here is an object that seems to be
        instantiated in assissted_mode.xml using the ArrowsView.java class */
        

    }


    private void setTestImage() {
        TrailerCameraView t = findViewById(R.id.TrailerCameraView);
        try {
            ContextWrapper cw = new ContextWrapper(this);

            //path to /data/data/yourapp/app_data/dirName
            File directory = cw.getDir("testTrailerImage.jpg", Context.MODE_PRIVATE);
            FileInputStream fis = new FileInputStream(directory);
            char[] imageBytes = new char[300*1000];
            int b;
            for(int i = 0; (b = fis.read()) != -1; i++){
                imageBytes[i] = (char)b;
            }
            t.setCurrentImage(imageBytes);
        }catch(IOException e){
            Log.e("Image not found", "could not find image: sampledata/testTrailerImage.jpg");
        }
    }


    public void initializeGyroscope(){

        debugViews =new ArrayList<>(); /* these are to be the textboxes on the display which display gyroscope information */

        debugViews.add(findViewById(R.id.roll));
        debugViews.add(findViewById(R.id.pitch));
        debugViews.add(findViewById(R.id.yaw));

        gyro = new GyroDetector(this);
        SensorManager mgr = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        mgr.registerListener(
                gyro,
                mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI
        );
        mgr.registerListener(
                gyro,
                mgr.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_UI
        );
    }



    public void toggle_debug(){
        debug = !debug;
        for(TextView t: debugViews){
            if(debug)
                t.setVisibility(View.GONE);
            else
                t.setVisibility(View.VISIBLE);

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            toggle_debug();
            return true;
        }
        return false;
    }
    public void update_orientation(){
        //Log.d("Updating UI", "Updating orientation");
        float[] values = gyro.getPhoneRotation();
        double yaw = values[0] *(360/(2* Math.PI));
        double pitch = values[1]  *(360/(2* Math.PI));
        double roll = values[2] *(360/(2* Math.PI));
        debugViews.get(0).setText(String.format("yaw z: %.2f ", yaw));
        debugViews.get(1).setText(String.format("pitch x:%.2f ", pitch));
        debugViews.get(2).setText(String.format("roll y: %.2f ", roll));

        arrowsView.setTrueArrowAngle(pitch/100);
    }


    public void gas_pressed(View view){

        Log.d("Buttons", "Gas pressed!");
        //arrowsView.rotateArrowsContinuously();
    }

    public void brake_pressed(View view){
        //arrowsView.stopRotating();
    }

    public void forward_pressed(View view){
        Log.d("Buttons", "Forward pressed!");


    }



    public void onPush(){

    }
    public void onRelease(){

    }
}
