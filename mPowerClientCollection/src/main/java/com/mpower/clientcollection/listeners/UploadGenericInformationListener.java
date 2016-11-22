package com.mpower.clientcollection.listeners;

/**
 * Created by ratna on 4/4/16.
 */
public interface UploadGenericInformationListener {
    void uploadCompleted(String serverResponse);
    void showErrorMsg(String errorMsg);
}
