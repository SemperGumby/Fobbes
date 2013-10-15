/*
 * FOBBESAPP-TrendlineActivity.java
 * Programmer: Charles Schuh and Ryan McKee
 * Placeholder for an activity which would allow a user to view graphs of their data
 */
package com.fobbes.fobbesapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.csv.CSVWriter;
import com.fobbes.fobbesapp.PlotGraphView.PlotGraphData;

import com.jjoe64.graphview.GraphView.GraphViewContentView;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.compatible.ScaleGestureDetector;

public class TrendlineActivity extends Activity implements OnTouchListener {

	// db vars
	private DatabaseManager db;
	public Cursor a;
	public Cursor b;
	public Cursor p;

	public int uicolor = android.graphics.Color.parseColor("#696969");
	public int fontcolor = android.graphics.Color.parseColor("#E0E0E0");
	public int itemcolor = android.graphics.Color.WHITE;

	public float fontsize = 30.0f;

	// Touch interface
	private float downXValue;
	private float downYValue;

	// Layouts
	private ViewFlipper flipLayoutInput;
	private int fliphist;
	public ViewGroup vg;
	// public int pollId;
	public int trendid = 0;
	public int itemheight = 170;
	private int trendCount;
	public DatabaseManager dbp = new DatabaseManager(this);
	// Time
	public double timestamp;
	public int datatype;
	public double vstart = 0;
	public double vend = 0;
	public double maxTimestamp = 0;
	public double minTimestamp = 0;

	// Menu
	static final private int ANALYZE_T = Menu.FIRST;
	static final private int FACEBOOK_T = Menu.FIRST + 1;
	//static final private int TWITTER_T = Menu.FIRST + 2;

	// Colors
	public int trendcolor;
	
