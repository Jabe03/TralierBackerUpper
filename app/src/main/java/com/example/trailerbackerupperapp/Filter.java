package com.example.trailerbackerupperapp;

public class Filter {
    private float[] last;
    private int next;
    private int buffer;

    public Filter(int buffer) { /* the ANGLES_BUFFER = 2 value from GyroDetector is passed here */
        this.buffer = buffer;
        last = new float[buffer]; /* array of two floats */
        next = 0;
    }

    public void append(float f) {
        last[next % buffer] = f;
        next = next + 1;
    }

    public void setAll(float f){
        last = new float[buffer];
        for(int i = 0; i < buffer; i++){
            last[i] = f;
        }

    }

    public float eval(){
        float sum = 0;
        for (float current : last) { /* first float in lastTen is f */
            sum += current;
        }
        if (next >= buffer - 1) {
            return sum / (buffer);
        }
        return sum / (float) next;
    }


    public static double bound(double real, double lower, double upper) {
        //return (lower > real) ? lower : (upper < real ? upper : real);
        return Math.min(upper, Math.max(lower, real));
    }

    public static boolean areSimilar(double a, double b, double tolerance){
        return Math.abs(a-b) <= tolerance;
    }

}

