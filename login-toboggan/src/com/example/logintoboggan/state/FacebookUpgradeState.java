package com.example.logintoboggan.state;

import java.util.HashMap;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.LoginToboggan.FacebookConnectResult;
import com.example.logintoboggan.helper.MobageHelper;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;

public class FacebookUpgradeState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "FacebookUpgradeState";

	private LoginToboggan theApp;

	private GameStateMachine<AppState, LoginToboggan.LoginTobogganGameStateResult> mGameStateMachine;

	public FacebookUpgradeState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		if (theApp.getFacebookHelper().hasSession())
		{
			doUpgrade(stateMachine);
		}
		else
		{
			LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();
			gameStateResult.FacebookConnectUserID = null;
			stateMachine.moveTo(AppState.FacebookConnect);
		}
	}

	@Override
	public void onExit(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{

	}

	@Override
	public void onReturn(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		AppState returnFromState = stateMachine.getReturnFromState();
		if (returnFromState == AppState.FacebookConnect)
		{
			LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();
			if (gameStateResult.FacebookConnectResult != FacebookConnectResult.Success)
			{
				theApp.showSimpleDialog("Facebook connect error", gameStateResult.FacebookConnectMessage);
				stateMachine.setWaitStateRunning();
				stateMachine.returnPrevious();
			}
			else
			{
				doUpgrade(stateMachine);
			}
		}
	}

	private void doUpgrade(final GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		theApp.showSpinner("Performing upgrade...");

		stateMachine.setWaitStateWaiting();
		theApp.getMobageHelper().upgradeUser(theApp.getMainActivity(), theApp.getFacebookHelper(),
				new MobageHelper.SucceedCancelFailCallback()
				{
					@Override
					public void onSuccess()
					{
						theApp.hideSpinner();
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("fbid", theApp.getFacebookHelper().getUserId());
						params.put("fbtoken", theApp.getFacebookHelper().getAccessToken());
						theApp.showSimpleDialog("Facebook upgrade", "Facebook upgrade successful.");
						stateMachine.setWaitStateRunning();
						stateMachine.returnPrevious();
					}

					@Override
					public void onCancel()
					{
						theApp.hideSpinner();
						// TODO: friendly dialog.
						stateMachine.setWaitStateRunning();
						theApp.showSimpleDialog("Facebook upgrade error", "fbid already registered");
						stateMachine.setWaitStateRunning();
						stateMachine.returnPrevious();
					}

					@Override
					public void onFailure(String reason)
					{
						theApp.hideSpinner();
						stateMachine.setWaitStateRunning();
						theApp.showSimpleDialog("Facebook upgrade error", reason);
						stateMachine.setWaitStateRunning();
						stateMachine.returnPrevious();
					}
				});

	}
}
