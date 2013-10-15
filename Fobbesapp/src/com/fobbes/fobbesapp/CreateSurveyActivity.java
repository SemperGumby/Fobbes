/*
 * FOBBESAPP-CreateSurveyActivity.java
 * Programmers: Charles Schuh, Ryan McKee
 * Activity which handles user generation of surveys
 */
package com.fobbes.fobbesapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;

import com.fobbes.fobbesapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class CreateSurveyActivity extends Activity implements OnTouchListener {

	// Color
	public int uicolor = android.graphics.Color.parseColor("#696969");
	public int fontcolor = android.graphics.Color.parseColor("#E0E0E0");
	public int itemcolor = android.graphics.Color.WHITE;
	public int trendcolor;

	// vars
	private int numItems;
	private ViewFlipper flipLayout;

	private LinearLayout item;
	public LinearLayout itemcorners;
	public LinearLayout itemspace;
	public LinearLayout itemborder;
	public EditText itemNameField;
	private int viewid;

	// Fields
	public EditText nameField;
	public EditText unitsField;
	public Button submitButton;
	public Button addItemB;
	public Button addPollB;

	public RadioGroup pollSched;
	public RadioGroup itemSched;
	public RadioGroup dataType;
	public EditText maxscaleField;
	public EditText minscaleField;

	// UI
	public float fontsize = 30.0f;

	static final private int CREATEPOLL_T = Menu.FIRST;
	static final private int IMPORT = Menu.FIRST + 1;
	static final private int EXPORT = Menu.FIRST + 2;
	static final private int DELETEPOLL_T = Menu.FIRST + 3;

	private float downXValue;
	private float downYValue;

	// Poll
	public String pollTitle;
	Intent intent = getIntent();
	public int pollschedID;

	public int poll_id_history = 1;

	// Item List DB Vars
	public DatabaseManager db = new DatabaseManager(this);
	Cursor p;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("Edit Surveys");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		overridePendingTransition(0, 0);
		createLayout();
	}

	@SuppressWarnings("deprecation")
	public void createLayout() {
		numItems = 0;
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// ViewFlipper Base
		flipLayout = new ViewFlipper(this);
		flipLayout.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
		flipLayout.setLayoutParams(new ViewFlipper.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		this.setContentView(flipLayout);

		Resources res = getResources();
		NinePatchDrawable ps = (NinePatchDrawable) res.getDrawable(R.drawable.pollselect);
		NinePatchDrawable ob = (NinePatchDrawable) res.getDrawable(R.drawable.roundedbg);
		NinePatchDrawable ob2 = (NinePatchDrawable) res.getDrawable(R.drawable.roundedbg);
		ob.setColorFilter(uicolor, Mode.SRC_ATOP);
		ob2.setColorFilter(uicolor, Mode.SRC_ATOP);

		// Query DB
		db = new DatabaseManager(this);
		db.open();
		p = db.getAllPolls();
		if (p.getCount() > 0) {
			p.moveToFirst();

			for (int i = 0; i < p.getCount(); i++) {
				p.moveToPosition(i);

				// Layout of Poll Page
				RelativeLayout pollLayout = new RelativeLayout(this);
				pollLayout.setLayoutParams(new RelativeLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT));
				pollLayout.setId(Integer.parseInt((p.getString(0))));
				pollLayout.setTag(p.getString(0));
				flipLayout.addView(pollLayout);

				// Poll Name on top
				TextView pollName = new TextView(this);
				pollName.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						(int) (50.0f * getResources().getDisplayMetrics().density)));
				pollName.setTextSize(30.0f);
				pollName.setTextColor(fontcolor);
				pollName.setId(200 + i);
				pollName.setOnTouchListener(this);
				pollName.setBackgroundDrawable(ps);

				// Scroll Background
				ScrollView scroll = new ScrollView(this);
				scroll.setLayoutParams(new ScrollView.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				scroll.setPadding(0, (int) (50.0f * getResources().getDisplayMetrics().density), 0,
						0);
				scroll.setId(300 + i);
				pollLayout.addView(scroll);

				// Layout within Scroll
				LinearLayout mainLayout = new LinearLayout(this);
				mainLayout.setOrientation(1);
				mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT));
				mainLayout.setId(400 + i);
				scroll.addView(mainLayout);

				RelativeLayout.LayoutParams buttonl = new RelativeLayout.LayoutParams(
						((getResources().getDisplayMetrics().widthPixels) / 3) - 5,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

				Button addItemB = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
				addItemB.setId(500 + i);
				addItemB.setText("Add");
				addItemB.setTag("" + i);
				addItemB.setTextSize(20.0f);
				// addItemB.setBackgroundDrawable(ob);
				addItemB.setTextColor(fontcolor);
				addItemB.setLayoutParams(buttonl);
				addItemB.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int buttid = Integer.parseInt((String) v.getTag());
						changeItem(buttid, false, 0);

					}
				});

				Button deleteItemB = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
				deleteItemB.setText("Delete");
				deleteItemB.setTextColor(fontcolor);
				deleteItemB.setId(i + 800);
				deleteItemB.setTag("0");
				deleteItemB.setTextSize(20.0f);
				deleteItemB.setVisibility(View.INVISIBLE);
				deleteItemB.setLayoutParams(buttonl);
				deleteItemB.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteItem(v.getId() - 800);
						createLayout();
					}
				});
				Button editItemB = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
				editItemB.setText("Edit");
				editItemB.setTextColor(fontcolor);
				editItemB.setId(i + 900);
				editItemB.setTag("" + i);
				editItemB.setTextSize(20.0f);
				editItemB.setVisibility(View.INVISIBLE);
				editItemB.setLayoutParams(buttonl);
				editItemB.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						int buttid = Integer.parseInt((String) view.getTag());
						int item2 = 0;
						LinearLayout mlayout = (LinearLayout) findViewById(buttid + 400);
						for (int i = 0; i < mlayout.getChildCount() - 1; i++) {
							LinearLayout v = (LinearLayout) mlayout.getChildAt(i);
							LinearLayout v2 = (LinearLayout) v.getChildAt(0);
							if (v2.getTag() == "delete") {
								item2 = v2.getId() - 5000;
							}
						}
						changeItem(buttid, true, item2);
					}
				});

				LinearLayout buttonLayout = new LinearLayout(this);
				buttonLayout.setOrientation(0);
				buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				buttonLayout.setId(i + 30000);
				buttonLayout.addView(addItemB);
				buttonLayout.addView(deleteItemB);
				buttonLayout.addView(editItemB);

				pollTitle = p.getString(1);
				pollTitle = pollTitle.replace('\"','\'');
				pollTitle = pollTitle.replace('_',' ');
				pollName.setText(" " + pollTitle);
				showItems(Integer.valueOf(p.getString(0)), i);
				mainLayout.addView(buttonLayout);

				pollLayout.addView(pollName);
				
				flipLayout.setDisplayedChild(poll_id_history - 1);

			}
			db.close();
		} else {
			introPage();
			db.close();
		}

	}
	@SuppressWarnings("deprecation")
	public void changeItem(int buttonId, final boolean edit, final int uitem) {
		numItems++;

		// edit items
		Long uitem2;
		String uname = "";
		String uunit = "";
		int utype = 0;
		String umin = "";
		String umax = "";
		int usched = 0;

		if (edit) {
			db.open();
			Cursor e = db.getItem(uitem);
			uitem2 = Long.valueOf(e.getString(0));
			utype = Integer.valueOf(e.getString(2));
			uname = e.getString(1);
			uunit = e.getString(3);
			umin = (e.getString(4));
			umax = (e.getString(5));
			usched = Integer.valueOf(e.getString(7));
			// db.updateItem(uitem2, uname, uunit, umin, umax, 2);
			e.close();
			db.close();
		}

		LinearLayout mlayout = (LinearLayout) findViewById(buttonId + 400);
		LinearLayout blayout = (LinearLayout) findViewById(buttonId + 30000);
		Button addb = (Button) findViewById(buttonId + 500);
		addb.setId(buttonId);
		blayout.setVisibility(View.GONE);

		// Get NinePatch from resources
		Resources res = getResources();
		NinePatchDrawable iw1 = (NinePatchDrawable) res.getDrawable(R.drawable.textbox);
		NinePatchDrawable iw2 = (NinePatchDrawable) res.getDrawable(R.drawable.textbox);
		NinePatchDrawable iw3 = (NinePatchDrawable) res.getDrawable(R.drawable.textbox);
		NinePatchDrawable iw4 = (NinePatchDrawable) res.getDrawable(R.drawable.textbox);
		Drawable rbg1 = res.getDrawable(R.drawable.radiobg1);
		Drawable rbg1c = res.getDrawable(R.drawable.radiobg1checked);

		NinePatchDrawable ob6 = (NinePatchDrawable) res.getDrawable(R.drawable.roundedbg);
		NinePatchDrawable npd = (NinePatchDrawable) res.getDrawable(R.drawable.cornerdkgrey);
		ob6.setColorFilter(uicolor, Mode.SRC_ATOP);
		rbg1.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);
		rbg1c.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);

		// border
		itemspace = new LinearLayout(this);
		itemspace.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		itemspace.setOrientation(1);
		itemspace.setPadding(2, 2, 2, 2);
		// itemspace.setBackgroundColor(android.graphics.Color.parseColor("#505050"));
		mlayout.addView(itemspace);

		// INPUT SPACE FOR INPUT DATA
		itemborder = new LinearLayout(this);
		itemborder.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		itemborder.setOrientation(1);
		itemborder.setBackgroundColor(android.graphics.Color.LTGRAY);
		itemspace.addView(itemborder);

		// CornerRounding
		itemcorners = new LinearLayout(this);
		itemcorners.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		itemcorners.setOrientation(1);
		itemcorners.setBackgroundDrawable(npd);
		itemcorners.setPadding(3, 3, 3, 3);
		itemborder.addView(itemcorners);

		// New ViewGroup to hold the new Item
		item = new LinearLayout(this);
		item.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		item.setOrientation(1);
		// item.setPadding(2, 2, 2, 2);
		item.setBackgroundColor(android.graphics.Color.parseColor("#303030"));
		item.setTag("item" + numItems);
		item.setId(numItems);

		// ItemName field
		itemNameField = new EditText(this);
		itemNameField.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		itemNameField.setHint("Item Name");

		if (edit) {
			itemNameField.setText(uname);
		}
		itemNameField.setTextColor(android.graphics.Color.BLACK);
		itemNameField.setBackgroundDrawable(iw1);
		itemNameField.setTag("itemNameField");
		itemNameField.setSingleLine(true);

		InputFilter nameFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
					int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (source.charAt(i) == ' ' || source.charAt(i) == ','|| source.charAt(i) == '\''|| source.charAt(i) == '.'|| source.charAt(i) == '\\'|| source.charAt(i) == '/') {
						return "";
					}
				}
				return null;
			}
		};
		itemNameField.setFilters(new InputFilter[]{nameFilter});

		// New datatype RadioGroup
		dataType = new RadioGroup(this);
		// Set LayoutParams for dataType RadioGroup
		dataType.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		dataType.setPadding(20, 0, 0, 0);

		// Build RadioButtons for each data type
		TextView dataTitle = new TextView(this);
		dataTitle.setText("  Survey input type");
		// Set LayoutParams for nameField
		dataTitle.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		dataTitle.setTextSize(20.0f);
		dataTitle.setTextColor(android.graphics.Color.BLACK);
		dataTitle.setBackgroundColor(android.graphics.Color.LTGRAY);
		if(edit){dataTitle.setVisibility(View.GONE);}
		// Decimals with custom units
		dataType.setTag("dataType");
		dataType.setId(numItems);
		if(edit){dataType.setVisibility(View.GONE);}
		RadioButton decimalButton = new RadioButton(this);
		decimalButton.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		decimalButton.setText("Measure - Related Numerical Unit");
		decimalButton.setTextColor(android.graphics.Color.LTGRAY);
		decimalButton.setTag("decimalButton");
		decimalButton.setId(1001);
		decimalButton.setButtonDrawable(rbg1);

		unitsField = new EditText(this);
		unitsField.setHint("Units");
		if (edit) {
			unitsField.setText(uunit);
		}
		unitsField.setTag("unitsField");
		unitsField.setBackgroundDrawable(iw2);
		unitsField.setTextColor(android.graphics.Color.LTGRAY);
		unitsField.setSingleLine(true);
		// Scale
		RadioButton scaleButton = new RadioButton(this);
		scaleButton.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		scaleButton.setText("Scale - Min and Max");
		scaleButton.setTextColor(android.graphics.Color.LTGRAY);
		scaleButton.setButtonDrawable(rbg1);
		scaleButton.setTag("scaleButton");
		scaleButton.setId(1002);
		maxscaleField = new EditText(this);
		maxscaleField.setHint("Max");
		maxscaleField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		maxscaleField.setTag("maxscaleField");
		maxscaleField.setBackgroundDrawable(iw3);
		maxscaleField.setTextColor(android.graphics.Color.LTGRAY);
		if (edit) {
			maxscaleField.setText(umax);
			maxscaleField.setVisibility(View.GONE);
		}
		minscaleField = new EditText(this);
		minscaleField.setHint("Min");
		minscaleField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		minscaleField.setTag("minscaleField");
		minscaleField.setBackgroundDrawable(iw4);
		minscaleField.setTextColor(android.graphics.Color.LTGRAY);
		if (edit) {
			minscaleField.setText(umin);
			minscaleField.setVisibility(View.GONE);
		}
		// Filters for min/max fields
		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
					int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (source.charAt(i) == ' ' || source.charAt(i) == ','|| source.charAt(i) == '\''
							|| source.charAt(i) == '.'|| source.charAt(i) == '\\'|| source.charAt(i) == '/') {
						return "";
					}
				}
				return null;
			}
		};
		minscaleField.setFilters(new InputFilter[]{filter});
		maxscaleField.setFilters(new InputFilter[]{filter});

		minscaleField.setVisibility(View.GONE);
		maxscaleField.setVisibility(View.GONE);
		unitsField.setVisibility(View.GONE);

		
		// Increment
		RadioButton incButton = new RadioButton(this);
		incButton.setHint("Increment");
		incButton.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		incButton.setText("Increment - Cumulative Add or Subtract");
		incButton.setTextColor(android.graphics.Color.LTGRAY);
		incButton.setTag("incButton");
		incButton.setButtonDrawable(rbg1);
		incButton.setId(1003);

		// Add RadioButtons to dataType
		// dataType.addView(dataTitle);
		dataType.addView(decimalButton);
		dataType.addView(scaleButton);
		dataType.addView(incButton);

		item.addView(dataTitle);
		item.addView(dataType);
		item.addView(minscaleField);
		item.addView(maxscaleField);
		item.addView(unitsField);

		dataType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Resources res = getResources();
				Drawable rbg1 = res.getDrawable(R.drawable.radiobg1);
				Drawable rbg1c = res.getDrawable(R.drawable.radiobg1checked);
				rbg1.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);
				rbg1c.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);

				int viewId = group.getId();
				LinearLayout layout = (LinearLayout) findViewById(400 + viewid);

				try {
					layout.findViewById(viewId);
				} catch (ClassCastException e) {
					toastwarning(Integer.toString(viewId));
				}

				
				switch (checkedId - 1000) {
					case 1 :
						((CompoundButton) item.findViewById(1001)).setButtonDrawable(rbg1c);
						((CompoundButton) item.findViewById(1002)).setButtonDrawable(rbg1);
						((CompoundButton) item.findViewById(1003)).setButtonDrawable(rbg1);
						item.findViewWithTag("maxscaleField").setVisibility(View.GONE);
						item.findViewWithTag("minscaleField").setVisibility(View.GONE);
						item.findViewWithTag("unitsField").setVisibility(View.VISIBLE);
						break;
					case 2 :
						((CompoundButton) item.findViewById(1001)).setButtonDrawable(rbg1);
						((CompoundButton) item.findViewById(1002)).setButtonDrawable(rbg1c);
						((CompoundButton) item.findViewById(1003)).setButtonDrawable(rbg1);
						item.findViewWithTag("unitsField").setVisibility(View.GONE);
						if (!edit){item.findViewWithTag("maxscaleField").setVisibility(View.VISIBLE);
						item.findViewWithTag("minscaleField").setVisibility(View.VISIBLE);}
						break;
					case 3 :
						((CompoundButton) item.findViewById(1001)).setButtonDrawable(rbg1);
						((CompoundButton) item.findViewById(1002)).setButtonDrawable(rbg1);
						((CompoundButton) item.findViewById(1003)).setButtonDrawable(rbg1c);
						item.findViewById(viewId).findViewWithTag("maxscaleField").setVisibility(View.GONE);
						item.findViewById(viewId).findViewWithTag("minscaleField").setVisibility(View.GONE);
						item.findViewById(viewId).findViewWithTag("unitsField").setVisibility(View.GONE);

						break;
					default :
						toastwarning("please enter unit type");
						break;
				}
				

			}
		});

		// New Per-Item Schedule RadioGroup
		itemSched = new RadioGroup(this);
		itemSched.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		itemSched.setTag("itemSched");
		itemSched.setId(numItems);
		itemSched.setPadding(20, 0, 0, 0);
		itemSched.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Resources res = getResources();
				Drawable rbg1 = res.getDrawable(R.drawable.radiobg1);
				Drawable rbg1c = res.getDrawable(R.drawable.radiobg1checked);
				rbg1.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);
				rbg1c.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);

				switch (checkedId - 2000) {
					case 1 :
						((RadioButton) itemSched.findViewById(2001)).setButtonDrawable(rbg1c);
						((RadioButton) itemSched.findViewById(2002)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2003)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2004)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2005)).setButtonDrawable(rbg1);
						break;
					case 2 :
						((RadioButton) itemSched.findViewById(2001)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2002)).setButtonDrawable(rbg1c);
						((RadioButton) itemSched.findViewById(2003)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2004)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2005)).setButtonDrawable(rbg1);
						break;
					case 3 :
						((RadioButton) itemSched.findViewById(2001)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2002)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2003)).setButtonDrawable(rbg1c);
						((RadioButton) itemSched.findViewById(2004)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2005)).setButtonDrawable(rbg1);
						break;
					case 4 :
						((RadioButton) itemSched.findViewById(2001)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2002)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2003)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2004)).setButtonDrawable(rbg1c);
						((RadioButton) itemSched.findViewById(2005)).setButtonDrawable(rbg1);
						break;
					case 5 :
						((RadioButton) itemSched.findViewById(2001)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2002)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2003)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2004)).setButtonDrawable(rbg1);
						((RadioButton) itemSched.findViewById(2005)).setButtonDrawable(rbg1c);
						break;
					default :
						toastwarning("please enter unit type");
						break;
				}

			}
		});

		TextView schedTitle = new TextView(this);
		schedTitle.setText("  Schedule");
		// Set LayoutParams for nameField
		schedTitle.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		schedTitle.setTextSize(20.0f);
		schedTitle.setTextColor(android.graphics.Color.BLACK);
		schedTitle.setBackgroundColor(android.graphics.Color.LTGRAY);

		RadioButton nosButton = new RadioButton(this);
		nosButton.setText("no Schedule");
		nosButton.setTextColor(android.graphics.Color.LTGRAY);
		nosButton.setId(2001);
		nosButton.setButtonDrawable(rbg1);
		RadioButton hourButton = new RadioButton(this);
		hourButton.setText("per Hour");
		hourButton.setTextColor(android.graphics.Color.LTGRAY);
		hourButton.setId(2002);
		hourButton.setButtonDrawable(rbg1);
		RadioButton subdayButton = new RadioButton(this);
		subdayButton.setText("per 3 Hours");
		subdayButton.setTextColor(android.graphics.Color.LTGRAY);
		subdayButton.setId(2003);
		subdayButton.setButtonDrawable(rbg1);
		RadioButton dayButton = new RadioButton(this);
		dayButton.setText("per Day");
		dayButton.setTextColor(android.graphics.Color.LTGRAY);
		dayButton.setId(2004);
		dayButton.setButtonDrawable(rbg1);
		RadioButton weekButton = new RadioButton(this);
		weekButton.setText("per Week");
		weekButton.setTextColor(android.graphics.Color.LTGRAY);
		weekButton.setId(2005);
		weekButton.setButtonDrawable(rbg1);
		// itemSched.addView(schedTitle);
		itemSched.addView(nosButton);
		itemSched.addView(hourButton);
		itemSched.addView(subdayButton);
		itemSched.addView(dayButton);
		itemSched.addView(weekButton);

		// Submit Button
		Button submitButton = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
		if (edit) {
			submitButton.setText("Edit");
		} else {
			submitButton.setText("Submit");
		}
		// submitButton.setId(i);
		submitButton.setTextSize(20.0f);
		submitButton.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		submitButton.setTag("" + (buttonId + 1));
		// submitButton.setBackgroundDrawable(ob6);
		submitButton.setTextColor(fontcolor);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int buttid = Integer.parseInt((String) v.getTag());

				if (edit) {
					if (submitItem(buttid, true, uitem)) {
						createLayout();
					} else {
						Log.i(null, "Item Edit Error");
					}
				}

				else if (submitItem(buttid, false, uitem)) {
					createLayout();
				} else {
					Log.i(null, "Item Creation Error");
				}

			}
		});
		itemcorners.addView(itemNameField);
		itemcorners.addView(item);
		item.addView(schedTitle);
		item.addView(itemSched);
		mlayout.addView(submitButton);
		itemspace.startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_up_in));
		poll_id_history = addb.getId();
		itemNameField.requestFocus();

		// Edit mode
		if (edit) {
			dataType.check(1000 + utype);
		}
		if (edit) {
			itemSched.check(2000 + usched);
		}

	}
	public boolean submitItem(int buttonid, boolean edit, int uitem) {

		int type = dataType.getCheckedRadioButtonId() - 1000;
		if (type == -1001) {
			toastwarning("Please select a data type");
			db.close();
			return false;
		}
		int sched = itemSched.getCheckedRadioButtonId() - 2000;
		if (sched == -2001) {
			toastwarning("Please select a schedule");
			db.close();
			sched = 1;
			return false;
		}
		db.open();
		// Item Name
		String itemName = itemNameField.getText().toString();
		if (itemName == " " | itemName.length() == 0) {
			itemName = "Not Named";
			toastwarning("Please give item a name");
			db.close();
			return false;
		}

		// DataType Fields
		String unit = "scale";
		int min = 0;
		int max = 0;

		if (type == 3) {
			unit = "increment";
		}

		else if (type == 2) {
			// Scale Sanitation
			String minString = minscaleField.getText().toString();
			String maxString = maxscaleField.getText().toString();
			if (minString == " " | minString.length() == 0) {
				toastwarning("Min must be a number");
				db.close();
				return false;
			}
			if (maxString == " " | maxString.length() == 0) {
				toastwarning("Max must be a number");
				db.close();
				return false;
			}
			min = Integer.parseInt(minString);
			max = Integer.parseInt(maxString);
			if (min >= max) {
				toastwarning("Max must be greater than Min");
				db.close();
				return false;
			}
		} else if (type == 1) {
			// Unit Sanitation
			unit = unitsField.getText().toString();
			if (unit == " " | unit.length() == 0) {
				toastwarning("Please enter a unit of measure");
				db.close();
				return false;
			}
		}
		// Put into DB

		try {
			if (edit) {
				db.updateItem(uitem, itemName, unit, min, max, sched);
				// long entryID, String name, String unit, int min, int max, int
				// sched, int datatype
			} else {
				db.addItem(itemName, type, unit, min, max, sched, findPoll(),
						android.graphics.Color.RED);
			}
			poll_id_history = buttonid;
			db.close();
			return true;

		} catch (SQLiteException e1) {
			toastwarning("database error");
			System.err.println(e1.toString());
			db.close();
			return false;
		}
	}
	public void deleteItem(int poll) {
		poll_id_history = poll + 1;
		LinearLayout mlayout = (LinearLayout) findViewById(poll + 400);
		for (int i = 0; i < mlayout.getChildCount() - 1; i++) {
			LinearLayout v = (LinearLayout) mlayout.getChildAt(i);
			Log.i(null, "child of mlayout " + String.valueOf(i));
			LinearLayout v2 = (LinearLayout) v.getChildAt(0);
			if (v2.getTag() == "delete") {
				int item2 = v2.getId() - 5000;
				Log.i(null, "deleted id " + String.valueOf(item2));
				db.open();
				db.deleteItem(item2);
				db.close();

			} else {
				
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void addPoll() {

		Resources res = getResources();

		NinePatchDrawable iw2 = (NinePatchDrawable) res.getDrawable(R.drawable.textbox);

		flipLayout.removeAllViews();
		// Layout of Poll Page
		RelativeLayout pollLayout = new RelativeLayout(this);
		pollLayout.setLayoutParams(new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		// Layout within Scroll
		LinearLayout mainLayout = new LinearLayout(this);
		mainLayout.setOrientation(1);
		mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		flipLayout.addView(pollLayout);
		pollLayout.addView(mainLayout);

		// Field for naming the data entry
		nameField = new EditText(this);
		nameField.setHint("Please name your survey");
		nameField.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		nameField.setBackgroundDrawable(iw2);
		nameField.setTextColor(android.graphics.Color.LTGRAY);
		nameField.setSingleLine(true);
		InputFilter nameFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
					int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (source.charAt(i) == ',' || source.charAt(i) == '.'|| source.charAt(i) == '/'|| source.charAt(i) == '\\') {
						return "";
					}
					
				}
				return null;
			}
		};
		nameField.setFilters(new InputFilter[]{nameFilter});

		// New poll-wide sched pollSched = new RadioGroup(this);
		pollSched = new RadioGroup(this);
		pollSched.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		pollSched.setTag("pollSched");

		RadioButton pollHourButton = new RadioButton(this);
		pollHourButton.setText("Poll:Hourly");
		pollHourButton.setId(1);

		RadioButton pollDayButton = new RadioButton(this);
		pollDayButton.setText("Poll:Daily");
		pollDayButton.setId(2);

		RadioButton pollWeekButton = new RadioButton(this);
		pollWeekButton.setText("Poll:Weekly");
		pollWeekButton.setId(3);

		pollSched.addView(nameField);
		// pollSched.addView(pollHourButton);
		// pollSched.addView(pollDayButton);
		// pollSched.addView(pollWeekButton);

		// Submit Poll Button
		Button submitPoll = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
		submitPoll.setText("Submit");
		submitPoll.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

		submitPoll.setTextColor(fontcolor);

		submitPoll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pollTitle = nameField.getText().toString();
				pollTitle = pollTitle.replace('\'','\"');
				pollTitle = pollTitle.replace(' ','_');
				pollschedID = pollSched.getCheckedRadioButtonId();
				db.open();
				db.addPoll(pollTitle, pollschedID);
				Cursor a = db.getAllPolls();
				a.getCount();
				poll_id_history = a.getCount();
				a.close();
				db.close();
				createLayout();
			}
		});
		mainLayout.addView(pollSched);
		mainLayout.addView(submitPoll);
	}
	@SuppressWarnings("deprecation")
	public void introPage() {

		// Welcome page
		Resources res = getResources();
		NinePatchDrawable ob7 = (NinePatchDrawable) res.getDrawable(R.drawable.roundedbg);
		NinePatchDrawable iw3 = (NinePatchDrawable) res.getDrawable(R.drawable.textbox);
		ob7.setColorFilter(uicolor, Mode.SRC_ATOP);

		flipLayout.removeAllViews();
		// Layout of Poll Page
		RelativeLayout pollLayout = new RelativeLayout(this);
		pollLayout.setLayoutParams(new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		pollLayout.setId(0);
		pollLayout.setTag("" + 0);

		// Layout within Scroll
		LinearLayout mainLayout = new LinearLayout(this);
		mainLayout.setOrientation(1);
		mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		flipLayout.addView(pollLayout);
		pollLayout.addView(mainLayout);

		TextView introText = new TextView(this);
		introText.setPadding(20, 10, 20, 30);
		introText
				.setText("Welcome to Insight!\n\n"
						+ "Insight is a self-monitoring survey designed to help you track whats important in your life."
						+ "  Through monitoring each survey item on its dedicated trendline,"
						+ " you will discover patterns you might not have noticed previously.\n\n"
						+ "Use Insight to monitor:\n\n" + "-Daily habits.\n"
						+ "-Effects of certain activities on other activities.\n"
						+ "-History of activities.\n"
						+ "-Items such as finances, rent, weather...\n"
						+ "or anything else you can imagine.\n\n"
						+ "Start by creating your first survey\n" + "or select a preset here.");
		introText.setTextColor(android.graphics.Color.LTGRAY);

		// Field for naming the data entry
		nameField = new EditText(this);
		nameField.setHint("Please name your survey");
		nameField.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		nameField.setBackgroundDrawable(iw3);
		nameField.setTextColor(android.graphics.Color.LTGRAY);
		nameField.setSingleLine(true);
		InputFilter nameFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
					int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (source.charAt(i) == ','	|| source.charAt(i) == '.'|| source.charAt(i) == '\\'|| source.charAt(i) == '/') {
						return "";
					}
				}
				return null;
			}
		};
		nameField.setFilters(new InputFilter[]{nameFilter});

		// Submit Poll Button
		Button submitPoll = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
		submitPoll.setText("Create new survey");
		submitPoll.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		// submitPoll.setBackgroundDrawable(ob7);
		submitPoll.setTextSize(20.0f);
		submitPoll.setTextColor(fontcolor);
		submitPoll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pollTitle = nameField.getText().toString();

				db.open();
				db.addPoll(pollTitle, pollschedID);
				Cursor a = db.getAllPolls();
				a.getCount();
				poll_id_history = a.getCount();
				a.close();
				db.close();
				createLayout();
			}
		});
		//

		Button importPoll = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
		importPoll.setText("Select survey preset");
		importPoll.setTextSize(20.0f);
		importPoll.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		// importPoll.setBackgroundDrawable(ob7);
		importPoll.setTextColor(fontcolor);
		importPoll.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				presetPopup(CreateSurveyActivity.this);
				// int whospoll = 1;
				// importPollPreset(whospoll);
			}
		});
		mainLayout.addView(introText);
		mainLayout.addView(nameField);
		mainLayout.addView(submitPoll);
		mainLayout.addView(importPoll);

	}
	@SuppressWarnings("deprecation")
	public void showItems(int pollId, int id) {
		Resources res = getResources();
		// Get out the DB
		db = new DatabaseManager(this);
		db.open();
		// poll_id = pollId;
		NinePatchDrawable npd = (NinePatchDrawable) res.getDrawable(R.drawable.roundedge);
		LinearLayout layout = (LinearLayout) findViewById(400 + id);
		Cursor a = db.getAllPollsItems(pollId); // OPENING ITEM DB
		a.moveToFirst();
		if (a.getCount() > 0) {
			do {

				int deleted = Integer.parseInt(a.getString(0));
				// ITEM DB
				String itemname = a.getString(1);
				String itemunit = a.getString(3);
				int val = Integer.parseInt(a.getString(7));
				String itemsched = sched2word(val);
				id2color(Integer.parseInt(a.getString(0).toString()));

				// border
				final LinearLayout itemspace2 = new LinearLayout(this);
				itemspace2.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				itemspace2.setOrientation(1);
				itemspace2.setPadding(2, 2, 2, 2);
				itemspace2.setBackgroundColor(android.graphics.Color.parseColor("#333333"));

				// INPUT SPACE FOR INPUT DATA
				LinearLayout itemborder2 = new LinearLayout(this);
				itemborder2.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				itemborder2.setOrientation(1);
				itemborder2.setId(deleted + 5000);
				itemborder2.setTag("");
				itemborder2.setBackgroundColor(trendcolor);
				itemspace2.addView(itemborder2);
				itemborder2.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (v.getTag() == "") {
							v.setBackgroundColor(android.graphics.Color.LTGRAY);
							v.setTag("delete");

							int nid = v.getId() - 5000;
							TextView title = (TextView) findViewById(nid + 6000);
							title.setTextColor(android.graphics.Color.LTGRAY);
							Button delb = (Button) findViewById(flipLayout.getDisplayedChild() + 800);
							Button editb = (Button) findViewById(flipLayout.getDisplayedChild() + 900);
							int delcount = Integer.parseInt(delb.getTag().toString());
							delcount++;
							delb.setTag("" + delcount);
							if (delcount > 0) {
								delb.setVisibility(View.VISIBLE);
							}
							if (delcount == 1) {
								editb.setVisibility(View.VISIBLE);
							} else {
								editb.setVisibility(View.GONE);
							}

						} else if (v.getTag() == "delete") {
							int color = v.getId() - 5000;
							id2color(color);
							TextView title = (TextView) findViewById(color + 6000);
							title.setTextColor(trendcolor);
							v.setBackgroundColor(trendcolor);
							v.setTag("");
							Button delb = (Button) findViewById(flipLayout.getDisplayedChild() + 800);
							Button editb = (Button) findViewById(flipLayout.getDisplayedChild() + 900);
							int delcount = Integer.parseInt(delb.getTag().toString());
							delcount--;
							delb.setTag("" + delcount);
							if (delcount == 0) {
								delb.setVisibility(View.GONE);
							}
							if (delcount != 1) {
								editb.setVisibility(View.GONE);
							} else {
								editb.setVisibility(View.VISIBLE);
							}
						}
					}
				});

				// CornerRounding
				LinearLayout itemcorners2 = new LinearLayout(this);
				itemcorners2.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				itemcorners2.setOrientation(1);
				itemcorners2.setBackgroundDrawable(npd);
				itemcorners2.setPadding(3, 3, 3, 3);
				itemborder2.addView(itemcorners2);

				// ItemName field
				TextView itemName2 = new TextView(this);
				itemName2.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				itemName2.setPadding(2, 2, 2, 2);
				itemName2.setText(" " + itemname + " " + itemunit + " " + itemsched);
				itemName2.setTextColor(trendcolor);
				itemName2.setTextSize(18.0f);
				itemName2.setSingleLine(true);
				// itemName2.setBackgroundColor(android.graphics.Color.BLACK);
				itemName2.setId(deleted + 6000);
				itemcorners2.addView(itemName2);
				layout.addView(itemspace2);

			} while (a.moveToNext());
			a.close();
			db.close();
		} else {
			toastwarning("No Items in " + p.getString(1) + " Poll");
			a.close();
			db.close();
		}

	}
	@SuppressWarnings("deprecation")
	private void presetPopup(final Activity context) {
		
		RelativeLayout hookv = (RelativeLayout) findViewById(findPoll());

		Resources res = getResources();
		NinePatchDrawable svbg = (NinePatchDrawable) res.getDrawable(R.drawable.roundedbg);
		svbg.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);

		RelativeLayout rl1 = new RelativeLayout(this);
		rl1.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		rl1.setClickable(true);
		rl1.setBackgroundColor(R.color.ghost);

		RelativeLayout.LayoutParams svlp = new RelativeLayout.LayoutParams(
				(int) (250.0f * getResources().getDisplayMetrics().density),
				(int) (350.0f * getResources().getDisplayMetrics().density));
		svlp.addRule(RelativeLayout.CENTER_IN_PARENT);

		HorizontalScrollView sv1 = new HorizontalScrollView(this);
		sv1.setLayoutParams(svlp);
		sv1.setBackgroundDrawable(svbg);
		rl1.addView(sv1);

		hookv.addView(rl1);

		LinearLayout ll1 = new LinearLayout(this);
		ll1.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		sv1.addView(ll1);

		ArrayList<String> presetFiles = new ArrayList<String>();

		try {
			File sd = new File(Environment.getExternalStorageDirectory() + "/insightdata/");
			if (sd.canWrite()) {
				FilenameFilter ff = new FilenameFilter() {
					@Override
					public boolean accept(File sd, String name) {
						return name.startsWith("insightp_");
					}
				};
				File[] foundFiles = sd.listFiles(ff);

				for (int i = 0; i < foundFiles.length; i++) {
					File filename = foundFiles[i];
					Scanner sc = new Scanner(filename);
					presetFiles.add(sc.nextLine()); // add to arraylist
				}
			}
		} catch (IOException e) {
			Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
		
		presetFiles.add(res.getString(R.string.PP_Ryans_Survey));
		presetFiles.add(res.getString(R.string.PP_Kevins_Survey));
		presetFiles.add(res.getString(R.string.PP_Workout_Survey));
		presetFiles.add(res.getString(R.string.PP_TimeSheet_Survey));
		presetFiles.add(res.getString(R.string.PP_Car_Survey));
		presetFiles.add(res.getString(R.string.PP_Growth_Survey));
		presetFiles.add(res.getString(R.string.PP_Finances_Survey));
		//presetFiles.add(res.getString(R.string.PP_Alexs_Survey));
		Object[] presetlist = presetFiles.toArray();
		for (int i = 0; i < presetlist.length; i++) {
			String stringpreset;
			try {
				stringpreset = presetlist[i].toString();
				Log.i(null, "preset string is  = " + stringpreset);
			} catch (Exception e) {
				continue;
			}

			// Create new Popup Relative layout for each preset found
			String[] parsestring = stringpreset.split(",");
			String stringtitle = parsestring[0];

			RelativeLayout.LayoutParams buttonl = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			buttonl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

			RelativeLayout pcopy = new RelativeLayout(context);
			pcopy.setLayoutParams(new LinearLayout.LayoutParams(
					(int) (230.0f * getResources().getDisplayMetrics().density),
					android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			// pcopy.setPadding(2, 2, 2, 2);
			pcopy.setBackgroundColor(android.graphics.Color.BLACK);

			TextView ptitle = new TextView(context);
			ptitle.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
					(int) (30.0f * getResources().getDisplayMetrics().density)));
			ptitle.setText(stringtitle);
			ptitle.setBackgroundColor(android.graphics.Color.LTGRAY);
			ptitle.setTextSize(20);
			ptitle.setId(i + 6000);
			
			LinearLayout pButtons = new LinearLayout(this);
			pButtons.setLayoutParams(buttonl);
			pButtons.setPadding(0, 0, 0, 8);

			Button pselect = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
			pselect.setLayoutParams(new LinearLayout.LayoutParams(
					(int) (115.0f * getResources().getDisplayMetrics().density),
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			pselect.setId(i + 9000);
			pselect.setText("Select");
			// pselect.setBackgroundDrawable(rb1);
			pselect.setTextColor(fontcolor);
			pselect.setTag(stringpreset);
			pselect.setPadding(2, 2, 2, 2);
			pselect.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String ppoll = (String) v.getTag();
					importPollPreset(ppoll);
					createLayout();
					poll_id_history = flipLayout.getChildCount()+1;
				}
			});
			
			Button pdelete = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
			pdelete.setLayoutParams(new LinearLayout.LayoutParams(
					(int) (115.0f * getResources().getDisplayMetrics().density),
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			pdelete.setId(i + 9000);
			pdelete.setText("Delete");
			// pselect.setBackgroundDrawable(rb1);
			pdelete.setTextColor(fontcolor);
			pdelete.setTag(stringtitle);
			pdelete.setPadding(2, 2, 2, 2);
			pdelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String title = (String) v.getTag();
					File sd = new File(Environment.getExternalStorageDirectory() + "/insightdata/");
					if (sd.canWrite()) {
						FilenameFilter ff = new FilenameFilter() {
							@Override
							public boolean accept(File sd, String name) {
								return name.startsWith("insightp_"+title);
							}
						};
						File[] foundFiles = sd.listFiles(ff);

						for (int i = 0; i < foundFiles.length; i++) {
							File filename = foundFiles[i];
							filename.delete();
						}
					}
					createLayout();
					poll_id_history = flipLayout.getChildCount()+1;
				}
			});
			pButtons.addView(pselect);
			pButtons.addView(pdelete);

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.BELOW, ptitle.getId());
			lp.addRule(RelativeLayout.ABOVE, pselect.getId());

			TextView pcontent = new TextView(context);
			pcontent.setLayoutParams(lp);
			pcontent.setPadding(10, 10, 10, 10);
			pcontent.setBackgroundColor(android.graphics.Color.BLACK);
			pcontent.setTextColor(android.graphics.Color.LTGRAY);

			String contentsorted = "";
			for (int s = 1; s < parsestring.length; s++) {

				String contentsorted2 = parsestring[s];
				String parseitem[] = contentsorted2.split("\\|");

				String schedstring = sched2word(Integer.parseInt(parseitem[6]));
				String contentsorted3 = parseitem[0] + " " + parseitem[2] + " " + schedstring;
				contentsorted = contentsorted + contentsorted3 + "\n";
			}
			pcontent.setText(contentsorted);

			// Combine into P layout
			pcopy.addView(ptitle);
			pcopy.addView(pcontent);
			pcopy.addView(pButtons);

			// Add to Preset Scroll List
			ll1.addView(pcopy);

		}

	}

	public void exportPoll(int pollid) {
		try {
			File direct = new File(Environment.getExternalStorageDirectory() + "/insightdata");
			if (!direct.exists()) {
				if (direct.mkdir()) {
				}
			}
			if (direct.canWrite()) {
				db = new DatabaseManager(this);
				db.open();
				String exportString = db.getPollCreateString(pollid);
				Log.i(null, "Export String is = " + exportString);
				Cursor c = db.getPoll(pollid);
				String exportPath = "insightp_" + c.getString(1);
				File exportFile = new File(direct, exportPath);
				ByteBuffer buffer = null;
				buffer = ByteBuffer.wrap(exportString.getBytes());
				FileChannel dst = new FileOutputStream(exportFile).getChannel();
				
				toastwarning(c.getString(1) + " is now a preset.");
				dst.write(buffer);
				dst.close();
				c.close();
				db.close();
				

			}
		} catch (IOException e) {

			Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
	}
	public void importPollPreset(String preset) {

		db = new DatabaseManager(this);
		db.open();
		db.importPoll(preset);
		
		db.close();
		
	
	}
	public void importPoll2() {
		try {
			File sd = Environment.getExternalStorageDirectory();

			if (sd.canWrite()) {
				db = new DatabaseManager(this);
				db.open();
				String importPath = "ImportPoll";
				File importFile = new File(sd, importPath);
				Scanner sc = new Scanner(importFile);
				String importString = sc.nextLine();
				db.importPoll(importString);
				db.close();

			}
		} catch (IOException e) {

			Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();

		}
		Intent restartIntent = new Intent(this, CreateSurveyActivity.class);
		startActivity(restartIntent);
	}

	// ToastWarning
	public void toastwarning(String texthere) {
		Toast.makeText(this, texthere, Toast.LENGTH_SHORT).show();
	}
	public void id2color(int id) {
		// sets id to be between 1-10 for color cases.
		if (id > 10 && id < 20) {
			id = id - 10;
		} else if (id > 20 && id < 30) {
			id = id - 20;
		} else if (id > 30 && id < 40) {
			id = id - 30;
		} else if (id > 40 && id < 50) {
			id = id - 40;
		} else if (id > 50 && id < 60) {
			id = id - 50;
		} else if (id > 60 && id < 70) {
			id = id - 60;
		} else if (id > 70 && id < 80) {
			id = id - 70;
		} else if (id > 80 && id < 90) {
			id = id - 80;
		} else if (id > 90 && id < 100) {
			id = id - 90;
		}

		// stupid xml wont pick up hex, so doing manually
		switch (id) {

			case 1 :
				trendcolor = android.graphics.Color.parseColor("#4a9bff"); // blue
				break;
			case 2 :
				trendcolor = android.graphics.Color.parseColor("#ffda2d"); // yellow
				break;
			case 3 :
				trendcolor = android.graphics.Color.parseColor("#ff7150"); // coral
				break;
			case 4 :
				trendcolor = android.graphics.Color.parseColor("#DDA0DD"); // purple
				break;
			case 5 :
				trendcolor = android.graphics.Color.parseColor("#5be65b"); // green
				break;
			case 6 :
				trendcolor = android.graphics.Color.parseColor("#FFA500"); // orange
				break;
			case 7 :
				trendcolor = android.graphics.Color.parseColor("#e64141"); // red
				break;
			case 8 :
				trendcolor = android.graphics.Color.parseColor("#9b8ae7"); // pink
				break;
			case 9 :
				trendcolor = android.graphics.Color.parseColor("#d0d637"); // dkgreen
				break;
			case 10 :
				trendcolor = android.graphics.Color.parseColor("#DEB887"); // brown
				break;
			default :
				trendcolor = android.graphics.Color.parseColor("#00FFFF"); // turquoise
				break;
		}
	}

	// ANDROID MENU ACTIONS
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);

		menu.add(0, CREATEPOLL_T, 0, "Create Survey");
		menu.add(0, IMPORT, 0, "Import Preset");
		menu.add(0, EXPORT, 0, "Export Preset");
		menu.add(0, DELETEPOLL_T, 0, "Delete Survey");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case CREATEPOLL_T :
				addPoll();
				return true;
			case IMPORT :
				presetPopup(this);
				return true;
			case EXPORT :
				exportPoll(findPoll());
				return true;
			case DELETEPOLL_T :
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Confirm");
				builder.setMessage("This will delete this poll and any input data. Are you sure you want to do this?");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           @Override
					public void onClick(DialogInterface dialog, int id) {
			        	   deletePoll(findPoll());
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
		}
		return super.onOptionsItemSelected(item);
	}

	private int findPoll() {
		int flipval = flipLayout.getDisplayedChild();
		View flipchild = flipLayout.getChildAt(flipval);
		int realPoll = Integer.parseInt((String) flipchild.getTag());
		return realPoll;
	}
	private void deletePoll(int pollid) {
		db = new DatabaseManager(this);
		db.open();
		Cursor c = db.getPoll(pollid);
		String pollname = c.getString(1);
		db.deletePoll(pollid);

		Cursor z = db.getAllPolls();
		if (z.getCount() > 0) {
			createLayout();
		} else {
			introPage();
		}
		c.close();
		db.close();
		toastwarning("Survey " + pollname + " deleted.");
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
	// Main Activity Event
	public void create(View view) {
		Intent intent = new Intent(this, CreateSurveyActivity.class);
		startActivity(intent);

	}
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {

		switch (arg1.getAction()) {

			case MotionEvent.ACTION_DOWN : {
				// store the X value when the user's finger was pressed down
				downXValue = arg1.getX();
				downYValue = arg1.getY();
				break;
			}
			case MotionEvent.ACTION_UP : {
				// Get the X value when the user released his/her finger
				float currentX = arg1.getX();
				float currentY = arg1.getY();

				float x = Math.abs(currentX - downXValue);
				float y = Math.abs(currentY - downYValue);
				if (flipLayout.getChildCount() > 1) {
					// going backwards: pushing stuff to the right
					if ((downXValue < currentX) & (x > y) & (x > 20)) {

						flipLayout.setInAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_right_in));
						flipLayout.setOutAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_right_out));
						flipLayout.showPrevious();
					}
					// going forwards: pushing stuff to the left
					if ((downXValue > currentX) & (x > y) & (x > 20)) {
						flipLayout.setInAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_left_in));
						flipLayout.setOutAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_left_out));
						flipLayout.showNext();
					}
				}
				// something going down
				if ((downYValue < currentY) & (y > x) & (y > 50)) {
				}
				// something going up
				if ((downYValue > currentY) & (y > x) & (y > 50)) {
				}
				break;
			}
		}
		// if you return false, these actions will not be recorded
		return true;
	}
	public String sched2word(int schedtype) {

		String word = "";

		switch (schedtype) {
			case 1 : // No schedule or 1 minute
				word = "not scheduled";
				break;
			case 2 : // per Hour
				word = "every hour";
				break;
			case 3 :// per 3 hours
				word = "every 3 hours";
				break;
			case 4 :// per day
				word = "every day";
				break;
			case 5 :// per week
				word = "every week";
				break;
			default :
				word = "";
				break;
		}

		return word;
	}

}
