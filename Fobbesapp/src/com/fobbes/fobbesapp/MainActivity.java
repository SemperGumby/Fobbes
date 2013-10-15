/*
 * FOBBESAPP-MainActivity.java
 * Programmer: Charles Schuh
 * Main Menu, branching into each mode of execution: Input, Trendline Viewing, and Survey Creation
 */
package com.fobbes.fobbesapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class MainActivity extends Activity {

	// Menu Vars
	static final private int DELETE_M = Menu.FIRST;
	static final private int DBBU_M = Menu.FIRST+1;
	static final private int DBIMPORT_M = Menu.FIRST+2;
	static final private int DEBUG_M = Menu.FIRST+3;

	
	// License
	private LicenseChecker mChecker;
	private LicenseCheckerCallback mLicenseCheckerCallback;
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiQaZeDVowBS2uU6qqw5YvhQBKppYgXcRMhGcZ7KqJNXSL9W2PeTi7tkwhmzoOZ/bNGBehEgQx90W/dXH2wXk8XRd45Loon3mjqse28shEgomCjuFdTjCEMNckEyn76I1hG+CFgku9TyQ7IeulkpYeTvApTqdoQlWpTIsiTiG/nyftYdBEx1TgcoZgOQEqoHa9BTLBQ689uh14e+5HJedCutTrpyTzkiXZwGuit1fkCvaE4SoyXnj5ZhrEo/O5H/8SQGPAal3MuJ8dulwRMu+FyiBVXkNJrp++sGx01hvl6ipiVJ1JGznrkScZNtEiDDTWDJIcxnw6xtjY6iov5zWywIDAQAB";
	private static final byte[] SALT = new byte[]{-18, 56, 21, -75, 65, -47, 69, -90, -14, 58, 73,
			-29, 43, -45, 81, 20, 12, 71, -85, 67};
	private AESObfuscator mObfuscator;
	private String androidID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		
		//Block Out for free version
		PackageInfo info;
		 try {
		     info = getPackageManager().getPackageInfo("com.fobbes.fobbesapp", PackageManager.GET_SIGNATURES);
		     for (Signature signature : info.signatures) {
		         MessageDigest mD;
		         mD = MessageDigest.getInstance("SHA");
		         mD.update(signature.toByteArray());
		         String keyString = new String(Base64.encode(mD.digest(), 0));
		         Log.e("hash key", keyString);
		     }
		 } catch (NameNotFoundException e1) {
		     Log.e("name not found", e1.toString());
		 } catch (NoSuchAlgorithmException e) {
		     Log.e("no such an algorithm", e.toString());
		 } 

		androidID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		mObfuscator = new AESObfuscator(SALT, getPackageName(), androidID);
		ServerManagedPolicy serverPolicy = new ServerManagedPolicy(this, mObfuscator);
		mLicenseCheckerCallback = new MyLicenseCheckerCallback();
		mChecker = new LicenseChecker(this, serverPolicy, BASE64_PUBLIC_KEY);
		
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		
		boolean licenseIsCached = sharedPref.getBoolean("License", false);
		
		if(licenseIsCached){
			mChecker.checkAccess(mLicenseCheckerCallback);
		}
		else{
			mainMenu();
		}
		
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	}

	public void mainMenu() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		overridePendingTransition(0, 0);
		DatabaseManager dB = new DatabaseManager(this);
		dB.open();
		Cursor a = dB.getAllPolls();
		a.moveToFirst();
		if (a.getCount() > 0) {
			//User has created polls, open main menu
			setContentView(R.layout.activity_main);
			RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.Layout);
			mainLayout.setBackgroundColor(android.graphics.Color.parseColor("#333333"));//TODO: Write this into layout xml
		} else {
			//User has not yet created a poll, start the CreateSurvey activity
			startIntro();
		}
		a.close();
		dB.close();

	}
	// ANDROID MENU ACTIONS
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		menu.add(0, DELETE_M, 0, "Delete Database");
		menu.add(0, DBBU_M, 0, "Create Backup");
		menu.add(0, DBIMPORT_M, 0, "Restore Backup");
		//menu.add(0, DEBUG_M, 0, "Debug");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case DELETE_M :
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Confirm");
				builder.setMessage("This will delete any data that has not been backed up. Are you sure you want to do this?");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           @Override
					public void onClick(DialogInterface dialog, int id) {
			        	   deleteDatabase("FobbesApp");
			        	   toastMessage("Database Deleted");
			               dialog.cancel();
			           }
			       });
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			           @Override
					public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			        	   dialog.cancel();
			           }
			       });
				AlertDialog dialog = builder.create();
				dialog.show();				
				return true;
				
			case DBBU_M :
				dbDumpConfirm(null);
				return true;
			case DBIMPORT_M :
				dbImportConfirm(null);
				return true;
				
			case DEBUG_M :
				SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		    	SharedPreferences.Editor editor = sharedPref.edit();
		    	editor.putBoolean("Licensed", false);
		    	editor.commit();
				dbDebug(null);
				
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void input(View view) {
		//Starts the Input activity
		Intent intent = new Intent(this, InputActivity.class);
		startActivity(intent);
	}

	public void trendline(View view) {
		//Starts the Trendline activity
		Intent intent = new Intent(this, TrendlineActivity.class);
		startActivity(intent);
	}
	public void help(View view) {
		//Starts the Help activity
		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}
	public void startIntro() {
		//Starts the CreateSurvey activity
		Intent intent = new Intent(this, CreateSurveyActivity.class);
		startActivity(intent);
	}
	public void dbDebug(View view) {
		//Starts the Debug activity
		Intent intent = new Intent(this, DBDebug.class);
		startActivity(intent);
	}

	
	public void toastMessage(String texthere) {
		//Convenience method for Toast.makeText
		Toast.makeText(this, texthere, Toast.LENGTH_SHORT).show();
	}

	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		//TODO: Make this a separate outer class
		@Override
		public void allow(int policyReason) {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// Should allow user access.
			runOnUiThread(new Runnable() {
			    @Override
				public void run() {
			    	SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
			    	SharedPreferences.Editor editor = sharedPref.edit();
			    	editor.putBoolean("Licensed", true);
			    	editor.commit();
			    	
			    	mainMenu();
			    }
			});
		}

		@Override
		public void dontAllow(int policyReason) {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// Should not allow access. In most cases, the app should assume
			// the user has access unless it encounters this. If it does,
			// the app should inform the user of their unlicensed ways
			// and then either shut down the app or limit the user to a
			// restricted set of features.
			// In this example, we show a dialog that takes the user to Market.
			// If the reason for the lack of license is that the service is
			// unavailable or there is another problem, we display a
			//toastwarning("License Failed");
			showDialog(0);
		}

		@Override
		public void applicationError(int errorCode) {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// This is a polite way of saying the developer made a mistake
			// while setting up or calling the license checker library.
			// Please examine the error code and fix the error.
			toastMessage("Application Error");
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		//TODO: Figure out what this method is for and where it should go, in relation to licensing
		//Alert dialog for unlicensed use
		return new AlertDialog.Builder(this)
				.setTitle("Application Not Licensed")
				.setCancelable(false)
				.setMessage(
						"This application is not licensed. Please purchase it from Android Market")
				.setPositiveButton("Buy App", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri
								.parse("market://details?id=" + getPackageName()));
						startActivity(marketIntent);
						finish();
					}
				}).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create();
	}
	public void dbDumpConfirm(View view){
		//Confirm dialogue for database backup
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm");
		builder.setMessage("This will overwrite your backup. Are you sure you want to do this?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           @Override
			public void onClick(DialogInterface dialog, int id) {
	               dbDump();
	               dialog.cancel();
	           }
	       });
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           @Override
			public void onClick(DialogInterface dialog, int id) {
	               // User cancelled the dialog
	        	   dialog.cancel();
	           }
	       });
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void dbDump(){
		//Backs up the database to externalStorage/insightdata/insight_backup
		//TODO: make separate class or fold into DatabaseManager
		try {			
			File direct = new File(Environment.getExternalStorageDirectory() + "/insightdata");
			if (!direct.exists()) {
				if (direct.mkdir()) {
				}
			}
			
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ "com.fobbes.fobbesapp" +"//databases//"+"FobbesApp";
                String backupDBPath = "//insightdata//insight_backup";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();
            }
        } 
		catch (IOException e) {
			//TODO: make this message more meaningful
            toastMessage(e.toString());
        }
	}
	
	public void dbImportConfirm(View view){
		//Dialog for confirming backup import
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm");
		builder.setMessage("This will overwrite your current polling data. Are you sure you want to do this?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           @Override
			public void onClick(DialogInterface dialog, int id) {
	               dbImport();
	               dialog.cancel();
	           }
	       });
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           @Override
			public void onClick(DialogInterface dialog, int id) {
	               // User cancelled the dialog
	        	   dialog.cancel();
	           }
	       });
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void dbImport(){
		//Imports a database stored in externalStorage/insightdata/insight_backup
		//TODO: make separate class or fold into DatabaseManager
		try {			
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ "com.fobbes.fobbesapp" +"//databases//"+"FobbesApp";
                String backupDBPath = "//insightdata//insight_backup";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();
            }
        } 
		catch (IOException e) {
           toastMessage(e.toString());
        }
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mChecker.onDestroy();
	}
	
}
