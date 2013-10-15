package com.fobbes.fobbesapp;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Extended ImageButton which supports LongPress-Events which count up or down
 * something.
 * 
 * taken from
 * (http://www.rochdev.com/2011/05/update-ui-when-holding-button.html)
 * 
 */

public class LongPressImageButton extends Button {

	/**
	 * Callback that will be called each 100ms while the Button will be touched.
	 */
	private int scaletime = 200;
	public interface LongPressCallback {
		/**
		 * Will be called during long press process.
		 */
		void onStep(LongPressImageButton button);
		
	}

	private final Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			
			if (mDown) {
				// Call method to increment or decrement counter
				if (mCallback != null) {
					mCallback.onStep(LongPressImageButton.this);
					// It is not necessary to repeat this runnable, if there is
					// no Callback set!
					scaletime=scaletime-2;
					if (scaletime<=0){scaletime = 1;}
					mHandler.postDelayed(this, scaletime);
					
				}
			}
		}
	};

	private boolean mDown;

	private Handler mHandler;
	private LongPressCallback mCallback;

	public LongPressImageButton(Context context) {
		super(context);
		initialize();
	}

	public LongPressImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public LongPressImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	private void initialize() {
		mDown = false;
		mHandler = new Handler();
		mCallback = null;
		setLongClickable(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#performLongClick()
	 */
	@Override
	public boolean performLongClick() {
		mDown = true;
		
		// Only necessary, if a callback was set to this class.
		if (mCallback != null) {
			
			mHandler.post(mRunnable);
		}

		super.performLongClick();

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		cancelLongpressIfRequired(event);
		
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		cancelLongpressIfRequired(event);
		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)) {
			cancelLongpress();
		     
		}
		return super.onKeyUp(keyCode, event);
	}

	private void cancelLongpressIfRequired(MotionEvent event) {
		if ((event.getAction() == MotionEvent.ACTION_CANCEL)
				|| (event.getAction() == MotionEvent.ACTION_UP)) {
			cancelLongpress();
		}
	}

	private void cancelLongpress() {
		scaletime = 200;
		mDown = false;
	}

	/**
	 * Returns current {@link LongPressCallback} or null if not set.
	 * 
	 * @return
	 */
	public LongPressCallback getLongPressCallback() {
		return mCallback;
	}

	/**
	 * Sets the current {@link LongPressCallback}. If you want to remove it, set
	 * it to null
	 * 
	 * @param callback
	 */
	public void setLongPressCallback(LongPressCallback callback) {
		this.mCallback = callback;
	}

}