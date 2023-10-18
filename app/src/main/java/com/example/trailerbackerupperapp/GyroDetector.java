package com.example.trailerbackerupperapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.example.trailerbackerupper.R;

import java.util.Arrays;

public class GyroDetector implements SensorEventListener {

    private static final int ANGLES_BUFFER = 2;

    private float[] m_rotationMatrix;
    private float[] m_orientation;

    private float[] angles;

    private Filter[] m_filters;

    private MainActivity act;

    private float[] mGravity;
    private float[] mGeomagnetic;

    public GyroDetector(MainActivity m) {
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
        Log.d("Backend", "Computing orientation...");
        float[] orientationData = new float[3];
        SensorManager.getOrientation(R, orientationData);
        angles[0] = m_filters[0].append(orientationData[0]);
        angles[1] = m_filters[1].append(orientationData[1]);
        angles[2] = m_filters[2].append(orientationData[2]);
        act.update_orientation();

            /*
            float yaw = (float)Math.toDegrees(m_orientation[0]) ;
            float pitch =(float)Math.toDegrees(m_orientation[1]);
            float roll = (float)Math.toDegrees(m_orientation[2]);
            */

            /* append returns an average of the last 10 values */



        //}
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("Gyroscope", "Sensor changed! Event type: " + event.sensor.getStringType());
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            mGravity = event.values;
            Log.d("Gyroscope", "Gravity values updated to: " + Arrays.toString(mGravity));
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
            Log.d("Gyroscope", "Mag field values updated to: " + Arrays.toString(mGeomagnetic));
        }
        if (mGravity != null && mGeomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
               computeOrientation(R);
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

    public double getSteeringAngle(){
        return 0.0;
    }

    static class Filter{
        private float[] lastTen;
        private int next;
        private int buffer;
        public Filter(int buffer){
            this.buffer = buffer;
            lastTen = new float[buffer];
            next = 0;
        }

        public float append(float f) {
            lastTen[next%buffer] = f;
            next = next + 1;
            float sum = 0;
            for(float current: lastTen){
                sum += current;
            }
            if(next >= buffer-1){
                return sum/(buffer);
            }
            return sum / (float)next;
        }
    }

}
