package com.example.logintoboggan.state;

import android.util.Log;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.LoginToboggan.FacebookConnectResult;
import com.example.logintoboggan.LoginToboggan.LoginMode;
import com.example.logintoboggan.LoginToboggan.LoginResult;
import com.example.logintoboggan.helper.MobageHelper;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;

public class FacebookLoginState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "FacebookLoginState";

	private LoginToboggan theApp;

	public FacebookLoginState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		final LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();
		
		gameStateResult.LoginMode = LoginMode.Facebook;
		gameStateResult.FacebookConnectUserID = null;

		// login to facebook
		stateMachine.moveTo(AppState.FacebookConnect);
	}

	@Override
	public void onExit(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{

	}

	@Override
	public void onReturn(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		final LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();
		
		// only attempt to login to Mobage as a Facebook user if the attempt to login
		// to Facebook succeeds
		if (gameStateResult.FacebookConnectResult != FacebookConnectResult.Success)
		{
			gameStateResult.LoginResult = LoginResult.Failure;
			gameStateResult.LoginMessage = gameStateResult.FacebookConnectMessage;
			stateMachine.setWaitStateRunning();
			stateMachine.returnPrevious();
		}
		else
		{
			theApp.showSpinner("Establishing Mobage session...");

			// since this an asynchronous task, tell the state machine to stop processing state transitions
			stateMachine.setWaitStateWaiting();
			
			// login to Mobage with Facebook credentials
			theApp.getMobageHelper().facebookLogin(theApp.getMainActivity(), theApp.getFacebookHelper(),
					new MobageHelper.SucceedCancelFailCallback()
					{
						@Override
						public void onSuccess()
						{
							theApp.hideSpinner();
							gameStateResult.LoginResult = LoginResult.Success;
							gameStateResult.LoginMessage = "Facebook login success.";
							Log.d(TAG, "Mobage establish session success.");
							stateMachine.setWaitStateRunning();
							stateMachine.returnPrevious();
						}

						public void onCancel()
						{
							theApp.hideSpinner();
							gameStateResult.LoginResult = LoginResult.Failure;
							gameStateResult.LoginMessage = "Facebook login canceled.";
							Log.d(TAG, "Mobage establish session cancel.");
							stateMachine.setWaitStateRunning();
							stateMachine.returnPrevious();
						}

						public void onFailure(String error)
						{
							theApp.hideSpinner();
							gameStateResult.LoginResult = LoginResult.Failure;
							gameStateResult.LoginMessage = "Facebook login failure.";
							Log.d(TAG, "Mobage establish session failure.");
							stateMachine.setWaitStateRunning();
							stateMachine.returnPrevious();
						}
					});
			
		}
	}

}
