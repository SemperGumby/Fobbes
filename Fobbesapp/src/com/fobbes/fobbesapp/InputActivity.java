/*
 * FOBBESAPP-InputActivity.java
 * Programmers: Charles Schuh, Ryan McKee
 * Loads user-made survey (and later pre-made surveys) and allows user to make input
 */
package com.fobbes.fobbesapp;

import com.fobbes.fobbesapp.LongPressImageButton.LongPressCallback;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class InputActivity extends Activity implements OnTouchListener {

	// UI
	public int uicolor = android.graphics.Color.parseColor("#696969");
	public int fontcolor = android.graphics.Color.parseColor("#E0E0E0");

	public int itemcolor = android.graphics.Color.WHITE;
	public int trendcolor;
	public float fontsize = 30.0f;
	public int itemheight = 165;
	// Items
	private int fieldID; // Itemized list number for Inputs

	// Touch interface
	private float downXValue;
	private float downYValue;

	// Layouts
	private ViewFlipper flipLayout;

	// Scheduler
	private int[][] endTimes;
	private Alarm alarm = new Alarm();

	// DB
	private double tsNow;
	private Cursor a;
	public DatabaseManager db = new DatabaseManager(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTitle("Take your survey");
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		overridePendingTransition(0, 0);

		inputLayout();
		Intent intent = getIntent();
		int pollId = intent.getIntExtra("Pollid", 0);
		int startPage = findPageById(pollId);
		//Log.e("Pollid", ""+pollId);
		if(startPage != -1)
			flipLayout.setDisplayedChild(startPage);
	}

	// Setup Input page

	@SuppressWarnings("deprecation")
	public void inputLayout() {

		// Current timestamp to test for schedule
		Long thetime = System.currentTimeMillis();
		tsNow = thetime;

		// ViewFlipper Base
		flipLayout = new ViewFlipper(this);
		flipLayout.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
		flipLayout.setLayoutParams(new ViewFlipper.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		this.setContentView(flipLayout);

		// Poll Title
		Resources res = getResources();
		NinePatchDrawable ps = (NinePatchDrawable) res.getDrawable(R.drawable.pollselect);

		// Query DB and populate with every Poll base layout
		db.open();
		fieldID = 0;
		a = db.getAllPolls();
		endTimes = new int[a.getCount()][];
		if (a.getCount() > 0) {
			a.moveToFirst();
			for (int i = 0; i < a.getCount(); i++) {
				a.moveToPosition(i);

				// Layout of Poll Page
				RelativeLayout pollLayout = new RelativeLayout(this);
				pollLayout.setLayoutParams(new RelativeLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT));
				pollLayout.setId(100 + i);
				pollLayout.setTag(a.getString(0));
				flipLayout.addView(pollLayout);

				// Poll Name on top
				TextView pollName = new TextView(this);
				pollName.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						(int) (50.0f * getResources().getDisplayMetrics().density)));
				pollName.setTextSize(30.0f);
				pollName.setTextColor(fontcolor);
				pollName.setOnTouchListener(this);

				pollName.setBackgroundDrawable(ps);

				// Scroll Background
				ScrollView scroll = new ScrollView(this);
				scroll.setLayoutParams(new ScrollView.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				scroll.setPadding(0, (int) (50.0f * getResources().getDisplayMetrics().density), 0,
						0);
				pollLayout.addView(scroll);

				// Layout within Scroll USED FOR CHILD ITEMS
				LinearLayout mainLayout = new LinearLayout(this);
				mainLayout.setOrientation(1);
				mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT));
				mainLayout.setId(400 + i);
				mainLayout.setTag(a.getString(0));
				scroll.addView(mainLayout);

				// Put Poll Name on Title
				String pollTitle = a.getString(1);
				pollTitle = pollTitle.replace('\"','\'');
				pollTitle = pollTitle.replace('_',' ');
				pollName.setText(" " + pollTitle);
				pollLayout.addView(pollName);
				// Populate

				// ads
				addInputItems(i);

			}
		} else {
			// When no DB is detected. Eventually send to intro page.
			toastwarning("no database");
		}
		db.close();
	}

	// Item Trend
	@SuppressWarnings("deprecation")
	public void addInputItems(int layoutId) {
		int itemheight = (int) (85.0f * getResources().getDisplayMetrics().density);
		Resources res = getResources();
		NinePatchDrawable iw1 = (NinePatchDrawable) res.getDrawable(R.drawable.textbox);
		NinePatchDrawable ob3 = (NinePatchDrawable) res.getDrawable(R.drawable.roundedbg);
		ob3.setColorFilter(uicolor, Mode.SRC_ATOP);

		// find device dimensions
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		// Find MainLayout from create.
		LinearLayout mlayout = (LinearLayout) findViewById(400 + layoutId);
		int pollId = Integer.parseInt(mlayout.getTag().toString());

		// submit Button for complete Poll
		Button submitButton = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
		submitButton.setText("Submit Poll");
		submitButton.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

		submitButton.setId(layoutId + 5000);
		submitButton.setTextSize(20.0f);
		submitButton.setTextColor(fontcolor);
		// submitButton.setBackgroundDrawable(ob3);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int pollid = v.getId() - 5000;
				submitInput(pollid);
			}
		});

		// Notes Field for general Notes per submit
		EditText noteField = new EditText(this);
		noteField.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		noteField.setBackgroundDrawable(iw1);
		noteField.setTextColor(android.graphics.Color.LTGRAY);
		noteField.setHintTextColor(android.graphics.Color.LTGRAY);
		noteField.setHint("Notes:");
		noteField.setId(layoutId + 6000);

		// OPENING ITEM DB per Poll
		Cursor b = db.getAllPollsItems(pollId);
		b.moveToFirst();
		int count = 0;
		endTimes[layoutId] = new int[b.getCount()];
		if (b.getCount() > 0) {
			do {
				// Find last timestamp in item data history
				int timeid = Integer.parseInt(b.getString(0));
				int schedtype = Integer.parseInt(b.getString(7));
				endTimes[layoutId][count] = schedtype;
				Log.i(null, schedtype + " item schedule");
				Log.i(null, timeid + " item position");
				Cursor item = db.getAllItemsInputs(timeid);
				item.moveToLast();

				double tsOld;
				if (item.getCount() > 0) {
					tsOld = Double.parseDouble(item.getString(2));
				} else {
					tsOld = 0;
				}

				Log.i(null, tsOld + " Last timestamp for that item");
				double tsDiff = tsNow - tsOld;

				if (timeSched(schedtype, tsDiff)) {

					// save datatype per item. Store info in??
					int datatype = Integer.parseInt(b.getString(2));
					id2color(Integer.valueOf(b.getString(0)));
					// border
					LinearLayout borderspace = new LinearLayout(this);
					borderspace.setLayoutParams(new LinearLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.MATCH_PARENT, itemheight));
					borderspace.setOrientation(1);
					borderspace.setPadding(2, 2, 2, 2);
					// borderspace.setBackgroundColor(android.graphics.Color.parseColor("#505050"));

					// INPUT SPACE FOR INPUT DATA
					LinearLayout inputspace = new LinearLayout(this);
					inputspace.setLayoutParams(new LinearLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							android.view.ViewGroup.LayoutParams.MATCH_PARENT));
					inputspace.setOrientation(1);
					// itemID
					fieldID++;
					inputspace.setId(fieldID);
					inputspace.setBackgroundColor(trendcolor);

					// CornerRounding
					NinePatchDrawable npd = (NinePatchDrawable) res
							.getDrawable(R.drawable.roundedge);
					LinearLayout cornerspace = new LinearLayout(this);
					cornerspace.setLayoutParams(new LinearLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							android.view.ViewGroup.LayoutParams.MATCH_PARENT));
					cornerspace.setOrientation(1);
					cornerspace.setBackgroundDrawable(npd);
					cornerspace.setPadding(3, 3, 3, 3);
					cornerspace.setId(020162013);
					cornerspace.setTag(Integer.parseInt(b.getString(0)));

					// Item name
					TextView nameView = new TextView(this);
					nameView.setText(" " + b.getString(1) + " " + b.getString(3));// + " "
							//+ sched2word(Integer.parseInt(b.getString(7))));
					// itemID
					fieldID++;
					nameView.setId(fieldID);
					nameView.setTag(datatype);
					// nameView.setBackgroundColor(android.graphics.Color.RED);
					nameView.setTextColor(trendcolor);

					fieldID++;
					// SkipButton Space
					LinearLayout skipSpace = new LinearLayout(this);
					skipSpace.setLayoutParams(new RelativeLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							android.view.ViewGroup.LayoutParams.MATCH_PARENT));
					// skipSpace.setPadding(10, 10, 10, 10);
					// skipSpace.setBackgroundColor(android.graphics.Color.BLACK);
					skipSpace.setId(fieldID);
					skipSpace.setTag(b.getString(0)); // Item ID

					// Skip Button
					Button skipButton = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
					skipButton.setText("Skip");
					skipButton.setLayoutParams(new LinearLayout.LayoutParams(
							(int) (60.0f * getResources().getDisplayMetrics().density),
							android.view.ViewGroup.LayoutParams.MATCH_PARENT));
					skipButton.setTag("vis");
					skipButton.setTextColor(trendcolor);
					// skipButton.setBackgroundDrawable(gb1);
					// itemID
					fieldID++;
					skipButton.setId(fieldID);

					skipButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {

							if (v.getTag() == "vis") {
								int skipId = v.getId();
								LinearLayout colorL = (LinearLayout) findViewById(skipId - 3);
								colorL.setBackgroundColor(android.graphics.Color.LTGRAY);

								TextView colorN = (TextView) findViewById(skipId - 2);
								int datatype = Integer.parseInt(colorN.getTag().toString());
								// colorN.setBackgroundColor(android.graphics.Color.BLACK);
								colorN.setTextColor(android.graphics.Color.LTGRAY);

								LinearLayout dataspace = (LinearLayout) findViewById(skipId - 1);

								for (int i = 0; i < dataspace.getChildCount(); i++) {
									View child = dataspace.getChildAt(i);
									if (child.getTag() != "vis") {
										child.setVisibility(android.view.View.INVISIBLE);
									}
								}

								v.setTag("invis");

								if (datatype == 1) {
									EditText input = (EditText) findViewById(skipId + 1);
									input.setTag("skipped");
								} else if (datatype == 2 | datatype == 3) {
									TextView input2 = (TextView) findViewById(skipId + 1);
									input2.setTag("skipped");
								}

							} else if (v.getTag() == "invis") {
								int skipId = v.getId();
								LinearLayout dataspace = (LinearLayout) findViewById(skipId - 1);
								int itemsId = Integer.parseInt(dataspace.getTag().toString());

								id2color(itemsId);

								LinearLayout colorL = (LinearLayout) findViewById(skipId - 3);
								colorL.setBackgroundColor(trendcolor);

								TextView colorN = (TextView) findViewById(skipId - 2);
								int datatype = Integer.parseInt(colorN.getTag().toString());
								// colorN.setBackgroundColor(android.graphics.Color.BLACK);
								colorN.setTextColor(trendcolor);

								for (int i = 0; i < dataspace.getChildCount(); i++) {
									View child = dataspace.getChildAt(i);
									if (child.getTag() != "vis") {
										child.setVisibility(android.view.View.VISIBLE);
									}
								}

								v.setTag("vis");

								if (datatype == 1) {
									EditText input = (EditText) findViewById(skipId + 1);
									input.setTag("used");
								} else if (datatype == 2 | datatype == 3) {
									TextView input2 = (TextView) findViewById(skipId + 1);
									input2.setTag("used");
								}

							}

						}
					});

					mlayout.addView(borderspace);
					borderspace.addView(inputspace);
					inputspace.addView(cornerspace);

					fieldID++;

					// Create Measure Input
					if (Integer.parseInt(b.getString(2)) == 1) {

						Drawable iw = res.getDrawable(R.drawable.pvtext);
						EditText inputText = new EditText(this);
						inputText
								.setLayoutParams(new LinearLayout.LayoutParams(
										(int) (getResources().getDisplayMetrics().widthPixels - (65.0f * getResources()
												.getDisplayMetrics().density)),
										android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
						inputText.setBackgroundDrawable(iw);
						inputText.setTextColor(android.graphics.Color.LTGRAY);
						inputText.setSingleLine(true);
						inputText.setInputType(InputType.TYPE_CLASS_NUMBER
								| InputType.TYPE_NUMBER_FLAG_DECIMAL
								| InputType.TYPE_NUMBER_FLAG_SIGNED);
						inputText.setHint(" " + b.getString(3));
						inputText.setPadding(
								(int) (8.0f * getResources().getDisplayMetrics().density),
								(int) (8.0f * getResources().getDisplayMetrics().density),
								(int) (8.0f * getResources().getDisplayMetrics().density),
								(int) (8.0f * getResources().getDisplayMetrics().density));
						// Input filter to disable comma and space keys
						InputFilter filter = new InputFilter() {
							@Override
							public CharSequence filter(CharSequence source, int start, int end,
									Spanned dest, int dstart, int dend) {
								for (int i = start; i < end; i++) {
									if (source.charAt(i) == ' ' || source.charAt(i) == ',') {
										return "";
									}
								}
								return null;
							}
						};
						inputText.setFilters(new InputFilter[]{filter});
						inputText.setId(fieldID);
						inputText.setTextSize(18.0f);
						inputText.setTag("used");

						cornerspace.addView(nameView);
						cornerspace.addView(skipSpace);
						skipSpace.addView(inputText);
						skipSpace.addView(skipButton);

					}
					// Create Scale input
					if (Integer.parseInt(b.getString(2)) == 2) {

						final int min = Integer.parseInt(b.getString(4));
						final int max = Integer.parseInt(b.getString(5));

						LinearLayout sliderspace = new LinearLayout(this);
						sliderspace
								.setLayoutParams(new LinearLayout.LayoutParams(
										(int) (getResources().getDisplayMetrics().widthPixels - (65.0f * getResources()
												.getDisplayMetrics().density)),
										android.view.ViewGroup.LayoutParams.MATCH_PARENT));
						sliderspace.setOrientation(0);
						sliderspace.setPadding(
								(int) (10.0f * getResources().getDisplayMetrics().density),
								(int) (8.0f * getResources().getDisplayMetrics().density), 0, 0);

						// Status TextView code
						final RelativeLayout inputRel = new RelativeLayout(this);
						inputRel.setLayoutParams(new RelativeLayout.LayoutParams(
								(int) (getResources().getDisplayMetrics().widthPixels - (180.0f * getResources() //180
										.getDisplayMetrics().density)),
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
						inputRel.setVisibility(4);

						final TextView inputFinal = new TextView(this);
						
						inputFinal.setTag("used");
						inputFinal.setVisibility(4);
						inputFinal.setId(fieldID);
						
						final TextView inputText = new TextView(this);
						RelativeLayout.LayoutParams layoutZ = new RelativeLayout.LayoutParams(
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
						layoutZ.addRule(RelativeLayout.CENTER_IN_PARENT);
						inputText.setLayoutParams(layoutZ);
						inputText.setSingleLine(true);
						inputText.setTextSize(fontsize);
						inputText.setTextColor(android.graphics.Color.GRAY);
						inputText.setText(Integer.toString((min + max) / 2));
						inputRel.addView(inputText);
						
						// Max Value
						final TextView maxText = new TextView(this);
						maxText.setLayoutParams(new LinearLayout.LayoutParams(
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
								android.view.ViewGroup.LayoutParams.MATCH_PARENT));
						maxText.setTextColor(trendcolor);
						maxText.setTextSize(fontsize);
						maxText.setText(Integer.toString(max));

						// Min Value
						final TextView minText = new TextView(this);
						minText.setLayoutParams(new LinearLayout.LayoutParams(
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
								android.view.ViewGroup.LayoutParams.MATCH_PARENT));
						minText.setTextColor(trendcolor);
						minText.setTextSize(fontsize);
						minText.setPadding(0, 0,
								(int) (5.0f * getResources().getDisplayMetrics().density), 0);
						minText.setText(Integer.toString(min));

						// Seekbar code

						cornerspace.addView(nameView);
						cornerspace.addView(skipSpace);
						skipSpace.addView(sliderspace);
						skipSpace.addView(skipButton);
						sliderspace.addView(minText);
						sliderspace.addView(maxText);
						sliderspace.addView(inputFinal);
						sliderspace.addView(inputRel);

						final SeekBar slider = (SeekBar) getLayoutInflater().inflate(
								R.layout.pvslider, null);
						slider.setLayoutParams(new LinearLayout.LayoutParams((int) (getResources()
								.getDisplayMetrics().widthPixels - (180.0f * getResources()
								.getDisplayMetrics().density)), (int) (50.0f * getResources()
								.getDisplayMetrics().density)));

						slider.setMax(max - min);

						slider.setProgress((max - min) / 2);
						if ((max - min) % 2 == 0)
							slider.setProgress((max - min + 1) / 2);

						slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								inputText.setDrawingCacheEnabled(false);
								inputRel.setDrawingCacheEnabled(false);
							}
							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								inputRel.setDrawingCacheEnabled(true);
								inputText.setDrawingCacheEnabled(true);
							}
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress,
									boolean fromUser) {
								inputText.setText(Integer.toString(progress + min)+"  ");
								inputFinal.setText(Integer.toString(progress+min));
								Bitmap bitmap = Bitmap.createBitmap(inputRel.getMeasuredWidth(),
										inputRel.getMeasuredHeight() + 17, Bitmap.Config.ARGB_8888);
								Canvas c = new Canvas(bitmap);
								c.drawBitmap(inputRel.getDrawingCache(), 10, 0, null);
								slider.setBackgroundDrawable(new BitmapDrawable(bitmap));
							}
						});
						sliderspace.addView(slider, 1);
						
					}

					// Create Increment Input
					if (Integer.parseInt(b.getString(2)) == 3) {
						LinearLayout buttonLayout = new LinearLayout(this);
						buttonLayout
								.setLayoutParams(new LinearLayout.LayoutParams(
										(int) (getResources().getDisplayMetrics().widthPixels - (65.0f * getResources()
												.getDisplayMetrics().density)),
										android.view.ViewGroup.LayoutParams.MATCH_PARENT));
						// buttonLayout.setBackgroundColor(android.graphics.Color.BLACK);

						RelativeLayout.LayoutParams svlp = new RelativeLayout.LayoutParams(
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
						svlp.addRule(RelativeLayout.CENTER_IN_PARENT);

						RelativeLayout incLayout = new RelativeLayout(this);
						incLayout.setLayoutParams((new LinearLayout.LayoutParams(
								(int) (100.0f * getResources().getDisplayMetrics().density),
								android.view.ViewGroup.LayoutParams.MATCH_PARENT)));

						// Increment Counter
						TextView inputText = new TextView(this);
						inputText.setLayoutParams(svlp);
						// inputText.setBackgroundColor(android.graphics.Color.BLACK);
						inputText.setTextColor(trendcolor);
						inputText.setText(Integer.toString(0));
						inputText.setId(fieldID);
						inputText.setTag("used");
						inputText.setTextSize(fontsize);
						incLayout.addView(inputText);

						Drawable gb2 = res.getDrawable(R.drawable.nullmap);
						// PlusButton
						final LongPressImageButton plus = new LongPressImageButton(this);
						plus.setLayoutParams(new LinearLayout.LayoutParams(
								(int) (60.0f * getResources().getDisplayMetrics().density),
								android.view.ViewGroup.LayoutParams.MATCH_PARENT));
						plus.setBackgroundDrawable(gb2);
						plus.setText("+");
						plus.setTextSize(30.0f);
						plus.setTextColor(trendcolor);
						plus.setId(fieldID + 11000);
						plus.setLongPressCallback(new LongPressCallback() {
							@Override
							public void onStep(LongPressImageButton button) {
								int idPlus = plus.getId() - 11000;
								TextView text = (TextView) findViewById(idPlus);
								int current = Integer.parseInt(text.getText().toString());
								text.setText(Integer.toString(current + 1));
							}
						});
						plus.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								int idPlus = v.getId() - 11000;
								TextView text = (TextView) findViewById(idPlus);
								int current = Integer.parseInt(text.getText().toString());
								text.setText(Integer.toString(current + 1));
							}
						});

						// Minus Button
						final LongPressImageButton minus = new LongPressImageButton(this);
						minus.setLayoutParams(new LinearLayout.LayoutParams(
								(int) (60.0f * getResources().getDisplayMetrics().density),
								android.view.ViewGroup.LayoutParams.MATCH_PARENT));
						minus.setBackgroundDrawable(gb2);
						minus.setText("-");
						minus.setTextSize(30.0f);
						minus.setTextColor(trendcolor);
						minus.setId(fieldID + 11000);

						minus.setLongPressCallback(new LongPressCallback() {
							@Override
							public void onStep(LongPressImageButton button) {
								int idminus = minus.getId() - 11000;
								TextView text = (TextView) findViewById(idminus);
								int current = Integer.parseInt(text.getText().toString());
								text.setText(Integer.toString(current - 1));
							}
						});
						minus.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								int idPlus = v.getId() - 11000;
								TextView text = (TextView) findViewById(idPlus);
								int current = Integer.parseInt(text.getText().toString());
								text.setText(Integer.toString(current - 1));
							}
						});

						cornerspace.addView(nameView);
						cornerspace.addView(skipSpace);
						skipSpace.addView(buttonLayout);
						skipSpace.addView(skipButton);
						buttonLayout.addView(minus);
						buttonLayout.addView(incLayout);
						buttonLayout.addView(plus);
					}
				} else {
					endTimes[layoutId][count] = 0;
				}
				count++;
			} while (b.moveToNext());

