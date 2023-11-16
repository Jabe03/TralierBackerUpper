package com.example.trailerbackerupperapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.example.trailerbackerupper.R;

import java.util.Arrays;

public class GyroDetector implements SensorEventListener { /* The GyroDetector class is declared, which implements the
SensorEventListener interface. This means that instances of this class can listen to and respond to sensor events. */

    private static final int ANGLES_BUFFER = 2;

    private float[] m_rotationMatrix;
    private float[] m_orientation;

    private float[] angles;

    private Filter[] m_filters;

    private MainActivity act; /* this variable is set to the m variable that is passed to the constructor from the onCreate method in the
    MainActivity class */

    private float[] mGravity;
    private float[] mGeomagnetic;

    public GyroDetector(MainActivity m) { /* this constructor is called in the onCreate function of the MainActivity class whose object
    here is represented by variable m */
        m_rotationMatrix = new float[3];
        m_orientation = new float[3];
        angles = new float[3];
        m_filters = new Filter[3];
        for(int i  = 0; i < 3; i++){
            m_filters[i] = new Filter(ANGLES_BUFFER);
        }
        act = m;
    }
    /*
    public void startPolling(){
        if(isPolling) return;
        Log.d("Backend", "Pilling thread started");
        Thread pollingThread = new Thread(() -> {
                long last = System.currentTimeMillis();
                while(isPolling){
                    long now = System.currentTimeMillis();
                    if(now - last >= 1000/pollingRate){
                        computeOrientation();
                        last = now;
                    }
                }
        });
        isPolling = true;
        pollingThread.start();
    }   */

    private void computeOrientation(float[] R) {
        //Log.d("Backend", "Computing orientation...");
        float[] orientationData = new float[3];
        SensorManager.getOrientation(R, orientationData); /* getOrientation is a method provided by SensorManager for calculating the
        orientation of the device in space. It takes two main parameters:
        R: This parameter is an array of float values representing a 3x3 rotation matrix. The rotation matrix describes the
        orientation of the device and is typically obtained from sensor data, such as gyroscope and accelerometer readings.
        orientationData: This parameter is an array of float values that will be populated with the resulting orientation angles.
        It's an output parameter where the calculated orientation data will be stored. */

        m_filters[0].append(orientationData[0]); /* yaw angle */
        m_filters[1].append(orientationData[1]); /* pitch angle */
        m_filters[2].append(orientationData[2]); /* roll angle */
        angles[0] = m_filters[0].eval(); /* yaw angle */
        angles[1] = m_filters[1].eval(); /* pitch angle */
        angles[2] = m_filters[2].eval(); /* roll angle */
        
        act.update_orientation(); /* act is a MainActivity object, and this line is calling one of its methods which
        sets the debugViews textboxes and the arrowsView display according to the values added to the angles array */

            /*
            float yaw = (float)Math.toDegrees(m_orientation[0]) ;
            float pitch =(float)Math.toDegrees(m_orientation[1]);
            float roll = (float)Math.toDegrees(m_orientation[2]);
            */
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d("Gyroscope", "Sensor changed! Event type: " + event.sensor.getStringType());
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            mGravity = event.values;
            //Log.d("Gyroscope", "Gravity values updated to: " + Arrays.toString(mGravity));
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
            //Log.d("Gyroscope", "Mag field values updated to: " + Arrays.toString(mGeomagnetic));
        }
        if (mGravity != null && mGeomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic); /*
                This line calculates the rotation matrix R and inclination matrix I based on the gravity and magnetic field sensor data. */
            if (success) {
               computeOrientation(R); /* sets the angles array to the yaw, pitch, and roll angles based on the data in R */
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
    public float[] getPhoneRotation(){
        return angles;
    }

    public float getPitch(){
        return getPhoneRotation()[1];
    }

    public double getSteeringAngle(){
        return 0.0;
    }



}
