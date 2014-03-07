package com.example.logintoboggan.state;

import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.MainActivity.FragmentType;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;


public class MainGameState implements GameState<AppState, LoginTobogganGameStateResult>
{
	private String TAG = "MainGameState";

	private LoginToboggan theApp;

	public MainGameState(LoginToboggan theApp)
	{
		this.theApp = theApp;
	}

	@Override
	public void onEnter(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{		
		theApp.getMainActivity().switchFragment(FragmentType.Game);		
	}

	@Override
	public void onExit(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{

	}

	@Override
	public void onReturn(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{
		theApp.getMainActivity().switchFragment(FragmentType.Game);
	}

	public void facebookLoginFailed(String error)
	{
		theApp.showSimpleDialog("Facebook login failure", error);
	}

	public void facebookLoginSuccess()
	{
		theApp.showSimpleDialog("Login succes", "Successfully logged in to Facebook.");
	}

	public void mobageLoginFailed(String error)
	{

	}

}
