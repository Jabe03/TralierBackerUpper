package com.example.trailerbackerupperapp.customwidgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ArrowsView extends View {
    private final Paint p;

    public ArrowsView(@NonNull Context context) {
        super(context);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public ArrowsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public ArrowsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
    }


    @Override
    public void onDraw(Canvas c){
        super.onDraw(c);
        p.setColor(Color.GREEN);
        c.drawRect(new Rect(0,0,50,50), p);
        c.drawLine(0,0, getWidth(), getHeight(), p);
    }
}
