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
     * Length of the target arrow
     */
    private final static int TARGET_ARROW_LENGTH = 300;
    /**
     * Length of the true arrow relative to the target arrow
     */
    private final static double TRUE_ARROW_RATIO = 0.65;

    /**
     * The actual range that the arrow view represents (+/- steeringAngleRange)
     */
    private final static double STEERING_ANGLE_RANGE = Math.toRadians(21);
    private final static double ARROW_TIP_RATIO = 0.2;
    private final static double ARROW_TIP_ANGLE = (5.0/6.0) * Math.PI;
    private static final double INFLATION_FACTOR = 2;
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

    private void init(){ /* this function is automatically called when the ArrowsView object is first created */
        p = new Paint(Paint.ANTI_ALIAS_FLAG); /* creates new paintbrush object called p with anti alias flag set to true which allows for
        smooth rendering of the edges of lines and shapes */
        continuouslyRotating = false;
        trueArrowAngle = 0;
        targetArrowAngle = 0;
        targetArrow = new float[]{0, TARGET_ARROW_LENGTH};
        trueArrow = new float[]{0,(float)(TARGET_ARROW_LENGTH * TRUE_ARROW_RATIO)};
        setTargetArrowAngle(Math.PI *0.25);


    }

    public double getTrueArrowAngle(){
        return trueArrowAngle -  Math.PI/((float)2);
    }
    public double getTargetArrowAngle(){
        return targetArrowAngle - Math.PI/((float)2);
    }

    private double getBoundedArrowAngle(double theta){
        double normalizedTheta = ((theta* INFLATION_FACTOR + Math.PI) % (2*Math.PI)) - Math.PI;

        if(normalizedTheta > STEERING_ANGLE_RANGE * INFLATION_FACTOR){
            return (STEERING_ANGLE_RANGE * INFLATION_FACTOR);
        }
        else if(normalizedTheta < -STEERING_ANGLE_RANGE * INFLATION_FACTOR){
            return -STEERING_ANGLE_RANGE * INFLATION_FACTOR;
        }
        return normalizedTheta;



    }
    public void setTrueArrowAngle(double theta){
        trueArrowAngle = getBoundedArrowAngle(theta);

        trueArrow[0] = (float)(Math.cos(trueArrowAngle + Math.PI/((float)2)) * TRUE_ARROW_RATIO * TARGET_ARROW_LENGTH);
        trueArrow[1] = (float)(Math.sin(trueArrowAngle + Math.PI/((float)2)) * TRUE_ARROW_RATIO * TARGET_ARROW_LENGTH);
        invalidate();
    }

    public void setTargetArrowAngle(double theta){
        targetArrowAngle = getBoundedArrowAngle(theta);
        targetArrow[0] = (float)(Math.cos(targetArrowAngle + Math.PI/((float)2)) * TARGET_ARROW_LENGTH);
        targetArrow[1] = (float)(Math.sin(targetArrowAngle + Math.PI/((float)2)) * TARGET_ARROW_LENGTH);
        Log.d("Arrow math", "New points for arrow after angle " + theta + ": " + Arrays.toString(targetArrow));
        invalidate();
    }






    @Override
    public void onDraw(Canvas c){
        super.onDraw(c);
        p.setColor(Color.GREEN); /* target arrow to be green colored */
        drawArrow(targetArrow, c , p); /* calls ArrowsView object function which takes in array of two floats called targetArrow representing
        horizontal distance of the end point of the target arrow
            (which is the green longer arrow that stays in vertical orientation and represents the direction which the other arrow called
            "true arrow" (representing current steer direction) must be matched up to as close as possible in order to achieve accurate steering)
        from the middle vertical of window (this value is set to 0) and the vertical distance of the end point of the target
        arrow from the top of window (this value is set to the target arrow length of 300), canvas object c which "hosts the draw calls",
        and Paint object p which represents the specified paint brush */
        p.setColor(Color.RED); /* true arrow to be red colored */
        drawArrow(trueArrow, c, p); /* trueArrow variable is a two float array with first value representing the horizontal distance of
        the end point of the true arrow from the middle vertical of the window (this value is set to 0) and the vertical distance of the end point
        of the target arrow from the top of the window (this value is set to 65 percent of the target arrow length of 300). the true arrow is the
        red shorter arrow which represents the current steering direction of the vehicle and the driver must align it as close to the target arrow as possible
        in order to accomplish correct steering angle */
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
        double cosRatio = Math.cos(ARROW_TIP_ANGLE);
        double sinRatio = Math.sin(ARROW_TIP_ANGLE);
        arrowTip[0] = (float)((arrow[0]*cosRatio - arrow[1]*sinRatio)* ARROW_TIP_RATIO);
        arrowTip[1] = (float)((arrow[0]*sinRatio + arrow[1]*cosRatio)* ARROW_TIP_RATIO);
        c.drawLine(endX, endY, endX + arrowTip[0], endY - arrowTip[1], p);
        cosRatio = Math.cos( -ARROW_TIP_ANGLE);
        sinRatio = Math.sin( -ARROW_TIP_ANGLE);
        arrowTip[0] = (float)((arrow[0]*cosRatio - arrow[1]*sinRatio)* ARROW_TIP_RATIO);
        arrowTip[1] = (float)((arrow[0]*sinRatio + arrow[1]*cosRatio)* ARROW_TIP_RATIO);
        c.drawLine(endX, endY, endX + arrowTip[0], endY - arrowTip[1], p);
    }
}
