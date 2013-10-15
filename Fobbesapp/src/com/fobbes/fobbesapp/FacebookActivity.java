/**
 * Copyright 2012 Facebook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fobbes.fobbesapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.*;

import java.util.*;

public class FacebookActivity extends FragmentActivity {

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

	protected final Context ctxfb = this;

	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

	
	public Button postPhotoButton;

	public LoginButton loginButton;
	public ProfilePictureView profilePictureView;
	public TextView greeting;
	private PendingAction pendingAction = PendingAction.NONE;
	private ViewGroup controlsContainer;
	public GraphUser user;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}
	public UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		
		if (savedInstanceState != null) {
			String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.main);

		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				FacebookActivity.this.user = user;
				updateUI();
				// It's possible that we were waiting for this.user to be
				// populated in order to post a
				// status update.
				handlePendingAction();
			}
		});

		profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
		greeting = (TextView) findViewById(R.id.greeting);
		postPhotoButton = (Button) findViewById(R.id.postPhotoButton);
		postPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClickPostPhoto();
			}
		});
		controlsContainer = (ViewGroup) findViewById(R.id.main_ui_container);

		final FragmentManager fm = getSupportFragmentManager();
		// Listen for changes in the back stack so we know if a fragment got
		// popped off because the user
		// clicked the back button.
		fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				if (fm.getBackStackEntryCount() == 0) {
					// We need to re-show our UI.
					controlsContainer.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();

		updateUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(FacebookActivity.this).setTitle(R.string.cancelled)
					.setMessage(R.string.permission_not_granted)
					.setPositiveButton(R.string.ok, null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	public void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		postPhotoButton.setEnabled(enableButtons);

		if (enableButtons && user != null) {
			profilePictureView.setProfileId(user.getId());
			greeting.setText(getString(R.string.hello_user, user.getFirstName()));
		} else {
			profilePictureView.setProfileId(null);
			greeting.setText(null);
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but
		// we assume they
		// will succeed.
		pendingAction = PendingAction.NONE;

		switch (previouslyPendingAction) {
			case POST_PHOTO :
				postPhoto();
				break;
			case POST_STATUS_UPDATE :
				postStatusUpdate();
				break;
		}
	}

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
		String title = null;
		String alertMessage = null;
		if (error == null) {
			title = getString(R.string.success);
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = getString(R.string.successfully_posted_post, message, id);
		} else {
			title = getString(R.string.error);
			alertMessage = error.getErrorMessage();
		}

		new AlertDialog.Builder(this).setTitle(title).setMessage(alertMessage)
				.setPositiveButton(R.string.ok, null).show();
	}

	public void onClickPostStatusUpdate() {
		performPublish(PendingAction.POST_STATUS_UPDATE);
	}

	// POST MESSAGE
	private void postStatusUpdate() {
		if (user != null && hasPublishPermission()) {
			final String message = getString(R.string.status_update, user.getFirstName(),
					(new Date().toString()));
			Request request = Request.newStatusUpdateRequest(Session.getActiveSession(), message,
					new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(message, response.getGraphObject(),
									response.getError());
						}
					});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_STATUS_UPDATE;
		}
	}

	public void onClickPostPhoto() {
		performPublish(PendingAction.POST_PHOTO);
	}

	// POST PHOTO
	private void postPhoto() {
		if (hasPublishPermission()) {
			Bitmap image = getimages();
			Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image,
					new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(getString(R.string.photo_post),
									response.getGraphObject(), response.getError());
						}
					});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_PHOTO;
		}
	}

	private Bitmap getimages() {

		try {
			String image = "fbtrend";
			String path = Environment.getExternalStorageDirectory().toString();
			Bitmap trendpick2 = BitmapFactory.decodeFile(path + "/insightdata/" + image + ".png");
			return trendpick2;
		} catch (Exception e) {
			toastwarning("Image not Found");
			return null;
		}
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains("publish_actions");
	}

	private void performPublish(PendingAction action) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
			} else {
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this,
						PERMISSIONS));
			}
		}
	}
	// Toast Popups
	public void toastwarning(String texthere) {
		Toast.makeText(this, texthere, Toast.LENGTH_SHORT).show();
	}
	
}
