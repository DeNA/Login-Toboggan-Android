package com.example.logintoboggan.state;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.helper.MobageHelper;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;

public class LogoutState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "LogoutState";

	private LoginToboggan theApp;

	public LogoutState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		theApp.showSpinner("Logging out...");

		// since this an asynchronous task, tell the state machine to stop processing state transitions
		stateMachine.setWaitStateWaiting();
		
		// logout of Mobage
		theApp.getMobageHelper().logout(theApp.getMainActivity(), new MobageHelper.SucceedFailCallback()
		{
			@Override
			public void onSuccess()
			{
				theApp.hideSpinner();
				theApp.getFacebookHelper().logout();
				stateMachine.setWaitStateRunning();
				stateMachine.moveTo(AppState.Login);
			}

			@Override
			public void onFailure(String reason)
			{
				theApp.hideSpinner();
				theApp.showSimpleDialog("Logout error", "Unable to logout from Mobage.");
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
