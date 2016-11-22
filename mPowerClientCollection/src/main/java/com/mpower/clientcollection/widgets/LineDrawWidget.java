package com.mpower.clientcollection.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.activities.FormEntryActivity;
import com.mpower.clientcollection.activities.LineDrawActivity;

import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.utilities.LogUtils;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

import org.javarosa.form.api.FormEntryPrompt;

/**
 * Created by ratna on 4/12/16.
 */
public class LineDrawWidget extends QuestionWidget implements IBinaryWidget{
    private FormEntryPrompt fep;
    protected TextView mAnswerText ;
    private Button mRedrawInfoButton;

    public LineDrawWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        fep = prompt;
        String s = null;
        if (prompt.getAnswerText() != null) {
            s = prompt.getAnswerText();
        }else if(mAnswerText != null && mAnswerText.getText() != null && mAnswerText.getText().toString().length() > 0){
            s = mAnswerText.getText().toString();
        }
        LogUtils.informationLog(this, "LineDrawWidget is creating...");


            mAnswerText = new TextView(context);
            mAnswerText.setId(QuestionWidget.newUniqueId());

            mAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);

            TableLayout.LayoutParams params = new TableLayout.LayoutParams();
            params.setMargins(7, 5, 7, 5);
            mAnswerText.setLayoutParams(params);

            // needed to make long read only text scroll
            mAnswerText.setHorizontallyScrolling(false);

            mRedrawInfoButton = new Button(getContext());
            mRedrawInfoButton.setId(QuestionWidget.newUniqueId());
            mRedrawInfoButton.setPadding(20, 20, 20, 20);
            mRedrawInfoButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                    mAnswerFontsize);
            mRedrawInfoButton.setText(getResources().getString(R.string.validated_with_server));
            mRedrawInfoButton.setEnabled(!prompt.isReadOnly());
            mRedrawInfoButton.setLayoutParams(params);
            mRedrawInfoButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startLineDrawActivity();
                }
            });
        addView(mAnswerText);
        addView(mRedrawInfoButton);
        if(s != null){
            setBinaryData(s);
        }else{
            //startLineDrawActivity();
        }
    }

    private void startLineDrawActivity(){
        Intent intent = new Intent(getContext(), LineDrawActivity.class);
        //intent.putExtra("searchedValue", ans);
        ClientCollection.getInstance().getFormController()
                .setIndexWaitingForData(mPrompt.getIndex());
        ((Activity) getContext()).startActivityForResult(intent,
                FormEntryActivity.LINE_DRAW);
    }

    @Override
    public void setBinaryData(Object answer) {
        LogUtils.informationLog(this, "******************************setBinaryData = " + (String)answer);
        String s = (String)answer;
        mAnswerText.setText(s);
        addView(mAnswerText);
        addView(mRedrawInfoButton);
        //ClientCollection.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void cancelWaitingForBinaryData() {

    }

    @Override
    public boolean isWaitingForBinaryData() {
        return false;
    }

    @Override
    public IAnswerData getAnswer() {
        String s = mAnswerText.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }

    @Override
    public void clearAnswer() {

    }

    @Override
    public void setFocus(Context context) {

    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }
}
