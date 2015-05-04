package com.alexstyl.locationreminder;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {
    private Paint mPaint;
    private Path mPath;

    // Height, width & center of main View
    int mParentWidth;
    int mParentHeight;
    int mParentCenterX;
    int mParentCenterY;


    public CompassView(Context context) {
        super(context);
        init();
    }


    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CompassView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.mPath = new Path();
        // Wedge-shaped path for the compass
        mPath.moveTo(0, -50);
        mPath.lineTo(-20, 60);
        mPath.lineTo(0, 50);
        mPath.lineTo(20, 60);
        mPath.close();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        this.mRotationInDegrees = 0;
    }

    //    private View mFrame;
    private double mRotationInDegrees;


    public void setRotationDegrees(double degrees) {
        this.mRotationInDegrees = degrees;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Compute main View size
        View mFrame = (View) getParent();

        mParentWidth = mFrame.getWidth();
        mParentHeight = mFrame.getHeight();
        mParentCenterX = mParentWidth / 2;
        mParentCenterY = mParentHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw everything in the middle of the canvas
        canvas.translate(mParentCenterX, mParentCenterY);
        // Rotate canvas to show orientation to the magnetic North Pole
        canvas.rotate(-(float) mRotationInDegrees);
        // Draw the arrow
        canvas.drawPath(mPath, mPaint);
    }
}