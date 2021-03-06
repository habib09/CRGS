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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.mpower.clientcollection.R;
import com.mpower.clientcollection.application.ClientCollection;
import com.mpower.clientcollection.preferences.PreferencesActivity;
import com.mpower.clientcollection.provider.InstanceProviderAPI;
import com.mpower.clientcollection.provider.InstanceProviderAPI.InstanceColumns;
import com.mpower.clientcollection.receivers.NetworkReceiver;
import com.mpower.clientcollection.utilities.CompatibilityUtils;
import com.mpower.clientcollection.utilities.LogUtils;
import com.mpower.clientcollection.utilities.UserCollection;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link ClientCollectionMainActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderList extends ListActivity implements
		OnLongClickListener {

	private static final String BUNDLE_SELECTED_ITEMS_KEY = "selected_items";
	private static final String BUNDLE_TOGGLED_KEY = "toggled";

	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_SHOW_UNSENT = Menu.FIRST + 1;
	private static final int INSTANCE_UPLOADER = 0;

	private Button mUploadButton;
	private Button mToggleButton;

	private boolean mShowUnsent = true;
	private SimpleCursorAdapter mInstances;
	private ArrayList<Long> mSelected = new ArrayList<Long>();
	private boolean mRestored = false;
	private boolean mToggled = false;

	public Cursor getUnsentCursor() {
		// get all complete or failed submission instances
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ClientCollection.getAppContext());
		String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
    	String userId = UserCollection.getInstance().getUserData().getUsername() != null ? UserCollection.getInstance().getUserData().getUsername():
    		storedUsername;
    	
		String selection = "(" + InstanceColumns.STATUS + "=? or "
				+ InstanceColumns.STATUS + "=?) AND ("+ InstanceColumns.USER_ID + " == ?)";
		String selectionArgs[] = { InstanceProviderAPI.STATUS_COMPLETE,
				InstanceProviderAPI.STATUS_SUBMISSION_FAILED, userId};
		String sortOrder = InstanceColumns.DISPLAY_NAME + " ASC";
		Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	public Cursor getAllCursor() {
		// get all complete or failed submission instances
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ClientCollection.getAppContext());
		String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
    	String userId = UserCollection.getInstance().getUserData().getUsername() != null ? UserCollection.getInstance().getUserData().getUsername():
    		storedUsername;
    	
		String selection = "(" + InstanceColumns.STATUS + "=? or "
				+ InstanceColumns.STATUS + "=? or " + InstanceColumns.STATUS
				+ "=?) AND ("+ InstanceColumns.USER_ID + " == ?)";
		String selectionArgs[] = { InstanceProviderAPI.STATUS_COMPLETE,
				InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
				InstanceProviderAPI.STATUS_SUBMITTED, userId };
		String sortOrder = InstanceColumns.DISPLAY_NAME + " ASC";
		Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crgs_form_uploader);
