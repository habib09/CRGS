package com.mpower.clientcollection.utilities;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by ratna on 5/24/16.
 */
public class ToastUtils {
    /**
     * Displaying toast message in the middle of the mobile or tablet screen.
     */
    public static final void showToastMsg(Context context, String message){
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
