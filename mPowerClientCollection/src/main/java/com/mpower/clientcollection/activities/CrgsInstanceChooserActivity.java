package com.mpower.clientcollection.activities;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.listeners.DeleteFormsListener;
import com.mpower.clientcollection.listeners.DeleteInstancesListener;
import com.mpower.clientcollection.preferences.PreferencesActivity;
import com.mpower.clientcollection.provider.InstanceProvider;
import com.mpower.clientcollection.provider.InstanceProviderAPI;
import com.mpower.clientcollection.tasks.DeleteFormsTask;
import com.mpower.clientcollection.tasks.DeleteInstancesTask;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.utilities.UserCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CrgsInstanceChooserActivity extends AppCompatActivity implements DeleteInstancesListener {
    private List<HashMap<String, String>> instanceList;
    private ArrayList<Long> mSelected = new ArrayList<Long>();
    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private AlertDialog mAlertDialog;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crgs_instance_chooser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        instanceList = new ArrayList<HashMap<String, String>>();
        updateListView();
    }

    private void updateListView(){
        createList();
        mListView = (ListView)findViewById(R.id.instanceList);

        SimpleAdapter adapter = new SimpleAdapter(this, instanceList, R.layout.crgs_instance_chooser_list, new String[] { InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT},
                new int[] { R.id.instance_name, R.id.instance_date});

        mListView.setAdapter(adapter);
    }

    private void createList(){
        instanceList = new ArrayList<HashMap<String, String>>();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ClientCollection.getAppContext());
        String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        String userId = UserCollection.getInstance().getUserData().getUsername() != null ? UserCollection.getInstance().getUserData().getUsername():
                storedUsername;

        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ? AND " + InstanceProviderAPI.InstanceColumns.USER_ID + " == ?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED, userId};
        String sortOrder = InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";
        Cursor c = managedQuery(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);

        if(c!=null && c.moveToFirst()){
            int id = c.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);
            int name = c.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
            int subtext = c.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT);
            int status = c.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS);
            do{
                LogUtils.informationLog(this, "****status = " + c.getString(subtext));
                String test = "on";
                String[] finalStatus = c.getString(subtext).split(test);
                String mergedStatus = finalStatus[0] + " on" + "\n" + finalStatus[1];
                LogUtils.informationLog(this, "****status after split= " + mergedStatus);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(InstanceProviderAPI.InstanceColumns._ID, String.valueOf(c.getLong(id)));
                map.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, c.getString(name));
                map.put(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT, c.getString(subtext));
                map.put(InstanceProviderAPI.InstanceColumns.STATUS, c.getString(status));
                map.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID)));
                instanceList.add(map);
            }while (c.moveToNext());
        }
    }

    public void onClickReview(View view){
        int position = mListView.getPositionForView((View) view.getParent());
        Uri instanceUri =
                ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI,Long.valueOf(instanceList.get(position).get(InstanceProviderAPI.InstanceColumns._ID)));
        LogUtils.informationLog(this, "****Instance uri = " + instanceUri);
        String formId = instanceList.get(position).get(InstanceProviderAPI.InstanceColumns.JR_FORM_ID);
        String formName = instanceList.get(position).get(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);

        ClientCollection.getInstance().getActivityLogger().logAction(this, "onListItemClick", instanceUri.toString());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, new Intent().setData(instanceUri));
        } else {
            // the form can be edited if it is incomplete or if, when it was
            // marked as complete, it was determined that it could be edited
            // later.
            String status = instanceList.get(position).get(InstanceProviderAPI.InstanceColumns.STATUS);
            String strCanEditWhenComplete = "true";
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
            Intent start = new Intent(Intent.ACTION_EDIT, instanceUri, CrgsInstanceChooserActivity.this, FormEntryActivity.class);
            start.putExtra("formName", formName);
            start.putExtra("formId", formId);
            startActivity(start);
        }
       // finish();
    }

    public void onClickDelete(View view){
        int position = mListView.getPositionForView((View) view.getParent());
        Long instanceId = Long.valueOf(instanceList.get(position).get(InstanceProviderAPI.InstanceColumns._ID));
        mSelected.add(instanceId);
        DeleteInstancesTask mDeleteInstancesTask = new DeleteInstancesTask();
        mDeleteInstancesTask.setContentResolver(getContentResolver());
        mDeleteInstancesTask.setDeleteListener(this);
        mDeleteInstancesTask.execute(mSelected.toArray(new Long[mSelected
                .size()]));
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


    @Override
    public void deleteComplete(int deletedForms) {

        ClientCollection.getInstance().getActivityLogger().logAction(this, "deleteComplete", Integer.toString(deletedForms));
        if (deletedForms == mSelected.size()) {
            // all deletes were successful
            Toast.makeText(getApplicationContext(),
                    getString(R.string.file_deleted_ok, deletedForms),
                    Toast.LENGTH_SHORT).show();
        } else {
            // had some failures

            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.file_deleted_error, mSelected.size()
                            - deletedForms, mSelected.size()),
                    Toast.LENGTH_LONG).show();
        }

        mSelected.clear();
        updateListView();
    }
}
