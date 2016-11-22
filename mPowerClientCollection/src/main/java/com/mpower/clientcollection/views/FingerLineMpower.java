package com.mpower.clientcollection.views;

/**
 * Created by ratna on 3/29/16.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class FingerLineMpower extends View {
    private static Paint mPaint;
    private static Canvas mCanvas;
    private PointList mPoint = new PointList();
    public static ArrayList<PointList> pointList = new ArrayList<PointList>();
    public FingerLineMpower(Context context) {
        this(context, null);
    }

    public FingerLineMpower(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(4);
        pointList.add(mPoint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        canvas.drawLine(mPoint.startX, mPoint.startY, mPoint.endX, mPoint.endY, mPaint);
        Log.w(FingerLineMpower.class.getSimpleName(), "Start point x = " + mPoint.startX);
        Log.w(FingerLineMpower.class.getSimpleName(), "Start point y = " + mPoint.startY);
        //drawArrow(mPoint,canvas);
        //Log.w(FingerLineMpower.class.getSimpleName(), "Size second= " + pointList.size());
        undoPreviousStep();
        /*for(PointList list: pointList){
            canvas.drawLine(list.startX, list.startY, list.endX, list.endY, mPaint);
            drawArrow(list,canvas);
        }*/
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPoint.startX = event.getX();
                mPoint.startY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mPoint.endX = event.getX();
                mPoint.endY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mPoint.endX = event.getX();
                mPoint.endY = event.getY();

                pointList.add(mPoint);
                mPoint = new PointList();
                invalidate();
                break;
        }
        return true;
    }

    private static void drawArrow(PointList point, Canvas canvas){
        Path path = new Path();
        path.moveTo(-5, 5);
        path.lineTo(-5, -5);
        path.lineTo(5, 0);
        path.close();
        path.offset(point.endX, point.endY);
        canvas.drawPath(path, mPaint);
    }

    private void drawArc(PointList point, Canvas canvas){
        float radius = 2;
        final RectF oval = new RectF();
        oval.set(point.startX - radius, point.startX - radius, point.startX + radius, point.startX+ radius);
        canvas.drawArc (oval, 0, 0, true, mPaint);
    }

    public static void undoPreviousStep(){
        Log.w(FingerLineMpower.class.getSimpleName(), String.valueOf(mCanvas.getHeight()));
        for(PointList list: pointList){
            mCanvas.drawLine(list.startX, list.startY, list.endX, list.endY, mPaint);
            drawArrow(list, mCanvas);
        }

    }

    public class PointList{
        public float startX;
        public float startY;
        public float endX;
        public float endY;
    }
}