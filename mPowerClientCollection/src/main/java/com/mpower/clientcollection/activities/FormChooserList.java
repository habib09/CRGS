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

package com.mpower.clientcollection.activities;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.listeners.DiskSyncListener;
import com.mpower.clientcollection.preferences.PreferencesActivity;
import com.mpower.clientcollection.provider.FormsProviderAPI.FormsColumns;
import com.mpower.clientcollection.tasks.DiskSyncTask;
import com.mpower.clientcollection.utilities.UserCollection;
import com.mpower.clientcollection.utilities.VersionHidingCursorAdapter;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores the path to
 * selected form for use by {@link ClientCollectionMainActivity}.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */

public class FormChooserList extends ListActivity implements DiskSyncListener {

    private static final String t = "FormChooserList";
    private static final boolean EXIT = true;
    private static final String syncMsgKey = "syncmsgkey";

    private DiskSyncTask mDiskSyncTask;

    private AlertDialog mAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // must be at the beginning of any activity that can be called from an external intent
        try {        	
            ClientCollection.createApplicationDir();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }            
        
        setContentView(R.layout.chooser_list_layout);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.enter_data));

        String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";

        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ClientCollection.getAppContext());
		String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
    	String userId = UserCollection.getInstance().getUserData().getUsername() != null ? UserCollection.getInstance().getUserData().getUsername():
    		storedUsername;
    	String selection = FormsColumns.USER_ID + "=?";
    	String[] selectionArgs = {userId};
        String[] data = new String[] {
                FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_SUBTEXT, FormsColumns.JR_VERSION
        };
        int[] view = new int[] {
                R.id.text1, R.id.text2, R.id.text3
        };

        Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
        // render total instance view
        SimpleCursorAdapter instances =
            new VersionHidingCursorAdapter(FormsColumns.JR_VERSION, this, R.layout.two_item, c, data, view);
        setListAdapter(instances);

        if (savedInstanceState != null && savedInstanceState.containsKey(syncMsgKey)) {
            TextView tv = (TextView) findViewById(R.id.status_text);
            tv.setText(savedInstanceState.getString(syncMsgKey));
        }

        // DiskSyncTask checks the disk for any forms not already in the content provider
        // that is, put here by dragging and dropping onto the SDCard
        mDiskSyncTask = (DiskSyncTask) getLastNonConfigurationInstance();
        if (mDiskSyncTask == null) {
            Log.i(t, "Starting new disk sync task");
            mDiskSyncTask = new DiskSyncTask();
            mDiskSyncTask.setDiskSyncListener(this);
            mDiskSyncTask.execute((Void[]) null);
        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        // pass the thread on restart
        return mDiskSyncTask;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView tv = (TextView) findViewById(R.id.status_text);
        outState.putString(syncMsgKey, tv.getText().toString());
    }


    /**
     * Stores the path of selected form and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // get uri to form
    	long idFormsTable = ((SimpleCursorAdapter) getListAdapter()).getItemId(position);
        Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, idFormsTable);

		ClientCollection.getInstance().getActivityLogger().logAction(this, "onListItemClick", formUri.toString());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, new Intent().setData(formUri));
        } else {
            // caller wants to view/edit a form, so launch formentryactivity
            //startActivity(new Intent(Intent.ACTION_EDIT, formUri));
        	//TODO previous syntax would be used to FormEntryActivity via mimetype and view/edit
        	
        	//MPOWER: But, When two or more application uses the library project a serious problem is occurred,
        	//will show two project list which used the library project or FormEntryActivity with same mimetype.
        	//So, we can overcome the problem via below intent syntax.
            Intent start = new Intent(Intent.ACTION_EDIT, formUri, FormChooserList.this, FormEntryActivity.class);
            startActivity(start);
        }

        finish();
    }


    @Override
    protected void onResume() {
        mDiskSyncTask.setDiskSyncListener(this);
        super.onResume();

        if (mDiskSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
        	SyncComplete(mDiskSyncTask.getStatusMessage());
        }
    }


    @Override
    protected void onPause() {
        mDiskSyncTask.setDiskSyncListener(null);
        super.onPause();
    }


    @Override
    protected void onStart() {
    	super.onStart();
		ClientCollection.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
		ClientCollection.getInstance().getActivityLogger().logOnStop(this);
    	super.onStop();
    }


    /**
     * Called by DiskSyncTask when the task is finished
     */
    @Override
    public void SyncComplete(String result) {
        Log.i(t, "disk sync task complete");
        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setText(result);
    }


    /**
     * Creates a dialog with the given message. Will exit the activity when the user preses "ok" if
     * shouldExit is set to true.
     *
     * @param errorMsg
     * @param shouldExit
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {

    	ClientCollection.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");

        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                    	ClientCollection.getInstance().getActivityLogger().logAction(this, "createErrorDialog",
                    			shouldExit ? "exitApplication" : "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }
}
