package com.example.logintoboggan.state;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.LoginToboggan.LoginMode;
import com.example.logintoboggan.LoginToboggan.LoginResult;
import com.example.logintoboggan.helper.MobageHelper;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;


public class MobageLoginState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "MoabgeLoginState";

	private LoginToboggan theApp;

	public MobageLoginState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		theApp.showSpinner("Logging into Mobage...");

		final LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();
		gameStateResult.LoginMode = LoginMode.Mobage;

		// since this an asynchronous task, tell the state machine to stop processing state transitions
		stateMachine.setWaitStateWaiting();
		
		// login to Mobage
		theApp.getMobageHelper().mobageLogin(theApp.getMainActivity(), new MobageHelper.SucceedCancelFailCallback()
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
			public void onCancel()
			{
				theApp.hideSpinner();
				gameStateResult.LoginResult = LoginResult.Cancel;
				gameStateResult.LoginMessage = "Login canceled.";
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