/*
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
//		getActionBar().setTitle("SEND REPORT");

		// set up long click listener

		mUploadButton = (Button) findViewById(R.id.upload_button);
		mUploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LogUtils.informationLog(this, "*****On Upload button Click....");
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

				if (NetworkReceiver.running == true) {
					Toast.makeText(
							InstanceUploaderList.this,
							"Background send running, please try again shortly",
							Toast.LENGTH_SHORT).show();
				} else if (ni == null || !ni.isConnected()) {
					ClientCollection.getInstance().getActivityLogger()
							.logAction(this, "uploadButton", "noConnection");

					Toast.makeText(InstanceUploaderList.this,
							R.string.no_connection, Toast.LENGTH_SHORT).show();
				} else {
					ClientCollection.getInstance()
							.getActivityLogger()
							.logAction(this, "uploadButton",
									Integer.toString(mSelected.size()));

					if (mSelected.size() > 0) {
						// items selected
						uploadSelectedFiles();
						mToggled = false;
						mSelected.clear();
						InstanceUploaderList.this.getListView().clearChoices();
						mUploadButton.setEnabled(false);
					} else {
						// no items selected
						Toast.makeText(getApplicationContext(),
								getString(R.string.noselect_error),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		mToggleButton = (Button) findViewById(R.id.toggle_button);
		mToggleButton.setLongClickable(true);
		mToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// toggle selections of items to all or none
				ListView ls = getListView();
				mToggled = !mToggled;

				ClientCollection.getInstance()
						.getActivityLogger()
						.logAction(this, "toggleButton",
								Boolean.toString(mToggled));
				// remove all items from selected list
				mSelected.clear();
				for (int pos = 0; pos < ls.getCount(); pos++) {
					ls.setItemChecked(pos, mToggled);
					// add all items if mToggled sets to select all
					if (mToggled)
						mSelected.add(ls.getItemIdAtPosition(pos));
				}
				mUploadButton.setEnabled(!(mSelected.size() == 0));

			}
		});
		mToggleButton.setOnLongClickListener(this);

		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();

		String[] data = new String[] { InstanceColumns.DISPLAY_NAME,
				InstanceColumns.DISPLAY_SUBTEXT };
		int[] view = new int[] { R.id.text1, R.id.text2 };

		// render total instance view
		mInstances = new SimpleCursorAdapter(this,
				R.layout.two_item_multiple_choice, c, data, view);

		setListAdapter(mInstances);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemsCanFocus(false);
		mUploadButton.setEnabled(!(mSelected.size() == 0));

		// set title
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.send_data));

		// if current activity is being reinitialized due to changing
		// orientation restore all check
		// marks for ones selected
		if (mRestored) {
			ListView ls = getListView();
			for (long id : mSelected) {
				for (int pos = 0; pos < ls.getCount(); pos++) {
					if (id == ls.getItemIdAtPosition(pos)) {
						ls.setItemChecked(pos, true);
						break;
					}
				}

			}
			mRestored = false;
		}
		 
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

	private void uploadSelectedFiles() {
		// send list of _IDs.
		long[] instanceIDs = new long[mSelected.size()];
		for (int i = 0; i < mSelected.size(); i++) {
			instanceIDs[i] = mSelected.get(i);
		}

		Intent i = new Intent(this, InstanceUploaderActivity.class);
		i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIDs);
		startActivityForResult(i, INSTANCE_UPLOADER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ClientCollection.getInstance().getActivityLogger()
				.logAction(this, "onCreateOptionsMenu", "show");
		super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
						.setIcon(R.drawable.ic_menu_preferences),
				MenuItem.SHOW_AS_ACTION_NEVER);
		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_SHOW_UNSENT, 1, R.string.change_view)
						.setIcon(R.drawable.ic_menu_manage),
				MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			ClientCollection.getInstance().getActivityLogger()
					.logAction(this, "onMenuItemSelected", "MENU_PREFERENCES");
			createPreferencesMenu();
			return true;
		case MENU_SHOW_UNSENT:
			ClientCollection.getInstance().getActivityLogger()
					.logAction(this, "onMenuItemSelected", "MENU_SHOW_UNSENT");
			showSentAndUnsentChoices();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void createPreferencesMenu() {
		Intent i = new Intent(this, PreferencesActivity.class);
		startActivity(i);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// get row id from db
		Cursor c = (Cursor) getListAdapter().getItem(position);
		long k = c.getLong(c.getColumnIndex(InstanceColumns._ID));

		ClientCollection.getInstance().getActivityLogger()
				.logAction(this, "onListItemClick", Long.toString(k));

		// add/remove from selected list
		if (mSelected.contains(k))
			mSelected.remove(k);
		else
			mSelected.add(k);

		mUploadButton.setEnabled(!(mSelected.size() == 0));

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long[] selectedArray = savedInstanceState
				.getLongArray(BUNDLE_SELECTED_ITEMS_KEY);
		for (int i = 0; i < selectedArray.length; i++)
			mSelected.add(selectedArray[i]);
		mToggled = savedInstanceState.getBoolean(BUNDLE_TOGGLED_KEY);
		mRestored = true;
		mUploadButton.setEnabled(selectedArray.length > 0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		long[] selectedArray = new long[mSelected.size()];
		for (int i = 0; i < mSelected.size(); i++)
			selectedArray[i] = mSelected.get(i);
		outState.putLongArray(BUNDLE_SELECTED_ITEMS_KEY, selectedArray);
		outState.putBoolean(BUNDLE_TOGGLED_KEY, mToggled);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		switch (requestCode) {
		// returns with a form path, start entry
		case INSTANCE_UPLOADER:
			if (intent.getBooleanExtra(FormEntryActivity.KEY_SUCCESS, false)) {
				mSelected.clear();
				getListView().clearChoices();
				if (mInstances.isEmpty()) {
					finish();
				}
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	private void showUnsent() {
		mShowUnsent = true;
		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();
		Cursor old = mInstances.getCursor();
		try {
			mInstances.changeCursor(c);
		} finally {
			if (old != null) {
				old.close();
				this.stopManagingCursor(old);
			}
		}
		getListView().invalidate();
	}

	private void showAll() {
		mShowUnsent = false;
		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();
		Cursor old = mInstances.getCursor();
		try {
			mInstances.changeCursor(c);
		} finally {
			if (old != null) {
				old.close();
				this.stopManagingCursor(old);
			}
		}
		getListView().invalidate();
	}

	@Override
	public boolean onLongClick(View v) {
		ClientCollection.getInstance()
				.getActivityLogger()
				.logAction(this, "toggleButton.longClick",
						Boolean.toString(mToggled));
		return showSentAndUnsentChoices();
	}

	private boolean showSentAndUnsentChoices() {
		/**
		 * Create a dialog with options to save and exit, save, or quit without
		 * saving
		 */
		String[] items = { getString(R.string.show_unsent_forms),
				getString(R.string.show_sent_and_unsent_forms) };

		ClientCollection.getInstance().getActivityLogger()
				.logAction(this, "changeView", "show");

		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(getString(R.string.change_view))
				.setNeutralButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								ClientCollection.getInstance()
										.getActivityLogger()
										.logAction(this, "changeView", "cancel");
								dialog.cancel();
							}
						})
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {

						case 0: // show unsent
							ClientCollection.getInstance()
									.getActivityLogger()
									.logAction(this, "changeView", "showUnsent");
							InstanceUploaderList.this.showUnsent();
							break;

						case 1: // show all
							ClientCollection.getInstance().getActivityLogger()
									.logAction(this, "changeView", "showAll");
							InstanceUploaderList.this.showAll();
							break;

						case 2:// do nothing
							break;
						}
					}
				}).create();
		alertDialog.show();
		return true;
	}

}
