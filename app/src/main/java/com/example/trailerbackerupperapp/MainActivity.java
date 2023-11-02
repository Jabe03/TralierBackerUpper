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
        instantiated in assisted_mode.xml using the ArrowsView.java class */
        

    }




    public void initializeGyroscope(){

        debugViews =new ArrayList<>(); /* these are to be the textboxes on the display which display gyroscope information */

        debugViews.add(findViewById(R.id.roll)); /* Roll is the rotation of an object around its longitudinal (or "roll") axis.
        The longitudinal axis is an imaginary line that runs from the front to the back of the object, passing through its center.
        When an object rolls, it tilts from side to side, similar to how a ship rolls on the waves or an airplane banks during a turn. */
        debugViews.add(findViewById(R.id.pitch)); /* Pitch is the rotation of an object around its lateral (or "pitch") axis.
        The lateral axis is an imaginary line that runs from one side of the object to the other, passing through its center.
        When an object pitches, it tilts forward or backward, causing its nose or tail to move up or down. */
        debugViews.add(findViewById(R.id.yaw)); /* Yaw is the rotation of an object around its vertical (or "yaw") axis.
        The vertical axis is an imaginary line that runs vertically through the object's center. When an object yaws,
        it rotates around this axis, causing it to turn left or right. */

        gyro = new GyroDetector(this);
        SensorManager mgr = (SensorManager)this.getSystemService(SENSOR_SERVICE); /* a Sensor Manager instance is created by the
        getSystemService method of this instance of the MainActivity class */
        mgr.registerListener(
                gyro,
                mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI
        ); /* This line registers a listener for sensor events related to the magnetic field sensor. Here's what each part does:
        gyro is the GyroDetector instance created earlier. It will be the object that listens to sensor events for the magnetic field.
        mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) gets the default magnetic field sensor for the device from the Sensor Manager
            instance.
        SensorManager.SENSOR_DELAY_UI specifies the rate at which the sensor events will be delivered.
            In this case, it's set to SENSOR_DELAY_UI, which is a relatively fast update rate suitable for user interface interaction.
        */
        mgr.registerListener(
                gyro,
                mgr.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_UI
        ); /*  Similarly, this line registers a listener for sensor events related to the gravity sensor.
        The parameters are similar to the previous line. */
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
        float[] values = gyro.getPhoneRotation(); /* gyro is the instance of the GyroDetector class, which has a getPhoneRotation method
        which returns the angles array containing the latest yaw, pitch, and roll angles from the sensor output */
        double yaw = values[0] *(360/(2* Math.PI)); /* left or right turn */
        double pitch = values[1]  *(360/(2* Math.PI)); /* forward or backward tilt */
        double roll = values[2] *(360/(2* Math.PI)); /* side to side tilt */
        debugViews.get(0).setText(String.format("yaw z: %.2f ", yaw));
        debugViews.get(1).setText(String.format("pitch x:%.2f ", pitch));
        debugViews.get(2).setText(String.format("roll y: %.2f ", roll));

        arrowsView.setTrueArrowAngle(pitch/100); /* true arrow represents the current angle of the vehicle */
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
