package com.example.trailerbackerupperapp;

import static java.lang.String.format;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Sensor;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trailerbackerupper.R;
import com.example.trailerbackerupperapp.customwidgets.ArrowsView;

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
        Thread clientmaker = new Thread(new Runnable(){
            public void run(){
                new Client("172.17.61.115", 5000);
            }


        });
        clientmaker.start();

        initializeGyroscope();
        arrowsView = findViewById(R.id.ArrowsView); /* the ArrowView element being assigned here is an object that seems to be
        instantiated in assissted_mode.xml using the ArrowsView.java class */
        

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