	private Analyze aa = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		trendlineActivity();
	}

	@SuppressWarnings("deprecation")
	public void trendlineActivity() {
		// Orientation changes item height
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == 2) {
			itemheight = (int) (210.0f * getResources().getDisplayMetrics().density);
		} else {
			itemheight = (int) (110.0f * getResources().getDisplayMetrics().density);
		}
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    fliphist = Integer.parseInt(extras.getString("PollId"));
		    
		}

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTitle("View Trends");
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		overridePendingTransition(0, 0);
		trendCount = 0;

		// ViewFlipper Base
		flipLayoutInput = new ViewFlipper(this);
		flipLayoutInput.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
		flipLayoutInput.setLayoutParams(new ViewFlipper.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		this.setContentView(flipLayoutInput);

		// Poll Title
		Resources res = getResources();
		NinePatchDrawable ps = (NinePatchDrawable) res.getDrawable(R.drawable.pollselect);

		// Query DB and populate with every Poll base layout
		dbp.open();
		p = dbp.getAllPolls();
		if (p.getCount() > 0) {
			p.moveToFirst();
			for (int i = 0; i < p.getCount(); i++) {
				p.moveToPosition(i);

				// Layout of Poll Page
				RelativeLayout pollLayout = new RelativeLayout(this);
				pollLayout.setLayoutParams(new RelativeLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT));
				pollLayout.setId(100 + i);
				pollLayout.setTag(p.getString(0));
				flipLayoutInput.addView(pollLayout);

				// Poll Name on top
				TextView pollName = new TextView(this);
				pollName.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						(int) (50.0f * getResources().getDisplayMetrics().density)));
				pollName.setTextSize(30);
				pollName.setTextColor(fontcolor);
				pollName.setOnTouchListener(this);
				pollName.setBackgroundDrawable(ps);
				

				// Scroll Background
				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);

				ScrollView scroll = new ScrollView(this);
				scroll.setLayoutParams(new ScrollView.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				scroll.setPadding(0, (int) (50.0f * getResources().getDisplayMetrics().density), 0,
						0);
				scroll.setId(500 + i);
				scroll.setOnTouchListener(new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (isScaling(event)) {
							return false;
						}
						synchMovement(event);
						return false;
					}
				});
				pollLayout.addView(scroll);

				// Layout within Scroll USED FOR CHILD ITEMS
				LinearLayout mainLayout = new LinearLayout(this);
				mainLayout.setOrientation(1);
				mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT));
				mainLayout.setId(400 + i);
				mainLayout.setTag(p.getString(0));
				scroll.addView(mainLayout);

				// Put Poll Name on Title
				String pollTitle = p.getString(1);
				pollTitle = pollTitle.replace('\"','\'');
				pollTitle = pollTitle.replace('_',' ');
				pollName.setText(" " + pollTitle);
				pollLayout.addView(pollName);
				// Populate
				createTrends(i);
			}
			

		} else {
			toastwarning("no database");
		}
		flipLayoutInput.setDisplayedChild(fliphist);
		dbp.close();
	}

	
	public void createTrends(int layoutId) {

		LinearLayout mlayout = (LinearLayout) findViewById(400 + layoutId);
		int pollId = Integer.parseInt(mlayout.getTag().toString());
		// Inner layout of scroll, Can only be 1 layout.
		LinearLayout trendmain = new LinearLayout(this);
		trendmain.setOrientation(1);
		trendmain.setId(layoutId + 1000);
		trendmain.setTag(Integer.toString(pollId));
		
		mlayout.addView(trendmain);

		// add an ad
//		av = new AdView(this, AdSize.SMART_BANNER, "a1512bdf5634911");
//		AdRequest adRequest = new AdRequest();
//		av.loadAd(adRequest);
//
//		mlayout.addView(av);

		// add a space

		final LinearLayout space = new LinearLayout(this);
		space.setId(Integer.parseInt((mlayout.getTag().toString())) + 8000);
		RelativeLayout.LayoutParams spacelayout;

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == 2) {
			spacelayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					(int) ((getResources().getDisplayMetrics().heightPixels) / 2.5));
		} else {
			spacelayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					(getResources().getDisplayMetrics().heightPixels) / 3);
		}
		space.setLayoutParams(spacelayout);
		space.setVisibility(View.GONE);
		mlayout.addView(space);

		// Get out the DB
		db = new DatabaseManager(this);
		db.open();
		a = db.getAllPollsItems(pollId); // OPENING ITEM DB
		a.moveToFirst();
		if (a.getCount() > 0) {
			do {
				// get ID from Item
				trendid = Integer.parseInt(a.getString(0)); // GETTING ID PER
															// ROW on ITEM DB
				datatype = Integer.valueOf(a.getString(2));
				Cursor b = db.getItem(trendid);
				b.moveToFirst();
				if (b.getCount() > 0) {
					int itemSched = Integer.parseInt(b.getString(7));
					if (Integer.parseInt(b.getString(2)) == 1) {
						Cursor c = db.getAllItemsInputs(trendid);
						if (c.moveToFirst() == true) {
							c.moveToFirst();
							PlotGraphData[] trendpoints = new PlotGraphData[c.getCount()];
							do {
								timestamp = Double.valueOf(c.getString(2));
								double value = Double.valueOf(c.getString(1));
								String note = c.getString(3);
								trendpoints[c.getPosition()] = new PlotGraphData(timestamp, value,
										note);
								Log.i(null,
										c.getPosition() + " " + c.getString(0) + " "
												+ c.getString(1) + " " + timestamp);
							} while (c.moveToNext());
							c.moveToFirst();
							addTrend(trendpoints, 1, 0, 0, layoutId, itemSched);
							if (Double.valueOf(c.getString(2)) < minTimestamp || minTimestamp == 0) {
								minTimestamp = Double.valueOf(c.getString(2));
							}

							c.close();
						} else {
							//toastwarning("No Entries for " + b.getString(1));
						}

					} else {
						Cursor c = db.getAllItemsInputs(trendid);
						if (c.moveToFirst() == true) {
							c.moveToFirst();
							PlotGraphData[] trendpoints = new PlotGraphData[c.getCount()];

							double value2 = 0;

							do {
								timestamp = Double.valueOf(c.getString(2));
								double value = Double.valueOf(c.getString(1));
								value2 = value2 + value;
								String note = c.getString(3);

								if (Integer.parseInt(a.getString(2)) == 3) {
									trendpoints[c.getPosition()] = new PlotGraphData(timestamp,
											value2, note);
								} else {
									trendpoints[c.getPosition()] = new PlotGraphData(timestamp,
											value, note);
								}
								Log.i(null,
										c.getPosition() + " " + c.getString(0) + " "
												+ c.getString(1) + " " + timestamp);

							} while (c.moveToNext());

							c.moveToFirst();

							if (Integer.parseInt(a.getString(2)) == 2) {
								final int min = Integer.parseInt(a.getString(4));
								final int max = Integer.parseInt(a.getString(5));
								addTrend(trendpoints, 1, min, max, layoutId, itemSched);
								if (Double.valueOf(c.getString(2)) < minTimestamp
										|| minTimestamp == 0) {
									minTimestamp = Double.valueOf(c.getString(2));
								}

							} else {
								addTrend(trendpoints, 1, 0, 0, layoutId, itemSched);
								if (Double.valueOf(c.getString(2)) < minTimestamp
										|| minTimestamp == 0) {
									minTimestamp = Double.valueOf(c.getString(2));
								}

							}
							c.close();
						} else {
							toastwarning("No Entries for " + b.getString(1));
						}

					}
				}

			} while (a.moveToNext());
			int numGraphs = trendmain.getChildCount();
			for (int i = 0; i < numGraphs; i++) {
				PlotGraphView graph = (PlotGraphView) mlayout.findViewById(20000 + i);
				if (minTimestamp != timestamp) {
					graph.setViewPort(minTimestamp, maxTimestamp - minTimestamp);
				} else {
					graph.setViewPort(timestamp - 21600000, (43200000));
				};
			}
			maxTimestamp = 0;
			minTimestamp = 0;
			db.close();
		} else {

		}
	}
	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	// Item Trend
	@SuppressWarnings("deprecation")
	public void addTrend(PlotGraphData[] GVD, int datatype, int min, int max, int layoutid,
			int plotSched) {

		Resources res = getResources();
		Drawable bg = res.getDrawable(R.drawable.bg1);
		id2color(trendid);
		LinearLayout m2layout = (LinearLayout) findViewById(layoutid + 1000);

		// border
		LinearLayout borderspace = new LinearLayout(this);
		borderspace.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, itemheight));
		borderspace.setOrientation(1);
		borderspace.setPadding(2, 2, 2, 2);
		borderspace.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
		borderspace.setId(10000 + trendid);
		borderspace.setTag("notskipped");
		

		m2layout.addView(borderspace);

		// Trend Block
		LinearLayout trendspace = new LinearLayout(this);
		trendspace.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.Gravity.RIGHT));
		trendspace.setId(trendid);
		trendspace.setOrientation(1);
		trendspace.setBackgroundColor(trendcolor);
		borderspace.addView(trendspace);

		// CornerRounding
		LinearLayout cornerspace = new LinearLayout(this);
		cornerspace.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		NinePatchDrawable npd = (NinePatchDrawable) res.getDrawable(R.drawable.roundedge);

		cornerspace.setOrientation(1);
		cornerspace.setBackgroundDrawable(npd);
		cornerspace.setPadding(3, 3, 3, 3);

		trendspace.addView(cornerspace);
		// TitleBlock
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		TextView nameView = new TextView(this);
		nameView.setText(" " + a.getString(1) + " " + a.getString(3));// + " "
				//+ sched2word(Integer.parseInt(a.getString(7))));

		nameView.setLayoutParams(new LinearLayout.LayoutParams(dm.widthPixels,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		// nameView.setBackgroundColor(android.graphics.Color.BLACK);
		// nameView.setTextColor(android.graphics.Color.BLACK);
		nameView.setTextColor(trendcolor);

		// Line Colors, thickness
		GraphViewStyle graphStyle = new GraphViewStyle();
		// graphStyle.setGridColor(trendcolor);
		// graphStyle.setVerticalLabelsColor(trendcolor);
		// graphStyle.setHorizontalLabelsColor(trendcolor);

		LinearLayout aspace = new LinearLayout(this);
		aspace.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		aspace.setOrientation(0);
		// aspace.setBackgroundColor(android.graphics.Color.BLACK);

		Button aButt = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
		aButt.setLayoutParams(new LinearLayout.LayoutParams((int) (45.0f * getResources()
				.getDisplayMetrics().density), android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		aButt.setText("=");
		aButt.setTextColor(trendcolor);

		// aButt.setBackgroundDrawable(gb1);
		aButt.setId(30000 + m2layout.getChildCount() - 1);
		aButt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				//showDialog(0);
				//Pro version
				int selected = v.getId() - 30000;
				analyzePopup2(TrendlineActivity.this);				
				TextView analyzeView = (TextView) findViewById(04032013);
				TextView titleView = (TextView) findViewById(04052013);
				LinearLayout mLayout = (LinearLayout) findViewById(flipLayoutInput
						.getDisplayedChild() + 1000);
				int numGraphs = mLayout.getChildCount();
				int[] schedArray = new int[numGraphs];
				PlotGraphView[] graphArray = new PlotGraphView[numGraphs];
				for (int i = 0; i < numGraphs; i++) {
					// crossAverages[i] = ((PlotGraphView)
					// mLayout.findViewById(20000 + i)).getAverage();
					graphArray[i] = (PlotGraphView) mLayout.findViewById(20000 + i);
					schedArray[i] = ((PlotGraphView) mLayout.findViewById(20000 + i)).getSched();
				}
				titleView.setText("Results for " + graphArray[selected].getName());
				
				
				Analyze aa = new Analyze(graphArray, vstart, vend, analyzeView, selected, schedArray, (TrendlineActivity) v.getContext());
				try{
					Analyze oldAnalyze = ((TrendlineActivity) v.getContext()).getAnalyze();
					oldAnalyze.cancel(true);
					setProgressBarIndeterminateVisibility(false);
				}
				catch(NullPointerException e){
					
				}
				((TrendlineActivity) v.getContext()).setAnalyze(aa);
				
				
				aa.execute();
			}
		});

		// Graph
		PlotGraphSeries exampleSeries = new PlotGraphSeries(a.getString(3),
				new GraphViewSeriesStyle(trendcolor, 3), GVD);
		PlotGraphView graphView = new PlotGraphView(this, (""), a.getString(1), this, plotSched) {

			@Override
			protected String formatLabel(double value, boolean isValueX) {
				double viewStart = this.getViewportStart();
				double viewEnd = this.getViewportEnd();
				vstart = viewStart;
				vend = viewEnd;

				double span = viewEnd - viewStart;
				if (isValueX) {
					// convert unix time to human time

					String format = "HH:mm MM/dd";
					if (span <= 129600000) // Less than 36 hours
						format = "HH:mm MM/dd";
					if (span > 129600000 && span <= 604800000) // Between 36
																// hours and 1
																// week
						format = "MM/dd";
					if (span > 604800000 && span <= 31557600000L)// Between 1
																	// week and
																	// 1 year
						format = "MM/yyyy";
					if (span > 31557600000L)
						format = "MM/yyyy";
					SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
					return sdf.format(new Date((long) value));
				} else // return super.formatLabel(value, isValueX); // let the
						// y-value be normal-formatted
				{
					return super.formatLabel(value, isValueX);
				}
			}
		};

		graphView.setGraphViewStyle(graphStyle);

		graphView.addSeries(exampleSeries); // data
		if (Integer.valueOf(a.getString(2)) == 2) {
			graphView.setManualYAxis(true);
			graphView.setManualYAxisBounds(max, min);
		} else {
			graphView.setManualYAxisBounds((graphView.getMaxY()), graphView.getMinY());
		}
		graphView.setViewPort(timestamp - 103680000, (129600000)); // 36 hour
																	// spread
		if (timestamp > maxTimestamp)
			maxTimestamp = timestamp;
		// graphView.setBackgroundColor(android.graphics.Color.BLACK);
		graphView.setLayoutParams(new LinearLayout.LayoutParams(
				(int) (getResources().getDisplayMetrics().widthPixels - (50.0f * getResources()
						.getDisplayMetrics().density)),
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		graphView.setBackgroundDrawable(bg);
		// Set X scrolling and zooming
		graphView.setScrollable(true);
		graphView.setScalable(true);
		graphView.setId(20000 + m2layout.getChildCount() - 1);
		trendCount++;
		// X labels when not scrolling or scaling
		// graphView.setHorizontalLabels(new String[]{"24", "12", "Now",
		// "Hours ago    "});
		cornerspace.addView(nameView);

		cornerspace.addView(aspace);
		aspace.addView(graphView);
		aspace.addView(aButt);
		borderspace.setVisibility(0);
	}

	// convert id int into a color int
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

		// menu.add(0, ANALYZE_T, 0, "Analyze");
		// menu.add(0, FACEBOOK_T, 0, "Post to Facebook");
		// menu.add(0, TWITTER_T, 0, "Post to Twitter");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case ANALYZE_T :
				
			case FACEBOOK_T :
				snapshots(flipLayoutInput.getDisplayedChild());
				return true;
			
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * public void onScaleEnd(float f){ for (int i = 0; i<trendCount; i++){
	 * PlotGraphView v = (PlotGraphView) findViewById(200+i);
	 * v.setScaleFactor(f); } }
	 */
	public void synchMovement(MotionEvent e) {
		LinearLayout mLayout = (LinearLayout) findViewById(flipLayoutInput.getDisplayedChild() + 1000);
		int numGraphs = mLayout.getChildCount();
		for (int i = 0; i < numGraphs; i++) {
			PlotGraphView graphView = (PlotGraphView) mLayout.findViewById(20000 + i);
			GraphViewContentView gvm = graphView.getContentView();
			gvm.processTouchEvent(e);
		}
	}

	public void zeroLastX() {
		LinearLayout mLayout = (LinearLayout) findViewById(flipLayoutInput.getDisplayedChild() + 1000);
		int numGraphs = mLayout.getChildCount();
		for (int i = 0; i < numGraphs; i++) {
			GraphViewContentView gvm = ((PlotGraphView) mLayout.findViewById(20000 + i))
					.getContentView();
			gvm.zeroLastX();
		}
	}

	public void onScale(float f) {
		// toastwarning("Scale gesture");
		LinearLayout mLayout = (LinearLayout) findViewById(flipLayoutInput.getDisplayedChild() + 1000);
		int numGraphs = mLayout.getChildCount();
		for (int i = 0; i < numGraphs; i++) {
			PlotGraphView v = (PlotGraphView) mLayout.findViewById(20000 + i);
			// toastwarning(""+f);
			v.setScaleFactor(f);
		}
	}

	public boolean isScaling(MotionEvent event) {
		LinearLayout mLayout = (LinearLayout) findViewById(flipLayoutInput.getDisplayedChild() + 1000);
		int numGraphs = mLayout.getChildCount();
		for (int i = 0; i < numGraphs; i++) {
			PlotGraphView v = (PlotGraphView) mLayout.findViewById(20000 + i);
			ScaleGestureDetector detector = v.scaleDetector;
			detector.onTouchEvent(event);
			boolean handled = detector.isInProgress();
			if (handled) {
				invalidateGraphs();
				return true;
			}
		}
		return false;
	}

	public void invalidateGraphs() {
		LinearLayout mLayout = (LinearLayout) findViewById(flipLayoutInput.getDisplayedChild() + 1000);
		int numGraphs = mLayout.getChildCount();
		for (int i = 0; i < numGraphs; i++) {
			PlotGraphView v = (PlotGraphView) mLayout.findViewById(20000 + i);
			GraphViewContentView gvm = v.getContentView();
			gvm.invalidate();
		}
	}

	public void disableScroll() {
		ScrollView scroll = (ScrollView) findViewById(flipLayoutInput.getDisplayedChild() + 500);
		scroll.setEnabled(false);
	}

	public void enableScroll() {
		ScrollView scroll = (ScrollView) findViewById(flipLayoutInput.getDisplayedChild() + 500);
		scroll.setEnabled(true);
	}

	public void invalidateAll() {
		LinearLayout mLayout = (LinearLayout) findViewById(flipLayoutInput.getDisplayedChild() + 1000);
		int numGraphs = mLayout.getChildCount();
		for (int i = 0; i < numGraphs; i++) {
			GraphViewContentView v = ((PlotGraphView) mLayout.findViewById(20000 + i))
					.getContentView();
			v.invalidate();
		}
	}

	// Toast Popups
	public void toastwarning(String texthere) {
		Toast.makeText(this, texthere, Toast.LENGTH_SHORT).show();
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
	public void fb(View v) {
		Intent intent = new Intent(this, FacebookActivity.class);
		startActivity(intent);
	}

	// Main Activity Event
	public void debug(KeyEvent event) {
		Intent intent = new Intent(this, DBDebug.class);
		startActivity(intent);
	}
	private void snapshots(int poll) {

		try {
			// directory is created;
			File direct = new File(Environment.getExternalStorageDirectory() + "/insightdata");
			if (!direct.exists()) {
				if (direct.mkdir()) {
				}
			}
			// get poll area
			LinearLayout slayout = (LinearLayout) findViewById(1000 + poll);
			LinearLayout slayoutchild = (LinearLayout) slayout.getChildAt(0);
			slayoutchild.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			slayoutchild.layout(0, 0, slayoutchild.getMeasuredWidth(),
					slayoutchild.getMeasuredHeight());

			int bmwidth = slayout.getMeasuredWidth() + 10;
			int childheight = slayoutchild.getMeasuredHeight();
			int childstacksize = 0;

			for (int i = 0; i < slayout.getChildCount(); i++) {
				LinearLayout ssl = (LinearLayout) slayout.getChildAt(i);
				String grabtag = ssl.getTag().toString();
				if (grabtag == "notskipped") {
					childstacksize++;
				}
			}

			int bmheight = childstacksize * childheight;

			Bitmap bitmap = Bitmap.createBitmap(bmwidth, bmheight, Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);
			// c.setDensity(c.getDensity()/2);

			for (int i = 0; i < slayout.getChildCount(); i++) {
				// Get BorderSpace
				LinearLayout ssl = (LinearLayout) slayout.getChildAt(i);
				String grabtag = ssl.getTag().toString();

				if (grabtag == "notskipped") {
					ssl.setDrawingCacheEnabled(true);
					ssl.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
							MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
					ssl.layout(0, 0, ssl.getMeasuredWidth(), ssl.getMeasuredHeight());
					ssl.buildDrawingCache();
					Bitmap bmp = Bitmap.createBitmap(ssl.getDrawingCache());
					c.drawBitmap(bmp, 0, childheight * i, null);
					ssl.setDrawingCacheEnabled(false);
				}
			}
			String path = Environment.getExternalStorageDirectory().toString();
			FileOutputStream out = new FileOutputStream(path + "/insightdata/" + "fbtrend.png");
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

		} catch (Exception e) {
			e.printStackTrace();
			toastwarning("Cant Make image");
		}
		trendlineActivity();
		// fbPopup(this); //Facebook Popup
		fb(null); // Facebook Activity
		toastwarning("Trendlines Captured");
	}

	private void fbPopup(final Activity context) {
		// Inflate the popup_layout.xml
		RelativeLayout hookv = (RelativeLayout) findViewById(100);
		LinearLayout viewGroup = (LinearLayout) findViewById(R.id.main_ui_container);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.main, viewGroup);

		// Creating the PopupWindow
		final PopupWindow popup = new PopupWindow(context);
		hookv.addView(layout);
		popup.setFocusable(true);

		popup.showAtLocation(layout, 0, 0, 0);

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

				if (flipLayoutInput.getChildCount() > 1) {
					// going backwards: pushing stuff to the right
					if ((downXValue < currentX) & (x > y) & (x > 20)) {
						if (findViewById(04042013) != null) {
							RelativeLayout v = (RelativeLayout) findViewById(04042013);
							RelativeLayout vp = (RelativeLayout) v.getParent();
							vp.removeView(v);
							
						}

						flipLayoutInput.setInAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_right_in));
						flipLayoutInput.setOutAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_right_out));
						try{
							aa.cancel(true);
							setProgressBarIndeterminateVisibility(false);
						}
						catch(NullPointerException e){
							
						}
						flipLayoutInput.showPrevious();
					}
					// going forwards: pushing stuff to the left
					if ((downXValue > currentX) & (x > y) & (x > 20)) {
						if (findViewById(04042013) != null) {
							RelativeLayout v = (RelativeLayout) findViewById(04042013);
							RelativeLayout vp = (RelativeLayout) v.getParent();
							vp.removeView(v);
						}

						flipLayoutInput.setInAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_left_in));
						flipLayoutInput.setOutAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_left_out));
						try{
							aa.cancel(true);
							setProgressBarIndeterminateVisibility(false);
						}
						catch(NullPointerException e){
							
						}
						flipLayoutInput.showNext();
						
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

	private void analyzePopup2(final Activity context) {

		// Get FlipChild
		int flipval = flipLayoutInput.getDisplayedChild();
		final RelativeLayout hookv = (RelativeLayout) flipLayoutInput.getChildAt(flipval);
		Resources res = getResources();

		LinearLayout spacer = (LinearLayout) findViewById(findPoll() + 8000);
		spacer.setVisibility(View.VISIBLE);

		if (findViewById(04042013) == null) {

			// Set base relative with ghost coloring
			final RelativeLayout rl1 = new RelativeLayout(this);
			rl1.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			rl1.setId(04042013);
			rl1.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_up_in));
			hookv.addView(rl1);

			// Set center content white rounded box
			NinePatchDrawable svbg = (NinePatchDrawable) res.getDrawable(R.drawable.roundedbg);
			svbg.setColorFilter(android.graphics.Color.LTGRAY, Mode.SRC_ATOP);

			RelativeLayout.LayoutParams popuplayout;

			int orientation = getResources().getConfiguration().orientation;
			if (orientation == 2) {
				popuplayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						(int) ((getResources().getDisplayMetrics().heightPixels) / 2.5));
			} else {
				popuplayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						(getResources().getDisplayMetrics().heightPixels) / 3);
			}
			popuplayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

			LinearLayout pcopy = new LinearLayout(context);
			pcopy.setLayoutParams(popuplayout);
			// pcopy.setPadding(2, 2, 2, 2);
			pcopy.setBackgroundDrawable(svbg);
			pcopy.setOrientation(1);

			// Set Title
			TextView ptitle = new TextView(context);
			ptitle.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					(int) (26.0f * getResources().getDisplayMetrics().density)));
			ptitle.setText("Results");
			ptitle.setBackgroundColor(android.graphics.Color.LTGRAY);
			ptitle.setId(04052013);
			ptitle.setTextColor(android.graphics.Color.BLACK);
			ptitle.setTextSize(20);

			// Set Button OK
			LinearLayout buttonbg = new LinearLayout(this);
			buttonbg.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));
			buttonbg.setBackgroundColor(android.graphics.Color.BLACK);

			RelativeLayout.LayoutParams buttonl = new RelativeLayout.LayoutParams(((getResources()
					.getDisplayMetrics().widthPixels) / 2) - 5,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			Button pselect = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
			pselect.setLayoutParams(buttonl);
			pselect.setText("Ok");
			pselect.setTextColor(android.graphics.Color.LTGRAY);
			pselect.setPadding(2, 2, 2, 2);
			pselect.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LinearLayout spacer = (LinearLayout) findViewById(findPoll() + 8000);
					spacer.setVisibility(View.GONE);
					hookv.removeView(rl1);
				}
			});
			Button bfb = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
			bfb.setLayoutParams(buttonl);
			bfb.setText("Facebook");
			bfb.setTextColor(android.graphics.Color.LTGRAY);
			bfb.setPadding(2, 2, 2, 2);
			bfb.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					hookv.removeView(rl1);
					snapshots(flipLayoutInput.getDisplayedChild());
				}
			});
			Button bdump = (Button) getLayoutInflater().inflate(R.layout.pvb, null);
			bdump.setLayoutParams(buttonl);
			bdump.setText("Export");
			bdump.setTextColor(android.graphics.Color.LTGRAY);
			bdump.setPadding(2, 2, 2, 2);
			bdump.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					toastwarning("CSV located in \"insightdata\" folder");
					//showDialog(0);
					
					//ProVersion
					dbWrite();
				}
			});

			// Set Content
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			ScrollView sv1 = new ScrollView(this);

			if (orientation == 2) {
				sv1.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						(int) ((((getResources().getDisplayMetrics().heightPixels) / 2.5)) - (80.0f * getResources()
								.getDisplayMetrics().density))));
			} else {
				sv1.setLayoutParams(new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT, (int) (((getResources()
								.getDisplayMetrics().heightPixels) / 3) - (80.0f * getResources()
								.getDisplayMetrics().density))));
			}

			sv1.setBackgroundColor(android.graphics.Color.BLACK);

			TextView pcontent = new TextView(context);
			pcontent.setLayoutParams(lp);
			pcontent.setPadding(10, 10, 10, 10);
			pcontent.setTextColor(android.graphics.Color.LTGRAY);
			pcontent.setId(04032013);
			pcontent.setText("Analyzing...");
			// Combine into P layout

			pcopy.addView(ptitle);
			pcopy.addView(sv1);
			sv1.addView(pcontent);
			pcopy.addView(buttonbg);
			buttonbg.addView(bdump);
			//buttonbg.addView(bfb);
			buttonbg.addView(pselect);

			// Add to Preset Scroll List
			rl1.addView(pcopy);
		}

	}
	public void setAnalyzeText(String message){
		final String finalString = message;
		final TextView analyzeView = (TextView) findViewById(04032013);
		analyzeView.post(new Runnable(){
			public void run(){
				analyzeView.setText(finalString);
			}
		});
	}
	private int findPoll() {
		int flipval = flipLayoutInput.getDisplayedChild();
		View flipchild = flipLayoutInput.getChildAt(flipval);
		int realPoll = Integer.parseInt((String) flipchild.getTag());
		return realPoll;
	}
	public Analyze getAnalyze(){
		return aa;
	}
	public void setAnalyze(Analyze aa){
		this.aa = aa;
	}
	public void dbWrite() {
		ArrayList<String> exs = new ArrayList<String>();
		String ListDB;
		String format = "MM/dd/yyyy HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		
		
		db.open();
		Cursor a = db.getPoll(findPoll());
		if (a.getCount() > 0) {
			a.moveToFirst();

			// Name of Poll
			ListDB = String.valueOf(a.getString(1));
			String NameTest = ListDB + " Survey" + "\n";
			exs.add(ListDB + " Survey\n");

			// Item per Poll
			String line;

			a = db.getAllPollsItems(findPoll());
			a.moveToFirst();
			if (a.getCount() > 0) {
				do {
					line = ("\n" + a.getString(1) + " " + a.getString(3) + " "
							+ sched2word(Integer.parseInt(a.getString(7))) + "\n");
					NameTest = NameTest + line;
					line = (a.getString(1) + " " + a.getString(3) + " "
							+ sched2word(Integer.parseInt(a.getString(7))) + "\n");
					exs.add(line);

					// Input per Item
					Cursor z = db.getAllItemsInputs(Integer.parseInt(a.getString(0)));
					z.moveToFirst();
					if (z.getCount() > 0) {
						int counter = 1;
						do {
							line = String.valueOf(counter) + "   " + z.getString(1) + "   "
									+sdf.format(new Date((long) Long.valueOf(z.getString(2))))+ " " + z.getString(3) + "\n";
							NameTest = NameTest + line;
							line = String.valueOf(counter) + "," + z.getString(1) + "," +sdf.format(new Date((long) Long.valueOf(z.getString(2))))+
									","+ z.getString(2) + "," + z.getString(3) + "\n";
							exs.add(line);
							counter++;
						} while (z.moveToNext());
						z.close();

					}

				} while (a.moveToNext());
			}

			else {
				toastwarning("No items");
			}
			a.close();
			db.close();
			TextView writeview = (TextView) findViewById(04032013);
			writeview.setText(NameTest);

			TextView titleView = (TextView) findViewById(04052013);
			titleView.setText("Exported");
			// Convert to String[]
			String[] exsa = new String[exs.size()];
			exsa = exs.toArray(exsa);

			File direct = new File(Environment.getExternalStorageDirectory() + "/insightdata");
			if (!direct.exists()) {
				if (direct.mkdir()) {
				}
			}
			String ename = "insightdata_" + ListDB + ".csv";
			File file = new File(direct, ename);
			try {
				CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ',',
						CSVWriter.NO_QUOTE_CHARACTER);

				csvWrite.writeNext(exsa);

				csvWrite.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Write to file

		} else
			a.close();
		db.close();

	}
	@Override
	protected Dialog onCreateDialog(int id) {
		// We have only one dialog.
		return new AlertDialog.Builder(this)
				.setTitle("Insight Pro Feature")
				.setCancelable(false)
				.setMessage("Please purchase Insight Pro from the Android Market")
				.setPositiveButton("Buy App", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri
								.parse("market://details?id="));  //Add to url
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
}
