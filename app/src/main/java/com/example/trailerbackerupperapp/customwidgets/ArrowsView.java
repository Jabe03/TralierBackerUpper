package com.example.trailerbackerupperapp.customwidgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * @author Joshua Bergthold
 * ArrowsView is a custom widget that displays a "true" arrow and a "target" arrow.
 * The intent is for the arrows to represent turning angles which the computer suggests and the actual turning angle that the user has input.
 * The true arrow represents the user's input and the target arrow represents the computer's suggestion.
 */
public class ArrowsView extends View {
    private Paint p;

    /**
     *
     */
    private final static int targetArrowLength = 300;
    private final static double trueArrowRatio = 0.65;
    private final static double steeringAngleRange = Math.toRadians(21);
    private final static double arrowTipRatio = 0.2;
    private final static double arrowTipAngle = (5.0/6.0) * Math.PI;
    private static final double inflationFactor = 2;
    private boolean continuouslyRotating;
    private double trueArrowAngle;
    private float[] trueArrow;
    private double targetArrowAngle;
    private float[] targetArrow;


    public ArrowsView(@NonNull Context context) {
        super(context);
        init();
    }

    public ArrowsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void rotateArrowsContinuously(){
        if(continuouslyRotating) return;
        Thread arrowRotator = new Thread(() -> {
            double refreshRate = 60;
            double angleTrueVelocity = Math.PI;
            double angleTargetVelocity = Math.PI * 0.5;
            float angleTrue = 0;
            float angleTarget = 0;
            long last = System.currentTimeMillis();
            while(continuouslyRotating){
                long now = System.currentTimeMillis();
                if(now - last >= 1000/refreshRate){
                    angleTrue += angleTrueVelocity/refreshRate;
                    angleTarget += angleTargetVelocity/refreshRate;
                    setTargetArrowAngle(angleTarget);
                    setTrueArrowAngle(angleTrue);
                    invalidate();
                    last = now;
                }
            }
        });
        continuouslyRotating = true;
        arrowRotator.start();
    }

    public void stopRotating(){
        continuouslyRotating = false;
    }

    private void init(){
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStrokeWidth(10);
        continuouslyRotating = false;
        //p.setColor(Color.GREEN);
        trueArrowAngle = 0;
        targetArrowAngle = 0;
        targetArrow = new float[]{0,targetArrowLength};
        trueArrow = new float[]{0,(float)(targetArrowLength*trueArrowRatio)};
        setTargetArrowAngle(Math.PI *0.25);
        setTrueArrowAngle(-Math.PI * 0.25);

    }

    public double getTrueArrowAngle(){
        return trueArrowAngle -  Math.PI/((float)2);
    }
    public double getTargetArrowAngle(){
        return targetArrowAngle - Math.PI/((float)2);
    }

    private double getBoundedArrowAngle(double theta){
        double normalizedTheta = ((theta*inflationFactor + Math.PI) % (2*Math.PI)) - Math.PI;

        if(normalizedTheta > steeringAngleRange*inflationFactor){
            return (steeringAngleRange*inflationFactor);
        }
        else if(normalizedTheta < -steeringAngleRange*inflationFactor){
            return -steeringAngleRange*inflationFactor;
        }
        return normalizedTheta;



    }
    public void setTrueArrowAngle(double theta){
        trueArrowAngle = getBoundedArrowAngle(theta);

        trueArrow[0] = (float)(Math.cos(trueArrowAngle + Math.PI/((float)2)) * trueArrowRatio* targetArrowLength);
        trueArrow[1] = (float)(Math.sin(trueArrowAngle + Math.PI/((float)2)) * trueArrowRatio*targetArrowLength);
        invalidate();
    }

    public void setTargetArrowAngle(double theta){
        targetArrowAngle = getBoundedArrowAngle(theta);
        targetArrow[0] = (float)(Math.cos(targetArrowAngle + Math.PI/((float)2)) * targetArrowLength);
        targetArrow[1] = (float)(Math.sin(targetArrowAngle + Math.PI/((float)2)) * targetArrowLength);
        Log.d("Arrow math", "New points for arrow after angle " + theta + ": " + Arrays.toString(targetArrow));
        invalidate();
    }






    @Override
    public void onDraw(Canvas c){
        super.onDraw(c);
        p.setColor(Color.GREEN);
        drawArrow(targetArrow, c , p);
        p.setColor(Color.RED);
        drawArrow(trueArrow, c, p);
    }


    private void drawArrow(float[] arrow, Canvas c, Paint p){
        p.setStrokeWidth(10);
        Log.d("Drawing arrow", "Arrow coords: " + Arrays.toString(arrow));
        float startX = getWidth()/((float)2);
        float startY = getHeight();
        float endX = startX + arrow[0];
        float endY = startY - arrow[1];
        Log.d(
                "Drawing arrow" ,
                "On-screen coords" + "[" + startX + ", " + startY + ", " + endX + ", " + endY + "]"
        );

        //c.drawLine(0,0, arrow[0], arrow[1], p);
        c.drawLine(startX, startY, endX, endY, p);
        p.setStrokeWidth(5);
        float[] arrowTip = new float[2];
        double cosRatio = Math.cos( arrowTipAngle);
        double sinRatio = Math.sin(arrowTipAngle);
        arrowTip[0] = (float)((arrow[0]*cosRatio - arrow[1]*sinRatio)*arrowTipRatio);
        arrowTip[1] = (float)((arrow[0]*sinRatio + arrow[1]*cosRatio)*arrowTipRatio);
        c.drawLine(endX, endY, endX + arrowTip[0], endY - arrowTip[1], p);
        cosRatio = Math.cos( -arrowTipAngle);
        sinRatio = Math.sin( -arrowTipAngle);
        arrowTip[0] = (float)((arrow[0]*cosRatio - arrow[1]*sinRatio)*arrowTipRatio);
        arrowTip[1] = (float)((arrow[0]*sinRatio + arrow[1]*cosRatio)*arrowTipRatio);
        c.drawLine(endX, endY, endX + arrowTip[0], endY - arrowTip[1], p);
    }
}
