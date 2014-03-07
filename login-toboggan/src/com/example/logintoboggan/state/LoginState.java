package com.example.logintoboggan.state;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.LoginToboggan.LoginResult;
import com.example.logintoboggan.MainActivity.FragmentType;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;

public class LoginState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "LoginState";

	private LoginToboggan theApp;

	public LoginState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		theApp.getMainActivity().switchFragment(FragmentType.Login);
	}

	@Override
	public void onExit(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{

	}

	@Override
	public void onReturn(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		final LoginTobogganGameStateResult gameStateResult = stateMachine.getGameStateResult();
		AppState returnFromState = stateMachine.getReturnFromState();
		
		// After the appropriate login method has been executed for the login type chosen by the
		// user, the state machine will return here. After a successful login, the next step is
		// to connect to the game server to get details about the user.
		
		if (returnFromState == AppState.FacebookLogin)
		{
			if (gameStateResult.LoginResult != LoginResult.Success)
				facebookLoginFailed(gameStateResult.LoginMessage);
			else
			{
				stateMachine.moveTo(AppState.Game);
			}
		}
		else if (returnFromState == AppState.MobageGuestLogin)
		{
			if (gameStateResult.LoginResult != LoginResult.Success)
				mobageLoginFailed(gameStateResult.LoginMessage);
			else
			{
				stateMachine.moveTo(AppState.Game);
			}
		}
		else if (returnFromState == AppState.MobageLogin)
		{
			if (gameStateResult.LoginResult != LoginResult.Success)
				mobageLoginFailed(gameStateResult.LoginMessage);
			else
			{
				stateMachine.moveTo(AppState.Game);
			}
		}
	}

	public void facebookLoginFailed(String error)
	{
		theApp.showSimpleDialog("Facebook login failure", error);
	}

	public void mobageLoginFailed(String error)
	{
		theApp.showSimpleDialog("Login error", error);
	}

}
