package com.example.logintoboggan.helper;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

// Mobage NDK imports
import com.mobage.android.analytics.IAnalyticsActivity;
import com.mobage.global.android.Mobage;
import com.mobage.global.android.ServerMode;
import com.mobage.global.android.data.User;
import com.mobage.global.android.lang.CancelableAPIStatus;
import com.mobage.global.android.lang.Error;
import com.mobage.global.android.lang.SimpleAPIStatus;
import com.mobage.global.android.social.common.People;
import com.mobage.global.android.social.common.People.IGetCurrentUserCallback;
import com.mobage.global.android.social.common.Service;
import com.mobage.global.android.social.common.Auth;

import java.util.ArrayList;
import java.util.List;

public class MobageHelper
{
	final String TAG = "MobageHelper";
	
	public interface GetUserInfoCallback
	{
		public void onComplete(User user);
	};

	public interface SucceedFailCallback
	{
		public void onSuccess();
		public void onFailure(String reason);
	};
	
	public interface AuthorizeTokenCallback
	{
		public void onSuccess(String verifier);
		public void onFailure(String error);
	};
	
	public interface SucceedCancelFailCallback
	{
		public void onSuccess();
		public void onCancel();
		public void onFailure(String reason);
	};
	
	public interface EstablishSessionCallback
	{
		public void onSuccess();
		public void onNoExistingSession();
		public void onFailure(String reason);
	};

	
	



	final static String AppVersion = "1.0";

	// These come from the developer.mobage.com dev-portal
	final static String AppKey = "LoginToboggan-Android";
	final static String ConsumerKey = "g3qZAEZPY9Ac5bIZNdnNA";
	final static String ConsumerSecret = "GpBsi3Df8kSYwvaUEAjQ3eLV29YCye8g1zD2Nnzo";

	IAnalyticsActivity mAnalyticsReporter = null;

	private User currentUser;
	
	Application mApp;

	boolean mHasSession;

	public MobageHelper(Application app)
	{
		mApp = app;
		mHasSession = false;
	}

	public void initialize(Activity activity)
	{
		if (!Mobage.isInitialized())
		{
			Mobage.initialize(activity, ServerMode.SANDBOX, AppKey, AppVersion, ConsumerKey, ConsumerSecret);

			// TODO: hook up MobageUIVisible Notifications
			// TODO: hook up UserSessionReestablished Notification

			// TODO: analytics
			/*
			 * Mobage mobage = Mobage.getInstance(); mAnalyticsReporter =
			 * mobage.
			 * newAnalyticsActivity(activity.getComponentName().flattenToString
			 * ()); AuthNotifications.UserLogout.addObserver(new
			 * INotificationCenterCallback() {
			 * 
			 * @Override public void onNotificationReceived(Notification arg0) {
			 * executeLogin(theActivity); } });
			 */
		}
	}

	// attempt to re-establish an existing mobage session.
	public void establishSession(Activity activity, final EstablishSessionCallback callback)
	{
		if (hasSession())
		{
			//listener.onFailure("already has session");
			callback.onNoExistingSession();
			return;
		}

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		keys.add("LOGIN_TYPE");
		values.add("establish_session");

		Service.executeLoginWithParams(activity, keys, values, new Service.IExecuteLoginWithParamsCallback()
		{
			@Override
			public void onComplete(CancelableAPIStatus status, Error error)
			{
				switch (status)
				{
				case cancel:
					mHasSession = false;
					callback.onNoExistingSession();
					break;
				case error:
					mHasSession = false;
					callback.onFailure(error.getDescription());
					break;
				case success:
					mHasSession = true;
					callback.onSuccess();
					break;
				}
			}
		});
	}

	public boolean hasSession()
	{
		return mHasSession;
	}
	
	public void getUserInfo(final GetUserInfoCallback callback)
	{		
		People.getCurrentUser(new IGetCurrentUserCallback(){
			@Override
			public void onComplete(SimpleAPIStatus status, Error error, User user)
			{
				currentUser = user;
				callback.onComplete(user);
			}						
		});
	}

	// login with mobage id
	public void mobageLogin(Activity activity, final SucceedCancelFailCallback callback)
	{
		if (hasSession())
		{
			callback.onFailure("already has session");
			return;
		}

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		keys.add("LOGIN_TYPE");
		values.add("mobage");

		Service.executeLoginWithParams(activity, keys, values, new Service.IExecuteLoginWithParamsCallback()
		{
			@Override
			public void onComplete(CancelableAPIStatus status, Error error)
			{
				switch (status)
				{
				case cancel:
					mHasSession = false;
					callback.onCancel();							
					break;
				case error:
					mHasSession = false;
					callback.onFailure(error.getDescription());
					break;
				case success:
					mHasSession = true;
					callback.onSuccess();	
					break;
				}
			}
		});
	}

	// login with guest id
	public void guestLogin(Activity activity, final SucceedFailCallback callback)
	{
		if (hasSession())
		{
			callback.onFailure("already has session");
			return;
		}

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		keys.add("LOGIN_TYPE");
		values.add("guest");

		Service.executeLoginWithParams(activity, keys, values, new Service.IExecuteLoginWithParamsCallback()
		{
			@Override
			public void onComplete(CancelableAPIStatus status, Error error)
			{
				switch (status)
				{
				case cancel:
					mHasSession = false;
					callback.onFailure("cancel");
					break;
				case error:
					mHasSession = false;
					callback.onFailure(error.getDescription());
					break;
				case success:
					mHasSession = true;
					callback.onSuccess();
					break;
				}
			}
		});
	}

