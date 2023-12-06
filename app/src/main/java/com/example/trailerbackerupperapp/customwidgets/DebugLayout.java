package com.example.trailerbackerupperapp.customwidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.trailerbackerupper.R;
import com.example.trailerbackerupperapp.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Online.DefaultOnlineCommands;

public class DebugLayout extends LinearLayout {
    Map<String, Pair<Integer,String>> tags;
    Thread debugger;
    ArrayList<Button> debugButtons;
    Debuggable target;
    public static boolean debug;

    public DebugLayout(Context context) {
        super(context);
        init();
    }

    public DebugLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DebugLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DebugLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    private void init(){
        this.debug = false;
        this.tags = new HashMap<>();
    }

    public void initDebugButtons(MainActivity act){
        this.debugButtons = new ArrayList<>();
        debugButtons.add((Button)act.findViewById(R.id.off_button));
        setDebug(debug);
    }
    public void addDebugField(String tag, String prefix){
        tags.put(tag,new Pair<>(this.getChildCount(),prefix));
        TextView newDebug = new TextView(this.getContext());
        newDebug.setText("noinit");
        newDebug.setVisibility(VISIBLE);
        this.addView(newDebug);
        //System.out.println("DebugLayout now has " + this.getChildCount());
        setText(tag, ":)");
    }
    public void setText(String tag, String value){
        Pair<Integer,String> indexAndPrefix= tags.get(tag);
        if(indexAndPrefix != null){
            TextView t =  ((TextView)this.getChildAt(indexAndPrefix.first));
            //System.out.println(indexAndPrefix.first + ", " + t + indexAndPrefix.second + ": " + value+ " ");
            t.setText(indexAndPrefix.second + ": " + value+ " ");
        }

    }

    public void setText(String tag, double value){
        setText(tag, String.format("%.3f", value));
    }

    public void setDebug(boolean debug){
        this.debug = debug;
        if(this.debug){
            for(Button b: debugButtons){
                b.setVisibility(VISIBLE);
            }
            this.setVisibility(VISIBLE);
            startDebugger();
            return;
        }
        for(Button b: debugButtons){
            b.setVisibility(GONE);
        }
        this.setVisibility(GONE);
    }

    private void startDebugger(){
        debugger = new Thread(() ->{
            double updateRate = 60; /* this is the rate at which the view is refreshed while the app is running */
            long last = System.currentTimeMillis();
            while(debug){
                long now = System.currentTimeMillis();
                if(now - last >= 1000/updateRate){ /* 1000 milliseconds is equal to 1 second, the contained code executes every 1/60 second */
                    if(target  != null)
                        target.updateDebug();
                    last = now;
                }
            }
        });
        debugger.start();
    }

    public void toggleDebug(){
        setDebug(!debug);
    }

    public void setDebugger(MainActivity act){
        target = act;
        initDebugButtons(act);

    }

    public static boolean isDebugging(){
        return debug;
    }
}



