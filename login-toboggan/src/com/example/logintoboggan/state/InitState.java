package com.example.logintoboggan.state;

import android.util.Log;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.LoginToboggan.MobageInitResult;
import com.example.logintoboggan.MainActivity.FragmentType;
import com.example.logintoboggan.helper.MobageHelper.EstablishSessionCallback;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;
import com.example.logintoboggan.ui.InitFragment;

public class InitState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "InitState";

	private LoginToboggan theApp;

	public InitState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		// set UI of MainActivity
		theApp.getMainActivity().switchFragment(FragmentType.Init);
		
		// Initialize Mobage
		theApp.getMobageHelper().initialize(theApp.getMainActivity());

		final LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();

		// this is an asynchronous operation so tell the state machine to wait
		stateMachine.setWaitStateWaiting();
		
		// establish Mobage session
		theApp.getMobageHelper().establishSession(theApp.getMainActivity(), new EstablishSessionCallback()
		{
			@Override
			public void onSuccess()
			{
				Log.d(TAG, "Successfully initialized Mobage - existing session.");
				
				gameStateResult.MobageInitResult = MobageInitResult.SuccessExisting;
				gameStateResult.MobageInitMessage = "Mobage successfully initialized with existing session.";
				stateMachine.setWaitStateRunning();
				
				// Move to GameServerConnect state to retrieve info about current session
				stateMachine.moveTo(AppState.Game);
			}

			@Override
			public void onNoExistingSession()
			{
				Log.d(TAG, "Successfully initialized Mobage - new session.");
				gameStateResult.MobageInitResult = MobageInitResult.SuccessNew;
				gameStateResult.MobageInitMessage = "Mobage successfully initialized with new  session.";
				stateMachine.setWaitStateRunning();
				
				// Move to login state since no session exists
				stateMachine.moveTo(AppState.Login);
			}

			@Override
			public void onFailure(String reason)
			{
				Log.d(TAG, "Error initializing Mobage: " + reason);
				
				gameStateResult.MobageInitResult = MobageInitResult.Failure;
				gameStateResult.MobageInitMessage = reason;
				
				stateMachine.setWaitStateRunning();
				
				mobageInitFailure(reason);
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
		// set default error string, we assume error has occurred at this point even though it may not have
		String error = "Error occurred while initializing mobage.";
		boolean hasError = true;

		if (hasError)
		{
			mobageInitFailure(error);
		}
	}

	private void mobageInitFailure(final String msg)
	{
		final InitFragment initFragment = (InitFragment) theApp.getMainActivity().getFragment(FragmentType.Init);
		theApp.getMainActivity().switchFragment(FragmentType.Init);
		
		theApp.getMainActivity().runOnUiThread(new Runnable(){

			@Override
			public void run()
			{
				// enable init retry
				initFragment.enableRetryInit(msg);
			}
		});
		
	}

}