	// login with facebook token
	public void facebookLogin(Activity activity, FacebookHelper facebookHelper, final SucceedCancelFailCallback callback)
	{
		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();

		keys.add("LOGIN_TYPE");
		values.add("facebook");

		keys.add("FACEBOOK_ID");
		values.add(facebookHelper.getUserId());

		keys.add("FACEBOOK_TOKEN");
		values.add(facebookHelper.getAccessToken());
		
		if (facebookHelper.getUserBirthday() != null)
		{
			keys.add("BIRTHDAY");
			values.add(facebookHelper.getUserBirthday());
		}

		keys.add("FIRST_NAME");
		values.add(facebookHelper.getUserFirstName());

		keys.add("LAST_NAME");
		values.add(facebookHelper.getUserLastName());
		
		if (facebookHelper.getPictureURL() != null)
		{
			keys.add("PHOTO");
			values.add(facebookHelper.getPictureURL());
		}

		Service.executeLoginWithParams(activity, keys, values, new Service.IExecuteLoginWithParamsCallback()
		{
			@Override
			public void onComplete(CancelableAPIStatus status, Error error)
			{
				switch (status)
				{
				case cancel:
					mHasSession = false;
					callback.onCancel();
					break;
				case error:
					mHasSession = false;
					Log.d(TAG, "mobage fail = " + error.getDescription());
					callback.onFailure(error.getDescription());
					break;
				case success:
					mHasSession = true;
					Log.d(TAG, "mobage success!");
					callback.onSuccess();
					break;
				}
			}
		});
	}

	// upgrade user with facebook info
	public void upgradeUser(Activity activity, FacebookHelper facebookHelper, final SucceedCancelFailCallback callback)
	{
		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();

		keys.add("UPGRADE_TYPE");
		values.add("facebook");

		keys.add("FACEBOOK_ID");
		values.add(facebookHelper.getUserId());

		keys.add("FACEBOOK_TOKEN");
		values.add(facebookHelper.getAccessToken());

		if (facebookHelper.getUserBirthday() != null)
		{
			keys.add("BIRTHDAY");
			values.add(facebookHelper.getUserBirthday());
		}

		keys.add("FIRST_NAME");
		values.add(facebookHelper.getUserFirstName());

		keys.add("LAST_NAME");
		values.add(facebookHelper.getUserLastName());
		
		if (facebookHelper.getPictureURL() != null)
		{
			keys.add("PHOTO");
			values.add(facebookHelper.getPictureURL());
		}

		Auth.executeUserUpgradeWithParams(activity, keys, values, new Auth.IExecuteUserUpgradeWithParamsCallback()
		{
			@Override
			// MobageNDK BUG: method name should be onComplete
			public void onComplete(CancelableAPIStatus status, Error error)
			{
				switch (status)
				{
				case cancel:
					// occurs when already registered.
					callback.onCancel();
					break;
				case error:
					Log.d(TAG, "mobage upgrade fail = " + error.getDescription());
					callback.onFailure(error.getDescription());
					break;
				case success:
					Log.d(TAG, "mobage upgrade success!");
					callback.onSuccess();
					break;
				}
			}
		});
	}

	// used to report lifecycle events to Mobage
	public void onPause()
	{
		if (!Mobage.isInitialized())
			Mobage.getInstance().onPause();
		
		if (mAnalyticsReporter != null)
			mAnalyticsReporter.onPause();
	}

	public void onResume()
	{
		if (Mobage.isInitialized())
			Mobage.getInstance().onResume();
		
		if (mAnalyticsReporter != null)
			mAnalyticsReporter.onResume();
	}

	public void onStop()
	{
		if (Mobage.isInitialized())
			Mobage.getInstance().onStop();
		
		if (mAnalyticsReporter != null)
			mAnalyticsReporter.onStop();
	}

	public void onRestart()
	{
		if (Mobage.isInitialized())
			Mobage.getInstance().onRestart();
		// Only the Mobage instance reports restart events.
	}

	public void onDestroy()
	{
		if (Mobage.isInitialized())
			Mobage.getInstance().onDestroy();
		// Only the Mobage instance reports destroy events.
	}

	public void authorizeToken(String token, final AuthorizeTokenCallback callback)
	{
		Auth.authorizeToken(token, new Auth.IAuthorizeTokenCallback()
		{
			@Override
			public void onComplete(SimpleAPIStatus status, Error error, String verifier)
			{
				switch (status)
				{
					case success:
						callback.onSuccess(verifier);
						break;
					case error:
						callback.onFailure(error.getDescription());
						break;
					default:
						callback.onFailure("unknown error in authorizeToken");
						break;
				}
			}
		});
	}

	public void logout(Activity activity, final SucceedFailCallback callback)
	{
		Service.executeLogout(activity, new Service.IExecuteLogoutCallback()
		{
			@Override
			public void onComplete(SimpleAPIStatus status, Error error)
			{
				switch (status)
				{
					case success:
						mHasSession = false;
						callback.onSuccess();
						break;
					case error:
						callback.onFailure(error.getDescription());
						break;
					default:
						callback.onFailure("unknown error in executeLogout");
						break;
				}
			}
		});
	}
}
