package com.example.trailerbackerupperapp;

import static java.lang.String.format;

import android.annotation.SuppressLint;

import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.hardware.Sensor;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trailerbackerupper.R;
import com.example.trailerbackerupperapp.customwidgets.ArrowsView;
import com.example.trailerbackerupperapp.customwidgets.DebugLayout;
import com.example.trailerbackerupperapp.customwidgets.Debuggable;

import java.util.ArrayList;
import java.util.Arrays;

import Online.Client;
import Online.DefaultOnlineCommands;
import Online.ImageReceiver;


public class MainActivity extends AppCompatActivity implements Debuggable {
    private ArrowsView arrowsView; /* a view object is created on the window, showing the navigation guidance arrows,
     the name of the object is the name of the class wherein it is defined and also the name of the element as referenced
     in assisted_mode.xml */

    private GyroDetector gyro;
    private ImageView connectionDot;
    DebugLayout debugLayout;

    private ImageView trailerCam;

    private ArrayList<Button> controlStateButtons;

    private ArrayList<Button> gearButtons;
    double steeringAngle;
    double lastSAVal;
    double gasVal;
    double lastGasVal;
    private static final int FORWARD = 1;
    private static final int REVERSE = -1;
    private static final int PARKED = 0;
    double gasDir;
    volatile Filter accel;

    private static final int BUTTON_DISABLED = Color.rgb(129, 125, 140);
    private static final int BUTTON_ENABLED = Color.rgb(103,80,164);
    private static final int BUTTON_HILIGHTED = Color.rgb(137,116,197);



    int controlState;
    private static final int DEFAULT_CONTROL_STATE = DefaultOnlineCommands.MANUAL_MODE;



    Client me;
    private Button gasButton;

    private static final int SEND_RATE = 60;

