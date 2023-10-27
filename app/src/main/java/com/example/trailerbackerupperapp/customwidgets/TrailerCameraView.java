package com.example.trailerbackerupperapp.customwidgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class TrailerCameraView extends AppCompatImageView {

    Bitmap currentImage;

    public TrailerCameraView(@NonNull Context context) {
        super(context);
    }

    public TrailerCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TrailerCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCurrentImage(byte[] image){
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
        this.setImageBitmap(Bitmap.createScaledBitmap(bmp, this.getWidth(), this.getHeight(), false));
    }
    public void setCurrentImage(char[] bytes){
        setCurrentImage((char[]) bytes);
    }
    public void setCurrentImage(Bitmap image){
        this.currentImage = image;
    }

}
