package com.example.logintoboggan.state;

import android.util.Log;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.LoginToboggan.FacebookConnectResult;
import com.example.logintoboggan.helper.FacebookHelper;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;

public class FacebookConnectState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "FacebookConnectState";

	private LoginToboggan theApp;

	public FacebookConnectState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		theApp.showSpinner("Establishing Facebook session...");
		final LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();

		stateMachine.setWaitStateWaiting();
		theApp.getFacebookHelper().openSession(theApp.getMainActivity(), theApp.getMainActivity().getCurrentFragment(),
				gameStateResult.FacebookConnectUserID, new FacebookHelper.OpenSessionCallback()
				{

					@Override
					public void onFailure(String error)
					{
						theApp.hideSpinner();
						Log.d(TAG, "facebook connect failure: " + error);
						gameStateResult.FacebookConnectResult = FacebookConnectResult.Failure;
						gameStateResult.FacebookConnectMessage = error;
						stateMachine.setWaitStateRunning();
						stateMachine.returnPrevious();
					}

					@Override
					public void onUserMismatch(String error)
					{
						theApp.hideSpinner();
						Log.d(TAG, "facebook mismatch failure: " + error);
						gameStateResult.FacebookConnectResult = FacebookConnectResult.Mismatch;
						gameStateResult.FacebookConnectMessage = error;
						stateMachine.setWaitStateRunning();
						stateMachine.returnPrevious();
					}

					@Override
					public void onSuccess()
					{
						// TODO: what if no activity?
						theApp.hideSpinner();
						gameStateResult.FacebookConnectResult = FacebookConnectResult.Success;
						gameStateResult.FacebookConnectMessage = "Facebook conenct success";
						stateMachine.setWaitStateRunning();
						stateMachine.returnPrevious();
					}

					@Override
					public void onClose()
					{
						// TODO Auto-generated method stub

					}
				});

	}

	@Override
	public void onExit(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{

	}

	@Override
	public void onReturn(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{

	}
}
