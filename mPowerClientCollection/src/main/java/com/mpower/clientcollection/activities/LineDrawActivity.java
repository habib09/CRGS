package com.mpower.clientcollection.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.views.FingerLineMpower;

public class LineDrawActivity extends AppCompatActivity {
    private ImageView thirdImage;
    private ImageView firstImage;
    private ImageView secondImage;
    private ImageView fourthImage;
    private int[] firstImagePosition_x = new int[2];
    private int[] firstImagePosition_y = new int[2];
    private int[] secondImagePosition_x = new int[2];
    private int[] secondImagePosition_y = new int[2];
    private int[] thirdImagePosition_x = new int[2];
    private int[] thirdImagePosition_y = new int[2];
    private int[] fourthImagePosition_x = new int[2];
    private int[] fourthImagePosition_y = new int[2];
    private int thresholdValue = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_draw);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findImagePosition();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        firstImagePosition_x[0] = firstImage.getLeft() - thresholdValue;
        firstImagePosition_x[1] = firstImage.getRight() + thresholdValue;
        firstImagePosition_y[0] = firstImage.getTop() - thresholdValue;
        firstImagePosition_y[1] = firstImage.getBottom() + thresholdValue;
        LogUtils.informationLog(this, "image-top = " + firstImage.getTop());
        LogUtils.informationLog(this, "image-left = " + firstImage.getLeft());
        LogUtils.informationLog(this, "image-bottom = " + firstImage.getBottom());
        LogUtils.informationLog(this, "image-right = " + firstImage.getRight());

        LogUtils.informationLog(this, "image-top = " + secondImage.getTop());
        LogUtils.informationLog(this, "image-left = " + secondImage.getLeft());
        LogUtils.informationLog(this, "image-bottom = " + secondImage.getBottom());
        LogUtils.informationLog(this, "image-right = " + secondImage.getRight());

        LogUtils.informationLog(this, "image-top = " + thirdImage.getTop());
        LogUtils.informationLog(this, "image-left = " + thirdImage.getLeft());
        LogUtils.informationLog(this, "image-bottom = " + thirdImage.getBottom());
        LogUtils.informationLog(this, "image-right = " + thirdImage.getRight());

        secondImagePosition_x[0] = secondImage.getLeft() - thresholdValue;
        secondImagePosition_x[1] = secondImage.getRight() + thresholdValue;
        secondImagePosition_y[0] = secondImage.getTop() - thresholdValue;
        secondImagePosition_y[1] = secondImage.getBottom() +thresholdValue;

        thirdImagePosition_x[0] = thirdImage.getLeft() - thresholdValue;
        thirdImagePosition_x[1] = thirdImage.getRight() + thresholdValue;
        thirdImagePosition_y[0] = thirdImage.getTop() - thresholdValue;
        thirdImagePosition_y[1] = thirdImage.getBottom() + thresholdValue;

        fourthImagePosition_x[0] = fourthImage.getLeft() - thresholdValue;
        fourthImagePosition_x[1] = fourthImage.getRight() + thresholdValue;
        fourthImagePosition_y[0] = fourthImage.getTop() - thresholdValue;
        fourthImagePosition_y[1] = fourthImage.getBottom() + thresholdValue;

        /*int[] x = new int[2];
        thirdImage.getLocationInWindow(x);
        thirdImagePosition[0] = x[0] ;//+ thirdImage.getDrawable().getMinimumWidth();
        thirdImagePosition[1] = x[1] ;//+ thirdImage.getDrawable().getMinimumHeight();*/
    }

    private void findImagePosition() {
        firstImage = (ImageView) this.findViewById(R.id.imageView);
        secondImage = (ImageView) this.findViewById(R.id.imageView2);
        thirdImage = (ImageView) this.findViewById(R.id.imageView3);
        fourthImage = (ImageView) this.findViewById(R.id.imageView4);
    }

    public void undoPreviousStep(View view) {
        Log.w(FingerLineMpower.class.getSimpleName(), "undoPreviousStep method is called...size = " + FingerLineMpower.pointList.size());
        FingerLineMpower.pointList.remove(FingerLineMpower.pointList.size() - 1);
        FingerLineMpower.undoPreviousStep();
    }

    public void saveFinalResult(View view){
        boolean firstStartingPoint = true;
        String answer = "";
        for(FingerLineMpower.PointList point : FingerLineMpower.pointList){
            LogUtils.informationLog(this, "Start point x = " + point.startX);
            LogUtils.informationLog(this, "Start point y = " + point.startY);
            LogUtils.informationLog(this, "end point x = " + point.endX);
            LogUtils.informationLog(this, "end point y = " + point.endY);

            if(firstStartingPoint){
                answer += findSelectedImageNumber(point.startX, point.startY);
            }else{
                answer += ",";
                answer += findSelectedImageNumber(point.endX, point.endY);
            }
            firstStartingPoint = false;
        }

        LogUtils.warningLog(this, "final_answer = " + answer);
        returnInformation(answer);
    }

    private int findSelectedImageNumber(double xPoint, double yPoint){
        if((xPoint >= firstImagePosition_x[0] && xPoint <= firstImagePosition_x[1]) &&
                (yPoint >= firstImagePosition_y[0] && yPoint <= firstImagePosition_y[1])){
            return 1;
        }else if((xPoint >= secondImagePosition_x[0] && xPoint <= secondImagePosition_x[1]) &&
                (yPoint >= secondImagePosition_y[0] && yPoint <= secondImagePosition_y[1])){
            return 2;
        }else if((xPoint >= thirdImagePosition_x[0] && xPoint <= thirdImagePosition_x[1]) &&
                (yPoint >= thirdImagePosition_y[0] && yPoint <= thirdImagePosition_y[1])){
            return 3;
        }else if((xPoint >= fourthImagePosition_x[0] && xPoint <= fourthImagePosition_x[1]) &&
                (yPoint >= fourthImagePosition_y[0] && yPoint <= fourthImagePosition_y[1])){
            return 4;
        }
        return 0;
    }

    private void returnInformation(String result) {
        if (result != null) {
            Intent i = new Intent();
            i.putExtra(
                    FormEntryActivity.LINE_RESULT,
                    result);
            setResult(RESULT_OK, i);
            finish();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        LineDrawActivity.this.finish();
    }
}