//			av = new AdView(this, AdSize.SMART_BANNER, "a1512bdf5634911");
//			AdRequest adRequest = new AdRequest();
//			av.loadAd(adRequest);

			mlayout.addView(noteField);

			mlayout.addView(submitButton);
			//mlayout.addView(av);

		} else {
			toastwarning("No Items in Poll");
		}
	}
	// old setup
	public void submitInput(int layoutid) {
		LinearLayout pLayout = (LinearLayout) findViewById(400 + layoutid);
		EditText noteView = (EditText) findViewById(layoutid + 6000);
		String note = noteView.getText().toString();
		Long time = System.currentTimeMillis();
		String ts = time.toString();
		for (int i = 0; i < ((pLayout.getChildCount()) - 2); i++) { // CHANGE to
																	// 2 WHEN
																	// YOU
																	// REMOVE
																	// ADS
			View viewkid = pLayout.getChildAt(i);
			LinearLayout itemName = (LinearLayout) viewkid.findViewById(020162013);
			int itemid = (Integer) itemName.getTag();
			double val = 0;
			DatabaseManager dbs = new DatabaseManager(this);
			dbs.open();
			try {
				TextView viewName2 = (TextView) viewkid.findViewWithTag("used");
				val = Double.valueOf(viewName2.getText().toString());
				try {

					Cursor n = dbs.getAllItemsInputs(itemid);
					if ((n.getCount() == 0) && (noteView.getText().toString().equals(""))) {
						note = "Started Insight";
					}
					dbs.addInput(val, ts, note, itemid);
				} catch (NumberFormatException e) {
					toastwarning("Input must be a number");
					return;
				}
			} catch (Exception e) {

			}
		}
		int[] times = endTimes[flipLayout.getDisplayedChild()];
		int pollId = Integer.parseInt((String) findViewById(flipLayout.getDisplayedChild()+100).getTag());
		Log.e("Pollid", ""+pollId);
		db.open();
		Cursor pollCursor = db.getPoll(pollId);
		pollCursor.moveToFirst();
		String pollName = pollCursor.getString(1);
		db.close();
		for (int i : times) {
			if (i > 1) {
				// scheduler = new SchedulerService(""+pollid,
				// (NotificationManager)this.getSystemService("notification"));
				Intent schedIntent = new Intent(this, Alarm.class);
				schedIntent.putExtra("Endtime", i);
				schedIntent.putExtra("Pollid", pollId);
				schedIntent.putExtra("Pollname", pollName);
				alarm.SetAlarm(this, schedIntent);
			}
		}
		trendline(null,String.valueOf(flipLayout.getDisplayedChild()));
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
	// Trendline Activity Event
	public void trendline(KeyEvent event, String value) {
		Intent intent = new Intent(this, TrendlineActivity.class);
		intent.putExtra("PollId", value);
		startActivity(intent);
	}

	public boolean timeSched(int schedtype, double timedifference) {

		double unix = 0;
		switch (schedtype) {
			case 1 : // No schedule
				unix = 0;
				break;
			case 2 : // per Hour
				unix = 3600000;
				break;
			case 3 :// per 3 hours
				unix = 10800000 - 1800000; // minus 30 mins.
				break;
			case 4 :// per day
				unix = 86400000 - 7200000; // minus 2 hours
				break;
			case 5 :// per week
				unix = 604800000 - 86400000; // minus 24 hours
				break;
			default :

				break;
		}
		if (timedifference > unix) {
			return true;
		} else {
			return false;
		}
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
	// GENERIC TOAST POPUP, STATIC TEXT ONLY
	public void toastwarning(String texthere) {
		Toast.makeText(this, texthere, Toast.LENGTH_SHORT).show();
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
	private int findPoll() {
		int flipval = flipLayout.getDisplayedChild();
		View flipchild = flipLayout.getChildAt(flipval);
		int realPoll = Integer.parseInt((String) flipchild.getTag());
		return realPoll;
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
	public int findPageById(int pollId){
		//Finds a viewFlipper page given a poll's ID
		//Returns -1 if no page is found with given ID
		for(int i=0; i<flipLayout.getChildCount(); i++){
			RelativeLayout pollLayout = (RelativeLayout) findViewById(100+i);
			int thisPollsId = Integer.parseInt((String) pollLayout.getTag());
			if(thisPollsId == pollId)
				return i;
		}
		return -1;
	}
}
