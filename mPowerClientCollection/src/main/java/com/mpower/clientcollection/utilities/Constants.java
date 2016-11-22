package com.mpower.clientcollection.utilities;

import com.mpower.clientcollection.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ratna on 10/6/16.
 */

public class Constants {
    public static final HashMap<String, Integer> map = new HashMap<String, Integer>() {
        {
            put("1", R.drawable.water_icon);
            put("2", R.drawable.sanitation_icon);
            put("3", R.drawable.environment_icon);
            put("4", R.drawable.sports_icon);
        }
    };

    public static final ArrayList<FormButtonInfo> formInfo = new ArrayList<FormButtonInfo>(){
        {
            add(new FormButtonInfo("1", R.id.progressBarFirstForm, R.id.water_download_text));
            add(new FormButtonInfo("2", R.id.progressBarSecondForm, R.id.sanitation_download_text));
            add(new FormButtonInfo("3", R.id.progressBarThirdForm, R.id.environment_download_text));
            add(new FormButtonInfo("4", R.id.progressBarFourthForm, R.id.sports_download_text));

        }
    };
}
