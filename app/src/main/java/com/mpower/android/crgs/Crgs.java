package com.mpower.android.crgs;

import android.app.Application;

import com.mpower.clientcollection.application.ClientCollection;

/**
 * Created by ratna on 10/7/16.
 */

public class Crgs extends ClientCollection{
    public static String FORM_AUTHORITY_NAME = "com.mpower.crgs.provider.crgs.forms";
    public static String INSTANCE_AUTHORITY_NAME = "com.mpower.crgs.provider.crgs.instances";
    private static Crgs singleton = null;


    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;
    }

    @Override
    protected void setApplicationName() {
        ClientCollection.setApplicationName("VOICE");
    }

    @Override
    protected void setAuthorityName() {
        ClientCollection.setAuthorityName(FORM_AUTHORITY_NAME, INSTANCE_AUTHORITY_NAME);
    }

    @Override
    protected void setDynActivityIntentFilter() {
        ClientCollection.setDynActivityIntentFilter("android.intent.action.mpower.snl.profile", "Profile", null);
    }

    @Override
    protected void isUseLibraryNotification() {

    }

    @Override
    protected void isUsePushNotification() {
        ClientCollection.isUsePushNotification(false);
    }

    @Override
    protected void notificationArrived(String mssage) {

    }

    public static Crgs getInstance() {
        return singleton;
    }


}
