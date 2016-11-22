package com.mpower.clientcollection.widgets;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.activities.FormEntryActivity;
import com.mpower.clientcollection.activities.WebServiceActivity;
import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.logic.FormController;
import com.mpower.clientcollection.utilities.FileUtils;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.views.MediaLayout;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by ratna on 4/10/16.
 */
public class ReArrangeWidget extends QuestionWidget{
    private static final String ROWS = "rows";

    boolean mReadOnly = false;
    private Button mGetServerInfoButton;
    protected TextView mAnswerText;
    private FormEntryPrompt fep;
    private int imagePosition = 0;
    private int numberOfImagePickted = 1;
    private String qusAnswer = "";
    private View mainView;
    private LayoutInflater li;
    Vector<SelectChoice> mItems;
    private static final String TAG = ReArrangeWidget.class.getSimpleName();

    /*public ReArrangeWidget(Context context, FormEntryPrompt prompt) {
        this(context, prompt, true);
        fep = prompt;
    }*/

    protected ReArrangeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        LogUtils.informationLog(this, "ReArrangeWidget is creating..");

        mPrompt = prompt;
        mItems = prompt.getSelectChoices();

        numberOfImagePickted = 1;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = li.inflate(R.layout.content_re_arranging, null, false);
        setQuesImage(mainView);


        mainView.findViewById(R.id.firstImageView).setOnLongClickListener(onlongClick);
        mainView.findViewById(R.id.secondImageView).setOnLongClickListener(onlongClick);
        mainView.findViewById(R.id.thirdImageView).setOnLongClickListener(onlongClick);
        mainView.findViewById(R.id.fourthImageView).setOnLongClickListener(onlongClick);

        mainView.findViewById(R.id.firstAnsImageView).setOnDragListener(onDragListener);
        /*mainView.findViewById(R.id.secondAnsImageView).setOnDragListener(onDragListener);
        mainView.findViewById(R.id.thirdAnsImageView).setOnDragListener(onDragListener);
        mainView.findViewById(R.id.fourthAnsImageView).setOnDragListener(onDragListener);*/


