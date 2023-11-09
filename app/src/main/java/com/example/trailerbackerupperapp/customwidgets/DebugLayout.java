package com.example.trailerbackerupperapp.customwidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DebugLayout extends LinearLayout {
    Map<String, Pair<Integer,String>> tags;

    boolean debug;
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
    public void addDebugField(String tag, String prefix){
        tags.put(tag,new Pair<>(this.getChildCount(),prefix));
        TextView newDebug = new TextView(this.getContext());
        newDebug.setText("noinit");
        newDebug.setVisibility(VISIBLE);

        this.addView(newDebug);
        System.out.println("DebugLayout now has " + this.getChildCount());
        setText(tag, ":)");
    }
    public void setText(String tag, String value){
        Pair<Integer,String> indexAndPrefix= tags.get(tag);
        if(indexAndPrefix != null){
            ((TextView)this.getChildAt(indexAndPrefix.first)).setText(indexAndPrefix.second + ": " + value+ " ");
        }

    }

    public void setDebug(boolean debug){
        this.debug = debug;
        if(this.debug){
            this.setVisibility(VISIBLE);
            return;
        }
        this.setVisibility(GONE);
    }

    public void toggleDebug(){
        setDebug(!debug);
    }
}



