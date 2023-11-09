package com.example.trailerbackerupperapp;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.hardware.Sensor;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trailerbackerupper.R;
import com.example.trailerbackerupperapp.customwidgets.ArrowsView;
import com.example.trailerbackerupperapp.customwidgets.DebugLayout;

import java.util.ArrayList;

import Online.Client;

public class MainActivity extends AppCompatActivity {
    private ArrowsView arrowsView; /* a view object is created on the window, showing the navigation guidance arrows,
     the name of the object is the name of the class wherein it is defined and also the name of the element as referenced
     in assisted_mode.xml */
    private boolean debug;
    private GyroDetector gyro;
    private ImageView connectionDot;
    DebugLayout debugLayout;

    double gasVal;
    private static final int FORWARD = 1;
    private static final int REVERSE = -1;
    private static final int PARKED = 0;
    double gasDir;

    Client me;
    private Button gasButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assisted_mode); /* this is the mode with the guidance arrows aka ArrowsView */
        initDebug(false);
        initializeGasAndBrake();
        initializeGyroscope();
        setupClient();

        arrowsView = findViewById(R.id.ArrowsView); /* the ArrowView element being assigned here is an object that seems to be
        instantiated in assisted_mode.xml using the ArrowsView.java class */
    }


    public void initDebug(boolean debug){
        debugLayout = findViewById(R.id.debugLayout); /* these are to be the textboxes on the display which display gyroscope information */
        debugLayout.addDebugField("steeringAngle", "StrAng");
        debugLayout.addDebugField("gasValue", "gas");
       /* debugViews.add(findViewById(R.id.roll)); *//* Roll is the rotation of an object around its longitudinal (or "roll") axis.
        The longitudinal axis is an imaginary line that runs from the front to the back of the object, passing through its center.
        When an object rolls, it tilts from side to side, similar to how a ship rolls on the waves or an airplane banks during a turn. */
        /*debugViews.add(findViewById(R.id.pitch)); /* Pitch is the rotation of an object around its lateral (or "pitch") axis.
        The lateral axis is an imaginary line that runs from one side of the object to the other, passing through its center.
        When an object pitches, it tilts forward or backward, causing its nose or tail to move up or down. */
        /*debugViews.add(findViewById(R.id.yaw)); /* Yaw is the rotation of an object around its vertical (or "yaw") axis.
        The vertical axis is an imaginary line that runs vertically through the object's center. When an object yaws,
        it rotates around this axis, causing it to turn left or right. */

        debugLayout.setDebug(debug);
    }



    public void setupClient(){
        connectionDot = findViewById(R.id.connection_indicator);
        setConnectionIcon(false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        me = new Client("192.168.1.102", 1102);
        //172.17.50.27
        attemptToConnectClient();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initializeGasAndBrake(){
        gasDir = FORWARD;
        gasVal = 0;
        gasButton = findViewById(R.id.GasButton);
        gasButton.setOnTouchListener((v, event) -> {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    gasVal = gasDir * (v.getHeight()- event.getY())/v.getHeight();
                    System.out.println("Gas pressed!");
                    break;
                case MotionEvent.ACTION_UP:
                    gasVal = 0;
                    System.out.println("Gas released!");
                    break;
                case MotionEvent.ACTION_MOVE:
                    return false;
            }
            return true;
        });
    }

    public void attemptToConnectClient (){
        Thread clientmaker = new Thread(new Runnable(){
            public void run(){
                me.attemptConnection();
                me.startProcessingPackets();

                /*me.sendMessage(new Packet(
                        DefaultOnlineCommands.SIMPLE_TEXT,
                        "Sup guy",
                        me.getID()
                ));*/
                setConnectionIcon(true);
                //Log.d("Starting stream threads", "yay");
                startStreamThreads();
            }


        });
        clientmaker.start();
    }
    public void setConnectionIcon(boolean connected){
        runOnUiThread(()-> {
            if (connected) {
                connectionDot.setImageResource(R.drawable.connected_icon);
            } else {
                connectionDot.setImageResource(R.drawable.disconnected_icon);
            }
        });
    }
    private void startStreamThreads(){
        Thread sender = new Thread(()->{
            double sendRate = 60; /* this is the rate at which the view is refreshed while the app is running */
            long last = System.currentTimeMillis();
            while(me.isRunning()){
                long now = System.currentTimeMillis();
                if(now - last >= 1000/sendRate){ /* 1000 milliseconds is equal to 1 second, the contained code executes every 1/60 second */
                    double reading = Math.toDegrees(arrowsView.getSteeringAngle());
                    me.sendGyroReading(reading);
                    me.sendGasReading(gasVal);
                    debugLayout.setText("gasVal", ""+ gasVal);
                    debugLayout.setText("steeringAngle", "" + reading);
                    last = now;
                }
            }
            setConnectionIcon(false);
            attemptToConnectClient();
        });


        sender.start();
    }


    public void initializeGyroscope(){


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




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            debugLayout.setDebug(!debug);
            return true;
        }
        return false;
    }
    public void update_orientation(){
        //Log.d("Updating UI", "Updating orientation");
        float[] values = gyro.getPhoneRotation(); /* gyro is the instance of the GyroDetector class, which has a getPhoneRotation method
        which returns the angles array containing the latest yaw, pitch, and roll angles from the sensor output */
        double yaw = values[0] *(360/(2* Math.PI)); /* left or right turn */
        double pitch = values[1] * (360 / (2 * Math.PI)); /* forward or backward tilt */
        double roll = values[2] *(360/(2* Math.PI)); /* side to side tilt */


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
        gasDir = FORWARD;
    }

    public void reverse_pressed(View view){
        gasDir = REVERSE;
    }

    public void park_pressed(View view){
        gasDir = PARKED;
    }





}
