package com.example.logintoboggan.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.logintoboggan.R;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookServiceException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.widget.WebDialog;

public class FacebookHelper
{
	final String TAG = "FacebookHelper";
	final String InviteRequestMessage = "Play this game, it's great!";

	public interface OpenSessionCallback
	{
		public void onFailure(String error);
		public void onUserMismatch(String error);
		public void onSuccess();
		public void onClose();
	};

	private interface DialogCallback
	{
		public void onFailure(String error);
		public void onCancel();
		public void onSuccess(Bundle values);
	}

	public interface InviteCallback
	{
		public void onFailure(String error);
		public void onCancel();
		public void onSuccess(String requestId);
	}

	// NOTE: Facebook App id should be stored in srings.xml as
	// facebookApplicationId.

	boolean mOpenSessionPending;
	Application mApp;

	String mUserId;
	String mUserFirstName;
	String mUserLastName;
	String mUserBirthday;
	String mUserPictureURL;

	WebDialog mDialog;
	String mDialogAction;
	Bundle mDialogParams;

	public FacebookHelper(Application app)
	{
		mOpenSessionPending = false;
		mApp = app;
		mUserId = null;
		mDialog = null;
		mDialogAction = null;
		mDialogParams = null;
	}

	public boolean hasSession()
	{
		return (Session.getActiveSession() != null) && Session.getActiveSession().isOpened();
	}

	public String getUserId()
	{
		return hasSession() ? mUserId : null;
	}

	public String getUserFirstName()
	{
		return hasSession() ? mUserFirstName : null;
	}

	public String getUserLastName()
	{
		return hasSession() ? mUserLastName : null;
	}

	public String getUserBirthday()
	{
		return hasSession() ? mUserBirthday : null;
	}

	public String getPictureURL()
	{
		return hasSession() ? mUserPictureURL : null;
	}

	public String getAccessToken()
	{
		if (hasSession())
		{
			return Session.getActiveSession().getAccessToken();
		}
		else
		{
			return null;
		}
	}
	
	

	public void openSession(Activity activity, final String expectedUserId, final OpenSessionCallback listener)
	{
		openSession(activity, null, expectedUserId, listener);
	}

	public void openSession(Activity activity, android.support.v4.app.Fragment fragment, final String expectedUserId,
			final OpenSessionCallback listener)
	{
		if (hasSession())
		{
			listener.onSuccess();
			Log.d(TAG, "Previous FB session success...");
		}
		else if (mOpenSessionPending)
		{
			listener.onFailure("earlier openSession is pending");
			Log.d(TAG, "Error: earlier open session is pending...");
		}
		else
		{
			mOpenSessionPending = true;
			Log.d(TAG, "Opening Facebook Session...");

			Session.StatusCallback sessionCallback = new Session.StatusCallback()
			{				
				
				// callback when session changes state
				@Override
				public void call(Session session, SessionState state, Exception exception)
				{
					Log.d("Facebook", "AppId: "+session.getApplicationId());
					
					if (session.isOpened())
					{
						getUserInfo(expectedUserId,session, listener);
					}
					else if (session.isClosed())
					{
						mOpenSessionPending = false;
						if (session.getState() == SessionState.CLOSED_LOGIN_FAILED)
							listener.onFailure("Facebook login failed. Session was closed.");
						else
							listener.onClose();
					}
				}
			};

			Session.openActiveSession(activity, true, sessionCallback);
		}
	}
	
	private void getUserInfo(final String expectedUserId, Session session, final OpenSessionCallback listener)
	{
		Log.d(TAG, "Requesting User...");

		// request additional info about the user
		Bundle params = new Bundle();
		params.putString("fields", "picture,id,birthday,first_name,last_name");

		Request request = new Request(session, "me", params, HttpMethod.GET, new Request.Callback()
		{
			@Override
			public void onCompleted(Response response)
			{
				Log.d(TAG, "Facebook login onComplete().");

				FacebookRequestError error = response.getError();
				if (error != null)
				{
					mOpenSessionPending = false;
					listener.onFailure(error.toString());
				}
				else
				{
					GraphObject go = response.getGraphObject();
					if (go != null)
					{
						JSONObject json = go.getInnerJSONObject();

						String userId = json.optString("id");

						if (expectedUserId != null && !expectedUserId.equals(userId))
						{
							mOpenSessionPending = false;
							listener.onUserMismatch("wrong user: expected " + expectedUserId + ", got " + userId);
						}
						else
						{
							mUserId = userId;
							mUserFirstName = json.optString("first_name");
							mUserLastName = json.optString("last_name");
							mUserBirthday = json.optString("birthday");
							JSONObject picture = json.optJSONObject("picture");
							if (picture != null)
							{
								JSONObject data = picture.optJSONObject("data");
								if (data != null)
								{
									mUserPictureURL = data.optString("url");
								}
							}
							mOpenSessionPending = false;
							listener.onSuccess();
						}
					}
					else
					{
						mOpenSessionPending = false;
						listener.onFailure("Error parsing user graph object");
					}
				}
			}
		});
		request.executeAsync();
	}

