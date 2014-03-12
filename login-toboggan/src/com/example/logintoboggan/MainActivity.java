package com.example.logintoboggan;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;

import com.example.logintoboggan.R;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.helper.MobageHelper;
import com.example.logintoboggan.statemachine.GameStateMachine;
import com.example.logintoboggan.ui.GameFragment;
import com.example.logintoboggan.ui.InitFragment;
import com.example.logintoboggan.ui.LoginFragment;
import com.mobage.global.android.notification.MobageNotifications.MobageUIVisible;
import com.mobage.global.android.notification.Notification;
import com.mobage.global.android.notification.NotificationCenter.INotificationCenterCallback;

public class MainActivity extends FragmentActivity 
{
	public enum FragmentType
	{
		Init(0), 
		Login(1), 
		Game(2);

		private int value;

		private FragmentType(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}
	}

	private static final String TAG = "MainActivity";
	private GameStateMachine<AppState, LoginTobogganGameStateResult> mGameStateMachine;
	private ProgressDialog mProgressDialog;

	private HashMap<FragmentType, Fragment> fragmentMap;
	private LoginToboggan theApp;
	private Fragment currentFragment;
	private FragmentType currentFragmentType;
	private String currentDialogMessage;
	private boolean dialogActive;

	private boolean mIsInForegroundMode;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_main_v2);

		theApp = (LoginToboggan) getApplication();
		theApp.setMainActivity(this);
		mGameStateMachine = theApp.getGameStateMachine();

		mIsInForegroundMode = false;
		currentDialogMessage = "";
		dialogActive = false;

		if (savedInstanceState == null)
		{
			// move to Init state since we have not done so already
			mGameStateMachine.moveTo(AppState.Init);
		}
		else
		{
			// only move to the Init state if we have done so already
			String initRun = savedInstanceState.getString("InitRun");
			if (initRun == null || !initRun.equals("true"))
			{
				savedInstanceState.putString("InitRun", "true");
				mGameStateMachine.moveTo(AppState.Init);
			}

			// detertmine which fragment should be showing
			int currentModeOrdinal = savedInstanceState.getInt("CurrentMode");
			FragmentType[] values = FragmentType.values();
			currentFragmentType = values[currentModeOrdinal];

			// restore dialog state
			currentDialogMessage = savedInstanceState.getString("CurrentDialogMessage");
			dialogActive = savedInstanceState.getBoolean("DialogActive");
		}

		initFragments();
		currentFragment = getFragment(currentFragmentType);
		
		getHash();
	}
	
	private void getHash()
	{
		 String hash = null;
		 
		String packageName = getPackageName();
		 
		Log.d("KeyHash", "PACKAGE: " + packageName);
		
		 try 
		 {
		       PackageInfo info = getPackageManager().getPackageInfo(
		    		   "com.example.logintoboggan", 
		                PackageManager.GET_SIGNATURES);
		       
		        for (Signature signature : info.signatures) 
		        {
		            MessageDigest md = MessageDigest.getInstance("SHA");
		            md.update(signature.toByteArray());
		            //Log.d(LogTag, "Key hash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
		            hash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
		        }
		 } 
		 catch (NameNotFoundException e) 
		 {
		    	Log.d("KeyHash","hash not found");
		 } 
		 catch (NoSuchAlgorithmException e) 
		 {
		    	Log.d("KeyHash","no such algorithm");
		 }
		 
		 Log.d("KeyHash", hash);
	}


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putString("InitRun", "true");
		savedInstanceState.putInt("CurrentMode", currentFragmentType.ordinal());
		savedInstanceState.putBoolean("DialogActive", dialogActive);
		savedInstanceState.putString("CurrentDialogMessage", currentDialogMessage);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		currentDialogMessage = savedInstanceState.getString("CurrentDialogMessage");
		dialogActive = savedInstanceState.getBoolean("DialogActive");
	}

	public void initFragments()
	{
		fragmentMap = new HashMap<FragmentType, Fragment>();

		Fragment gameFragment = new GameFragment();
		fragmentMap.put(FragmentType.Game, gameFragment);

		Fragment initFragment = new InitFragment();
		fragmentMap.put(FragmentType.Init, initFragment);

		Fragment loginFragment = new LoginFragment();
		fragmentMap.put(FragmentType.Login, loginFragment);
	}

	// queue UI runnable to switch mode
	public void switchFragment(final FragmentType type)
	{
		Handler h = new Handler();
		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				switchFragmentDirect(type);
			}
		});
	}

	// queue UI runnable to switch mode
	public void switchFragment(final FragmentType type, final Fragment altFragment)
	{
		Handler h = new Handler();
		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				switchFragmentDirect(type, altFragment);
			}
		});
	}

	// switch currently visible fragment to the default for specified mode
	private void switchFragmentDirect(FragmentType type)
	{
		if(mIsInForegroundMode && currentFragmentType != type)
		{
			currentFragmentType = type;
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment newFragment = fragmentMap.get(type);
			ft.replace(R.id.main_fragment_container, newFragment, type.toString());
			ft.commit();
			fm.executePendingTransactions();
			currentFragment = newFragment;
		}
	}

	// switch mode and override the fragment displayed for it
	private void switchFragmentDirect(FragmentType type, Fragment altFragment)
	{
		if(mIsInForegroundMode && currentFragmentType != type)
		{
			currentFragmentType = type;
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.main_fragment_container, altFragment, type.toString());
			ft.show(altFragment);
			ft.commit();
			fm.executePendingTransactions();
			currentFragment = altFragment;
		}
	}

	public Fragment getCurrentFragment()
	{
		return currentFragment;
	}

	public Fragment getFragment(FragmentType type)
	{
		return fragmentMap.get(type);
	}

	public void onStateUpdate(String heading, String message, boolean connectButtonEnabled,
			boolean inviteButtonEnabled, boolean logoutButtonEnabled)
	{

		/*
		 * mHeadingText.setText(heading); mMessageText.setText(message);
		 * 
		 * mConnectButton.setVisibility(connectButtonEnabled ? View.VISIBLE :
		 * View.INVISIBLE); mInviteButton.setVisibility(inviteButtonEnabled ?
		 * View.VISIBLE : View.INVISIBLE);
		 * mLogoutButton.setVisibility(logoutButtonEnabled ? View.VISIBLE :
		 * View.INVISIBLE);
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		theApp.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		hideSpinnerDirect();
		mIsInForegroundMode = false;

		MobageHelper mobageHelper = ((LoginToboggan) getApplication()).getMobageHelper();
		mobageHelper.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		MobageHelper mobageHelper = ((LoginToboggan) getApplication()).getMobageHelper();
		mobageHelper.onResume();

		mIsInForegroundMode = true;

		if (dialogActive)
		{
			showSpinnerDirect(currentDialogMessage);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		MobageHelper mobageHelper = ((LoginToboggan) getApplication()).getMobageHelper();
		mobageHelper.onStop();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();

		MobageHelper mobageHelper = ((LoginToboggan) getApplication()).getMobageHelper();
		mobageHelper.onRestart();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		MobageHelper mobageHelper = ((LoginToboggan) getApplication()).getMobageHelper();
		mobageHelper.onDestroy();
	}

	public void showSpinner(final String message)
	{
		final Handler h = new Handler();
		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mIsInForegroundMode)
				{
					showSpinnerDirect(message);
				}
			}
		});
	}

	public void showSpinnerDirect(final String message)
	{
		if (mIsInForegroundMode)
		{
			if(mProgressDialog != null)
			{
				mProgressDialog.setMessage(message);
			}
			else mProgressDialog = ProgressDialog.show(MainActivity.this, "", message, true);
			
			currentDialogMessage = message;
			dialogActive = true;
		}
	}

	public void hideSpinner()
	{
		final Handler h = new Handler();
		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mIsInForegroundMode)
				{
					hideSpinnerDirect();
				}
			}
		});
	}

	public void hideSpinnerDirect()
	{
		if (mIsInForegroundMode && mProgressDialog != null)
		{
			mProgressDialog.cancel();
			mProgressDialog = null;
			dialogActive = false;
		}
	}

	public void toast(String message)
	{
		Context context = getBaseContext();
		int duration = Toast.LENGTH_SHORT;
		Toast.makeText(context, message, duration).show();
	}


}