        Vector<Selection> ve = new Vector<Selection>();
        if (prompt.getAnswerValue() != null) {
            ve = (Vector<Selection>) prompt.getAnswerValue().getValue();
        }
        setAnsImage(ve, mainView);
        addView(mainView);
    }

    private Bitmap getImageFromUri(String imageURI){
        Bitmap b = null;
        try {
            String imageFilename = ReferenceManager._().DeriveReference(imageURI).getLocalURI();
            final File imageFile = new File(imageFilename);
            if (imageFile.exists()) {
                Display display =
                        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                                .getDefaultDisplay();
                int screenWidth = display.getWidth();
                int screenHeight = display.getHeight();
                b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
            }else{
                b = BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.doctor);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return b;
    }

    private void setQuesImage(View view){
        String imageUri =
                mPrompt.getSpecialFormSelectChoiceText(mItems.get(0),
                        FormEntryCaption.TEXT_FORM_IMAGE);
        ((ImageView)view.findViewById(R.id.firstImageView)).setImageBitmap(getImageFromUri(imageUri));
        imageUri =
                mPrompt.getSpecialFormSelectChoiceText(mItems.get(1),
                        FormEntryCaption.TEXT_FORM_IMAGE);
        ((ImageView)view.findViewById(R.id.secondImageView)).setImageBitmap(getImageFromUri(imageUri));
        imageUri =
                mPrompt.getSpecialFormSelectChoiceText(mItems.get(2),
                        FormEntryCaption.TEXT_FORM_IMAGE);
        ((ImageView)view.findViewById(R.id.thirdImageView)).setImageBitmap(getImageFromUri(imageUri));
        imageUri =
                mPrompt.getSpecialFormSelectChoiceText(mItems.get(3),
                        FormEntryCaption.TEXT_FORM_IMAGE);
        ((ImageView)view.findViewById(R.id.fourthImageView)).setImageBitmap(getImageFromUri(imageUri));
    }

    private void setAnsImage(Vector<Selection> ve, View view){
        if(ve.size() > 0 && ve.size() == 4){
            String imageUri =
                    mPrompt.getSpecialFormSelectChoiceText(mItems.get(Integer.valueOf(ve.get(0).getValue()) -1),
                            FormEntryCaption.TEXT_FORM_IMAGE);
            ((ImageView)view.findViewById(R.id.firstAnsImageView)).setImageBitmap(getImageFromUri(imageUri));
            imageUri =
                    mPrompt.getSpecialFormSelectChoiceText(mItems.get(Integer.valueOf(ve.get(1).getValue()) -1),
                            FormEntryCaption.TEXT_FORM_IMAGE);
            ((ImageView)view.findViewById(R.id.secondAnsImageView)).setImageBitmap(getImageFromUri(imageUri));
            imageUri =
                    mPrompt.getSpecialFormSelectChoiceText(mItems.get(Integer.valueOf(ve.get(2).getValue()) -1),
                            FormEntryCaption.TEXT_FORM_IMAGE);
            ((ImageView)view.findViewById(R.id.thirdAnsImageView)).setImageBitmap(getImageFromUri(imageUri));
            imageUri =
                    mPrompt.getSpecialFormSelectChoiceText(mItems.get(Integer.valueOf(ve.get(3).getValue()) -1),
                            FormEntryCaption.TEXT_FORM_IMAGE);
            ((ImageView)view.findViewById(R.id.fourthAnsImageView)).setImageBitmap(getImageFromUri(imageUri));
        }else{

            ((ImageView)view.findViewById(R.id.firstAnsImageView)).setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.circular_blank));
            ((ImageView)view.findViewById(R.id.secondAnsImageView)).setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.circular_blank));
            ((ImageView)view.findViewById(R.id.thirdAnsImageView)).setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.circular_blank));
            ((ImageView)view.findViewById(R.id.fourthAnsImageView)).setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.circular_blank));
        }
    }

    private View.OnDragListener onDragListener = new View.OnDragListener() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (action){
                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d(TAG,"ACTION_DRAG_ENDED");
                    break;
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG,"ACTION_DRAG_STARTED");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG,"ACTION_DRAG_ENTERED");
                    break;
                case  DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG,"ACTION_DRAG_EXITED");
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "ACTION_DROP");
                    numberOfImagePickted += 1;
                    enableResultImageView();
                    Log.i(TAG, "numberOfImagePickted = " + numberOfImagePickted);
                    ImageView view = (ImageView) event.getLocalState();
                    //view.setVisibility(View.INVISIBLE);//TODO
                    ImageView container = (ImageView) v;
                    container.setImageDrawable(view.getDrawable());
                    createAnswerText(view.getId());
                    return true;
            }
            return true;
        }
    };

    private void createAnswerText(int imageId){
        if(imageId == R.id.firstImageView){
            qusAnswer = qusAnswer + 0+ ",";
        }else if(imageId == R.id.secondImageView){
            qusAnswer = qusAnswer + 1 + ",";
        }else if(imageId == R.id.thirdImageView){
            qusAnswer = qusAnswer + 2 + ",";
        }else if(imageId == R.id.fourthImageView){
            qusAnswer = qusAnswer + 3 + ",";
        }
    }

    private void enableResultImageView(){
        if(numberOfImagePickted == 2){
            mainView.findViewById(R.id.secondAnsImageView).setOnDragListener(onDragListener);
        }else if(numberOfImagePickted == 3){
            LogUtils.informationLog(this, "in third listener");
            mainView.findViewById(R.id.thirdAnsImageView).setOnDragListener(onDragListener);
        }else if(numberOfImagePickted == 4){
            LogUtils.informationLog(this, "in fourth listener");
            mainView.findViewById(R.id.fourthAnsImageView).setOnDragListener(onDragListener);
        }
    }
    private View.OnLongClickListener onlongClick = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View v) {

            ClipData.Item item = new ClipData.Item("icon bitmap");
            ClipData dragData = new ClipData("icon bitmap", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},item);

            //ClipData dragData = ClipData.newPlainText("","");
            //DrawShadowBuilder drawShadowBuilder = new DrawShadowBuilder(v);
            if(numberOfImagePickted<5) {
                View.DragShadowBuilder drawShadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(dragData, drawShadowBuilder, v, 0);
                //view.setVisibility(View.INVISIBLE);
            }
            return true;
        }
    };


    @Override
    public void clearAnswer() {
        mAnswerText.setText(null);
    }

    @Override
    public void setFocus(Context context) {

    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }


    @Override
    public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
        String[] answerList = qusAnswer.split(",");
        if(answerList != null && answerList.length>1){
            LogUtils.informationLog(this, "length = " + answerList.length);
            for ( int i = 0; i < answerList.length ; ++i ) {
                vc.add(new Selection(mItems.get(Integer.valueOf(answerList[i]))));
            }
        }else if(mPrompt.getAnswerValue() != null){
            vc = (Vector<Selection>) mPrompt.getAnswerValue().getValue();
        }
        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.isAltPressed() == true) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

    }


    private void updateButtonLabelsAndVisibility(boolean b) {
    /*    if(b){
            mGetServerInfoButton.setText("Sync Again");
        }else{
            mAnswerText.setVisibility(View.GONE);
        }*/
    }

}