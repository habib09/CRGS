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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.preferences.PreferencesActivity;
import com.mpower.clientcollection.provider.InstanceProviderAPI;
import com.mpower.clientcollection.provider.InstanceProviderAPI.InstanceColumns;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.utilities.UserCollection;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceChooserList extends ListActivity {

    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
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
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.review_data));
        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setVisibility(View.GONE);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ClientCollection.getAppContext());
		String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
    	String userId = UserCollection.getInstance().getUserData().getUsername() != null ? UserCollection.getInstance().getUserData().getUsername():
    		storedUsername;
    	
        String selection = InstanceColumns.STATUS + " != ? AND " + InstanceColumns.USER_ID + " == ?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED, userId};
        String sortOrder = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " ASC";
        Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);

        String[] data = new String[] {
                InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT
        };
        int[] view = new int[] {
                R.id.text1, R.id.text2
        };

        // render total instance view
        SimpleCursorAdapter instances =
            new SimpleCursorAdapter(this, R.layout.two_item, c, data, view);
        setListAdapter(instances);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Cursor c = (Cursor) getListAdapter().getItem(position);
        startManagingCursor(c);
        Uri instanceUri =
            ContentUris.withAppendedId(InstanceColumns.CONTENT_URI,
                c.getLong(c.getColumnIndex(InstanceColumns._ID)));
        LogUtils.informationLog(this, "****Instance uri = " + instanceUri);

        ClientCollection.getInstance().getActivityLogger().logAction(this, "onListItemClick", instanceUri.toString());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, new Intent().setData(instanceUri));
        } else {
            // the form can be edited if it is incomplete or if, when it was
            // marked as complete, it was determined that it could be edited
            // later.
            String status = c.getString(c.getColumnIndex(InstanceColumns.STATUS));
            String strCanEditWhenComplete =
                c.getString(c.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE));

            boolean canEdit = status.equals(InstanceProviderAPI.STATUS_INCOMPLETE)
                	           || Boolean.parseBoolean(strCanEditWhenComplete);
            if (!canEdit) {
            	createErrorDialog(getString(R.string.cannot_edit_completed_form),
                    	          DO_NOT_EXIT);
            	return;
            }
            // caller wants to view/edit a form, so launch formentryactivity
            //startActivity(new Intent(Intent.ACTION_EDIT, instanceUri));
            //TODO previous syntax would be used to FormEntryActivity via mimetype and view/edit
        	
        	//MPOWER: But, When two or more application uses the library project a serious problem is occurred,
        	//will show two project list which used the library project or FormEntryActivity with same mimetype.
        	//So, we can overcome the problem via below intent syntax.
            Intent start = new Intent(Intent.ACTION_EDIT, instanceUri, InstanceChooserList.this, FormEntryActivity.class);
            startActivity(start);

        
        }
        finish();
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
