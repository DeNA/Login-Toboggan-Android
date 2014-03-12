package com.example.logintoboggan;

import java.util.HashMap;

import com.example.logintoboggan.helper.FacebookHelper;
import com.example.logintoboggan.helper.MobageHelper;
import com.example.logintoboggan.helper.MobageHelper.GetUserInfoCallback;
import com.example.logintoboggan.state.FacebookConnectState;
import com.example.logintoboggan.state.FacebookLoginState;
import com.example.logintoboggan.state.FacebookUpgradeState;
import com.example.logintoboggan.state.GuestLoginState;
import com.example.logintoboggan.state.InitState;
import com.example.logintoboggan.state.LoginState;
import com.example.logintoboggan.state.LogoutState;
import com.example.logintoboggan.state.MainGameState;
import com.example.logintoboggan.state.MobageLoginState;
import com.example.logintoboggan.state.StartState;
import com.example.logintoboggan.statemachine.GameStateMachine;
import com.example.logintoboggan.statemachine.GameStateLink.LinkDirection;
import com.mobage.global.android.data.User;
import com.mobage.global.android.notification.Notification;
import com.mobage.global.android.notification.MobageNotifications.MobageUIVisible;
import com.mobage.global.android.notification.NotificationCenter.INotificationCenterCallback;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class LoginToboggan extends Application implements INotificationCenterCallback  
{
	public enum AppState
	{
		Start, 
		Init, 
		Login, 
		Logout, 
		FacebookLogin, 
		FacebookUpgrade,
		FacebookConnect, 
		MobageLogin, 
		MobageGuestLogin, 
		Game
	}

	private static final String TAG = "LoginToboggan";

	public static final int LOGIN_ACTIVITY = 0;

	private ProgressDialog mProgressDialog;
	private FacebookHelper mFacebookHelper;
	private MobageHelper mMobageHelper;
	private GameStateMachine<AppState, LoginTobogganGameStateResult> mGameStateMachine;
	private MainActivity mainActivity;
	private LoginTobogganGameStateResult gameStateResult;
	
	private Runnable onMobageUIVisible;
	private Runnable onMobageUINotVisible;

	private boolean mFirstLogin;

	private static volatile LoginToboggan theInstance;

	public static LoginToboggan getInstance()
	{
		if(theInstance == null)
		{
			theInstance = new LoginToboggan();
		}
		return theInstance;
	}

	public LoginToboggan()
	{
		theInstance = this;
		onMobageUIVisible = null;
		onMobageUINotVisible = null;
		
		// Listen to Mobage UI Visible notification
		MobageUIVisible.addObserver(this);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		mFacebookHelper = new FacebookHelper(this);
		mMobageHelper = new MobageHelper(this);
		mGameStateMachine = new GameStateMachine<AppState, LoginTobogganGameStateResult>();

		setupStateMachine();

		mFirstLogin = false;
	}
	

	@Override
	public void onNotificationReceived(Notification notification)
	{
		if (notification instanceof MobageUIVisible) 
		{
		//	Log.d(TAG, "UI Notification received");
			
			boolean visible = ((MobageUIVisible)notification).getVisible();

		//	String toastMsg = "MobageUIVisible notification received: "+visible;
		//	Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
			
			if(!visible)mobageUINotVisible();
			else mobageUIVisible();
		}
	}
	
	public void setOnMobageUIVisibleCallabck(Runnable runnable)
	{
		onMobageUIVisible = runnable;
	}
	
	public void setOnMobageUINotVisibleCallabck(Runnable runnable)
	{
		onMobageUINotVisible = runnable;
	}
	
	public void mobageUIVisible()
	{
		if(onMobageUIVisible != null)
		{
			onMobageUIVisible.run();
			onMobageUIVisible = null;
		}
	}
	
	public void mobageUINotVisible()
	{
		if(onMobageUINotVisible != null)
		{
			onMobageUINotVisible.run();
			onMobageUINotVisible = null;
		}
	}

	public MainActivity getMainActivity()
	{
		return mainActivity;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		mFacebookHelper.onActivityResult(mainActivity, requestCode, resultCode, data);
	}

	/**
	 * Create all states used in the app and setup the links between them. 
	 */
	private void setupStateMachine()
	{
		gameStateResult = new LoginTobogganGameStateResult(this);

		mGameStateMachine.setGameStateResultObject(gameStateResult);
		
		mGameStateMachine.addState(AppState.Start, new StartState());
		mGameStateMachine.addState(AppState.Init, new InitState(this));
		mGameStateMachine.addState(AppState.Login, new LoginState(this));
		mGameStateMachine.addState(AppState.Logout, new LogoutState(this));
		mGameStateMachine.addState(AppState.Game, new MainGameState(this));
		mGameStateMachine.addState(AppState.FacebookLogin, new FacebookLoginState(this));
		mGameStateMachine.addState(AppState.FacebookConnect, new FacebookConnectState(this));
		mGameStateMachine.addState(AppState.FacebookUpgrade, new FacebookUpgradeState(this));
		mGameStateMachine.addState(AppState.MobageGuestLogin, new GuestLoginState(this));
		mGameStateMachine.addState(AppState.MobageLogin, new MobageLoginState(this));
		
		mGameStateMachine.linkStates(AppState.Start, AppState.Init, LinkDirection.To);

		mGameStateMachine.linkStates(AppState.Init, AppState.Init, LinkDirection.To);
		mGameStateMachine.linkStates(AppState.Init, AppState.Login, LinkDirection.To);
		mGameStateMachine.linkStates(AppState.Init, AppState.Game, LinkDirection.BiDirectional);
		mGameStateMachine.linkStates(AppState.Init, AppState.Logout, LinkDirection.BiDirectional);

		mGameStateMachine.linkStates(AppState.Login, AppState.FacebookLogin, LinkDirection.BiDirectional);
		mGameStateMachine.linkStates(AppState.Login, AppState.FacebookConnect, LinkDirection.BiDirectional);	
		mGameStateMachine.linkStates(AppState.Login, AppState.MobageGuestLogin, LinkDirection.BiDirectional);
		mGameStateMachine.linkStates(AppState.Login, AppState.MobageLogin, LinkDirection.BiDirectional);
		mGameStateMachine.linkStates(AppState.Login, AppState.Game, LinkDirection.To);
		
		mGameStateMachine.linkStates(AppState.FacebookLogin, AppState.FacebookConnect, LinkDirection.BiDirectional);
		mGameStateMachine.linkStates(AppState.FacebookUpgrade, AppState.FacebookConnect, LinkDirection.BiDirectional);

		mGameStateMachine.linkStates(AppState.Game, AppState.Logout, LinkDirection.BiDirectional);
		mGameStateMachine.linkStates(AppState.Game, AppState.FacebookConnect, LinkDirection.BiDirectional);
		mGameStateMachine.linkStates(AppState.Game, AppState.FacebookUpgrade, LinkDirection.BiDirectional);

		mGameStateMachine.linkStates(AppState.Logout, AppState.Login, LinkDirection.BiDirectional);

		mGameStateMachine.setStartState(AppState.Start);

		mGameStateMachine.initialize();
	}

	public boolean isFirstLogin()
	{
		return mFirstLogin;
	}


	public void setMainActivity(MainActivity activity)
	{
		this.mainActivity = activity;
	}

	public FacebookHelper getFacebookHelper()
	{
		return mFacebookHelper;
	}

	public MobageHelper getMobageHelper()
	{
		return mMobageHelper;
	}

	public GameStateMachine<AppState, LoginTobogganGameStateResult> getGameStateMachine()
	{
		return mGameStateMachine;
	}

	public void showSpinner(String msg)
	{
		mainActivity.showSpinnerDirect(msg);
	}

	public void hideSpinner()
	{
		mainActivity.hideSpinnerDirect();
	}

	public void getUser(final GetUserInfoCallback callback)
	{
		getMobageHelper().getUserInfo(new GetUserInfoCallback(){

			@Override
			public void onComplete(User user)
			{
				callback.onComplete(user);			
			}	
		});	
	}


	public void showSimpleDialog(String title, String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder.setMessage(message).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{

					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	public void toast(String msg)
	{
		mainActivity.toast(msg);
	}

	public enum MobageInitResult
	{
		SuccessNew, 
		SuccessExisting, 
		Failure
	}

	public enum LoginResult
	{
		Success, 
		Failure, 
		Closed, 
		Cancel, 
		FacebookMismatch
	}

	public enum LoginMode
	{
		Facebook, 
		Guest,
		Mobage
	}

	public enum GameServerConnectResult
	{
		Success, 
		Failure, 
		FacebookConnectMistmatch
	}

	public enum FacebookConnectResult
	{
		Success, 
		Failure, 
		Mismatch
	}

	/**
	 * FaceBlasterGameStateResult
	 * @author mark.kellogg
	 *
	 * Custom class for passing data from one state to the next.
	 *
	 */
	public class LoginTobogganGameStateResult 
	{
		public MobageInitResult MobageInitResult;
		public String MobageInitMessage;

		public LoginMode LoginMode;

		public LoginResult LoginResult;
		public String LoginMessage;

		public FacebookConnectResult FacebookConnectResult;
		public String FacebookConnectUserID;
		public String FacebookConnectMessage;

		public GameServerConnectResult GameServerConnectResult;
		public String GameServerConnectMessage;

		public HashMap<AppState, Integer> stageMap;

		private LoginToboggan theApp;

		public LoginTobogganGameStateResult(LoginToboggan theApp)
		{
			this.theApp = theApp;
			this.stageMap = new HashMap<AppState, Integer>();
		}

		public void setStage(AppState appState, int stage)
		{
			stageMap.put(appState, stage);
		}

		public int getStage(AppState appState)
		{
			Integer i = stageMap.get(appState);
			if (i != null) return i.intValue();
			return -1;
		}

		public LoginToboggan getApp()
		{
			return theApp;
		}
	}
}
