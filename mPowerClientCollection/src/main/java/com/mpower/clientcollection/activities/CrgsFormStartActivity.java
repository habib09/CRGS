package com.mpower.clientcollection.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.listeners.FormDownloaderListener;
import com.mpower.clientcollection.listeners.FormListDownloaderListener;
import com.mpower.clientcollection.logic.FormDetails;
import com.mpower.clientcollection.provider.FormsProviderAPI;
import com.mpower.clientcollection.tasks.DownloadFormListTask;
import com.mpower.clientcollection.tasks.DownloadFormsTask;
import com.mpower.clientcollection.utilities.Constants;
import com.mpower.clientcollection.utilities.FormButtonInfo;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.utilities.NetUtils;
import com.mpower.clientcollection.utilities.UserCollection;

import java.util.ArrayList;
import java.util.HashMap;

public class CrgsFormStartActivity extends AppCompatActivity implements FormListDownloaderListener, FormDownloaderListener {

    private ProgressBar progressBar;
    private String FORM_ID;
    private int progressStatus = 0;
    private ArrayList<FormButtonInfo> formButtonList = new ArrayList<FormButtonInfo>();
    private ArrayList<String> storedFormId = new ArrayList<String>();
    private boolean isAllFormDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crgs_form_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        formButtonList = Constants.formInfo;
        storedFormId = storedFormId();
        downloadForm();

        ((ImageButton)this.findViewById(R.id.imageButtonFirst)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar = (ProgressBar)CrgsFormStartActivity.this.findViewById(R.id.progressBarFirstForm);
                progressBar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                startFormEntryActivity("1");
            }
        });

        ((ImageButton)this.findViewById(R.id.imageButtonSecond)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar = (ProgressBar)CrgsFormStartActivity.this.findViewById(R.id.progressBarSecondForm);
                progressBar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                startFormEntryActivity("2");
            }
        });

        ((ImageButton)this.findViewById(R.id.imageButtonThird)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar = (ProgressBar)CrgsFormStartActivity.this.findViewById(R.id.progressBarThirdForm);
                progressBar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                startFormEntryActivity("3");
            }
        });

        ((ImageButton)this.findViewById(R.id.imageButtonFourth)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar = (ProgressBar)CrgsFormStartActivity.this.findViewById(R.id.progressBarFourthForm);
                progressBar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                startFormEntryActivity("4");
            }
        });
    }

    private ArrayList<String> storedFormId(){
        String sortOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        String selection = FormsProviderAPI.FormsColumns.USER_ID + "=?";
        String[] selectionArgs = {UserCollection.getInstance().getUserData().getUsername()};
        String[] data = new String[] {
                FormsProviderAPI.FormsColumns.DISPLAY_NAME, FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT, FormsProviderAPI.FormsColumns.JR_VERSION
        };
        int[] view = new int[] {
                R.id.text1, R.id.text2, R.id.text3
        };

        Cursor c = managedQuery(FormsProviderAPI.FormsColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
        ArrayList<String> list = new ArrayList<String>();
        if(c.moveToFirst()){
            int formId = c.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
            do{
                list.add(c.getString(formId));
            }while (c.moveToNext());
        }
        return list;
    }


    private void startFormEntryActivity(String formId){
        FORM_ID = formId;
        LogUtils.warningLog(this, "*****formId =  " + formId);
        String selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=?";
        String[] selectionArgs = {formId};
        //long idFormsTable = ((SimpleCursorAdapter) getListAdapter()).getItemId(position);
        String[] projection = {FormsProviderAPI.FormsColumns._ID};
        Cursor c = managedQuery(FormsProviderAPI.FormsColumns.CONTENT_URI,null, selection, selectionArgs, null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            long idFormsTable = c.getLong(c.getColumnIndex(FormsProviderAPI.FormsColumns._ID));
            String formName = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
            LogUtils.informationLog(this, "idFormsTable = " + idFormsTable);

            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, idFormsTable);

            Intent start = new Intent(Intent.ACTION_EDIT, formUri, this, FormEntryActivity.class);
            start.putExtra("formName", formName);
            start.putExtra("formId", formId);
            startActivity(start);
        }else{
            Toast.makeText(this, " Form is not available, trying to download form. Please wait a minuite", Toast.LENGTH_LONG).show();
            downloadSpecificForm();
        }
    }

    private void downloadSpecificForm(){
        String formId = FORM_ID;
        HashMap<String, FormDetails> value = UserCollection.getInstance().getUserData().getFormList();
        //HashMap<String, FormDetails> value = new HashMap<String, FormDetails>();//TODO


        if(value!=null &&value.size()>0){
            ArrayList<FormDetails> filesToDownload = new ArrayList<FormDetails>();
            //this loop to add first chapter first session only because this form have to download first
            for (FormDetails fd:value.values()) {
                LogUtils.informationLog(this, "***server formId = " + fd.formID);
               /* if (fd.formID != null && fd.formID.equalsIgnoreCase(formId)) {
                    filesToDownload.add(fd);
                    break;
                }*/

                if (getFormId(fd) != null && getFormId(fd).equalsIgnoreCase(formId)) {
                    filesToDownload.add(fd);
                    break;
                }
            }
            if(filesToDownload != null && filesToDownload.size()>0) {
                if(NetUtils.isConnected(this)) {
                    //showingProgressBar();
                    DownloadFormsTask mDownloadFormsTask = new DownloadFormsTask();
                    mDownloadFormsTask.setDownloaderListener(this);
                    mDownloadFormsTask.execute(filesToDownload);
                    //mDownloadFormsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filesToDownload);
                }else{
                    Toast.makeText(this, "Internet not available", Toast.LENGTH_SHORT).show();
                }
            }else{
               /* if(mProgressDialog != null && mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }*/
            }

        }else{
            //showingProgressBar();
            DownloadFormListTask mDownloadFormListTask = new DownloadFormListTask();
            mDownloadFormListTask.setDownloaderListener(this);
            mDownloadFormListTask.execute();
            //mDownloadFormListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void downloadForm(){
        for(int i = 0; i< formButtonList.size(); i++){
            if(!storedFormId.contains(formButtonList.get(i).getFormId())){
                isAllFormDownload = true;
                int pId = formButtonList.get(i).getProgressBarId();
                LogUtils.informationLog(this, "******pId = " + pId);

                FORM_ID = formButtonList.get(i).getFormId();
                progressBar = null;
                progressBar = (ProgressBar)this.findViewById(pId);
                progressBar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                progressStatus = 0;
                ((TextView)this.findViewById(formButtonList.get(i).getProgressTextId())).setText("Downloading");
                downloadSpecificForm();
                break;
            }else{
                ((TextView)this.findViewById(formButtonList.get(i).getProgressTextId())).setText("Updated");
            }
        }
    }

    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        progressBar.setProgress(100);
        if(isAllFormDownload){
            storedFormId = storedFormId();
            if(storedFormId.size() <= 4){
                downloadForm();
            }
        }
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        LogUtils.informationLog(this, "progressUpdate progress = " + progress);
        progressStatus += 7;
        progressBar.setProgress(progressStatus);
    }

    @Override
    public void formListDownloadingComplete(HashMap<String, FormDetails> value) {
        UserCollection.getInstance().getUserData().setFormList(value);
        downloadSpecificForm();
    }

    private String getFormId(FormDetails fd) {
        if(fd != null && fd.downloadUrl != null){
            String testVal[] = fd.downloadUrl.split("/");
            String formId = testVal[testVal.length - 1];
            LogUtils.informationLog(this, "***current form formid = " + formId);
            return formId;
        }
        return null;
    }
}