    private boolean breaking;
    private boolean gasDisabled;
    private boolean lastManualOn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assisted_mode); /* this is the mode with the guidance arrows aka ArrowsView */
        arrowsView = findViewById(R.id.ArrowsView); /* the ArrowView element being assigned here is an object that seems to be instantiated in assisted_mode.xml using the ArrowsView.java class */
        //System.out.println("Debug button is: " + (Button)findViewById(R.id.off_button));
        initializeControlButtons();
        initializeGyroscope();
        initDebug(false);
        setupClient();
        initializeImageReciever();


       }




    private void initializeControlButtons(){
        initializeGasAndBrake();
        initializeGearButtons();
        controlState = DEFAULT_CONTROL_STATE;
        controlStateButtons = new ArrayList<>();
        controlStateButtons.add(findViewById(R.id.ManualModeButton));
        controlStateButtons.add(findViewById(R.id.AssistedModeButton));
        controlStateButtons.add(findViewById(R.id.AutomaticModeButton));



        Log.d("ControlButtons", controlStateButtons.toString());
        setControlState(controlState);


    }

    public Bitmap tempGetBitMapOfTrailer(){

        return  BitmapFactory.decodeResource(getResources(),
                R.drawable.trailer_image);
    }
    public void initializeImageReciever(){
        ImageReceiver iR = new ImageReceiver(this);
        trailerCam = findViewById(R.id.TrailerCameraView);
        iR.start();
    }
    public void initializeGearButtons(){
        gearButtons = new ArrayList<>();
        gearButtons.add(findViewById(R.id.Forward));
        gearButtons.add(findViewById(R.id.Park));
        gearButtons.add(findViewById(R.id.Reverse));


        highlightOnlyOneButton(gearButtons, 1);
        gasDir = PARKED;

    }

    public void setupClient(){
        connectionDot = findViewById(R.id.connection_indicator);
        setConnectionIcon(false);

        me = new Client("192.168.1.103", 1102, this);
        //172.17.50.27
        attemptToConnectClient();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initializeGasAndBrake(){

        gasDir = FORWARD;
        gasVal = 0;
        gasButton = findViewById(R.id.GasButton);
        //gasButton.setBackgroundColor(Color.rgb(0,0,0));
        //accel = new Filter((int)(SEND_RATE*DECAY_RATE));
        gasDisabled = false;
        gasButton.setOnTouchListener((v, event) -> {
            if(gasDisabled){
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    double realVal = Filter.bound(((((v.getHeight() - event.getY()) / v.getHeight()))), 0,1);
                    if(controlState == DefaultOnlineCommands.ASSISTED_MODE){
                        gasVal = gasDir* getGasPercent(realVal, 0.4, 0.65 );
                    } else{
                        gasVal = gasDir* getGasPercent(realVal, 0.35, 1);
                    }
                    double colorWeight = Math.pow(Math.abs(gasVal),2);
                    int r = (int) (Color.red(BUTTON_ENABLED) * (1-colorWeight) + Color.red(BUTTON_HILIGHTED)*(colorWeight));
                    int g = (int) (Color.green(BUTTON_ENABLED) * (1-colorWeight) + Color.green(BUTTON_HILIGHTED)*(colorWeight));
                    int b = (int) (Color.blue(BUTTON_ENABLED) * (1-colorWeight) + Color.blue(BUTTON_HILIGHTED)*(colorWeight));
                    gasButton.setBackgroundColor(Color.rgb(r,g,b));
                    //System.out.println("Gas pressed!");
                    break;
                case MotionEvent.ACTION_UP:
                    gasButton.setBackgroundColor(BUTTON_ENABLED);
                    gasVal = 0;
                    //System.out.println("Gas released!");
                    break;
            }
            return true;
        });
        Button brakeButton = findViewById(R.id.BrakeButton);
        brakeButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(controlState == DefaultOnlineCommands.AUTOMATIC_MODE){
                        setControlState(DefaultOnlineCommands.MANUAL_MODE);
                    }
                    brakeButton.setBackgroundColor(BUTTON_HILIGHTED);
                    //System.out.println("Gas pressed!");
                    break;
                case MotionEvent.ACTION_UP:
                    brakeButton.setBackgroundColor(BUTTON_ENABLED);
                    //System.out.println("Gas released!");
                    break;
            }
            return true;
        });
        brakeButton.setBackgroundColor(BUTTON_ENABLED);
        /*
        Thread valUpdater = new Thread(()->{
            long last = System.currentTimeMillis();
            while(true){
                long now = System.currentTimeMillis();
                if(now - last >= 1000/SEND_RATE){
                    accel.append((float)gasVal);
                    last = now;
                }
            }

        });
        valUpdater.start();
        */
    }

    private double getGasPercent(double realPercent, double min, double max){
        if(realPercent == 0){
            return 0;
        }
        return min + realPercent*(max-min);
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
            me.startProcessingPackets();
           long last = System.currentTimeMillis();
            while(me.isRunning()){
                long now = System.currentTimeMillis();
                if(now - last >= 1000/SEND_RATE){ /* 1000 milliseconds is equal to 1 second, the contained code executes every 1/60 second */

                    steeringAngle = Math.toDegrees(arrowsView.getSteeringAngle());
                    if(!Filter.areSimilar(steeringAngle, lastSAVal, 0.25)){
                        me.sendGyroReading(steeringAngle);
                        lastSAVal = steeringAngle;
                    }

                    if(!breaking && !Filter.areSimilar(gasVal, lastGasVal, 0.05) && !gasDisabled) {
                        me.sendGasReading(gasVal);
                        lastGasVal = gasVal;
                    }
                    /*if(lastManualOn != manualOn) {
                        me.requestCameraChange(manualOn);
                        lastManualOn = manualOn;
                    }*/

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
            debugLayout.toggleDebug();
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
    public void updateSuggestedSteeringAngle(double val){
        //Log.d("PacketProcessing", "MainActivity level sa;");
        double rads = Math.toRadians(val);
        runOnUiThread(()-> {
                    debugLayout.setText("receivedTarget", "D(" + val + ") R(" + rads + ")");
                });
        arrowsView.setTargetArrowAngle(rads);
    }

    public void gas_pressed(View view){

        //Log.d("Buttons", "Gas pressed!");
        //arrowsView.rotateArrowsContinuously();
    }

    public void brake_pressed(View view){

        /*
        Thread breaker = new Thread(()-> {
            breaking = true;
            accel.setAll(accel.eval() * -1);
            me.sendGasReading(accel.eval());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            accel.setAll(0);
            breaking = false;
        });
        breaker.start();
        */

    }


    public void forward_pressed(View view){
        gasDir = FORWARD;
        highlightOnlyOneButton(gearButtons, 0);
    }


    public void reverse_pressed(View view){
        gasDir = REVERSE;
        highlightOnlyOneButton(gearButtons, 2);

    }

    public void park_pressed(View view){
        gasDir = PARKED;
        highlightOnlyOneButton(gearButtons, 1);
    }

    public void manual_pressed(View view){
        setControlState(DefaultOnlineCommands.MANUAL_MODE);
    }

    public void automatic_pressed(View view){
        setControlState(DefaultOnlineCommands.AUTOMATIC_MODE);
    }

    public void assisted_pressed(View view){
        setControlState(DefaultOnlineCommands.ASSISTED_MODE);
    }

    public void off_pressed(View view){
        sendUpdatedControlState(DefaultOnlineCommands.SHUTDOWN);
    }

    public void sendUpdatedControlState(int state){
        if(me == null){
            return;
        }
        Thread sender = new Thread(()-> me.sendControlModeChange(state));
        sender.start();
    }

    public void setControlState(int state){
        this.controlState = state;
        switch(this.controlState){
            case DefaultOnlineCommands.MANUAL_MODE:
                runOnUiThread(()-> {
                    gasDisabled = false;
                    gasButton.setBackgroundColor(BUTTON_ENABLED);
                    highlightOnlyOneButton(controlStateButtons, DefaultOnlineCommands.MANUAL_MODE);
                });
                break;
            case DefaultOnlineCommands.ASSISTED_MODE:
                runOnUiThread(()-> {
                    //ENABLE TARGET ARROW ON THIS LINE
                    gasDisabled = false;
                    gasButton.setBackgroundColor(BUTTON_ENABLED);
                    highlightOnlyOneButton(controlStateButtons, DefaultOnlineCommands.ASSISTED_MODE);
                });
                break;
            case DefaultOnlineCommands.AUTOMATIC_MODE:
                runOnUiThread(()-> {
                    gasButton.setBackgroundColor(BUTTON_DISABLED);
                    gasDisabled = true;
                    highlightOnlyOneButton(controlStateButtons, DefaultOnlineCommands.AUTOMATIC_MODE);
                });
                break;
            default:
                break;
        }
        sendUpdatedControlState(state);
    }

    private void highlightOnlyOneButton(ArrayList<Button> buttons, int index){
        runOnUiThread(()-> {
            for (int i = 0; i < buttons.size(); i++) {
                Button curr = buttons.get(i);
                if (i != index) {
                    curr.setBackgroundColor(BUTTON_ENABLED);
                } else {
                    curr.setBackgroundColor(BUTTON_HILIGHTED);
                }
            }
        });
    }

    public void initDebug(boolean debug){
        debugLayout = findViewById(R.id.debugLayout); /* these are to be the textboxes on the display which display gyroscope information */
        debugLayout.setDebugger(this);
        debugLayout.addDebugField("steeringAngle", "StrAng");
        //debugLayout.addDebugField("gasDir", "gas direction");
        //debugLayout.addDebugField("gasValue", "gas");
        //debugLayout.addDebugField("receivedTarget", "Terget angle received");
        //debugLayout.addDebugField("targetArrowVal", "target arrow angle");
        debugLayout.addDebugField("packetsReceived", "received");
        debugLayout.addDebugField("packetsSent", "sent");
        //debugLayout.addDebugField("UDPReceived", "UDP Packets Received");
        //debugLayout.addDebugField("LastUDP", "Last UDP packet received");
        debugLayout.setDebug(debug);

    }
    @Override
    public void updateDebug() {
        runOnUiThread(() ->{
            debugLayout.setText("gasValue", gasVal);
            debugLayout.setText("gasDir", gasDir);
            debugLayout.setText("steeringAngle", arrowsView.getSteeringAngle());
            debugLayout.setText("targetArrowVal", arrowsView.getTargetArrowAngle());
            debugLayout.setText("packetsSent", "" + me.packetsSent);
            debugLayout.setText("packetsReceived", "" + me.packetsReceived);
            debugLayout.setText("UDPReceived", ImageReceiver.count);
            debugLayout.setText("LastUDP", Arrays.toString(ImageReceiver.lastPacket));
        });
        }
    public void updateTrailerView(Bitmap image){
        runOnUiThread(() -> {
            ImageView imageView = findViewById(R.id.TrailerCameraView);
            imageView.setImageBitmap(image);
        });
    }


}
