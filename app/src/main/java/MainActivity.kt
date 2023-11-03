/*package com.example.trailerbackerupperapp

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Half.toFloat
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailerbackerupper.R
import com.example.trailerbackerupperapp.customwidgets.ArrowsView


class MainActivity : AppCompatActivity() {
    lateinit var v: ArrowsView;
    var debug: Boolean = false;
    lateinit var gyro: GyroDetector;
    lateinit var debugViews: Array<TextView> ;
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.assisted_mode)
        //initializeGyroscope()
        v= findViewById(R.id.ArrowsView);
    }

    fun initializeGyroscope(){

        debugViews = arrayOf(
            findViewById<TextView>(R.id.roll),
            findViewById<TextView>(R.id.pitch),
            findViewById<TextView>(R.id.yaw)
        );
        gyro = GyroDetector(this)
        var mgr : SensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        mgr.registerListener(
            gyro,
            mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_UI
        )
        mgr.registerListener(
            gyro,
            mgr.getDefaultSensor(Sensor.TYPE_GRAVITY),
            SensorManager.SENSOR_DELAY_UI
        )
    }



    fun toggle_debug(){

    }

    fun update_orientation(){
        Log.d("Updating UI", "Updating orientation")
        var values : FloatArray = gyro.phoneRotation;
        var yaw = values[0] *(360/(2* Math.PI));
        var pitch = values[1]  *(360/(2* Math.PI));
        var roll = values[2] *(360/(2* Math.PI));
        debugViews[0].text = "yaw z: %.2f ".format(yaw)
        debugViews[1].text = "pitch x:%.2f ".format(pitch)
        debugViews[2].text = "roll y: %.2f ".format(roll)

        v.setTrueArrowAngle(-pitch/100)
    }


    fun gas_pressed(view: View){

        Log.d("Buttons", "Gas pressed!");
        v.rotateArrowsContinuously();
    }

    fun brake_pressed(view: View){
        v.stopRotating();
    }

    fun forward_pressed(view: View){
        Log.d("Buttons", "Forward pressed!");
        initializeGyroscope()

    }


}*/