package com.example.logintoboggan.state;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;

import com.example.logintoboggan.LoginToboggan.LoginMode;
import com.example.logintoboggan.LoginToboggan.LoginResult;
import com.example.logintoboggan.helper.MobageHelper;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;


public class GuestLoginState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "GuestLoginState";

	private LoginToboggan theApp;

	public GuestLoginState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		theApp.showSpinner("Logging in as guest...");

		final LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();
		gameStateResult.LoginMode = LoginMode.Guest;

		// since this an asynchronous task, tell the state machine to stop processing state transitions
		stateMachine.setWaitStateWaiting();
		
		// perform guest login
		theApp.getMobageHelper().guestLogin(theApp.getMainActivity(), new MobageHelper.SucceedFailCallback()
		
		{
			@Override
			public void onSuccess()
			{
				theApp.hideSpinner();
				gameStateResult.LoginResult = LoginResult.Success;
				gameStateResult.LoginMessage = "Mobage login success";
				stateMachine.setWaitStateRunning();
				stateMachine.returnPrevious();
			}

			@Override
			public void onFailure(String reason)
			{
				theApp.hideSpinner();
				gameStateResult.LoginResult = LoginResult.Failure;
				gameStateResult.LoginMessage = reason;
				stateMachine.setWaitStateRunning();
				stateMachine.returnPrevious();
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
