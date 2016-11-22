/*
 * Copyright (C) 2009 University of Washington
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

import java.util.ArrayList;
import java.util.Vector;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.views.MediaLayout;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Ratna Halder (ratnacse06@gmail.com)
 */
public class SelectOneButtonWidget extends QuestionWidget {

    Vector<SelectChoice> mItems; // may take a while to compute
    ArrayList<Button> buttons;
    Context context;
    String clickedButtonText;

    public SelectOneButtonWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mItems = prompt.getSelectChoices();
        buttons = new ArrayList<Button>();
        this.context = context;

        // Layout holds the vertical list of buttons
        LinearLayout buttonLayout = new LinearLayout(context);

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        if (mItems != null) {
            for (int i = 0; i < mItems.size(); i++) {
                Button r = new Button(getContext());
                r.setText(prompt.getSelectChoiceText(mItems.get(i)));
                r.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
                r.setTag(Integer.valueOf(i));
                r.setId(QuestionWidget.newUniqueId());
                LogUtils.informationLog(this, "first_id = " + r.getId());
                r.setEnabled(!prompt.isReadOnly());
                r.setFocusable(!prompt.isReadOnly());
                r.setOnClickListener(onButtonClicked);

                buttons.add(r);

                if (mItems.get(i).getValue().equals(s)) {
                    //r.setChecked(true);TODO
                    clickedButtonText = r.getText().toString();
                    r.setBackgroundColor(Color.GREEN);
                }else{
                    r.setBackgroundColor(Color.GRAY);
                }

                String audioURI = null;
                audioURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
                        FormEntryCaption.TEXT_FORM_AUDIO);

                String imageURI = null;
                imageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
                        FormEntryCaption.TEXT_FORM_IMAGE);

                String videoURI = null;
                videoURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
                        "video");

                String bigImageURI = null;
                bigImageURI = prompt.getSpecialFormSelectChoiceText(
                        mItems.get(i), "big-image");

                MediaLayout mediaLayout = new MediaLayout(getContext());
                mediaLayout.setAVT(prompt.getIndex(), "." + Integer.toString(i), r, audioURI, imageURI,
                        videoURI, bigImageURI);

                if (i != mItems.size() - 1) {
                    // Last, add the dividing line (except for the last element)
                    ImageView divider = new ImageView(getContext());
                    divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                    mediaLayout.addDivider(divider);
                }
                buttonLayout.addView(mediaLayout);
            }
        }
        buttonLayout.setOrientation(LinearLayout.VERTICAL);

        // The buttons take up the right half of the screen
        LayoutParams buttonParams = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(40, 50, 40, 0);
        addView(buttonLayout, buttonParams);
    }

    @Override
    public void clearAnswer() {
        for (Button button : this.buttons) {
            /*if (button.isChecked()) {
                button.setChecked(false);
                return;
            }*/ //TODO
        }
    }

    @Override
    public IAnswerData getAnswer() {
        int i = getCheckedId();
        if (i == -1) {
            return null;
        } else {
            SelectChoice sc = mItems.elementAt(i);
            return new SelectOneData(new Selection(sc));
        }
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    public int getCheckedId() {
        for (int i = 0; i < buttons.size(); ++i) {
            Button button = buttons.get(i);
            if (button.getText().toString().equalsIgnoreCase(clickedButtonText)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (Button r : buttons) {
            r.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (Button button : this.buttons) {
            button.cancelLongPress();
        }
    }

    /*@Override
    public void onClick(View v) {
        LogUtils.informationLog(this, "Changing button color....");
        Button buttonView = (Button)v;
        for (Button button : buttons ) {
            if (!(buttonView == button)) {
                button.setBackgroundColor(Color.GRAY);
            }else {
                LogUtils.informationLog(this, "Changing button color....2222");
                button.setBackgroundColor(Color.GREEN);
                clickedButtonId = button.getId();
            }
        }

        ClientCollection.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged",
                mItems.get((Integer)buttonView.getTag()).getValue(), mPrompt.getIndex());
    }*/

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        LogUtils.informationLog(this, "Changing button color....setOnClickListener");
    }

    OnClickListener onButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            LogUtils.informationLog(this, "Changing button color....");
            Button buttonView = (Button)v;
            for (Button button : buttons ) {
                if (!(buttonView == button)) {
                    button.setBackgroundColor(Color.GRAY);
                }else {
                    LogUtils.informationLog(this, "Changing button color....2222");
                    button.setBackgroundColor(Color.GREEN);
                    clickedButtonText = button.getText().toString();
                }
            }

            ClientCollection.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged",
                    mItems.get((Integer)buttonView.getTag()).getValue(), mPrompt.getIndex());
        }
    };
}
