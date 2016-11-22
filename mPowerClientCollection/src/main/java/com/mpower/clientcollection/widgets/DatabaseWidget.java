/*
 * Copyright (C) 2015 mPower Social Enterprises
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.mpower.clientcollection.widgets;

import java.util.HashMap;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.database.MpowerDatabaseHelper;
import com.mpower.clientcollection.utilities.LogUtils;

/**
 * Mpower created widget that allows set question answer from database named by interviewInfo.
 * 
 * @author ratna halder (ratnacse06@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com) 
 */
public class DatabaseWidget extends QuestionWidget implements IBinaryWidget{
	private static final String ROWS = "rows";

    boolean mReadOnly = false;    
    protected TextView mAnswerText;
    

    public DatabaseWidget(Context context, FormEntryPrompt prompt) {
    	this(context, prompt, true);
       	setupChangeListener();
    }

    @SuppressWarnings("deprecation")
	protected DatabaseWidget(Context context, FormEntryPrompt prompt, boolean derived) {
        super(context, prompt);
        LogUtils.informationLog(this, "DatabaseWidget is creating..");
        mAnswerText = new TextView(context);
        mAnswerText.setId(QuestionWidget.newUniqueId());
        mReadOnly = prompt.isReadOnly();

        mAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();

        String height = prompt.getQuestion().getAdditionalAttribute(null, ROWS);
        if ( height != null && height.length() != 0 ) {
        	try {
	        	int rows = Integer.valueOf(height);
	        	mAnswerText.setMinLines(rows);
	        	mAnswerText.setGravity(Gravity.TOP); // to write test starting at the top of the edit area
        	} catch (Exception e) {
        		Log.e(this.getClass().getName(), "Unable to process the rows setting for the answer field: " + e.toString());
        	}
        }

        params.setMargins(7, 5, 7, 5);
        mAnswerText.setLayoutParams(params);

        // needed to make long read only text scroll
        mAnswerText.setHorizontallyScrolling(false);
                
        if (mReadOnly) {
            mAnswerText.setBackgroundDrawable(null);
            mAnswerText.setFocusable(false);
            mAnswerText.setClickable(false);
        }
        addView(mAnswerText);

		String s = prompt.getAnswerText();
		if (s != null && !s.equals("")) {

			setBinaryData(s);
		} else {
			Log.d(DatabaseWidget.class.getSimpleName(), "CurrentQusAns "
					+ getCurrentQusAns());
			setBinaryData((getCurrentQusAns() != null) ? getCurrentQusAns()
					: null);
		}     	
    }
        
    private static String getCurrentQusAns(){
    	HashMap<String, String> map = new HashMap<String, String>();
    	MpowerDatabaseHelper db = new MpowerDatabaseHelper(ClientCollection.getAppContext());
    	map = db.getInterviewBasicInfo();
    	if(map != null)
    		return map.get(MpowerDatabaseHelper.INTERVIEW_INFO);
    	return null;
    }
    protected void setupChangeListener() {
        mAnswerText.addTextChangedListener(new TextWatcher() {
        	private String oldText = "";

			@Override
			public void afterTextChanged(Editable s) {
				if (!s.toString().equals(oldText)) {
					ClientCollection.getInstance().getActivityLogger()
						.logInstanceAction(this, "answerTextChanged", s.toString(),	getPrompt().getIndex());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				oldText = s.toString();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) { }
        });
    }

    @Override
    public void clearAnswer() {
        mAnswerText.setText(null);
    }


    @Override
    public IAnswerData getAnswer() {
    	clearFocus();
    	String s = mAnswerText.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    @Override
    public void setFocus(Context context) {
        // Put focus on text input field and display soft keyboard if appropriate.
        mAnswerText.requestFocus();
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!mReadOnly) {
            inputManager.showSoftInput(mAnswerText, 0);
            /*
             * If you do a multi-question screen after a "add another group" dialog, this won't
             * automatically pop up. It's an Android issue.
             *
             * That is, if I have an edit text in an activity, and pop a dialog, and in that
             * dialog's button's OnClick() I call edittext.requestFocus() and
             * showSoftInput(edittext, 0), showSoftinput() returns false. However, if the edittext
             * is focused before the dialog pops up, everything works fine. great.
             */
        } else {
            inputManager.hideSoftInputFromWindow(mAnswerText.getWindowToken(), 0);
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
    public void setOnLongClickListener(OnLongClickListener l) {
        mAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mAnswerText.cancelLongPress();
    }

	@Override
	public void setBinaryData(Object answer) {
		String s = (String)answer;
		mAnswerText.setText(s);
		
		ClientCollection.getInstance().getFormController().setIndexWaitingForData(null);	
	}

	@Override
	public void cancelWaitingForBinaryData() {
		ClientCollection.getInstance().getFormController().setIndexWaitingForData(null);
		
	}

	@Override
	public boolean isWaitingForBinaryData() {
		return mPrompt.getIndex().equals(
				ClientCollection.getInstance().getFormController()
						.getIndexWaitingForData());
	}
}