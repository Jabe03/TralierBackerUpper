package com.example.trailerbackerupperapp;

public class Filter {
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

