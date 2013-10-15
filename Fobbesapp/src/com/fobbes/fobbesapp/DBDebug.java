package com.fobbes.fobbesapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DBDebug extends Activity {

	// Item List Table Vars
	public DatabaseManager db = new DatabaseManager(this);
	public TextView lister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_input);
		
		
		
		lister = (TextView) findViewById(R.id.DBList);
		lister.setTextColor(android.graphics.Color.LTGRAY);
		listSurvey();
		
		LinearLayout dbl = (LinearLayout) findViewById(R.id.DBLister);
		
		TextView tv1 = new TextView(this);
		tv1.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		//tv1.setTextSize();
		tv1.setText("Test 1 - Font Regular");
		TextView tv2 = new TextView(this);
		tv2.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		tv2.setTextSize(10);
		tv2.setText("Test 2 - Font 10");
		TextView tv3 = new TextView(this);
		tv3.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		tv3.setTextSize(14);
		tv3.setText("Test 3 - Font 14");
		TextView tv4 = new TextView(this);
		tv4.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		tv4.setTextSize(14 * getResources().getDisplayMetrics().density);
		tv4.setText("Test 4 - Font 14 multiply density" + getResources().getDisplayMetrics().density);
		TextView tv5 = new TextView(this);
		tv5.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		tv5.setTextSize(14 / getResources().getDisplayMetrics().density);
		tv5.setText("Test 5 - Font 14 divided density" + getResources().getDisplayMetrics().density);
		
		dbl.addView(tv1);
		dbl.addView(tv2);
		dbl.addView(tv3);
		dbl.addView(tv4);
		dbl.addView(tv5);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			main(event);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	// Main Activity Event
	public void main(KeyEvent event) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public void listSurvey() {
		db.open();
		Cursor a = db.getAllPolls();
		a.moveToFirst();
		String ListDB = String.valueOf(a.getCount());
		String NameTest = ListDB + " Polls recorded" + "\n" + "\n";
		String line;
		if (a.getCount() > 0) {
			do {
				line = a.getString(0) + " " + a.getString(1) + " "
						+ a.getString(2) + "\n";
				NameTest = NameTest + line;
				// a.moveToNext();
			} while (a.moveToNext());
			NameTest = NameTest + "\nItems \n\n";
			a = db.getAllItems();
			a.moveToFirst();
			if (a.getCount() > 0){
				do{	
					line = a.getString(0) + " " +a.getString(1)+" "+a.getString(2)+" "+a.getString(3)+
							" "+a.getString(4)+" "+a.getString(5)+" "+a.getString(6)+" "+a.getString(7)+" "+a.getString(8)+"\n";
					NameTest = NameTest + line;
				} while (a.moveToNext());
			}
			else{
				toastwarning("No items");
			}
			NameTest = NameTest + "\nInputs \n\n";
			a = db.getAllInputs();
			a.moveToFirst();
			if(a.getCount()>0){
				do{
					line = a.getString(0)+" "+a.getString(1)+" "+a.getString(2)+" "+a.getString(3)+" "+a.getString(4)+"\n";
					NameTest = NameTest + line;
				}while (a.moveToNext());
			}
			else{
				toastwarning("No inputs");				
			}

			lister.setText(NameTest);
		} else
			toastwarning("No Database");
		db.close();
	}
	
	public void dbDumpConfirm(View view){
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
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ "com.fobbes.fobbesapp" +"//databases//"+"FobbesApp";
                String backupDBPath = "FobbesApp";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();


        }
	}
	
	public void dbImportConfirm(View view){
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
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ "com.fobbes.fobbesapp" +"//databases//"+"FobbesApp";
                String backupDBPath = "FobbesApp";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();


        }
	}
	
	public void createSampleTest(View view)
	{
		
		new CreateSample().execute();
				
	}
	
	public void createSample(){
		
		Random r = new Random();
		db.open();
		long row = db.addPoll("Sample", 0);
		long measRow = db.addItem("MeasureSample", 1, "Sample Unit", 0, 0, 1, row, -256);
		long scaleRow = db.addItem("ScaleSample", 2, "", 1, 10, 1, row, -256);
		long incRow = db.addItem("IncrementSample", 3, "", 0, 0, 1, row, -256);
		long currentTime = System.currentTimeMillis();
		long lastTime = currentTime - (3600000l)*99;
		for(int i = 0; i< 100; i++){
			long newTime = ((r.nextInt(3600000)));
			db.addInput(r.nextInt(100), ""+(lastTime + newTime), "Sample Note", +measRow);
			lastTime = lastTime+newTime;
		}
		lastTime = currentTime - (3600000l)*99;
		for(int i = 0; i<100; i++){
			long newTime = ((r.nextInt(3600000)));
			db.addInput(r.nextInt(10)+1, ""+(lastTime + newTime), "Sample Note", +scaleRow);
			lastTime = lastTime+ newTime;
		}
		lastTime = currentTime - (3600000l)*99;
		for(int i = 0; i<100; i++){
			long newTime = ((r.nextInt(3600000)));
			db.addInput(r.nextInt(100), ""+(lastTime + newTime), "Sample Note", +incRow);
			lastTime = lastTime + newTime;
		}
		
	}


	// GENERIC TOAST POPUP, STATIC TEXT ONLY
	public void toastwarning(String texthere) {
		Toast.makeText(this, texthere, Toast.LENGTH_SHORT).show();
	}

	 private class CreateSample extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			createSample();
			return null;
		}
		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}
		@Override
		protected void onPostExecute(Void result) {
			setProgressBarIndeterminateVisibility(false);
		}
	 }
	
}
