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
    /**
     * Paint object used to draw the arrows in the screen
     */
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
    /**
     * The factor by which the angles will be exaggerated visually.
     * For example, if the STEERING_ANGLE_RANGE is 20 degrees and the inflation factor is 2, the maximum visual angle of both arrows is +/- 2*20 = +/- 40 degrees).
     * 0 degrees will still be represented as 0 degrees visually, but any other angle is angle*INFLATION_FACTOR.
     */
    private static final double INFLATION_FACTOR = 2;
    /**
     * Length of the tip of the arrow relative to the length of the arrow itself.
     */
    private final static double ARROW_TIP_RATIO = 0.2;
    /**
     * Angle between shaft of the arrow and one of its tips
     */
    private final static double ARROW_TIP_ANGLE = Math.PI/4;
    /**
     * Used for debug. True if and only if the thread that continuously rotates the arrows is running
     */
    private boolean continuouslyRotating;
    /**
     * Angle, in radians, of the true arrow.
     */
    private double trueArrowAngle;

    /**
     * Vector representing the position of the trueArrow
     */
    private float[] trueArrow;

    /**
     * Angle, in radians, of the target arrow
     */
    private double targetArrowAngle;

    /**
     *Vector representing the position of the targetArrow
     */
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

    /**
     * Used for testing, creates an arrowRotator thread that continuously rotates both of the arrows in the ArrowsView
     */
    public void rotateArrowsContinuously(){
        if(continuouslyRotating) return; /* This check ensures that only one arrow rotator thread is active at a time. */
        Thread arrowRotator = new Thread(() -> { /* while this thread is running, it rotates both arrows at the given rates */
            double refreshRate = 60; /* this is the rate at which the view is refreshed while the app is running */
            double angleTrueVelocity = Math.PI; /* pi radians rotation of true arrow to occur on every cycle*/
            double angleTargetVelocity = Math.PI * 0.5; /* half of pi radians rotation of target arrow to occur on every cycle */
            float angleTrue = 0;
            float angleTarget = 0;
            long last = System.currentTimeMillis();
            while(continuouslyRotating){
                long now = System.currentTimeMillis();
                if(now - last >= 1000/refreshRate){ /* 1000 milliseconds is equal to 1 second, the contained code executes every 1/60 second */
                    angleTrue += angleTrueVelocity/refreshRate; /* pi radians rotation of true arrow every second */
                    angleTarget += angleTargetVelocity/refreshRate; /*half of pi radians rotation of target arrow every second */
                    setTargetArrowAngle(angleTarget);  /* sets end point coordinate of target arrow to what is required for the angle specified by
                    angleTarget value, but before it makes some adjustments on the angleTarget value to correspond it to the bounds required on
                    the display */
                    setTrueArrowAngle(angleTrue); /* mirrors setTargetArrowAngle operation except for the true arrow */
                    invalidate(); /* Android operates on a continuous UI rendering cycle. This cycle is responsible for rendering and updating the
                    user interface components. In custom View components, you may want to update the appearance or content of the view dynamically.
                    To trigger this update, you call the invalidate() method on the View. When invalidate() is called, it marks the view as
                    invalid, indicating that it needs to be redrawn. However, the actual redraw doesn't happen immediately. Instead, it's
                    scheduled to occur during the next rendering cycle. */
                    last = now;
                }
            }
        });
        continuouslyRotating = true;
        arrowRotator.start(); /* starts the arrow rotator thread defined in this function */
    }

    /**
     * Stops the arrowRotator thread
     */
    public void stopRotating(){
        continuouslyRotating = false;
    }

    /**
     * Setup for ArrowsView, always called during its constructors
     */
    private void init(){ /* this function is automatically called when the ArrowsView object is first created */
        p = new Paint(Paint.ANTI_ALIAS_FLAG); /* creates new paintbrush object called p with anti alias flag set to true which allows for
        smooth rendering of the edges of lines and shapes */
        continuouslyRotating = false; /* arrows not auto rotating right away */
        trueArrowAngle = 0;
        targetArrowAngle = 0;
        targetArrow = new float[]{0, TARGET_ARROW_LENGTH}; /* represents initial start and end points of target arrow relative to axises,
        is vertical */
        trueArrow = new float[]{0,(float)(TARGET_ARROW_LENGTH * TRUE_ARROW_RATIO)};  /* true arrow is 65 percent the length of target arrow,
        also vertically oriented */

    }

    /**
     * converts the theta, in radians, from the interval (-inf,inf) to [pi, -pi), then bounds it
     * by STEERING_ANGLE_RANGE: If it is less than -STEERING_ANGLE_RANGE then it sets it to
     * -STEERING_ANGLE_RANGE. If it is greater than STEERING_ANGLE_RANGE then it sets it to
     * STEERING_ANGLE_RANGE. The final value is then multiplied by the INFLATION_FACTOR to get
     * the displayed angle of the arrow.
     *
     * @param theta angle, in radians
     * @return the bounded angle, in radians
     */
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
    /**
     * Computes the coordinates of the true arrow given the new angle of the arrow
     * @param theta the new angle, in radians
     */
    public void setTrueArrowAngle(double theta){
        trueArrowAngle = getBoundedArrowAngle(theta); /* makes sure that the true arrow angle is within the specified bounds,
        otherwise sets the angle to the nearest bounding angle */

        trueArrow[0] = (float)(Math.cos(trueArrowAngle + Math.PI/((float)2)) * TRUE_ARROW_RATIO * TARGET_ARROW_LENGTH); /* given the bound
        corrected true arrow angle, sets the x coordinate judged by distance to the vertical halfline axis */
        trueArrow[1] = (float)(Math.sin(trueArrowAngle + Math.PI/((float)2)) * TRUE_ARROW_RATIO * TARGET_ARROW_LENGTH); /*given the bound corrected
        true arrow angle, sets the y coordinate judged by distance from the horizontal top boundary of the view */
        invalidate(); /* marks view as invalid, schedules redraw on next rendering cycle */
    }
    /**
     * Computes the coordinates of the target arrow given the new angle of the arrow
     * @param theta the new angle, in radians
     */

    public void setTargetArrowAngle(double theta){  /* this function mirrors the setTrueArrowAngle function except does those operations
    with the target arrow */
        //Log.d("PacketProcessing", "" + theta);

        targetArrowAngle = getBoundedArrowAngle(theta);
        targetArrow[0] = (float)(Math.cos(targetArrowAngle + Math.PI/((float)2)) * TARGET_ARROW_LENGTH);
        targetArrow[1] = (float)(Math.sin(targetArrowAngle + Math.PI/((float)2)) * TARGET_ARROW_LENGTH);
        //Log.d("Arrow math", "New points for arrow after angle " + theta + ": " + Arrays.toString(targetArrow));
        invalidate();
    }


    /**
     * Draws this component consisting of two arrows: true and target.
     * @param c Canvas to be drawn on
     */
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

    /**
     * This draws an arrow on the screen
     * @param arrow array that contains the coordinates of the tip of the arrow
     * @param c Canvas to draw the arrow on
     * @param p paint object that draws the arrow. (NOTE: The arrow will be drawn with the current color of the arrow)
     */
    private void drawArrow(float[] arrow, Canvas c, Paint p){
        p.setStrokeWidth(10);
        //Log.d("Drawing arrow", "Arrow coords: " + Arrays.toString(arrow));
        float startX = getWidth()/((float)2); /* indicates that the x value of the starting point of the arrow to be drawn corresponds to
         the east end of the view*/
        float startY = getHeight(); /* indicates that the y value of the starting point of the arrow to be drawn corresponds to the south end
         the view */
        float endX = startX + arrow[0]; /* sets the x value of the end point of the arrow to be drawn is absolute value of arrow[0] pixels
        shifted  west from the east end of the view */
        float endY = startY - arrow[1]; /* sets the y value of the end point of the arrow to be drawn as being arrow[1] pixels shifted north from
        the south end of the view */
        /*Log.d(
                "Drawing arrow" ,
                "On-screen coords" + "[" + startX + ", " + startY + ", " + endX + ", " + endY + "]"
        );*/

        //c.drawLine(0,0, arrow[0], arrow[1], p);
        c.drawLine(startX, startY, endX, endY, p); /* draws the arrow shaft */
        p.setStrokeWidth(5); /* arrow tip to be half the width of 10px arrow shaft width */
        float[] arrowTip = new float[2];
        double cosRatio = Math.cos(Math.PI - ARROW_TIP_ANGLE); /* pi part represents shaft, minus indicates clockwise rotation, clockwise
        from the shaft is left */
        double sinRatio = Math.sin(Math.PI - ARROW_TIP_ANGLE);
        arrowTip[0] = (float)((arrow[0]*cosRatio - arrow[1]*sinRatio)* ARROW_TIP_RATIO);
        arrowTip[1] = (float)((arrow[0]*sinRatio + arrow[1]*cosRatio)* ARROW_TIP_RATIO);
        c.drawLine(endX, endY, endX + arrowTip[0], endY - arrowTip[1], p); /* left side of arrowhead drawn */
        cosRatio = Math.cos( Math.PI + ARROW_TIP_ANGLE);
        sinRatio = Math.sin( Math.PI + ARROW_TIP_ANGLE);
        arrowTip[0] = (float)((arrow[0]*cosRatio - arrow[1]*sinRatio)* ARROW_TIP_RATIO);
        arrowTip[1] = (float)((arrow[0]*sinRatio + arrow[1]*cosRatio)* ARROW_TIP_RATIO);
        c.drawLine(endX, endY, endX + arrowTip[0], endY - arrowTip[1], p); /* right side of the arrowhead is drawn */
    }

    public double getSteeringAngle(){
        return trueArrowAngle/INFLATION_FACTOR;
    }
    public double getTargetArrowAngle(){
        return targetArrowAngle/INFLATION_FACTOR;
    }
}
