package com.example.logintoboggan.state;

import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.statemachine.GameState;
import com.example.logintoboggan.statemachine.GameStateMachine;

public class StartState implements GameState<AppState, LoginTobogganGameStateResult>
{
	@Override
	public void onEnter(GameStateMachine<AppState, LoginTobogganGameStateResult> stateMachine)
	{

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