	public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data)
	{
		Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
	}

	// shows the WebDialog app request dialog.
	public void invite(Activity activity, List<String> friendIds, final InviteCallback listener)
	{
		if (!hasSession())
		{
			listener.onFailure("No active facebook session");
		}
		else
		{
			Bundle params = new Bundle();
			params.putString("app_id", mApp.getResources().getString(R.string.facebookApplicationId));
			params.putString("message", InviteRequestMessage);

			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < friendIds.size(); i++)
			{
				if (i > 0)
				{
					builder.append(",");
				}
				builder.append(friendIds.get(i));
			}
			params.putString("to", builder.toString());

			showDialog(activity, "apprequests", params, new DialogCallback()
			{
				@Override
				public void onFailure(String error)
				{
					listener.onFailure(error);
				}

				@Override
				public void onCancel()
				{
					listener.onCancel();
				}

				@Override
				public void onSuccess(Bundle bundle)
				{
					listener.onSuccess(bundle.getString("request"));
				}
			});
		}
	}

	// shows the WebDialog app request dialog.
	public void sendRequest(String requestMsg, Activity activity, List<String> friendIds, final InviteCallback listener)
	{
		if (!hasSession())
		{
			listener.onFailure("No active facebook session");
		}
		else
		{
			Bundle params = new Bundle();
			params.putString("app_id", mApp.getResources().getString(R.string.facebookApplicationId));
			params.putString("message", requestMsg);

			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < friendIds.size(); i++)
			{
				if (i > 0)
				{
					builder.append(",");
				}
				builder.append(friendIds.get(i));
			}
			params.putString("to", builder.toString());

			showDialog(activity, "apprequests", params, new DialogCallback()
			{
				@Override
				public void onFailure(String error)
				{
					listener.onFailure(error);
				}

				@Override
				public void onCancel()
				{
					listener.onCancel();
				}

				@Override
				public void onSuccess(Bundle bundle)
				{
					listener.onSuccess(bundle.getString("request"));
				}
			});
		}
	}

	private void showDialog(Activity activity, String action, Bundle params, final DialogCallback listener)
	{
		mDialog = new WebDialog.Builder(activity, Session.getActiveSession(), action, params).setOnCompleteListener(
				new WebDialog.OnCompleteListener()
				{
					@Override
					public void onComplete(Bundle values, FacebookException error)
					{
						if (error != null)
						{
							if (error instanceof FacebookOperationCanceledException)
							{
								// back button or x is pressed.
								listener.onCancel();
							}
							else if (error instanceof FacebookServiceException)
							{
								FacebookServiceException facebookServiceException = (FacebookServiceException) error;
								FacebookRequestError facebookRequestError = facebookServiceException.getRequestError();
								
								// TODO: figure out meaning of magic error code 4201 and replace with constant
								if (facebookRequestError.getRequestStatusCode() == FacebookRequestError.INVALID_HTTP_STATUS_CODE
										&& facebookRequestError.getErrorCode() == 4201)
								{
									// cancel button is pressed on webView.
									listener.onCancel();
								}
								else
								{
									listener.onFailure(error.toString());
								}
							}
						}
						else
						{
							Log.d(TAG, "fb dialog success, values = " + values.toString());
							listener.onSuccess(values);
						}
					}
				}).build();

		Window window = mDialog.getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mDialog.show();
	}

	public void logout()
	{
		if (hasSession())
		{
			Session.getActiveSession().closeAndClearTokenInformation();
		}
	}
}
