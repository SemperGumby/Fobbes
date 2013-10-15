/*
 * FOBBESAPP-MainActivity.java
 * Programmer: Charles Schuh
 * Main Menu, branching into each mode of execution: Input, Trendline Viewing, and Survey Creation
 */
package com.fobbes.fobbesapp;

import com.fobbes.fobbesapp.R;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class HelpActivity extends Activity implements OnTouchListener {

	// Menu Vars
	static final private int OPTION1_M = Menu.FIRST;
	static final private int OPTION2_M = Menu.FIRST + 1;
	public int uicolor = android.graphics.Color.parseColor("#696969");
	public int fontcolor = android.graphics.Color.parseColor("#E0E0E0");
	public ViewFlipper vf;
	private float downXValue;
	private float downYValue;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		theActivity();
	}

	public void theActivity() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_help);
		overridePendingTransition(0, 0);
		this.setTitle("Insight Help");

		vf = (ViewFlipper) findViewById(R.id.helpflipper);
		vf.setOnTouchListener(this);

		TextView tv1 = (TextView) findViewById(R.id.help1);
		tv1.setText("Welcome!\n\n"
				+ "  Insight is an all-purpose, personal survey tool."
				+ "  By recording data over a period of time, you can examine its trends for patterns."
				+ "  Insight comes with a variety of ways to take data, convenient graphical views, and the ability to analyze data and uncover statistical patterns.\n\n"
				+ "  To name just a few possible uses for Insight, you can track data for:\n"
				+ "  • Health and well-being\n"
				+ "  • Habits\n"
				+ "  • Exercise routines\n"
				+ "  • Finances and investments\n"
				+ "  • Progress towards goals\n"
				+ "  • Your child’s growth rate\n"
				+ "  • Company car mileage\n"
				+ "  • Allowances\n"
				+ "  • Stock market\n"
				+ "  • Multiple project hours\n"
				+ "  • Medications\n"
				+ "  • Multiple tenant rent\n"
				+ "  • Tips\n"
				+ "  • or anything that's important to you!\n\n"
				+ "  To begin, swipe the top bar to navigate between pages.");

		TextView tv2 = (TextView) findViewById(R.id.help2);
		tv2.setText(
				 "  Select Edit Surveys from the main menu to begin a new servey, or add items to an existing one."
				+ "  Multiple surveys can be created and selected by swiping left or right on the top bar.\n\n"
				+ "  There are three types of data:\n\n"
				+ "  • Measure: Lets you enter any number with a unit of measurement.\n"
				+ "  • Scale: Uses a sliding bar between your chosen minimum and maximum.\n"
				+ "  • Increment: Add or subtract from the total of all its entries."
				+ "  Hold a button down to enter larger values.\n\n"
				);
		TextView tv2b = (TextView) findViewById(R.id.help2b);
		
		tv2b.setText(
				 "  Adding a schedule will limit how frequently you can enter data for an item."
				+ "  By selecting a daily schedule, items will be hidden until the scheduled time has elapsed."
				+ "  Use schedules to keep your data tidy and consistent."
				+ "  See the next section, Entering Data, for more on using schedules.\n\n");

		
		TextView tv3 = (TextView) findViewById(R.id.help3);
		tv3.setText(
				 "  Once a survey is created, you may start recording your data."
				+ "  Each item you create will be represented as an input field as defined in Edit Survey.\n\n"
				+ "  • Measure: Type in the value in the enclosed space.\n"
				+ "  • Scale: Move the slider to the left or right.\n"
				+ "  • Increment: Press the + or - keys to increase or decrease the value.\n\n"
				);
		
		TextView tv3b = (TextView) findViewById(R.id.help3b);
		tv3b.setText(
				 "  You also have the ability to skip items in order to save them for a later time."
				+ "  Those values will not be recorded."
				+ "  Once all fields are completed, you may include a note at the bottom to record any extra data or circumstances.\n\n"
				+ "  Touch \"Submit Poll\", and you have completed your survey.\n\n");

		TextView tv4 = (TextView) findViewById(R.id.help4);
		tv4.setText(
				 "  The View Trends page provides an overview of your data across time."
				+ "  Use pinching and sliding motions to scale and navigate your trends."
				+ "  Press the dots along a trend to read your notes.\n\n"
				+ "  The time is displayed on the bottom axis of each trend, and the range of values are defined on the left side.\n\n"
				+ "  A \"dot \" represents a note you have created at that particular time. Click the dot to see your note.\n\n"
				);
		
		TextView tv4b = (TextView) findViewById(R.id.help4b);
		tv4b.setText(
				 "  Use analysis to find correlations in your trends."
				+ "  Pressing the analyze button next to a trend will search for and describe the relationship that trend has to all of the others within the same survey.\n\n"
				+ "  Remember that while two trends may relate, it is not necessarily true that one causes the other."
				+ "  The analysis can only tell you whether they tend to change in a similar way.\n\n");

		TextView tv5 = (TextView) findViewById(R.id.help5);
		tv5.setText(
				 "  • Consider your data type very carefully. Once a datatype is made, only the name and schedule may be changed.\n\n"
				+ "Measure for specific units:\n"
				+ " Meters, Cups, Steps, Dollars, etc\n\n"
				+ "Scale for arbitrary ranges:\n"
				+ " Stress, Energy, Mood, etc\n\n"
				+ "Increment for unknown initial values:\n"
				+ " Take/leave a penny, car mileage, etc.\n\n"
				+ "  • Save your database frequently.  As long as the app is installed, your data will remain on your device. To export your data, go into analyze then export to a .csv file for Excel or similar spreadsheet software.\n\n"
				+ "  • Be honest with your values. The data is only as good as you make it.\n\n"
				+ "  • There is a folder created on your device labelled \"insightdata\". Within it you can find your preset files, csv database export, and facebook post images.\n\n"
				);
		
		TextView tv6 = (TextView) findViewById(R.id.help6);
		tv6.setText(
				 "Insight was developed by Team Fobbes:\n\n"
				+ "Kevin McKee - Producer, Concept, Analysis\n\n"
				+ "Charles Schuh - Lead Programmer, Analysis\n\n"
				+ "Ryan McKee - Programmer, Interface, PR\n\n"
				+ " If you have any questions or comments, please feel free to email us at FobbesApp@gmail.com\n\n\n\n"
				+ "Thanks for downloading!\n\n\n");
		
	}
	// ANDROID MENU ACTIONS
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		//menu.add(0, OPTION1_M, 0, "Option 1");
		//menu.add(0, OPTION2_M, 0, "Option 2");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case OPTION1_M :
				return true;
			case OPTION2_M :
				return true;
		}
		return super.onOptionsItemSelected(item);
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
				if ((vf.getChildCount() > 1)) {
					// going backwards: pushing stuff to the right
					if (((downXValue < currentX) & (x > y) & (x > 20)) && (vf.getDisplayedChild() != 0)) {

						vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
						vf.setOutAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_right_out));
						vf.showPrevious();
					}
					// going forwards: pushing stuff to the left
					if ((downXValue > currentX) & (x > y) & (x > 20)) {
						vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
						vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
						vf.showNext();
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
}
