package com.example.trailerbackerupperapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.trailerbackerupper.R
import com.example.trailerbackerupperapp.customwidgets.ArrowsView

class MainActivity : AppCompatActivity() {
    lateinit var v: ArrowsView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.assisted_mode)
        v= findViewById(R.id.ArrowsView);


    }


    fun gas_pressed(view: View){
        v.invalidate();
        Log.d("Buttons", "Gas pressed!");
        v.rotateArrowsContinuously();
    }


}